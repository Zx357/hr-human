<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { GOOGLE_MAPS_API_KEY, GOOGLE_MAPS_LANGUAGE, GOOGLE_MAPS_REGION } from '@/constants/map-sdk';
import { gcj02ToWgs84, wgs84ToGcj02 } from '@/utils/coord-transform';
import { loadGoogleMapsApi } from '@/utils/google-maps';
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

const defaultCenter = { lat: 39.90923, lng: 116.397428 };
const serviceTimeout = 8000;

let googleMaps: any = null;
let map: any = null;
let marker: any = null;
let geocoder: any = null;
let placesService: any = null;
let mapClickListener: any = null;

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

function createLatLng(latitude: number, longitude: number) {
  return new googleMaps.LatLng(latitude, longitude);
}

function hideMarker() {
  marker?.setMap(null);
}

function updateMarkerFromGcj(longitude: number, latitude: number, zoom = 16) {
  if (!googleMaps || !map || !marker) {
    return;
  }

  const [wgsLatitude, wgsLongitude] = gcj02ToWgs84(latitude, longitude);
  const point = createLatLng(wgsLatitude, wgsLongitude);
  marker.setPosition(point);
  marker.setMap(map);
  map.setCenter(point);
  map.setZoom(zoom);
}

function applySelectionFromGcj(longitude: number, latitude: number, address?: string) {
  const roundedLongitude = roundCoordinate(longitude);
  const roundedLatitude = roundCoordinate(latitude);

  selectedLongitude.value = roundedLongitude;
  selectedLatitude.value = roundedLatitude;
  selectedAddress.value = address?.trim() || getFallbackAddress(roundedLongitude, roundedLatitude);
  updateMarkerFromGcj(roundedLongitude, roundedLatitude);
}

function applySelectionFromWgs84(longitude: number, latitude: number, address?: string) {
  const [gcjLatitude, gcjLongitude] = wgs84ToGcj02(latitude, longitude);
  applySelectionFromGcj(gcjLongitude, gcjLatitude, address);
}

function normalizeAddressText(value?: string) {
  return value?.replace(/\s+/g, ' ').trim() || '';
}

function mergeAddressParts(parts: Array<string | undefined>) {
  const normalizedParts = parts.map(normalizeAddressText).filter(Boolean);
  return Array.from(new Set(normalizedParts)).join(' ');
}

function reverseGeocode(longitude: number, latitude: number) {
  return withTimeout(
    new Promise<string>((resolve, reject) => {
      if (!geocoder) {
        reject(new Error('Geocoder is not ready'));
        return;
      }

      geocoder.geocode({ location: { lat: latitude, lng: longitude } }, (results: any[], status: string) => {
        if (status !== googleMaps.GeocoderStatus.OK || !results?.length) {
          reject(new Error('Reverse geocoding failed'));
          return;
        }

        resolve(results[0]?.formatted_address || '');
      });
    }),
    serviceTimeout,
    'Reverse geocoding timeout'
  );
}

function geocodeAddress(keyword: string) {
  return withTimeout(
    new Promise<any>((resolve, reject) => {
      if (!geocoder) {
        reject(new Error('Geocoder is not ready'));
        return;
      }

      geocoder.geocode(
        {
          address: keyword,
          region: GOOGLE_MAPS_REGION
        },
        (results: any[], status: string) => {
          if (status !== googleMaps.GeocoderStatus.OK || !results?.length) {
            reject(new Error(`Geocode failed: ${status}`));
            return;
          }

          resolve(results[0]);
        }
      );
    }),
    serviceTimeout,
    'Geocode timeout'
  );
}

function searchPlace(keyword: string) {
  return withTimeout(
    new Promise<any>((resolve, reject) => {
      if (!placesService) {
        reject(new Error('PlacesService is not ready'));
        return;
      }

      placesService.textSearch(
        {
          query: keyword,
          region: GOOGLE_MAPS_REGION
        },
        (results: any[], status: string) => {
          if (status !== googleMaps.places.PlacesServiceStatus.OK || !results?.length) {
            reject(new Error(`Place search failed: ${status}`));
            return;
          }

          resolve(results[0]);
        }
      );
    }),
    serviceTimeout,
    'Place search timeout'
  );
}

function getPlaceDetails(placeId: string) {
  return withTimeout(
    new Promise<any>((resolve, reject) => {
      if (!placesService) {
        reject(new Error('PlacesService is not ready'));
        return;
      }

      placesService.getDetails(
        {
          placeId,
          fields: ['name', 'formatted_address', 'geometry']
        },
        (result: any, status: string) => {
          if (status !== googleMaps.places.PlacesServiceStatus.OK || !result) {
            reject(new Error(`Place details failed: ${status}`));
            return;
          }

          resolve(result);
        }
      );
    }),
    serviceTimeout,
    'Place details timeout'
  );
}

function getSearchFailureMessage() {
  if (!GOOGLE_MAPS_API_KEY) {
    return $t('org.mapPicker.googleMapsApiKeyIsNotConfiguredPleaseSetViteGoogleMapsApiKeyFirst');
  }

  return $t('org.mapPicker.noMatchingPlaceFoundOrGeocodingPlacesIsNotEnabledForTheCurrentGoogleMapsKey');
}

function getGoogleSearchFailureMessage(error: unknown) {
  const message = error instanceof Error ? error.message : String(error);

  if (message.includes('REQUEST_DENIED')) {
    return $t('org.mapPicker.googleRejectedTheSearchRequestCheckThatPlacesApiAndGeocodingApiAreEnabledForTheKeyAndTheCurrentOriginIsAllowed');
  }

  if (message.includes('ZERO_RESULTS')) {
    return $t('org.mapPicker.noMatchingPlaceFoundTryAMoreCompleteAddressOrEnterLatitudeLongitudeDirectly');
  }

  if (message.includes('OVER_QUERY_LIMIT')) {
    return $t('org.mapPicker.googleMapsQuotaIsTemporarilyLimitedTryAgainLaterOrCheckBillingSettings');
  }

  return getSearchFailureMessage();
}

async function restoreSelection() {
  const hasCoordinateValue = hasCoordinatePair(props.latitude, props.longitude);
  searchKeyword.value = preferredKeyword.value;

  if (hasCoordinateValue) {
    applySelectionFromGcj(props.longitude as number, props.latitude as number, props.address);

    if (!props.address?.trim()) {
      try {
        const [wgsLatitude, wgsLongitude] = gcj02ToWgs84(props.latitude as number, props.longitude as number);
        const address = await reverseGeocode(wgsLongitude, wgsLatitude);
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

async function resolveLocationByKeyword(keyword: string) {
  try {
    const place = await searchPlace(keyword);
    const details = place?.place_id ? await getPlaceDetails(place.place_id).catch(() => null) : null;
    const location = details?.geometry?.location || place?.geometry?.location;
    if (location) {
      const longitude = location.lng();
      const latitude = location.lat();
      const reverseAddress = await reverseGeocode(longitude, latitude).catch(() => '');

      return {
        longitude,
        latitude,
        address:
          mergeAddressParts([
            reverseAddress,
            details?.formatted_address,
            place.formatted_address,
            details?.name || place.name
          ]) || keyword
      };
    }
  } catch {
    // Fall through to geocode search.
  }

  const geocode = await geocodeAddress(keyword);
  const location = geocode?.geometry?.location;
  if (!location) {
    return null;
  }

  return {
    longitude: location.lng(),
    latitude: location.lat(),
    address: geocode.formatted_address || keyword
  };
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
    const coordinateLocation = parseCoordinateKeyword(normalizedKeyword);
    if (coordinateLocation) {
      const [wgsLatitude, wgsLongitude] = gcj02ToWgs84(coordinateLocation.latitude, coordinateLocation.longitude);
      const address = await reverseGeocode(wgsLongitude, wgsLatitude).catch(() => '');
      applySelectionFromGcj(coordinateLocation.longitude, coordinateLocation.latitude, address || normalizedKeyword);
      if (showMessage) {
        ElMessage.success($t('org.mapPicker.locatedByLatLngPleaseConfirmTheMapPoint'));
      }
      return;
    }

    const result = await resolveLocationByKeyword(normalizedKeyword);
    if (!result) {
      throw new Error('Location not found');
    }

    applySelectionFromWgs84(result.longitude, result.latitude, result.address);
    if (showMessage) {
      ElMessage.success($t('org.mapPicker.locatedToTheSearchResultPleaseConfirmTheMapPoint'));
    }
  } catch (error) {
    console.warn('Google Maps search failed:', error);
    if (showMessage) {
      ElMessage.warning(getGoogleSearchFailureMessage(error));
    }
  } finally {
    searching.value = false;
  }
}

async function handleMapClick(event: any) {
  const latLng = event?.latLng;
  if (!latLng) {
    return;
  }

  locating.value = true;
  try {
    const longitude = latLng.lng();
    const latitude = latLng.lat();
    const address = await reverseGeocode(longitude, latitude);
    applySelectionFromWgs84(longitude, latitude, address);
  } catch {
    applySelectionFromWgs84(latLng.lng(), latLng.lat());
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
    const longitude = position.coords.longitude;
    const latitude = position.coords.latitude;
    const address = await reverseGeocode(longitude, latitude).catch(() => '');

    applySelectionFromWgs84(longitude, latitude, address || $t('org.mapPicker.myLocation'));
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
    googleMaps = await loadGoogleMapsApi({
      apiKey: GOOGLE_MAPS_API_KEY,
      language: GOOGLE_MAPS_LANGUAGE,
      region: GOOGLE_MAPS_REGION,
      libraries: ['places']
    });

    await nextTick();

    if (!mapContainerRef.value) {
      return;
    }

    map = new googleMaps.Map(mapContainerRef.value, {
      center: defaultCenter,
      zoom: 11,
      disableDefaultUI: true,
      zoomControl: true,
      fullscreenControl: false,
      streetViewControl: false,
      mapTypeControl: false,
      gestureHandling: 'greedy'
    });

    marker = new googleMaps.Marker({
      clickable: false
    });
    geocoder = new googleMaps.Geocoder();
    placesService = googleMaps.places ? new googleMaps.places.PlacesService(map) : null;
    mapClickListener = map.addListener('click', handleMapClick);

    hideMarker();
    await restoreSelection();
    googleMaps.event.trigger(map, 'resize');
    window.setTimeout(() => {
      googleMaps.event.trigger(map, 'resize');
      if (!hasSelection.value) {
        map?.setCenter(defaultCenter);
        map?.setZoom(11);
      }
    }, 120);
  } catch {
    ElMessage.error(
      $t('org.mapPicker.failedToLoadGoogleMapsCheckTheApiKeyPlacesGeocodingSettingsAndNetworkAccessToMapsGoogleapisCom')
    );
    dialogVisible.value = false;
  } finally {
    loading.value = false;
  }
}

function runAsyncTask(task: Promise<unknown>) {
  task.catch(() => undefined);
}

function destroyMap() {
  if (mapClickListener) {
    mapClickListener.remove();
  }

  if (marker) {
    marker.setMap(null);
  }

  mapClickListener = null;
  marker = null;
  geocoder = null;
  placesService = null;
  map = null;
  googleMaps = null;
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
        :title="$t('org.mapPicker.searchAndPickOnGoogleMapsTheAddressIsFilledAutomaticallyAfterClickingTheMapAndConvertedBackToGcj02WhenSaved')"
        type="info"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <ElAlert
        v-if="!GOOGLE_MAPS_API_KEY"
        :title="$t('org.mapPicker.googleMapsApiKeyIsNotConfiguredPleaseAddViteGoogleMapsApiKeyToTheFrontendEnv')"
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
