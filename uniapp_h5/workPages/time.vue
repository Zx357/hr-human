<template>
  <view class="clock-page">
    <tn-navbar fixed bg-color="#FFFFFF" :bottom-shadow="false" :placeholder="true" home-icon="">
      <template #back>
        <view class="clock-nav-back" @click="goWorkbench">
          <tn-icon name="left"></tn-icon>
        </view>
      </template>
      <view class="clock-nav-title">考勤打卡</view>
    </tn-navbar>

    <view class="map-section">
      <!-- 打卡地图:微信小程序端为原生腾讯地图(免 key);H5 端由 manifest.json 的 h5.sdkConfigs.maps.amap 提供高德 key -->
      <map
        v-if="!IS_H5 || hasAmapKey"
        class="attendance-map"
        :latitude="mapLatitude"
        :longitude="mapLongitude"
        :scale="scale"
        :markers="markers"
        :circles="circles"
        :include-points="includePoints"
        show-location
        show-compass
        @click="openCompanyLocation"
      />
      <!-- H5 端未配置高德 Key 时降级:只显示打卡点半径示意信息(经纬度文字+距离),不展示报错 -->
      <view v-else class="map-fallback-card">
        <view class="map-fallback-name">{{ companyName || '未配置打卡点' }}</view>
        <view class="map-fallback-row">
          <text class="map-fallback-label">打卡点坐标</text>
          <text class="map-fallback-value">{{ clockPointText }}</text>
        </view>
        <view class="map-fallback-row">
          <text class="map-fallback-label">当前距离</text>
          <text class="map-fallback-value">{{ distanceDisplay }}</text>
        </view>
        <view class="map-fallback-row">
          <text class="map-fallback-label">允许半径</text>
          <text class="map-fallback-value">{{ rangeDisplay }}</text>
        </view>
        <view class="map-fallback-tip">未配置高德地图 Key，暂时无法展示地图。可在 manifest.json 的 h5.sdkConfigs.maps.amap 与 config.local.js 中配置后启用。</view>
      </view>

      <view class="location-badge" :class="attendanceStatusClass">{{ locationBadgeText }}</view>

      <view class="company-card" @click="openCompanyLocation">
        <view class="company-name">{{ companyName || '未配置打卡点' }}</view>
        <view class="company-address">{{ companyAddress || '管理员还没有配置可用的公司打卡点' }}</view>
      </view>
    </view>

    <view class="clock-content">
      <!-- 考勤信息加载失败:提示 + 重试入口 -->
      <view v-if="clockInfoFailed" class="clock-error-bar" @click="retryLoadClockInfo">
        <text class="clock-error-text">考勤信息加载失败</text>
        <view class="clock-error-retry">
          <tn-icon name="refresh"></tn-icon>
          <text>重试</text>
        </view>
      </view>

      <view class="time-card">
        <view class="time-text">{{ currentTimeText }}</view>
        <view class="date-text">{{ currentDateText }}</view>

        <view class="warn-tip" :class="attendanceStatusClass">
          <tn-icon name="tips-fill"></tn-icon>
          <text>{{ warningText }}</text>
        </view>

        <view class="clock-actions">
          <view
            class="round-clock"
            :class="{
              done: hasClockedIn,
              ready: !hasClockedIn && canClockIn,
              disabled: !hasClockedIn && !canClockIn
            }"
            @click="handleClockIn"
          >
            <view class="round-icon">
              <tn-icon :name="hasClockedIn ? 'success-circle-fill' : 'clock-fill'"></tn-icon>
            </view>
            <view class="round-label">{{ hasClockedIn ? '已签到' : '签到' }}</view>
            <view class="round-time">{{ clockInTime || scheduledInText }}</view>
          </view>

          <view class="action-divider">
            <view></view>
            <text></text>
            <view></view>
          </view>

          <view
            class="round-clock"
            :class="{
              done: hasClockedOut,
              ready: !hasClockedOut && hasClockedIn && canClockOut,
              disabled: !hasClockedOut && (!hasClockedIn || !canClockOut)
            }"
            @click="handleClockOut"
          >
            <view class="round-icon">
              <tn-icon :name="hasClockedOut ? 'success-circle-fill' : 'clock-fill'"></tn-icon>
            </view>
            <view class="round-label">{{ hasClockedOut ? '已签退' : '签退' }}</view>
            <view class="round-time">{{ clockOutTime || scheduledOutText }}</view>
          </view>
        </view>
      </view>

      <view class="range-card">
        <view class="range-head">
          <view class="range-title">打卡范围</view>
          <view class="refresh-location" @click="refreshPage(true)">
            <tn-icon name="refresh"></tn-icon>
            <text>刷新定位</text>
          </view>
        </view>

        <view class="range-grid">
          <view class="range-item">
            <view class="range-label">当前距离</view>
            <view class="range-value">{{ distanceDisplay }}</view>
          </view>
          <view class="range-item">
            <view class="range-label">允许半径</view>
            <view class="range-value">{{ rangeDisplay }}</view>
          </view>
          <view class="range-item">
            <view class="range-label">定位精度</view>
            <view class="range-value">{{ locationAccuracyDisplay }}</view>
          </view>
          <view class="range-item">
            <view class="range-label">打卡点</view>
            <view class="range-value">{{ clockPointText }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { onLoad, onPullDownRefresh, onShow, onUnload } from '@dcloudio/uni-app'
import { clock, getClockInfo } from '@/api/attendance'
import config from '@/config'

const DEFAULT_MAP_POINT = {
  latitude: 39.9042,
  longitude: 116.4074
}
const GCJ_A = 6378245.0
const GCJ_EE = 0.00669342162296594323

let isH5Platform = false
// #ifdef H5
isH5Platform = true
// #endif
const IS_H5 = isH5Platform

// 高德地图配置(H5 端 <map> 组件渲染使用;微信小程序端为原生腾讯地图,无需 key)
const amapConfig = config ? config.amap || {} : {}
// H5 端是否已配置高德 Key(与 manifest.json 的 h5.sdkConfigs.maps.amap 需同时配置)
const hasAmapKey = !!amapConfig.key

const mapLatitude = ref(DEFAULT_MAP_POINT.latitude)
const mapLongitude = ref(DEFAULT_MAP_POINT.longitude)
const scale = ref(15)
const markers = ref([])
const circles = ref([])
const includePoints = ref([])

const latitude = ref(null)
const longitude = ref(null)
const rawLatitude = ref(null)
const rawLongitude = ref(null)
const locationAccuracy = ref(null)
const locationDenied = ref(false)
const locationFailureReason = ref('')
const locationPermissionState = ref('')

const companyId = ref(null)
const companyName = ref('')
const companyAddress = ref('')
const companyLat = ref(null)
const companyLng = ref(null)
const clockRange = ref(null)
const clockLocations = ref([])
const attendanceConfigured = ref(false)
const distance = ref(null)

const hasClockedIn = ref(false)
const hasClockedOut = ref(false)
const todayRecords = ref([])
const clockInRecord = ref(null)
const clockOutRecord = ref(null)
const todayDaily = ref(null)
const scheduledIn = ref(null)
const scheduledOut = ref(null)

const clocking = ref(false)
const currentTimeText = ref('--:--:--')
const currentDateText = ref('')
// 考勤信息加载失败标记(展示重试入口)
const clockInfoFailed = ref(false)
let timer = null
// 首次 onShow 标记:onLoad 已通过 refreshPage 做过全量刷新(考勤信息+定位),首次显示不再重复请求
let pageShownOnce = false

const hasLocation = computed(() => latitude.value !== null && longitude.value !== null)
const hasCompanyLocation = computed(() => companyLat.value !== null && companyLng.value !== null)
// 定位精度容差(与后端 MobileAttendanceController#resolveAccuracyToleranceMeters 完全一致):
// accuracy 无效/<=0/>200 时容差 0,否则取 min(round(accuracy), 80),双端(H5/小程序)统一生效
const accuracyToleranceMeters = () => {
  const accuracy = Number(locationAccuracy.value)
  if (!Number.isFinite(accuracy) || accuracy <= 0 || accuracy > 200) return 0
  return Math.min(Math.round(accuracy), 80)
}
const effectiveClockRange = computed(() => {
  if (clockRange.value === null || clockRange.value === undefined) return null
  return Number(clockRange.value) + accuracyToleranceMeters()
})
const withinClockRange = computed(() => {
  if (!attendanceConfigured.value) return true
  if (!hasCompanyLocation.value || !hasLocation.value || distance.value === null || effectiveClockRange.value === null) return false
  // 与后端一致:遍历全部已配置打卡点,任意一个满足 距离 <= 打卡范围+精度容差 即通过
  const tolerance = accuracyToleranceMeters()
  const locations = clockLocations.value.length ? clockLocations.value : [{
    latitude: companyLat.value,
    longitude: companyLng.value,
    range: clockRange.value
  }]
  return locations.some((item) => {
    if (item.latitude === null || item.longitude === null || item.range === null) return false
    const itemDistance = calculateDistanceMeters(latitude.value, longitude.value, item.latitude, item.longitude)
    return itemDistance <= Number(item.range) + tolerance
  })
})
const canClockAction = computed(() => {
  if (!attendanceConfigured.value) return true
  if (!hasCompanyLocation.value || !hasLocation.value) return false
  return withinClockRange.value
})
const canClockIn = computed(() => !hasClockedIn.value && !clocking.value && canClockAction.value)
const canClockOut = computed(() => hasClockedIn.value && !hasClockedOut.value && !clocking.value && canClockAction.value)
const attendanceStatusClass = computed(() => {
  if (!attendanceConfigured.value) return 'status-free'
  if (!hasCompanyLocation.value) return 'status-empty'
  if (!hasLocation.value) return 'status-warn'
  return withinClockRange.value ? 'status-ok' : 'status-warn'
})
const locationBadgeText = computed(() => {
  if (!attendanceConfigured.value) return '未限制范围'
  if (!hasCompanyLocation.value) return '未配置打卡点'
  if (!hasLocation.value) return '未获取定位'
  return withinClockRange.value ? '已进入范围' : '超出范围'
})
const locationIssueText = computed(() => {
  if (locationFailureReason.value === 'insecure-context') return '当前 H5 页面需要 HTTPS 才能定位'
  if (locationFailureReason.value === 'unsupported') return '当前浏览器不支持定位'
  if (locationFailureReason.value === 'not-declared') return '小程序未声明定位权限，请更新小程序版本'
  if (locationFailureReason.value === 'permission-denied') return '请开启定位权限后刷新'
  if (locationFailureReason.value === 'timeout') return '定位超时，请刷新定位'
  if (locationFailureReason.value === 'unavailable') return '暂时无法获取当前位置'
  return '请开启定位权限并刷新当前位置'
})
const warningText = computed(() => {
  if (!attendanceConfigured.value) return '后台未启用范围限制，当前可以直接打卡'
  if (!hasCompanyLocation.value) return '后台还没有配置可用打卡点，请先维护公司坐标'
  if (!hasLocation.value) return '请开启定位权限并刷新当前位置，进入打卡范围后即可打卡'
  if (!withinClockRange.value) return `当前位置超出打卡范围，当前距离 ${distanceDisplay.value}`
  if (!hasClockedIn.value) return `已进入打卡范围，当前距离 ${distanceDisplay.value}`
  if (!hasClockedOut.value) return `已签到，当前距离 ${distanceDisplay.value}，下班后可签退`
  return '今日打卡已完成'
})
const distanceDisplay = computed(() => distance.value === null ? '--' : formatDistance(distance.value))
// 允许半径展示为"打卡范围+精度容差"的实际放行半径,与后端判定口径一致
const rangeDisplay = computed(() => {
  if (clockRange.value === null || clockRange.value === undefined || clockRange.value <= 0) return '--'
  return `${effectiveClockRange.value}米`
})
const locationAccuracyDisplay = computed(() => {
  if (!Number.isFinite(Number(locationAccuracy.value))) return '--'
  const accuracy = Number(locationAccuracy.value)
  if (accuracy >= 1000) return `${(accuracy / 1000).toFixed(2)}公里`
  return `${Math.round(accuracy)}米`
})
const clockPointText = computed(() => {
  if (!hasCompanyLocation.value) return '--'
  return `${formatCoordinate(companyLat.value)}, ${formatCoordinate(companyLng.value)}`
})
const clockInTime = computed(() => clockInRecord.value ? formatTime(clockInRecord.value.clockTime || clockInRecord.value.createTime) : '')
const clockOutTime = computed(() => clockOutRecord.value ? formatTime(clockOutRecord.value.clockTime || clockOutRecord.value.createTime) : '')
const scheduledInText = computed(() => scheduledIn.value ? `规定 ${formatClockText(scheduledIn.value)}` : '等待打卡')
const scheduledOutText = computed(() => scheduledOut.value ? `规定 ${formatClockText(scheduledOut.value)}` : '等待打卡')

onLoad(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  refreshPage(false)
})

onShow(() => {
  // 首次显示由 onLoad 的 refreshPage 完成全量刷新(考勤信息+定位),跳过避免双请求
  if (pageShownOnce) {
    loadClockInfo()
    // 返回页面(如从系统设置开启定位后)时刷新定位
    getLocation(false)
  }
  pageShownOnce = true
})

onPullDownRefresh(() => {
  refreshPage(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})

onUnload(() => {
  stopTimer()
})
onUnmounted(stopTimer)

async function refreshPage(showLocationError = false) {
  await Promise.all([loadClockInfo(), getLocation(showLocationError)])
  updateActiveClockLocation()
  calcDistance()
  refreshMapData()
}

function updateTime() {
  const now = new Date()
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  const second = String(now.getSeconds()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  currentTimeText.value = `${hour}:${minute}:${second}`
  currentDateText.value = `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function goWorkbench() {
  uni.reLaunch({
    url: '/pages/index?index=2'
  })
}

function getLocation(showError = false) {
  if (IS_H5) return getH5Location(showError)
  return getUniLocation(showError)
}

function getUniLocation(showError = false) {
  return new Promise(resolve => {
    uni.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      success: res => {
        applyLocationSuccess(res.latitude, res.longitude, res.accuracy)
        resolve(res)
      },
      fail: error => {
        applyLocationFailure(resolveLocationFailureReason(error), error, showError)
        resolve(null)
      }
    })
  })
}

async function getH5Location(showError = false) {
  await updateH5PermissionState()

  if (!isH5LocationSupported()) {
    applyLocationFailure('unsupported', null, showError)
    return null
  }
  if (!isSecureLocationContext()) {
    applyLocationFailure('insecure-context', null, showError)
    return null
  }

  try {
    const position = await getBestH5Position()
    const rawLat = Number(position.coords.latitude)
    const rawLng = Number(position.coords.longitude)
    const normalized = normalizeH5Location(rawLat, rawLng)
    applyLocationSuccess(normalized.latitude, normalized.longitude, position.coords.accuracy, rawLat, rawLng)
    return normalized
  } catch (error) {
    applyLocationFailure(resolveLocationFailureReason(error), error, showError)
    return null
  }
}

function getH5PositionOnce(timeout = 6000) {
  return new Promise((resolve, reject) => {
    window.navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true,
      timeout,
      maximumAge: 0
    })
  })
}

async function getBestH5Position() {
  const positions = []
  let lastError = null

  for (let index = 0; index < 3; index += 1) {
    try {
      const position = await getH5PositionOnce(index === 0 ? 8000 : 5000)
      positions.push(position)
      const accuracy = Number(position.coords && position.coords.accuracy)
      if (Number.isFinite(accuracy) && accuracy <= 60) break
    } catch (error) {
      lastError = error
    }
  }

  if (!positions.length) throw lastError || new Error('定位失败')
  return positions.reduce((best, current) => {
    const bestAccuracy = Number(best.coords && best.coords.accuracy)
    const currentAccuracy = Number(current.coords && current.coords.accuracy)
    if (!Number.isFinite(bestAccuracy)) return current
    if (!Number.isFinite(currentAccuracy)) return best
    return currentAccuracy < bestAccuracy ? current : best
  })
}

function applyLocationSuccess(lat, lng, accuracy = null, rawLat = null, rawLng = null) {
  const parsedLat = Number(lat)
  const parsedLng = Number(lng)
  if (!Number.isFinite(parsedLat) || !Number.isFinite(parsedLng)) {
    applyLocationFailure('unavailable', null, false)
    return
  }

  latitude.value = parsedLat
  longitude.value = parsedLng
  rawLatitude.value = Number.isFinite(Number(rawLat)) ? Number(rawLat) : null
  rawLongitude.value = Number.isFinite(Number(rawLng)) ? Number(rawLng) : null
  locationAccuracy.value = Number.isFinite(Number(accuracy)) ? Math.round(Number(accuracy)) : null
  locationDenied.value = false
  locationFailureReason.value = ''
  if (IS_H5) locationPermissionState.value = 'granted'

  updateActiveClockLocation()
  calcDistance()
  refreshMapData()
}

function applyLocationFailure(reason, error, showError = false) {
  latitude.value = null
  longitude.value = null
  rawLatitude.value = null
  rawLongitude.value = null
  locationAccuracy.value = null
  locationFailureReason.value = reason || 'unknown'
  locationDenied.value = locationFailureReason.value === 'permission-denied' || locationPermissionState.value === 'denied'
  calcDistance()
  refreshMapData()

  if (showError) presentLocationError()
  // 定位失败时页面已有暂无位置信息兜底展示,此处仅忽略
}

function presentLocationError() {
  if (locationDenied.value || locationFailureReason.value === 'insecure-context' || locationFailureReason.value === 'unsupported') {
    promptEnableLocation()
    return
  }
  uni.showToast({ icon: 'none', title: locationIssueText.value })
}

async function updateH5PermissionState() {
  if (!IS_H5 || typeof window === 'undefined' || !window.navigator || !window.navigator.permissions || !window.navigator.permissions.query) {
    return
  }

  try {
    const status = await window.navigator.permissions.query({ name: 'geolocation' })
    locationPermissionState.value = status.state || ''
    locationDenied.value = status.state === 'denied'
  } catch (error) {
  }
}

function isH5LocationSupported() {
  return typeof window !== 'undefined' && !!(window.navigator && window.navigator.geolocation)
}

function isSecureLocationContext() {
  if (!IS_H5 || typeof window === 'undefined') return true
  if (window.isSecureContext) return true
  const hostname = window.location && window.location.hostname ? window.location.hostname : ''
  return hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '[::1]'
}

function normalizeH5Location(lat, lng) {
  const parsedLat = Number(lat)
  const parsedLng = Number(lng)
  if (!Number.isFinite(parsedLat) || !Number.isFinite(parsedLng)) {
    return { latitude: parsedLat, longitude: parsedLng }
  }

  const [gcjLat, gcjLng] = wgs84ToGcj02(parsedLat, parsedLng)
  return { latitude: gcjLat, longitude: gcjLng }
}

function resolveLocationFailureReason(error) {
  if (locationPermissionState.value === 'denied' || isLocationDenied(error)) return 'permission-denied'

  const message = error && (error.errMsg || error.message) ? String(error.errMsg || error.message) : ''
  const code = error && typeof error.code === 'number' ? error.code : null
  if (code === 1) return 'permission-denied'
  if (code === 2) return 'unavailable'
  if (code === 3) return 'timeout'
  if (/timeout/i.test(message)) return 'timeout'
  if (/unavailable|position unavailable/i.test(message)) return 'unavailable'
  if (/secure context|only secure origins|insecure/i.test(message)) return 'insecure-context'
  if (/not support|unsupported|not available/i.test(message)) return 'unsupported'
  // 微信小程序端 manifest 未声明定位权限时,API 直接报 "api scope is not declared"
  if (/scope is not declared|requiredPrivateInfos/i.test(message)) return 'not-declared'
  return 'unknown'
}

function isLocationDenied(error) {
  const message = error && (error.errMsg || error.message) ? String(error.errMsg || error.message) : ''
  return /auth deny|authorize no response|permission|denied/i.test(message)
}

function promptEnableLocation() {
  let content = '请在系统或浏览器设置中允许当前位置权限，然后返回页面刷新定位。'
  if (locationFailureReason.value === 'insecure-context') {
    content = '当前 H5 页面未启用 HTTPS，浏览器不会提供定位能力。请改用 HTTPS 域名访问后再刷新页面。'
  } else if (locationFailureReason.value === 'unsupported') {
    content = '当前浏览器不支持网页定位，请改用微信、Edge、Chrome 或 Safari 打开后再尝试打卡。'
  } else if (locationFailureReason.value === 'not-declared') {
    content = '当前小程序版本未声明定位权限，无法进行打卡定位。请将小程序更新到最新版本后再试。'
  }

  uni.showModal({
    title: IS_H5 ? 'H5 定位说明' : '需要定位权限',
    content,
    showCancel: !IS_H5,
    confirmText: '知道了',
    success: result => {
      if (!IS_H5 && result.confirm) uni.openSetting({})
    }
  })
}

async function loadClockInfo() {
  try {
    const res = await getClockInfo()
    if (res.code !== 200 || !res.data) {
      clockInfoFailed.value = true
      return
    }
    clockInfoFailed.value = false
    const data = res.data

    todayRecords.value = Array.isArray(data.todayRecords) ? data.todayRecords : []
    hasClockedIn.value = !!data.hasClockedIn
    hasClockedOut.value = !!data.hasClockedOut
    scheduledIn.value = data.scheduledIn || data.workStartTime || null
    scheduledOut.value = data.scheduledOut || data.workEndTime || null
    todayDaily.value = data.todayDaily || null
    companyId.value = data.companyId || null
    companyName.value = data.companyName || ''
    companyAddress.value = data.companyAddress || ''
    companyLat.value = parseNumber(data.companyLat)
    companyLng.value = parseNumber(data.companyLng)
    clockRange.value = parseNumber(data.clockRange)
    attendanceConfigured.value = typeof data.attendanceConfigured === 'undefined'
      ? !!(companyLat.value && companyLng.value && clockRange.value)
      : !!data.attendanceConfigured

    clockLocations.value = normalizeClockLocations(data.clockLocations)
    if (!clockLocations.value.length && hasCompanyLocation.value && clockRange.value) {
      clockLocations.value = [{
        id: companyId.value,
        name: companyName.value,
        address: companyAddress.value,
        latitude: companyLat.value,
        longitude: companyLng.value,
        range: clockRange.value
      }]
    }

    clockInRecord.value = todayRecords.value.find(isClockInRecord) || null
    clockOutRecord.value = todayRecords.value.find(isClockOutRecord) || null
    if (clockInRecord.value) hasClockedIn.value = true
    if (clockOutRecord.value) hasClockedOut.value = true

    updateActiveClockLocation()
    calcDistance()
    refreshMapData()
  } catch (error) {
    // 加载失败给出提示与重试入口,不再静默
    clockInfoFailed.value = true
    uni.showToast({ icon: 'none', title: '考勤信息加载失败，请重试' })
  }
}

// 重试拉取考勤信息
async function retryLoadClockInfo() {
  clockInfoFailed.value = false
  await loadClockInfo()
}

function parseNumber(value) {
  if (value === null || value === undefined || value === '') return null
  const number = Number(value)
  return Number.isFinite(number) ? number : null
}

function normalizeClockLocations(locations) {
  if (!Array.isArray(locations)) return []
  return locations
    .map(item => ({
      id: item.id,
      name: item.locationName || item.companyName || '',
      address: item.address || item.companyAddress || item.locationName || '',
      latitude: parseNumber(item.latitude ?? item.companyLat),
      longitude: parseNumber(item.longitude ?? item.companyLng),
      range: parseNumber(item.clockRange)
    }))
    .filter(item => item.latitude !== null && item.longitude !== null && item.range !== null && item.range > 0)
}

function updateActiveClockLocation() {
  if (!clockLocations.value.length) return

  let activeLocation = clockLocations.value[0]
  if (hasLocation.value) {
    activeLocation = clockLocations.value
      .map(item => ({
        ...item,
        distance: calculateDistanceMeters(latitude.value, longitude.value, item.latitude, item.longitude)
      }))
      .sort((a, b) => a.distance - b.distance)[0]
  }

  companyId.value = activeLocation.id || null
  companyName.value = activeLocation.name || companyName.value
  companyAddress.value = activeLocation.address || companyAddress.value
  companyLat.value = activeLocation.latitude
  companyLng.value = activeLocation.longitude
  clockRange.value = activeLocation.range
}

function refreshMapData() {
  markers.value = buildMarkers()
  circles.value = buildCircles()
  includePoints.value = buildIncludePoints()
  refreshMapCenter()
}

function buildMarkers() {
  if (!hasCompanyLocation.value) return []
  return [{
    id: 1,
    latitude: companyLat.value,
    longitude: companyLng.value,
    iconPath: '/static/logo.png',
    width: 28,
    height: 28,
    callout: {
      content: companyName.value || '打卡点',
      display: 'ALWAYS',
      color: '#223554',
      fontSize: 12,
      bgColor: '#ffffff',
      borderRadius: 14,
      padding: 8
    }
  }]
}

function buildCircles() {
  if (!hasCompanyLocation.value || !clockRange.value) return []
  return [{
    latitude: companyLat.value,
    longitude: companyLng.value,
    radius: clockRange.value,
    strokeWidth: 2,
    color: '#526FA6',
    fillColor: 'rgba(82, 111, 166, 0.16)'
  }]
}

function buildIncludePoints() {
  const points = []
  if (hasCompanyLocation.value) points.push({ latitude: companyLat.value, longitude: companyLng.value })
  if (hasLocation.value) points.push({ latitude: latitude.value, longitude: longitude.value })
  return points
}

function refreshMapCenter() {
  if (hasCompanyLocation.value && hasLocation.value) {
    mapLatitude.value = (companyLat.value + latitude.value) / 2
    mapLongitude.value = (companyLng.value + longitude.value) / 2
    scale.value = getScaleByDistance(Math.max(distance.value || 0, clockRange.value || 0, 300))
    return
  }
  if (hasCompanyLocation.value) {
    mapLatitude.value = companyLat.value
    mapLongitude.value = companyLng.value
    scale.value = getScaleByDistance(clockRange.value || 300)
    return
  }
  if (hasLocation.value) {
    mapLatitude.value = latitude.value
    mapLongitude.value = longitude.value
    scale.value = 16
    return
  }
  mapLatitude.value = DEFAULT_MAP_POINT.latitude
  mapLongitude.value = DEFAULT_MAP_POINT.longitude
  scale.value = 12
}

function calcDistance() {
  if (!hasCompanyLocation.value || !hasLocation.value) {
    distance.value = null
    return
  }
  updateActiveClockLocation()
  distance.value = Math.round(calculateDistanceMeters(latitude.value, longitude.value, companyLat.value, companyLng.value))
}

async function handleClockIn() {
  vibrateShort()
  if (hasClockedIn.value || clocking.value) return
  if (!canClockIn.value) {
    handleClockBlocked()
    return
  }
  await doClock(1)
}

async function handleClockOut() {
  vibrateShort()
  if (hasClockedOut.value || clocking.value) return
  if (!hasClockedIn.value) {
    uni.showToast({ icon: 'none', title: '请先签到' })
    return
  }
  if (!canClockOut.value) {
    handleClockBlocked()
    return
  }
  await doClock(2)
}

function handleClockBlocked() {
  if (attendanceConfigured.value && !hasCompanyLocation.value) {
    uni.showToast({ icon: 'none', title: '公司还没有配置可用打卡点' })
    return
  }
  if (attendanceConfigured.value && !hasLocation.value) {
    if (locationDenied.value) {
      promptEnableLocation()
    } else {
      uni.showToast({ icon: 'none', title: locationIssueText.value })
    }
    return
  }
  if (attendanceConfigured.value && !withinClockRange.value) {
    uni.showToast({ icon: 'none', title: `当前位置不在范围内，距离 ${distanceDisplay.value}` })
  }
}

async function doClock(clockType) {
  clocking.value = true
  uni.showLoading({ title: '打卡中...' })
  try {
    const payload = { clockType }
    if (companyId.value) payload.companyId = companyId.value
    if (hasLocation.value) {
      payload.location = `${latitude.value},${longitude.value}`
      if (Number.isFinite(Number(locationAccuracy.value))) {
        payload.accuracy = Math.round(Number(locationAccuracy.value))
      }
    }

    const res = await clock(payload)
    uni.hideLoading()
    if (res.code === 200) {
      uni.showToast({ icon: 'success', title: clockType === 1 ? '签到成功' : '签退成功' })
      await refreshPage(false)
    } else {
      uni.showToast({ icon: 'none', title: res.msg || '打卡失败' })
    }
  } catch (error) {
    uni.hideLoading()
    const message = error && error.message ? error.message : error
    uni.showToast({ icon: 'none', title: message || '打卡失败' })
  } finally {
    clocking.value = false
  }
}

function openCompanyLocation() {
  if (!hasCompanyLocation.value) {
    uni.showToast({ icon: 'none', title: '暂无打卡点位置' })
    return
  }
  // #ifdef MP-WEIXIN
  uni.openLocation({
    latitude: companyLat.value,
    longitude: companyLng.value,
    name: companyName.value || '打卡点',
    address: companyAddress.value || companyName.value || '打卡点'
  })
  // #endif
  // #ifdef H5
  // H5 端不支持 uni.openLocation,跳转高德网页版查看打卡点
  const lng = companyLng.value
  const lat = companyLat.value
  const name = encodeURIComponent(companyName.value || '打卡点')
  window.open(`https://uri.amap.com/marker?position=${lng},${lat}&name=${name}`, '_blank')
  // #endif
}

function vibrateShort() {
  if (uni.vibrateShort) {
    uni.vibrateShort({})
  }
}

function isClockInRecord(item) {
  const type = String(item.clockType ?? item.type ?? '').toLowerCase()
  return Number(item.clockType) === 1 || type.includes('in') || type.includes('上班') || type.includes('签到')
}

function isClockOutRecord(item) {
  const type = String(item.clockType ?? item.type ?? '').toLowerCase()
  return Number(item.clockType) === 2 || type.includes('out') || type.includes('下班') || type.includes('签退')
}

function formatTime(value) {
  if (!value) return ''
  const text = String(value).replace('T', ' ')
  const timePart = text.includes(' ') ? text.split(' ').pop() : text
  return timePart.slice(0, 5)
}

function formatClockText(value) {
  const text = formatTime(value)
  return text || String(value)
}

function formatDistance(value) {
  if (value >= 1000) return `${(value / 1000).toFixed(value >= 10000 ? 1 : 2)}公里`
  return `${Math.round(value)}米`
}

function formatCoordinate(value) {
  return typeof value === 'number' ? value.toFixed(6) : '--'
}

function getScaleByDistance(value) {
  if (!value || value <= 200) return 16
  if (value <= 500) return 15
  if (value <= 1000) return 14
  if (value <= 3000) return 13
  return 12
}

function calculateDistanceMeters(lat, lng, targetLat, targetLng) {
  const radLat1 = (lat * Math.PI) / 180
  const radLat2 = (targetLat * Math.PI) / 180
  const deltaLat = radLat1 - radLat2
  const deltaLng = ((lng - targetLng) * Math.PI) / 180
  const angle = 2 * Math.asin(
    Math.sqrt(
      Math.pow(Math.sin(deltaLat / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(deltaLng / 2), 2)
    )
  )
  return angle * 6378137
}

function wgs84ToGcj02(lat, lng) {
  if (isOutOfChina(lat, lng)) return [lat, lng]

  let deltaLat = transformLatitude(lng - 105.0, lat - 35.0)
  let deltaLng = transformLongitude(lng - 105.0, lat - 35.0)
  const radLat = (lat / 180.0) * Math.PI
  let magic = Math.sin(radLat)
  magic = 1 - GCJ_EE * magic * magic
  const sqrtMagic = Math.sqrt(magic)

  deltaLat = (deltaLat * 180.0) / (((GCJ_A * (1 - GCJ_EE)) / (magic * sqrtMagic)) * Math.PI)
  deltaLng = (deltaLng * 180.0) / ((GCJ_A / sqrtMagic) * Math.cos(radLat) * Math.PI)
  return [lat + deltaLat, lng + deltaLng]
}

function isOutOfChina(lat, lng) {
  return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
}

function transformLatitude(x, y) {
  let result = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
  result += ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0) / 3.0
  result += ((20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin((y / 3.0) * Math.PI)) * 2.0) / 3.0
  result += ((160.0 * Math.sin((y / 12.0) * Math.PI) + 320.0 * Math.sin((y * Math.PI) / 30.0)) * 2.0) / 3.0
  return result
}

function transformLongitude(x, y) {
  let result = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
  result += ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0) / 3.0
  result += ((20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin((x / 3.0) * Math.PI)) * 2.0) / 3.0
  result += ((150.0 * Math.sin((x / 12.0) * Math.PI) + 300.0 * Math.sin((x / 30.0) * Math.PI)) * 2.0) / 3.0
  return result
}
</script>

<script>
export default {
  name: 'TemplateTime'
}
</script>

<style lang="scss" scoped>
.clock-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F8F7F8;
  color: #121A2B;
}

.clock-nav-back {
  width: 88rpx;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #111827;
  font-size: 40rpx;
}

.clock-nav-title {
  color: #111827;
  font-size: 34rpx;
  font-weight: 900;
  text-align: center;
}

.map-section {
  position: relative;
  height: 410rpx;
  background: #DDEAF8;
}

.attendance-map {
  width: 100%;
  height: 100%;
}

/* H5 未配置高德 Key 时的打卡点半径示意卡片 */
.map-fallback-card {
  height: 100%;
  padding: 30rpx 32rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(135deg, #DDEAF8 0%, #E8F3FC 55%, #E4F5EE 100%);
}

.map-fallback-name {
  color: #14213D;
  font-size: 30rpx;
  font-weight: 900;
}

.map-fallback-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18rpx;
}

.map-fallback-label {
  color: #56657A;
  font-size: 24rpx;
}

.map-fallback-value {
  color: #172642;
  font-size: 25rpx;
  font-weight: 800;
}

.map-fallback-tip {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(34, 53, 84, 0.12);
  color: #7C8AA0;
  font-size: 22rpx;
  line-height: 1.5;
}

.location-badge {
  position: absolute;
  left: 32rpx;
  bottom: 138rpx;
  z-index: 2;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 800;
  background: #FFF6D8;
  color: #D56A00;
}

.location-badge.status-ok {
  background: #E8F8F1;
  color: #25976D;
}

.location-badge.status-free {
  background: #EEF5FF;
  color: #526FA6;
}

.location-badge.status-empty {
  background: #FFECEC;
  color: #C75B5B;
}

.company-card {
  position: absolute;
  left: 26rpx;
  right: 26rpx;
  bottom: -58rpx;
  z-index: 3;
  min-height: 132rpx;
  padding: 22rpx 26rpx;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: #FFFFFF;
  box-shadow: 0 18rpx 48rpx rgba(31, 41, 55, 0.12);
}

.company-name {
  color: #14213D;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.25;
}

.company-address {
  margin-top: 10rpx;
  color: #56657A;
  font-size: 25rpx;
  line-height: 1.42;
}

.clock-content {
  padding: 84rpx 26rpx 46rpx;
}

/* 考勤信息加载失败提示条 */
.clock-error-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 18rpx 24rpx;
  border-radius: 18rpx;
  background: #FFECEC;
  color: #C75B5B;
}

.clock-error-text {
  font-size: 25rpx;
  font-weight: 600;
}

.clock-error-retry {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: #526FA6;
  background: #EEF5FF;
}

.time-card,
.range-card {
  border-radius: 28rpx;
  background: #FFFFFF;
  box-shadow: 0 14rpx 38rpx rgba(31, 41, 55, 0.06);
}

.time-card {
  padding: 42rpx 28rpx 36rpx;
  text-align: center;
}

.time-text {
  color: #121A2B;
  font-size: 64rpx;
  font-weight: 900;
  line-height: 1.1;
  letter-spacing: 4rpx;
  font-variant-numeric: tabular-nums;
}

.date-text {
  margin-top: 16rpx;
  color: #8B95A7;
  font-size: 27rpx;
  font-weight: 600;
}

.warn-tip {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin: 30rpx 0 32rpx;
  padding: 20rpx 22rpx;
  border-radius: 16rpx;
  background: #FFF8E7;
  color: #C76500;
  font-size: 25rpx;
  line-height: 1.45;
  text-align: left;
}

.warn-tip.status-ok {
  background: #EAF8F1;
  color: #25976D;
}

.warn-tip.status-free {
  background: #EEF5FF;
  color: #526FA6;
}

.warn-tip.status-empty {
  background: #FFECEC;
  color: #C75B5B;
}

.clock-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

.round-clock {
  width: 144rpx;
  height: 144rpx;
  border-radius: 50%;
  border: 2rpx solid #E3E8EF;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F7F9FC;
  color: #9AA4B2;
}

.round-clock.ready {
  border-color: #D6E3F5;
  background: #F1F6FD;
  color: #526FA6;
}

.round-clock.done {
  border-color: #D6F1E4;
  background: #F1FBF6;
  color: #25976D;
}

.round-icon {
  font-size: 50rpx;
  line-height: 1;
}

.round-label {
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 900;
}

.round-time {
  margin-top: 4rpx;
  max-width: 128rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 19rpx;
  font-weight: 700;
}

.action-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 34rpx;
}

.action-divider view {
  width: 2rpx;
  height: 42rpx;
  background: #E7EBF1;
}

.action-divider text {
  width: 12rpx;
  height: 12rpx;
  margin: 10rpx 0;
  border-radius: 50%;
  background: #D3DAE5;
}

.range-card {
  margin-top: 24rpx;
  padding: 30rpx 28rpx;
}

.range-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.range-title {
  color: #121A2B;
  font-size: 31rpx;
  font-weight: 900;
}

.refresh-location {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #526FA6;
  font-size: 24rpx;
  font-weight: 800;
}

.range-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.range-item {
  min-height: 112rpx;
  padding: 18rpx 20rpx;
  border-radius: 18rpx;
  background: #F5F7FA;
  box-sizing: border-box;
}

.range-label {
  color: #95A0B2;
  font-size: 23rpx;
}

.range-value {
  margin-top: 12rpx;
  color: #172642;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
  word-break: break-all;
}
</style>
