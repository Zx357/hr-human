<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { wgs84ToGcj02 } from '@/utils/coord-transform';
import { loadAmapApi, resolveAmapConfig } from '@/utils/amap';
import { $t } from '@/locales';

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
// 运行时解析的高德 Key：后台参数配置优先，env 兜底，解析前先用 env 值兜底展示
const amapKeyReady = ref(false);

const defaultCenter: [number, number] = [116.397428, 39.90923];
const serviceTimeout = 8000;

let AMapNS: any = null;
let map: any = null;
let marker: any = null;
let geocoder: any = null;
let placeSearch: any = null;

const preferredKeyword = computed(() => props.address?.trim() || props.referenceAddress?.trim() || '');
const hasSelection = computed(() => hasCoordinatePair(selectedLatitude.value, selectedLongitude.value));

function hasCoordinatePair(latitude?: number, longitude?: number) {
  return latitude !== undefined && latitude !== null && longitude !== undefined && longitude !== null;
}

function roundCoordinate(value: number) {
  return Number(value.toFixed(6));
}

function getDisplayAddress() {
  return selectedAddress.value || preferredKeyword.value || $t('org.mapPicker.detailedAddressNotObtained');
}

function getFallbackAddress(longitude: number, latitude: number) {
  return $t('org.mapPicker.selectedLocation', { lat: latitude.toFixed(6), lng: longitude.toFixed(6) });
}

function parseCoordinateKeyword(keyword: string) {
  const trimmedKeyword = keyword.trim();
  const exactMatch = trimmedKeyword.match(/^(-?\d+(?:\.\d+)?)\s*[,，\s]\s*(-?\d+(?:\.\d+)?)$/);
  const wrappedMatch = trimmedKeyword.match(/[（(]\s*(-?\d+(?:\.\d+)?)\s*[,，\s]\s*(-?\d+(?:\.\d+)?)\s*[）)]/);
  const match = exactMatch || wrappedMatch;

  if (!match) {
    return null;
  }

  const first = Number(match[1]);
  const second = Number(match[2]);

  if (Number.isNaN(first) || Number.isNaN(second)) {
    return null;
  }

  const looksLikeLatitudeLongitude = Boolean(wrappedMatch) || (Math.abs(first) <= 90 && Math.abs(second) > 90);

  const longitude = looksLikeLatitudeLongitude ? second : first;
  const latitude = looksLikeLatitudeLongitude ? first : second;

  if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
    return null;
  }

  return {
    longitude,
    latitude
  };
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

function hideMarker() {
  marker?.setMap(null);
}

/**
 * 高德 JS API 与后端存储同为 GCJ-02 坐标系，选中/回显坐标直接使用，无需换算
 */
function applySelection(longitude: number, latitude: number, address?: string, zoom = 16) {
  const roundedLongitude = roundCoordinate(longitude);
  const roundedLatitude = roundCoordinate(latitude);

  selectedLongitude.value = roundedLongitude;
  selectedLatitude.value = roundedLatitude;
  selectedAddress.value = address?.trim() || getFallbackAddress(roundedLongitude, roundedLatitude);

  if (!map || !marker) {
    return;
  }

  const position: [number, number] = [roundedLongitude, roundedLatitude];
  marker.setPosition(position);
  marker.setMap(map);
  map.setCenter(position);
  map.setZoom(zoom);
}

function normalizeAddressText(value?: string) {
  return value?.replace(/\s+/g, ' ').trim() || '';
}

function mergeAddressParts(parts: Array<string | undefined>) {
  const normalizedParts = parts.map(normalizeAddressText).filter(Boolean);
  return Array.from(new Set(normalizedParts)).join(' ');
}

/** 逆地理编码（GCJ-02 坐标 → 地址） */
function reverseGeocode(longitude: number, latitude: number) {
  return withTimeout(
    new Promise<string>((resolve, reject) => {
      if (!geocoder) {
        reject(new Error('Geocoder is not ready'));
        return;
      }

      geocoder.getAddress([longitude, latitude], (status: string, result: any) => {
        if (status !== 'complete' || !result?.regeocode?.formattedAddress) {
          reject(new Error('Reverse geocoding failed'));
          return;
        }

        resolve(result.regeocode.formattedAddress);
      });
    }),
    serviceTimeout,
    'Reverse geocoding timeout'
  );
}

/** 地址关键词 → GCJ-02 坐标（地理编码） */
function geocodeAddress(keyword: string) {
  return withTimeout(
    new Promise<{ longitude: number; latitude: number; address: string }>((resolve, reject) => {
      if (!geocoder) {
        reject(new Error('Geocoder is not ready'));
        return;
      }

      geocoder.getLocation(keyword, (status: string, result: any) => {
        const first = result?.geocodes?.[0];
        if (status !== 'complete' || !first?.location) {
          reject(new Error(`Geocode failed: ${status}`));
          return;
        }

        resolve({
          longitude: first.location.getLng(),
          latitude: first.location.getLat(),
          address: first.formattedAddress || keyword
        });
      });
    }),
    serviceTimeout,
    'Geocode timeout'
  );
}

/** POI 关键词搜索 → 第一个结果（GCJ-02） */
function searchPlace(keyword: string) {
  return withTimeout(
    new Promise<{ longitude: number; latitude: number; address: string }>((resolve, reject) => {
      if (!placeSearch) {
        reject(new Error('PlaceSearch is not ready'));
        return;
      }

      placeSearch.search(keyword, (status: string, result: any) => {
        const poi = result?.poiList?.pois?.[0];
        if (status !== 'complete' || !poi?.location) {
          reject(new Error(`Place search failed: ${status}`));
          return;
        }

        resolve({
          longitude: poi.location.getLng(),
          latitude: poi.location.getLat(),
          address: mergeAddressParts([poi.address, poi.name]) || keyword
        });
      });
    }),
    serviceTimeout,
    'Place search timeout'
  );
}

function getSearchFailureMessage(error: unknown) {
  const message = error instanceof Error ? error.message : String(error);

  if (message.includes('INVALID_USER_SCODE') || message.includes('USERKEY_PLAT_NOMATCH')) {
    return $t('org.mapPicker.amapRejectedTheRequestCheckTheKeyAndSecurityCode');
  }

  if (!amapKeyReady.value) {
    return $t('org.mapPicker.amapKeyIsNotConfiguredPleaseSetMapAmapKeyInSystemConfigOrEnvViteAmapKey');
  }

  return $t('org.mapPicker.noMatchingPlaceFoundTryAMoreCompleteAddressOrEnterLatitudeLongitudeDirectly');
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

  map?.setCenter(defaultCenter);
  map?.setZoom(11);
}

async function locateByKeyword(keyword: string, showMessage = true) {
  const normalizedKeyword = keyword.trim();
  if (!normalizedKeyword) {
    if (showMessage) {
      ElMessage.warning($t('org.mapPicker.enterAPlaceAddressOrLatLngBeforeSearching'));
    }
    return;
  }

  searching.value = true;
  try {
    // 直接输入经纬度：按 GCJ-02 处理（与存储坐标系一致），仅补地址
    const coordinateLocation = parseCoordinateKeyword(normalizedKeyword);
    if (coordinateLocation) {
      const address = await reverseGeocode(coordinateLocation.longitude, coordinateLocation.latitude).catch(() => '');
      applySelection(coordinateLocation.longitude, coordinateLocation.latitude, address || normalizedKeyword);
      if (showMessage) {
        ElMessage.success($t('org.mapPicker.locatedByLatLngPleaseConfirmTheMapPoint'));
      }
      return;
    }

    let result: { longitude: number; latitude: number; address: string } | null = null;
    try {
      result = await searchPlace(normalizedKeyword);
    } catch {
      result = await geocodeAddress(normalizedKeyword).catch(() => null);
    }

    if (!result) {
      throw new Error('Location not found');
    }

    applySelection(result.longitude, result.latitude, result.address);
    if (showMessage) {
      ElMessage.success($t('org.mapPicker.locatedToTheSearchResultPleaseConfirmTheMapPoint'));
    }
  } catch (error) {
    console.warn('AMap search failed:', error);
    if (showMessage) {
      ElMessage.warning(getSearchFailureMessage(error));
    }
  } finally {
    searching.value = false;
  }
}

async function handleMapClick(event: any) {
  const lnglat = event?.lnglat;
  if (!lnglat) {
    return;
  }

  locating.value = true;
  try {
    const longitude = lnglat.getLng();
    const latitude = lnglat.getLat();
    const address = await reverseGeocode(longitude, latitude);
    applySelection(longitude, latitude, address);
  } catch {
    applySelection(lnglat.getLng(), lnglat.getLat());
    ElMessage.warning($t('org.mapPicker.theFullAddressOfThisLocationCannotBeResolvedAutomaticallyLatitudeLongitudeKept'));
  } finally {
    locating.value = false;
  }
}

function getCurrentBrowserPosition() {
  return withTimeout(
    new Promise<GeolocationPosition>((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported'));
        return;
      }

      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: serviceTimeout,
        maximumAge: 0
      });
    }),
    serviceTimeout + 1000,
    'Geolocation timeout'
  );
}

async function handleUseMyLocation() {
  locating.value = true;
  try {
    const position = await getCurrentBrowserPosition();
    // 浏览器定位返回 WGS-84，高德使用 GCJ-02，需换算后使用
    const [gcjLatitude, gcjLongitude] = wgs84ToGcj02(position.coords.latitude, position.coords.longitude);
    const address = await reverseGeocode(gcjLongitude, gcjLatitude).catch(() => '');

    applySelection(gcjLongitude, gcjLatitude, address || $t('org.mapPicker.myLocation'));
    ElMessage.success($t('org.mapPicker.myLocationSelected'));
  } catch (error) {
    console.warn('Browser geolocation failed:', error);
    ElMessage.warning($t('org.mapPicker.cannotGetYourLocationMakeSureBrowserGeolocationIsEnabledHttpsIsRecommendedInProduction'));
  } finally {
    locating.value = false;
  }
}

async function handleOpened() {
  loading.value = true;
  try {
    // key/安全密钥优先从后台「系统参数配置」读取（模块级缓存），env 仅兜底，均无则走报错路径
    const amapConfig = await resolveAmapConfig();
    amapKeyReady.value = Boolean(amapConfig.key);

    AMapNS = await loadAmapApi(amapConfig);

    await nextTick();

    if (!mapContainerRef.value) {
      return;
    }

    map = new AMapNS.Map(mapContainerRef.value, {
      center: defaultCenter,
      zoom: 11,
      viewMode: '2D'
    });

    marker = new AMapNS.Marker({
      clickable: false
    });
    geocoder = new AMapNS.Geocoder();
    placeSearch = new AMapNS.PlaceSearch({
      pageSize: 1,
      extensions: 'base'
    });
    map.on('click', handleMapClick);

    hideMarker();
    await restoreSelection();
  } catch {
    if (!amapKeyReady.value) {
      ElMessage.warning($t('org.mapPicker.amapKeyIsNotConfiguredPleaseSetMapAmapKeyInSystemConfigOrEnvViteAmapKey'));
    } else {
      ElMessage.error($t('org.mapPicker.failedToLoadAmapCheckTheKeySecurityCodeAndNetworkAccessToWebapiAmapCom'));
    }
    dialogVisible.value = false;
  } finally {
    loading.value = false;
  }
}

function runAsyncTask(task: Promise<unknown>) {
  task.catch(() => undefined);
}

function destroyMap() {
  if (marker) {
    marker.setMap(null);
  }

  if (map) {
    map.destroy();
  }

  marker = null;
  map = null;
  geocoder = null;
  placeSearch = null;
  AMapNS = null;
}

function handleClosed() {
  destroyMap();
  searchKeyword.value = '';
  loading.value = false;
  locating.value = false;
  searching.value = false;
  amapKeyReady.value = false;
}

function handleSearch() {
  runAsyncTask(locateByKeyword(searchKeyword.value));
}

function handleUseReferenceAddress() {
  if (!preferredKeyword.value) {
    ElMessage.warning($t('org.mapPicker.pleaseFillInTheOfficeAddressOrCurrentClockAddressFirst'));
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
    ElMessage.warning($t('org.mapPicker.pleaseSelectAClockLocationOnTheMapFirst'));
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
    :title="$t('org.mapPicker.selectClockLocation')"
    width="980px"
    append-to-body
    destroy-on-close
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <div class="location-picker">
      <div class="location-toolbar">
        <ElInput
          v-model="searchKeyword"
          :placeholder="$t('org.mapPicker.searchAddressParkBuildingOrLandmarkOrEnterLatLngLngLat')"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <ElButton :loading="searching" @click="handleSearch">{{ $t('org.mapPicker.searchLocate') }}</ElButton>
          </template>
        </ElInput>
        <ElButton @click="handleUseReferenceAddress">{{ $t('org.mapPicker.locateByCurrentAddress') }}</ElButton>
        <ElButton @click="handleResetSelection">{{ $t('org.mapPicker.restoreCurrentConfig') }}</ElButton>
      </div>

      <ElAlert
        :title="$t('org.mapPicker.searchAndPickOnAmapTheAddressIsFilledAutomaticallyAfterClickingTheMapCoordinatesAreStoredAsGcj02')"
        type="info"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <ElAlert
        v-if="!amapKeyReady"
        :title="$t('org.mapPicker.amapKeyIsNotConfiguredPleaseSetMapAmapKeyInSystemConfigOrEnvViteAmapKey')"
        type="warning"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <div class="location-map-shell">
        <div
          ref="mapContainerRef"
          v-loading="loading || locating"
          class="location-map"
          :element-loading-text="$t('org.mapPicker.mapLoading')"
        />
        <ElButton class="my-location-button" :loading="locating" @click="handleUseMyLocation">{{ $t('org.mapPicker.myLocation') }}</ElButton>
      </div>

      <div class="location-info">
        <div class="location-info-item">
          <span class="location-info-label">{{ $t('org.mapPicker.selectedLocation2') }}</span>
          <span class="location-info-value">{{ getDisplayAddress() }}</span>
        </div>
        <div class="location-info-grid">
          <div class="location-info-item">
            <span class="location-info-label">{{ $t('org.mapPicker.latitudeGcj02') }}</span>
            <span class="location-info-value">{{ selectedLatitude ?? '--' }}</span>
          </div>
          <div class="location-info-item">
            <span class="location-info-label">{{ $t('org.mapPicker.longitudeGcj02') }}</span>
            <span class="location-info-value">{{ selectedLongitude ?? '--' }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :disabled="!hasSelection" @click="handleConfirm">{{ $t('org.mapPicker.confirmFill') }}</ElButton>
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

.location-map-shell {
  position: relative;
}

.location-map {
  width: 100%;
  height: 460px;
  overflow: hidden;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
}

.my-location-button {
  position: absolute;
  right: 14px;
  bottom: 92px;
  z-index: 2;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);
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
