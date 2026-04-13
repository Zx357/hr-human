<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { AMAP_SECURITY_JS_CODE } from '@/constants/map-sdk';
import { loadAmapPlugins } from '@/utils/amap';

interface Props {
  modelValue: boolean;
  latitude?: number;
  longitude?: number;
  address?: string;
  referenceAddress?: string;
}

interface SelectedLocation {
  address: string;
  latitude: number;
  longitude: number;
}

const props = withDefaults(defineProps<Props>(), {
  latitude: undefined,
  longitude: undefined,
  address: '',
  referenceAddress: ''
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  confirm: [location: SelectedLocation];
}>();

const dialogVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
});

const mapContainerRef = ref<HTMLDivElement | null>(null);
const loading = ref(false);
const locating = ref(false);
const searching = ref(false);
const searchKeyword = ref('');
const selectedAddress = ref('');
const selectedLatitude = ref<number>();
const selectedLongitude = ref<number>();

const defaultCenter: [number, number] = [116.397428, 39.90923];

let amapApi: any = null;
let map: any = null;
let marker: any = null;
let geocoder: any = null;
let placeSearch: any = null;
let autoComplete: any = null;
let defaultLayer: any = null;
const serviceTimeout = 8000;

const preferredKeyword = computed(() => props.address?.trim() || props.referenceAddress?.trim() || '');
const hasSelection = computed(() => hasCoordinatePair(selectedLatitude.value, selectedLongitude.value));

function hasCoordinatePair(latitude?: number, longitude?: number) {
  return latitude !== undefined && latitude !== null && longitude !== undefined && longitude !== null;
}

function roundCoordinate(value: number) {
  return Number(value.toFixed(6));
}

function getDisplayAddress() {
  return selectedAddress.value || preferredKeyword.value || '未获取到详细地址';
}

function getFallbackAddress(longitude: number, latitude: number) {
  return `已选择位置 (${latitude.toFixed(6)}, ${longitude.toFixed(6)})`;
}

function getLocationValues(location: any) {
  if (!location) {
    return null;
  }

  const longitude = typeof location.getLng === 'function' ? location.getLng() : location.lng;
  const latitude = typeof location.getLat === 'function' ? location.getLat() : location.lat;

  if (typeof longitude !== 'number' || typeof latitude !== 'number') {
    return null;
  }

  return {
    longitude,
    latitude
  };
}

function parseCoordinateKeyword(keyword: string) {
  const match = keyword.trim().match(/^(-?\d+(?:\.\d+)?)\s*[,，\s]\s*(-?\d+(?:\.\d+)?)$/);

  if (!match) {
    return null;
  }

  const longitude = Number(match[1]);
  const latitude = Number(match[2]);

  if (Number.isNaN(longitude) || Number.isNaN(latitude)) {
    return null;
  }

  if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
    return null;
  }

  return {
    longitude,
    latitude
  };
}

function updateMarker(longitude: number, latitude: number, zoom = 16) {
  if (!marker || !map) {
    return;
  }

  marker.setPosition([longitude, latitude]);
  marker.show();
  map.setZoomAndCenter(zoom, [longitude, latitude]);
}

function applySelection(longitude: number, latitude: number, address?: string) {
  const roundedLongitude = roundCoordinate(longitude);
  const roundedLatitude = roundCoordinate(latitude);

  selectedLongitude.value = roundedLongitude;
  selectedLatitude.value = roundedLatitude;
  selectedAddress.value = address?.trim() || getFallbackAddress(roundedLongitude, roundedLatitude);
  updateMarker(roundedLongitude, roundedLatitude);
}

function hideMarker() {
  marker?.hide();
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number, message: string) {
  return new Promise<T>((resolve, reject) => {
    const timer = window.setTimeout(() => {
      reject(new Error(message));
    }, timeoutMs);

    promise
      .then(result => {
        window.clearTimeout(timer);
        resolve(result);
      })
      .catch(error => {
        window.clearTimeout(timer);
        reject(error);
      });
  });
}

function reverseGeocode(longitude: number, latitude: number) {
  return withTimeout(
    new Promise<string>((resolve, reject) => {
      if (!geocoder) {
        reject(new Error('Geocoder is not ready'));
        return;
      }

      geocoder.getAddress([longitude, latitude], (status: string, result: any) => {
        if (status !== 'complete' || !result?.regeocode) {
          reject(new Error('Reverse geocoding failed'));
          return;
        }

        resolve(result.regeocode.formattedAddress || '');
      });
    }),
    serviceTimeout,
    'Reverse geocoding timeout'
  );
}

function searchPoi(keyword: string) {
  return withTimeout(
    new Promise<any>((resolve, reject) => {
      if (!placeSearch) {
        reject(new Error('PlaceSearch is not ready'));
        return;
      }

      placeSearch.search(keyword, (status: string, result: any) => {
        const poi = result?.poiList?.pois?.[0];
        if (status !== 'complete' || !poi) {
          reject(new Error('No POI result'));
          return;
        }

        resolve(poi);
      });
    }),
    serviceTimeout,
    'POI search timeout'
  );
}

function searchTips(keyword: string) {
  return withTimeout(
    new Promise<any>((resolve, reject) => {
      if (!autoComplete) {
        reject(new Error('AutoComplete is not ready'));
        return;
      }

      autoComplete.search(keyword, (status: string, result: any) => {
        const tip = result?.tips?.find((item: any) => {
          return item?.location || item?.district || item?.address || item?.name;
        });

        if (status !== 'complete' || !tip) {
          reject(new Error('No tips result'));
          return;
        }

        resolve(tip);
      });
    }),
    serviceTimeout,
    'Input tips timeout'
  );
}

function buildSearchAddress(parts: Array<string | undefined>, fallbackKeyword: string) {
  return parts.filter(Boolean).join(' ').trim() || fallbackKeyword;
}

async function resolveLocationFromTip(tip: any, fallbackKeyword: string) {
  const tipLocation = getLocationValues(tip.location);
  if (tipLocation) {
    return {
      location: tipLocation,
      address: buildSearchAddress([tip.district, tip.address, tip.name], fallbackKeyword)
    };
  }

  const mergedKeyword = buildSearchAddress([tip.district, tip.address, tip.name], fallbackKeyword);
  const geocode = await geocodeAddress(mergedKeyword);
  const geocodeLocation = getLocationValues(geocode.location);

  if (!geocodeLocation) {
    return null;
  }

  return {
    location: geocodeLocation,
    address: geocode.formattedAddress || mergedKeyword
  };
}

async function resolveLocationByKeyword(keyword: string) {
  try {
    const poi = await searchPoi(keyword);
    const poiLocation = getLocationValues(poi.location);
    if (poiLocation) {
      return {
        location: poiLocation,
        address: buildSearchAddress([poi.name, poi.address], keyword)
      };
    }
  } catch {
    // Fall through to tip/geocode search.
  }

  try {
    const tip = await searchTips(keyword);
    const tipResult = await resolveLocationFromTip(tip, keyword);
    if (tipResult) {
      return tipResult;
    }
  } catch {
    // Fall through to geocode search.
  }

  const geocode = await geocodeAddress(keyword);
  const geocodeLocation = getLocationValues(geocode.location);
  if (!geocodeLocation) {
    return null;
  }

  return {
    location: geocodeLocation,
    address: geocode.formattedAddress || keyword
  };
}

function geocodeAddress(keyword: string) {
  return withTimeout(
    new Promise<any>((resolve, reject) => {
      if (!geocoder) {
        reject(new Error('Geocoder is not ready'));
        return;
      }

      geocoder.getLocation(keyword, (status: string, result: any) => {
        const geocode = result?.geocodes?.[0];
        if (status !== 'complete' || !geocode) {
          reject(new Error('No geocode result'));
          return;
        }

        resolve(geocode);
      });
    }),
    serviceTimeout,
    'Geocode timeout'
  );
}

function getSearchFailureMessage() {
  if (!AMAP_SECURITY_JS_CODE) {
    return '当前未配置高德安全密钥，搜索服务可能不可用。请先补充 VITE_AMAP_SECURITY_JS_CODE。';
  }

  return '未找到匹配地点，或当前地图 Key 未开通搜索服务';
}

async function restoreSelection() {
  const hasCoordinateValue = hasCoordinatePair(props.latitude, props.longitude);
  searchKeyword.value = preferredKeyword.value;

  if (hasCoordinateValue) {
    applySelection(props.longitude as number, props.latitude as number, props.address);

    if (!props.address?.trim()) {
      try {
        const address = await reverseGeocode(props.longitude as number, props.latitude as number);
        selectedAddress.value = address || getFallbackAddress(props.longitude as number, props.latitude as number);
      } catch {
        selectedAddress.value = getFallbackAddress(props.longitude as number, props.latitude as number);
      }
    }
    return;
  }

  selectedAddress.value = '';
  selectedLatitude.value = undefined;
  selectedLongitude.value = undefined;
  hideMarker();

  if (preferredKeyword.value) {
    await locateByKeyword(preferredKeyword.value, false);
    return;
  }

  map?.setZoomAndCenter(11, defaultCenter);
}

async function locateByKeyword(keyword: string, showMessage = true) {
  const normalizedKeyword = keyword.trim();
  if (!normalizedKeyword) {
    if (showMessage) {
      ElMessage.warning('请输入地点或地址后再搜索');
    }
    return;
  }

  searching.value = true;
  try {
    const coordinateLocation = parseCoordinateKeyword(normalizedKeyword);
    if (coordinateLocation) {
      const address = await reverseGeocode(coordinateLocation.longitude, coordinateLocation.latitude).catch(() => '');
      applySelection(coordinateLocation.longitude, coordinateLocation.latitude, address || normalizedKeyword);
      if (showMessage) {
        ElMessage.success('已按经纬度定位，请确认地图点位');
      }
      return;
    }

    const result = await resolveLocationByKeyword(normalizedKeyword);
    if (!result) {
      throw new Error('Location not found');
    }

    applySelection(result.location.longitude, result.location.latitude, result.address);
    if (showMessage) {
      ElMessage.success('已定位到搜索结果，请确认地图点位');
    }
  } catch {
    if (showMessage) {
      ElMessage.warning(getSearchFailureMessage());
    }
  } finally {
    searching.value = false;
  }
}

async function handleMapClick(event: any) {
  const location = getLocationValues(event?.lnglat);
  if (!location) {
    return;
  }

  locating.value = true;
  try {
    const address = await reverseGeocode(location.longitude, location.latitude);
    applySelection(location.longitude, location.latitude, address);
  } catch {
    applySelection(location.longitude, location.latitude);
    ElMessage.warning('该位置无法自动解析完整地址，已保留经纬度');
  } finally {
    locating.value = false;
  }
}

async function handleOpened() {
  loading.value = true;
  try {
    amapApi = await loadAmapPlugins(['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.AutoComplete']);
    await nextTick();

    if (!mapContainerRef.value) {
      return;
    }

    map = new amapApi.Map(mapContainerRef.value, {
      viewMode: '2D',
      zoom: 11,
      center: defaultCenter,
      resizeEnable: true,
      mapStyle: 'amap://styles/normal'
    });

    defaultLayer = new amapApi.TileLayer();
    map.add(defaultLayer);

    marker = new amapApi.Marker({
      anchor: 'bottom-center',
      offset: new amapApi.Pixel(0, 0)
    });
    marker.setMap(map);
    hideMarker();

    geocoder = new amapApi.Geocoder({});
    placeSearch = new amapApi.PlaceSearch({
      pageSize: 1,
      pageIndex: 1
    });
    autoComplete = new amapApi.AutoComplete({});

    map.on('click', handleMapClick);
    await restoreSelection();
    map.resize();
    window.setTimeout(() => {
      map?.resize();
      if (!hasSelection.value) {
        map?.setZoomAndCenter(11, defaultCenter);
      }
    }, 120);
  } catch {
    ElMessage.error('地图加载失败，请检查高德地图 Key、域名白名单或安全密钥配置');
    dialogVisible.value = false;
  } finally {
    loading.value = false;
  }
}

function runAsyncTask(task: Promise<unknown>) {
  task.catch(() => undefined);
}

function destroyMap() {
  if (map) {
    map.off('click', handleMapClick);
    map.destroy();
  }

  map = null;
  marker = null;
  geocoder = null;
  placeSearch = null;
  autoComplete = null;
  defaultLayer = null;
  amapApi = null;
}

function handleClosed() {
  destroyMap();
  searchKeyword.value = '';
  loading.value = false;
  locating.value = false;
  searching.value = false;
}

function handleSearch() {
  runAsyncTask(locateByKeyword(searchKeyword.value));
}

function handleUseReferenceAddress() {
  if (!preferredKeyword.value) {
    ElMessage.warning('请先填写办公地址或当前打卡地址');
    return;
  }

  searchKeyword.value = preferredKeyword.value;
  runAsyncTask(locateByKeyword(preferredKeyword.value));
}

function handleResetSelection() {
  runAsyncTask(restoreSelection());
}

function handleConfirm() {
  if (!hasSelection.value) {
    ElMessage.warning('请先在地图上选择打卡位置');
    return;
  }

  emit('confirm', {
    address: getDisplayAddress(),
    latitude: selectedLatitude.value as number,
    longitude: selectedLongitude.value as number
  });
  dialogVisible.value = false;
}
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    title="选择打卡位置"
    width="980px"
    append-to-body
    destroy-on-close
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <div class="location-picker">
      <div class="location-toolbar">
        <ElInput v-model="searchKeyword" placeholder="搜索地址、园区、楼宇或地标" clearable @keyup.enter="handleSearch">
          <template #append>
            <ElButton :loading="searching" @click="handleSearch">搜索定位</ElButton>
          </template>
        </ElInput>
        <ElButton @click="handleUseReferenceAddress">按当前地址定位</ElButton>
        <ElButton @click="handleResetSelection">恢复当前配置</ElButton>
      </div>

      <ElAlert
        title="在地图上点击任意位置后，会自动反查地址并回填经纬度。"
        type="info"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <ElAlert
        v-if="!AMAP_SECURITY_JS_CODE"
        title="当前未配置高德安全密钥，若地图底图空白，请在前端环境变量中补充 VITE_AMAP_SECURITY_JS_CODE。"
        type="warning"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <div
        ref="mapContainerRef"
        v-loading="loading || locating"
        class="location-map"
        element-loading-text="地图加载中..."
      />

      <div class="location-info">
        <div class="location-info-item">
          <span class="location-info-label">已选地点</span>
          <span class="location-info-value">{{ getDisplayAddress() }}</span>
        </div>
        <div class="location-info-grid">
          <div class="location-info-item">
            <span class="location-info-label">纬度</span>
            <span class="location-info-value">{{ selectedLatitude ?? '--' }}</span>
          </div>
          <div class="location-info-item">
            <span class="location-info-label">经度</span>
            <span class="location-info-value">{{ selectedLongitude ?? '--' }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :disabled="!hasSelection" @click="handleConfirm">确认并回填</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
.location-picker {
  display: flex;
  flex-direction: column;
}

.location-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 12px;
  margin-bottom: 16px;
}

.location-map {
  width: 100%;
  height: 460px;
  overflow: hidden;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
}

.location-info {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-fill-color-lighter);
}

.location-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.location-info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.location-info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.location-info-value {
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}
</style>
