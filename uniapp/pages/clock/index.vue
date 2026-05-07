<template>
  <view class="page-wrap">
    <view class="map-section">
      <!-- #ifdef H5 -->
      <view class="attendance-map google-map-shell">
        <view ref="googleMapCanvas" class="google-map-canvas"></view>
        <view v-if="googleMapLoading || googleMapError" class="google-map-state">
          <text v-if="googleMapLoading">Google 地图加载中...</text>
          <text v-else>{{ googleMapError }}</text>
        </view>
      </view>
      <!-- #endif -->

      <!-- #ifndef H5 -->
      <map
        id="attendanceMap"
        class="attendance-map"
        :latitude="mapLatitude"
        :longitude="mapLongitude"
        :scale="scale"
        :markers="markers"
        :circles="circles"
        :include-points="includePoints"
        show-location
        show-compass
      />
      <!-- #endif -->

      <view class="map-overlay">
        <view class="status-chip" :class="attendanceStatusClass">
          <text>{{ attendanceStatusText }}</text>
        </view>
        <view class="overlay-card">
          <text class="overlay-title">{{ companyName || '未配置打卡点' }}</text>
          <text class="overlay-desc">
            {{ companyAddress || '管理员还没有在后台配置可用的公司打卡点' }}
          </text>
        </view>
      </view>
    </view>

    <view class="content-area">
      <view class="clock-core">
        <view class="time-display">
          <text class="current-time">{{ currentTime }}</text>
          <text class="current-date">{{ currentDate }}</text>
        </view>

        <view class="clock-tip" :class="attendanceStatusClass">
          <u-icon
            :name="attendanceStatusClass === 'status-ok' ? 'checkmark-circle-fill' : 'info-circle-fill'"
            size="18"
            :color="attendanceStatusClass === 'status-ok' ? '#10b981' : '#f59e0b'"
          />
          <text>{{ clockTipText }}</text>
        </view>

        <view class="clock-buttons">
          <view
            class="clock-btn"
            :class="{
              done: hasClockedIn,
              ready: !hasClockedIn && canClockIn,
              blocked: !hasClockedIn && !canClockIn
            }"
            @click="handleClockIn"
          >
            <view class="btn-inner">
              <u-icon
                :name="hasClockedIn ? 'checkmark-circle-fill' : 'clock-fill'"
                size="36"
                :color="hasClockedIn ? '#10b981' : canClockIn ? '#ffffff' : '#9ca3af'"
              />
              <text class="btn-label">{{ hasClockedIn ? '已签到' : '签到' }}</text>
            </view>
          </view>

          <view class="clock-divider">
            <view class="divider-line"></view>
            <view class="divider-dot"></view>
            <view class="divider-line"></view>
          </view>

          <view
            class="clock-btn"
            :class="{
              done: hasClockedOut,
              ready: !hasClockedOut && hasClockedIn && canClockOut,
              locked: !hasClockedIn,
              blocked: hasClockedIn && !hasClockedOut && !canClockOut
            }"
            @click="handleClockOut"
          >
            <view class="btn-inner">
              <u-icon
                :name="hasClockedOut ? 'checkmark-circle-fill' : 'clock-fill'"
                size="36"
                :color="hasClockedOut ? '#10b981' : canClockOut ? '#ffffff' : '#9ca3af'"
              />
              <text class="btn-label">{{ hasClockedOut ? '已签退' : '签退' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="location-card">
        <view class="card-head">
          <text class="card-title">打卡范围</text>
          <view class="refresh-action" @click="refreshPage(true)">
            <u-icon name="reload" size="16" color="#4f46e5"></u-icon>
            <text>刷新定位</text>
          </view>
        </view>

        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">当前距离</text>
            <text class="info-value">{{ distanceDisplay }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">允许半径</text>
            <text class="info-value">{{ rangeDisplay }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">定位精度</text>
            <text class="info-value">{{ locationAccuracyDisplay }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">当前位置</text>
            <text class="info-value">{{ formatCoordinate(latitude) }}, {{ formatCoordinate(longitude) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">打卡点</text>
            <text class="info-value">{{ formatCoordinate(companyLat) }}, {{ formatCoordinate(companyLng) }}</text>
          </view>
        </view>
      </view>

      <view class="quick-actions">
        <view class="action-item" @click="goToCardApply">
          <view class="action-icon action-purple">
            <u-icon name="edit-pen-fill" size="24" color="#6366f1"></u-icon>
          </view>
          <text class="action-name">补卡申请</text>
        </view>
        <view class="action-item" @click="goToMyAttendance">
          <view class="action-icon action-blue">
            <u-icon name="calendar-fill" size="24" color="#0ea5e9"></u-icon>
          </view>
          <text class="action-name">我的考勤</text>
        </view>
      </view>

      <view class="record-section">
        <view class="section-header">
          <text class="section-title">今日打卡</text>
        </view>

        <view class="record-cards" v-if="todayRecords.length > 0">
          <view class="record-item">
            <view class="record-left">
              <view class="record-icon in">
                <u-icon name="arrow-downward" size="18" color="#6366f1"></u-icon>
              </view>
              <view class="record-info">
                <text class="record-type">上班签到</text>
                <text class="record-meta">{{ scheduledIn ? '规定 ' + scheduledIn : '' }}</text>
              </view>
            </view>
            <view class="record-right">
              <text class="record-time">{{ clockInTime || '--:--' }}</text>
              <view class="status-pill" :class="clockInStatus" v-if="clockInStatusText">
                <text>{{ clockInStatusText }}</text>
              </view>
            </view>
          </view>

          <view class="record-item">
            <view class="record-left">
              <view class="record-icon out">
                <u-icon name="arrow-upward" size="18" color="#0ea5e9"></u-icon>
              </view>
              <view class="record-info">
                <text class="record-type">下班签退</text>
                <text class="record-meta">{{ scheduledOut ? '规定 ' + scheduledOut : '' }}</text>
              </view>
            </view>
            <view class="record-right">
              <text class="record-time">{{ clockOutTime || '--:--' }}</text>
              <view class="status-pill" :class="clockOutStatus" v-if="clockOutStatusText">
                <text>{{ clockOutStatusText }}</text>
              </view>
            </view>
          </view>
        </view>

        <u-empty
          v-else
          mode="data"
          text="今日暂无打卡记录"
          iconSize="60"
          customStyle="padding: 40rpx 0;"
        ></u-empty>
      </view>
    </view>
  </view>
</template>

<script>
import { clock, getClockInfo } from '@/api/attendance'
import config from '@/config'
import { loadGoogleMapsApi } from '@/utils/google-maps'

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

export default {
  data() {
    return {
      isH5: IS_H5,
      mapLatitude: DEFAULT_MAP_POINT.latitude,
      mapLongitude: DEFAULT_MAP_POINT.longitude,
      scale: 15,
      markers: [],
      circles: [],
      includePoints: [],
      googleMapLoading: IS_H5,
      googleMapReady: false,
      googleMapError: '',
      latitude: null,
      longitude: null,
      rawLatitude: null,
      rawLongitude: null,
      locationAccuracy: null,
      companyId: null,
      companyName: '',
      companyAddress: '',
      companyLat: null,
      companyLng: null,
      clockRange: null,
      clockLocations: [],
      attendanceConfigured: false,
      locationReady: false,
      locationDenied: false,
      locationFailureReason: '',
      locationPermissionState: '',
      distance: null,
      hasClockedIn: false,
      hasClockedOut: false,
      todayRecords: [],
      clockInRecord: null,
      clockOutRecord: null,
      todayDaily: null,
      scheduledIn: null,
      scheduledOut: null,
      loading: false,
      currentTime: '',
      currentDate: '',
      timer: null
    }
  },
  computed: {
    hasLocation() {
      return this.latitude !== null && this.longitude !== null
    },
    hasCompanyLocation() {
      return this.companyLat !== null && this.companyLng !== null
    },
    withinClockRange() {
      if (!this.attendanceConfigured) return true
      if (!this.hasCompanyLocation || !this.hasLocation || this.distance === null || this.clockRange === null) return false
      return this.distance <= this.effectiveClockRange
    },
    effectiveClockRange() {
      if (this.clockRange === null || this.clockRange === undefined) return null
      const baseRange = Number(this.clockRange)
      if (!IS_H5 || !Number.isFinite(Number(this.locationAccuracy))) return baseRange

      const accuracy = Number(this.locationAccuracy)
      if (accuracy <= 0 || accuracy > 200) return baseRange

      return baseRange + Math.min(Math.round(accuracy), 80)
    },
    canClockAction() {
      if (!this.attendanceConfigured) return true
      if (!this.hasCompanyLocation || !this.hasLocation) return false
      return this.withinClockRange
    },
    canClockIn() {
      return !this.hasClockedIn && !this.loading && this.canClockAction
    },
    canClockOut() {
      return this.hasClockedIn && !this.hasClockedOut && !this.loading && this.canClockAction
    },
    locationIssueText() {
      if (this.locationFailureReason === 'insecure-context') {
        return '当前 H5 页面未启用 HTTPS，浏览器无法获取定位，请改用 HTTPS 访问后刷新页面'
      }
      if (this.locationFailureReason === 'unsupported') {
        return '当前浏览器不支持网页定位，请改用微信、Edge、Chrome 或 Safari 打开'
      }
      if (this.locationFailureReason === 'permission-denied') {
        return IS_H5
          ? '浏览器已拒绝定位，请在浏览器网站设置或系统设置中开启位置权限后刷新当前位置'
          : '请开启定位权限并刷新当前位置，进入打卡范围后即可打卡'
      }
      if (this.locationFailureReason === 'timeout') {
        return '定位超时，请确认系统定位已开启并在信号较好的位置刷新当前位置'
      }
      if (this.locationFailureReason === 'unavailable') {
        return IS_H5
          ? '浏览器暂时无法获取定位，请确认 GPS、系统定位和浏览器定位权限已开启后重试'
          : '获取定位失败，请稍后重试'
      }
      return '请开启定位权限并刷新当前位置，进入打卡范围后即可打卡'
    },
    attendanceStatusText() {
      if (!this.hasCompanyLocation) return '未配置打卡点'
      if (!this.attendanceConfigured) return '未限制范围'
      if (!this.hasLocation) {
        if (this.locationFailureReason === 'insecure-context') return 'H5 需 HTTPS'
        if (this.locationFailureReason === 'permission-denied') return '定位权限已拒绝'
        if (this.locationFailureReason === 'unsupported') return '浏览器不支持定位'
        if (this.locationFailureReason === 'timeout' || this.locationFailureReason === 'unavailable') return '定位获取失败'
        return '未获取定位'
      }
      return this.withinClockRange ? '已进入范围' : '超出范围'
    },
    attendanceStatusClass() {
      if (!this.hasCompanyLocation) return 'status-empty'
      if (!this.attendanceConfigured) return 'status-free'
      if (!this.hasLocation) return 'status-warn'
      return this.withinClockRange ? 'status-ok' : 'status-warn'
    },
    clockTipText() {
      if (!this.hasCompanyLocation) {
        return '后台还没有配置公司打卡点，请先到组织管理里维护公司坐标'
      }
      if (!this.attendanceConfigured) {
        return '公司已配置地图点位，但还没有启用范围限制，当前可正常打卡'
      }
      if (!this.hasLocation) {
        return this.locationIssueText
      }
      if (this.withinClockRange) {
        return `已进入打卡范围，当前距离 ${this.distanceDisplay}`
      }
      return `当前位置超出打卡范围，当前距离 ${this.distanceDisplay}`
    },
    distanceDisplay() {
      if (this.distance === null) return '--'
      return this.formatDistance(this.distance)
    },
    rangeDisplay() {
      if (this.clockRange === null || this.clockRange === undefined || this.clockRange <= 0) return '--'
      return `${this.clockRange}米`
    },
    locationAccuracyDisplay() {
      if (!Number.isFinite(Number(this.locationAccuracy))) return '--'
      const accuracy = Number(this.locationAccuracy)
      if (accuracy >= 1000) return `${(accuracy / 1000).toFixed(2)}公里`
      return `${Math.round(accuracy)}米`
    },
    clockInTime() {
      return this.clockInRecord ? this.formatTime(this.clockInRecord.clockTime) : ''
    },
    clockOutTime() {
      return this.clockOutRecord ? this.formatTime(this.clockOutRecord.clockTime) : ''
    },
    clockInStatusText() {
      if (!this.clockInRecord) return ''
      return this.todayDaily && this.todayDaily.lateMinutes > 0 ? '迟到' : '正常'
    },
    clockInStatus() {
      if (this.clockInStatusText === '迟到') return 'status-late'
      if (this.clockInStatusText === '正常') return 'status-normal'
      return ''
    },
    clockOutStatusText() {
      if (!this.clockOutRecord) return ''
      return this.todayDaily && this.todayDaily.earlyMinutes > 0 ? '早退' : '正常'
    },
    clockOutStatus() {
      if (this.clockOutStatusText === '早退') return 'status-early'
      if (this.clockOutStatusText === '正常') return 'status-normal'
      return ''
    }
  },
  created() {
    this.googleMapRuntime = {
      map: null,
      companyRangeCircle: null,
      companyPointCircle: null,
      userPointCircle: null
    }
  },
  onLoad() {
    this.updateTime()
    this.timer = setInterval(() => {
      this.updateTime()
    }, 1000)
    this.refreshPage()
  },
  onReady() {
    if (IS_H5) {
      this.initGoogleMap()
    }
  },
  onShow() {
    this.refreshPage()
  },
  onPullDownRefresh() {
    this.refreshPage(true).finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  onUnload() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
    if (IS_H5) {
      this.disposeGoogleMap()
    }
  },
  methods: {
    async refreshPage(showLocationError = false) {
      await Promise.all([this.getLocation(showLocationError), this.loadClockInfo()])
      this.refreshMapData()
    },
    updateTime() {
      const now = new Date()
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      const second = String(now.getSeconds()).padStart(2, '0')
      const month = now.getMonth() + 1
      const day = now.getDate()
      const weekLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      this.currentTime = `${hour}:${minute}:${second}`
      this.currentDate = `${month}月${day}日 ${weekLabels[now.getDay()]}`
    },
    getLocation(showError = false) {
      if (IS_H5) {
        return this.getH5Location(showError)
      }
      return this.getUniLocation(showError)
    },
    getUniLocation(showError = false) {
      return new Promise(resolve => {
        uni.getLocation({
          type: 'gcj02',
          success: res => {
            this.applyLocationSuccess(res.latitude, res.longitude)
            resolve(res)
          },
          fail: err => {
            this.applyLocationFailure(this.resolveLocationFailureReason(err), err, showError)
            resolve(null)
          }
        })
      })
    },
    async getH5Location(showError = false) {
      await this.updateH5PermissionState()

      return new Promise(resolve => {
        if (!this.isH5LocationSupported()) {
          this.applyLocationFailure('unsupported', null, showError)
          resolve(null)
          return
        }

        if (!this.isSecureLocationContext()) {
          this.applyLocationFailure('insecure-context', null, showError)
          resolve(null)
          return
        }

        this.getBestH5Position()
          .then(position => {
            const rawLatitude = Number(position.coords.latitude)
            const rawLongitude = Number(position.coords.longitude)
            const accuracy = Number(position.coords.accuracy)
            const normalizedPoint = this.normalizeH5Location(rawLatitude, rawLongitude)
            this.applyLocationSuccess(
              normalizedPoint.latitude,
              normalizedPoint.longitude,
              rawLatitude,
              rawLongitude,
              accuracy
            )
            resolve(normalizedPoint)
          })
          .catch(err => {
            this.applyLocationFailure(this.resolveLocationFailureReason(err), err, showError)
            resolve(null)
          })
      })
    },
    getH5PositionOnce(timeout = 6000) {
      return new Promise((resolve, reject) => {
        window.navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout,
          maximumAge: 0
        })
      })
    },
    async getBestH5Position() {
      const positions = []
      let lastError = null

      for (let index = 0; index < 3; index += 1) {
        try {
          const position = await this.getH5PositionOnce(index === 0 ? 8000 : 5000)
          positions.push(position)
          const accuracy = Number(position.coords && position.coords.accuracy)
          if (Number.isFinite(accuracy) && accuracy <= 60) break
        } catch (error) {
          lastError = error
        }
      }

      if (!positions.length) {
        throw lastError || new Error('定位失败')
      }

      return positions.reduce((best, current) => {
        const bestAccuracy = Number(best.coords && best.coords.accuracy)
        const currentAccuracy = Number(current.coords && current.coords.accuracy)
        if (!Number.isFinite(bestAccuracy)) return current
        if (!Number.isFinite(currentAccuracy)) return best
        return currentAccuracy < bestAccuracy ? current : best
      })
    },
    applyLocationSuccess(latitude, longitude, rawLatitude = null, rawLongitude = null, accuracy = null) {
      const parsedLatitude = Number(latitude)
      const parsedLongitude = Number(longitude)

      if (!Number.isFinite(parsedLatitude) || !Number.isFinite(parsedLongitude)) {
        this.applyLocationFailure('unavailable', null, false)
        return
      }

      this.latitude = parsedLatitude
      this.longitude = parsedLongitude
      this.rawLatitude = Number.isFinite(Number(rawLatitude)) ? Number(rawLatitude) : null
      this.rawLongitude = Number.isFinite(Number(rawLongitude)) ? Number(rawLongitude) : null
      this.locationAccuracy = Number.isFinite(Number(accuracy)) ? Math.round(Number(accuracy)) : null
      this.locationReady = true
      this.locationDenied = false
      this.locationFailureReason = ''
      if (IS_H5) {
        this.locationPermissionState = 'granted'
      }
      this.updateActiveClockLocation()
      this.calcDistance()
      this.refreshMapData()
    },
    applyLocationFailure(reason, err, showError = false) {
      this.latitude = null
      this.longitude = null
      this.rawLatitude = null
      this.rawLongitude = null
      this.locationAccuracy = null
      this.locationReady = true
      this.locationFailureReason = reason || 'unknown'
      this.locationDenied = this.locationFailureReason === 'permission-denied' || this.locationPermissionState === 'denied'
      this.calcDistance()
      this.refreshMapData()

      if (showError) {
        this.presentLocationError()
      }

      if (err) {
        console.log('定位失败', err)
      }
    },
    presentLocationError() {
      if (this.locationDenied) {
        this.promptEnableLocation()
        return
      }

      if (this.locationFailureReason === 'insecure-context' || this.locationFailureReason === 'unsupported') {
        this.promptEnableLocation()
        return
      }

      this.$modal.msgError(this.locationIssueText)
    },
    async updateH5PermissionState() {
      if (!IS_H5 || typeof window === 'undefined' || !window.navigator || !window.navigator.permissions || !window.navigator.permissions.query) {
        return
      }

      try {
        const status = await window.navigator.permissions.query({ name: 'geolocation' })
        this.locationPermissionState = status.state || ''
        this.locationDenied = status.state === 'denied'
      } catch (error) {
        console.log('读取浏览器定位权限状态失败', error)
      }
    },
    isH5LocationSupported() {
      return typeof window !== 'undefined' && !!(window.navigator && window.navigator.geolocation)
    },
    isSecureLocationContext() {
      if (!IS_H5 || typeof window === 'undefined') return true
      if (window.isSecureContext) return true

      const hostname = window.location && window.location.hostname ? window.location.hostname : ''
      return hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '[::1]'
    },
    normalizeH5Location(latitude, longitude) {
      const rawLatitude = Number(latitude)
      const rawLongitude = Number(longitude)
      if (!Number.isFinite(rawLatitude) || !Number.isFinite(rawLongitude)) {
        return { latitude: rawLatitude, longitude: rawLongitude }
      }

      const [gcjLatitude, gcjLongitude] = this.wgs84ToGcj02(rawLatitude, rawLongitude)
      return {
        latitude: gcjLatitude,
        longitude: gcjLongitude
      }
    },
    isLocationDenied(err) {
      const message = err && (err.errMsg || err.message) ? String(err.errMsg || err.message) : ''
      return /auth deny|authorize no response|permission|denied/i.test(message)
    },
    resolveLocationFailureReason(err) {
      if (this.locationPermissionState === 'denied' || this.isLocationDenied(err)) {
        return 'permission-denied'
      }

      const message = err && (err.errMsg || err.message) ? String(err.errMsg || err.message) : ''
      const code = err && typeof err.code === 'number' ? err.code : null

      if (code === 1) return 'permission-denied'
      if (code === 2) return 'unavailable'
      if (code === 3) return 'timeout'
      if (/timeout/i.test(message)) return 'timeout'
      if (/unavailable|position unavailable/i.test(message)) return 'unavailable'
      if (/secure context|only secure origins|insecure/i.test(message)) return 'insecure-context'
      if (/not support|unsupported|not available/i.test(message)) return 'unsupported'

      return 'unknown'
    },
    promptEnableLocation() {
      if (IS_H5) {
        let content = '请在浏览器地址栏的网站设置中允许当前站点访问位置，或在手机系统设置里打开浏览器定位权限，然后返回页面点击“刷新定位”。'

        if (this.locationFailureReason === 'insecure-context') {
          content = '当前 H5 页面未启用 HTTPS，浏览器不会提供定位能力。请改用 HTTPS 域名访问后再刷新页面。'
        } else if (this.locationFailureReason === 'unsupported') {
          content = '当前浏览器不支持网页定位，请改用微信、Edge、Chrome 或 Safari 打开此页面后再尝试打卡。'
        } else if (this.locationFailureReason === 'timeout') {
          content = '定位超时，请确认手机系统定位、GPS 和浏览器定位权限都已开启，并在信号较好的环境下重试。'
        } else if (this.locationFailureReason === 'unavailable') {
          content = '浏览器暂时无法获取当前位置，请确认手机系统定位、GPS 和浏览器定位权限都已开启后重试。'
        }

        uni.showModal({
          title: 'H5 定位说明',
          content,
          showCancel: false
        })
        return
      }

      uni.showModal({
        title: '需要定位权限',
        content: '考勤地图和范围打卡需要使用当前位置，是否前往设置开启定位权限？',
        success: result => {
          if (result.confirm) {
            uni.openSetting({})
          }
        }
      })
    },
    async loadClockInfo() {
      try {
        const res = await getClockInfo()
        if (res.code !== 200 || !res.data) return

        this.todayRecords = res.data.todayRecords || []
        this.hasClockedIn = !!res.data.hasClockedIn
        this.hasClockedOut = !!res.data.hasClockedOut
        this.scheduledIn = res.data.scheduledIn || null
        this.scheduledOut = res.data.scheduledOut || null
        this.todayDaily = res.data.todayDaily || null
        this.companyId = res.data.companyId || null
        this.companyName = res.data.companyName || ''
        this.companyAddress = res.data.companyAddress || ''
        this.companyLat = this.parseNumber(res.data.companyLat)
        this.companyLng = this.parseNumber(res.data.companyLng)
        this.clockRange = this.parseNumber(res.data.clockRange)
        this.clockLocations = this.normalizeClockLocations(res.data.clockLocations)
        this.updateActiveClockLocation()
        this.attendanceConfigured = !!res.data.attendanceConfigured
        this.clockInRecord = this.todayRecords.find(item => item.clockType === 1) || null
        this.clockOutRecord = this.todayRecords.find(item => item.clockType === 2) || null

        this.calcDistance()
        this.refreshMapData()
      } catch (error) {
        console.log('加载打卡信息失败', error)
      }
    },
    parseNumber(value) {
      if (value === null || value === undefined || value === '') return null
      const number = Number(value)
      return Number.isFinite(number) ? number : null
    },
    normalizeClockLocations(locations) {
      if (!Array.isArray(locations)) return []
      return locations
        .map(item => ({
          id: item.id,
          name: item.locationName || item.companyName || '',
          address: item.address || item.companyAddress || item.locationName || '',
          latitude: this.parseNumber(item.latitude ?? item.companyLat),
          longitude: this.parseNumber(item.longitude ?? item.companyLng),
          range: this.parseNumber(item.clockRange)
        }))
        .filter(item => item.latitude !== null && item.longitude !== null && item.range !== null && item.range > 0)
    },
    updateActiveClockLocation() {
      if (!this.clockLocations.length) return

      let activeLocation = this.clockLocations[0]
      if (this.hasLocation) {
        activeLocation = this.clockLocations
          .map(item => ({
            ...item,
            distance: this.calculateDistanceMeters(this.latitude, this.longitude, item.latitude, item.longitude)
          }))
          .sort((a, b) => a.distance - b.distance)[0]
      }

      this.companyId = activeLocation.id || null
      this.companyName = activeLocation.name || ''
      this.companyAddress = activeLocation.address || ''
      this.companyLat = activeLocation.latitude
      this.companyLng = activeLocation.longitude
      this.clockRange = activeLocation.range
    },
    wgs84ToGcj02(latitude, longitude) {
      if (this.isOutOfChina(latitude, longitude)) {
        return [latitude, longitude]
      }

      let deltaLatitude = this.transformLatitude(longitude - 105.0, latitude - 35.0)
      let deltaLongitude = this.transformLongitude(longitude - 105.0, latitude - 35.0)
      const radLatitude = (latitude / 180.0) * Math.PI
      let magic = Math.sin(radLatitude)
      magic = 1 - GCJ_EE * magic * magic
      const sqrtMagic = Math.sqrt(magic)

      deltaLatitude = (deltaLatitude * 180.0) / (((GCJ_A * (1 - GCJ_EE)) / (magic * sqrtMagic)) * Math.PI)
      deltaLongitude = (deltaLongitude * 180.0) / ((GCJ_A / sqrtMagic) * Math.cos(radLatitude) * Math.PI)

      return [latitude + deltaLatitude, longitude + deltaLongitude]
    },
    gcj02ToWgs84(latitude, longitude) {
      if (this.isOutOfChina(latitude, longitude)) {
        return [latitude, longitude]
      }

      const [gcjLatitude, gcjLongitude] = this.wgs84ToGcj02(latitude, longitude)
      return [latitude * 2 - gcjLatitude, longitude * 2 - gcjLongitude]
    },
    isOutOfChina(latitude, longitude) {
      return longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271
    },
    transformLatitude(x, y) {
      let result = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
      result += ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0) / 3.0
      result += ((20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin((y / 3.0) * Math.PI)) * 2.0) / 3.0
      result += ((160.0 * Math.sin((y / 12.0) * Math.PI) + 320.0 * Math.sin((y * Math.PI) / 30.0)) * 2.0) / 3.0
      return result
    },
    transformLongitude(x, y) {
      let result = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
      result += ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0) / 3.0
      result += ((20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin((x / 3.0) * Math.PI)) * 2.0) / 3.0
      result += ((150.0 * Math.sin((x / 12.0) * Math.PI) + 300.0 * Math.sin((x / 30.0) * Math.PI)) * 2.0) / 3.0
      return result
    },
    async initGoogleMap() {
      if (!IS_H5) return

      const googleMapConfig = (config && config.googleMaps) || {}
      if (!googleMapConfig.apiKey) {
        this.googleMapLoading = false
        this.googleMapReady = false
        this.googleMapError = '未配置 Google Maps API Key，H5 无法加载谷歌地图'
        return
      }

      const mapCanvas = this.$refs.googleMapCanvas
      const mapElement = mapCanvas && (mapCanvas.$el || mapCanvas)
      if (!mapElement) {
        this.googleMapLoading = false
        this.googleMapReady = false
        this.googleMapError = '未找到谷歌地图容器，H5 地图初始化失败'
        return
      }

      this.googleMapLoading = true
      this.googleMapError = ''

      try {
        await loadGoogleMapsApi({
          apiKey: googleMapConfig.apiKey,
          language: googleMapConfig.language || 'zh-CN',
          region: googleMapConfig.region || 'CN'
        })

        this.googleMapRuntime.map = new window.google.maps.Map(mapElement, {
          center: { lat: DEFAULT_MAP_POINT.latitude, lng: DEFAULT_MAP_POINT.longitude },
          zoom: 12,
          disableDefaultUI: true,
          zoomControl: true,
          fullscreenControl: false,
          streetViewControl: false,
          mapTypeControl: false,
          gestureHandling: 'greedy'
        })

        this.googleMapReady = true
        this.googleMapLoading = false
        this.refreshGoogleMap()
      } catch (error) {
        console.log('Google Maps 初始化失败', error)
        this.googleMapReady = false
        this.googleMapLoading = false
        this.googleMapError = 'Google 地图加载失败，请检查 API Key、Billing 配置和当前网络是否可访问 maps.googleapis.com'
      }
    },
    refreshGoogleMap() {
      if (!IS_H5 || !this.googleMapReady || !this.googleMapRuntime.map || !window.google || !window.google.maps) {
        return
      }

      const companyPoint = this.getGoogleMapCompanyPoint()
      const userPoint = this.getGoogleMapUserPoint()

      this.syncGoogleCircle('companyRangeCircle', companyPoint, {
        radius: this.clockRange || 0,
        strokeColor: '#6366f1',
        strokeOpacity: 0.95,
        strokeWeight: 2,
        fillColor: '#6366f1',
        fillOpacity: 0.14
      }, !!(companyPoint && this.clockRange))

      this.syncGoogleCircle('companyPointCircle', companyPoint, {
        radius: 12,
        strokeColor: '#1d4ed8',
        strokeOpacity: 1,
        strokeWeight: 2,
        fillColor: '#2563eb',
        fillOpacity: 1
      }, !!companyPoint)

      this.syncGoogleCircle('userAccuracyCircle', userPoint, {
        radius: Math.max(Number(this.locationAccuracy) || 0, 20),
        strokeColor: '#10b981',
        strokeOpacity: 0.65,
        strokeWeight: 1,
        fillColor: '#10b981',
        fillOpacity: 0.08
      }, !!(userPoint && Number.isFinite(Number(this.locationAccuracy))))

      this.syncGoogleCircle('userPointCircle', userPoint, {
        radius: 12,
        strokeColor: '#047857',
        strokeOpacity: 1,
        strokeWeight: 2,
        fillColor: '#10b981',
        fillOpacity: 1
      }, !!userPoint)

      this.fitGoogleMapViewport(companyPoint, userPoint)
    },
    syncGoogleCircle(key, point, styleOptions, shouldShow) {
      if (!this.googleMapRuntime[key]) {
        this.googleMapRuntime[key] = new window.google.maps.Circle({
          strokeOpacity: 0,
          strokeWeight: 0,
          fillOpacity: 0
        })
      }

      const overlay = this.googleMapRuntime[key]
      if (!shouldShow || !point) {
        overlay.setMap(null)
        return
      }

      overlay.setOptions({
        ...styleOptions,
        center: point,
        map: this.googleMapRuntime.map
      })
    },
    fitGoogleMapViewport(companyPoint, userPoint) {
      if (!this.googleMapRuntime.map) return

      if (companyPoint && userPoint) {
        const bounds = new window.google.maps.LatLngBounds()
        bounds.extend(companyPoint)
        bounds.extend(userPoint)
        this.googleMapRuntime.map.fitBounds(bounds, 60)
        return
      }

      if (companyPoint) {
        this.googleMapRuntime.map.setCenter(companyPoint)
        this.googleMapRuntime.map.setZoom(this.getScaleByDistance(this.clockRange || 300))
        return
      }

      if (userPoint) {
        this.googleMapRuntime.map.setCenter(userPoint)
        this.googleMapRuntime.map.setZoom(16)
        return
      }

      this.googleMapRuntime.map.setCenter({ lat: DEFAULT_MAP_POINT.latitude, lng: DEFAULT_MAP_POINT.longitude })
      this.googleMapRuntime.map.setZoom(12)
    },
    getGoogleMapCompanyPoint() {
      if (!this.hasCompanyLocation) return null
      const [latitude, longitude] = this.gcj02ToWgs84(this.companyLat, this.companyLng)
      return { lat: latitude, lng: longitude }
    },
    getGoogleMapUserPoint() {
      if (Number.isFinite(this.rawLatitude) && Number.isFinite(this.rawLongitude)) {
        return { lat: this.rawLatitude, lng: this.rawLongitude }
      }

      if (!this.hasLocation) return null
      const [latitude, longitude] = this.gcj02ToWgs84(this.latitude, this.longitude)
      return { lat: latitude, lng: longitude }
    },
    disposeGoogleMap() {
      if (!this.googleMapRuntime) return
      ;['companyRangeCircle', 'companyPointCircle', 'userAccuracyCircle', 'userPointCircle'].forEach(key => {
        if (this.googleMapRuntime[key]) {
          this.googleMapRuntime[key].setMap(null)
          this.googleMapRuntime[key] = null
        }
      })
      this.googleMapRuntime.map = null
    },
    refreshMapData() {
      this.markers = this.buildMarkers()
      this.circles = this.buildCircles()
      this.includePoints = this.buildIncludePoints()
      this.refreshMapCenter()
      this.refreshGoogleMap()
    },
    buildMarkers() {
      const markers = []

      if (this.hasCompanyLocation) {
        markers.push({
          id: 2,
          latitude: this.companyLat,
          longitude: this.companyLng,
          iconPath: '/static/logo.png',
          width: 28,
          height: 28,
          callout: {
            content: this.companyName || '打卡点',
            display: 'ALWAYS',
            color: '#111827',
            fontSize: 12,
            bgColor: '#ffffff',
            borderRadius: 14,
            padding: 8
          }
        })
      }

      return markers
    },
    buildCircles() {
      if (!this.hasCompanyLocation || !this.clockRange) return []

      return [
        {
          latitude: this.companyLat,
          longitude: this.companyLng,
          radius: this.clockRange,
          strokeWidth: 2,
          color: '#6366f1',
          fillColor: 'rgba(99, 102, 241, 0.14)'
        }
      ]
    },
    buildIncludePoints() {
      const points = []

      if (this.hasCompanyLocation) {
        points.push({
          latitude: this.companyLat,
          longitude: this.companyLng
        })
      }

      if (this.hasLocation) {
        points.push({
          latitude: this.latitude,
          longitude: this.longitude
        })
      }

      return points
    },
    refreshMapCenter() {
      if (this.hasCompanyLocation && this.hasLocation) {
        this.mapLatitude = (this.companyLat + this.latitude) / 2
        this.mapLongitude = (this.companyLng + this.longitude) / 2
        this.scale = this.getScaleByDistance(Math.max(this.distance || 0, this.clockRange || 0, 300))
        return
      }

      if (this.hasCompanyLocation) {
        this.mapLatitude = this.companyLat
        this.mapLongitude = this.companyLng
        this.scale = this.getScaleByDistance(this.clockRange || 300)
        return
      }

      if (this.hasLocation) {
        this.mapLatitude = this.latitude
        this.mapLongitude = this.longitude
        this.scale = 16
        return
      }

      this.mapLatitude = DEFAULT_MAP_POINT.latitude
      this.mapLongitude = DEFAULT_MAP_POINT.longitude
      this.scale = 12
    },
    calcDistance() {
      if (!this.hasCompanyLocation || !this.hasLocation) {
        this.distance = null
        return
      }

      this.updateActiveClockLocation()
      this.distance = Math.round(this.calculateDistanceMeters(this.latitude, this.longitude, this.companyLat, this.companyLng))
    },
    calculateDistanceMeters(latitude, longitude, targetLatitude, targetLongitude) {
      const radLat1 = (latitude * Math.PI) / 180
      const radLat2 = (targetLatitude * Math.PI) / 180
      const deltaLat = radLat1 - radLat2
      const deltaLng = ((longitude - targetLongitude) * Math.PI) / 180
      const angle = 2 * Math.asin(
        Math.sqrt(
          Math.pow(Math.sin(deltaLat / 2), 2) +
          Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(deltaLng / 2), 2)
        )
      )
      return angle * 6378137
    },
    getScaleByDistance(distance) {
      if (!distance || distance <= 200) return 16
      if (distance <= 500) return 15
      if (distance <= 1000) return 14
      if (distance <= 3000) return 13
      return 12
    },
    formatDistance(distance) {
      if (distance >= 1000) {
        return `${(distance / 1000).toFixed(distance >= 10000 ? 1 : 2)}公里`
      }
      return `${Math.round(distance)}米`
    },
    formatCoordinate(value) {
      return typeof value === 'number' ? value.toFixed(6) : '--'
    },
    async handleClockIn() {
      if (this.hasClockedIn || this.loading) return
      if (!this.canClockIn) {
        this.handleClockBlocked()
        return
      }
      await this.doClock(1)
    },
    async handleClockOut() {
      if (this.hasClockedOut || this.loading) return
      if (!this.hasClockedIn) {
        this.$modal.msgError('请先签到')
        return
      }
      if (!this.canClockOut) {
        this.handleClockBlocked()
        return
      }
      await this.doClock(2)
    },
    handleClockBlocked() {
      if (!this.hasCompanyLocation) {
        this.$modal.msgError('公司还没有配置可用的打卡点')
        return
      }

      if (this.attendanceConfigured && !this.hasLocation) {
        if (this.locationDenied) {
          this.promptEnableLocation()
        } else {
          this.$modal.msgError(this.locationIssueText)
        }
        return
      }

      if (this.attendanceConfigured && !this.withinClockRange) {
        this.$modal.msgError(`当前位置不在打卡范围内，当前距离 ${this.distanceDisplay}`)
      }
    },
    async doClock(clockType) {
      this.loading = true
      this.$modal.loading('打卡中...')

      try {
        const payload = { clockType }
        if (this.hasLocation) {
          payload.location = `${this.latitude},${this.longitude}`
          if (Number.isFinite(Number(this.locationAccuracy))) {
            payload.accuracy = Math.round(Number(this.locationAccuracy))
          }
        }

        const res = await clock(payload)
        this.$modal.closeLoading()

        if (res.code === 200) {
          this.$modal.showToast(clockType === 1 ? '签到成功' : '签退成功')
          await Promise.all([this.loadClockInfo(), this.getLocation(false)])
          this.refreshMapData()
        } else {
          this.$modal.msgError(res.msg || '打卡失败')
        }
      } catch (error) {
        this.$modal.closeLoading()
        const message = error && error.message ? error.message : error
        this.$modal.msgError(message || '打卡失败')
      } finally {
        this.loading = false
      }
    },
    formatTime(value) {
      if (!value) return ''
      const text = String(value).replace('T', ' ')
      const timePart = text.includes(' ') ? text.split(' ').pop() : text
      return timePart.slice(0, 5)
    },
    goToCardApply() {
      uni.navigateTo({ url: '/pages/apply/card/index' })
    },
    goToMyAttendance() {
      uni.navigateTo({ url: '/pages/attendance/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f3f4f6;
}

.map-section {
  position: relative;
  width: 100%;
  height: 420rpx;
}

.attendance-map {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #dbeafe 0%, #eef2ff 48%, #f8fafc 100%);
}

.google-map-shell {
  position: relative;
  overflow: hidden;
}

.google-map-canvas {
  width: 100%;
  height: 100%;
}

.google-map-state {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  text-align: center;
  color: #1f2937;
  font-size: 24rpx;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.76);
}

.map-overlay {
  position: absolute;
  left: 30rpx;
  right: 30rpx;
  bottom: 24rpx;
  z-index: 2;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  margin-bottom: 14rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.status-chip.status-ok {
  background: rgba(16, 185, 129, 0.16);
  color: #047857;
}

.status-chip.status-warn {
  background: rgba(245, 158, 11, 0.16);
  color: #b45309;
}

.status-chip.status-empty {
  background: rgba(239, 68, 68, 0.14);
  color: #b91c1c;
}

.status-chip.status-free {
  background: rgba(59, 130, 246, 0.14);
  color: #1d4ed8;
}

.overlay-card {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  padding: 20rpx 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(15, 23, 42, 0.08);
}

.overlay-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
}

.overlay-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: #4b5563;
}

.content-area {
  position: relative;
  z-index: 1;
  margin-top: -20rpx;
  padding: 0 30rpx 40rpx;
  background: #f3f4f6;
  border-top-left-radius: 32rpx;
  border-top-right-radius: 32rpx;
}

.clock-core,
.location-card,
.quick-actions,
.record-section {
  background: #ffffff;
  border-radius: 32rpx;
  box-shadow: 0 10rpx 30rpx rgba(15, 23, 42, 0.04);
}

.clock-core {
  margin-top: 24rpx;
  padding: 40rpx 30rpx;
}

.time-display {
  text-align: center;
}

.current-time {
  display: block;
  font-size: 64rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
  color: #111827;
  font-variant-numeric: tabular-nums;
}

.current-date {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #9ca3af;
  font-weight: 500;
}

.clock-tip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 28rpx 0 36rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  line-height: 1.5;
}

.clock-tip.status-ok {
  background: #ecfdf5;
  color: #047857;
}

.clock-tip.status-warn {
  background: #fffbeb;
  color: #b45309;
}

.clock-tip.status-empty {
  background: #fef2f2;
  color: #b91c1c;
}

.clock-tip.status-free {
  background: #eff6ff;
  color: #1d4ed8;
}

.clock-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
}

.clock-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 28rpx;
}

.divider-line {
  width: 2rpx;
  height: 38rpx;
  background: #e5e7eb;
}

.divider-dot {
  width: 12rpx;
  height: 12rpx;
  margin: 10rpx 0;
  border-radius: 50%;
  background: #d1d5db;
}

.clock-btn {
  width: 210rpx;
  height: 210rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clock-btn.ready {
  background: linear-gradient(135deg, #818cf8, #4f46e5);
  box-shadow: 0 18rpx 36rpx rgba(79, 70, 229, 0.28);
}

.clock-btn.done {
  background: #f0fdf4;
  border: 3rpx solid #bbf7d0;
}

.clock-btn.locked,
.clock-btn.blocked {
  background: #f8fafc;
  border: 3rpx solid #e5e7eb;
}

.btn-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.btn-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #ffffff;
}

.clock-btn.done .btn-label {
  color: #10b981;
}

.clock-btn.locked .btn-label,
.clock-btn.blocked .btn-label {
  color: #9ca3af;
}

.location-card {
  margin-top: 24rpx;
  padding: 30rpx;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
}

.refresh-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #4f46e5;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.info-item {
  padding: 20rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.info-label {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
}

.info-value {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: #1f2937;
  word-break: break-all;
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 24rpx;
  padding: 34rpx 20rpx;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.action-purple {
  background: rgba(99, 102, 241, 0.12);
}

.action-blue {
  background: rgba(14, 165, 233, 0.12);
}

.action-name {
  font-size: 24rpx;
  font-weight: 600;
  color: #4b5563;
}

.record-section {
  margin-top: 24rpx;
  padding: 36rpx 30rpx;
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #111827;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 20rpx;
  margin-bottom: 18rpx;
  border-radius: 24rpx;
  background: #f8fafc;
}

.record-item:last-child {
  margin-bottom: 0;
}

.record-left {
  display: flex;
  align-items: center;
}

.record-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-icon.in {
  background: rgba(99, 102, 241, 0.12);
}

.record-icon.out {
  background: rgba(14, 165, 233, 0.12);
}

.record-info {
  margin-left: 18rpx;
}

.record-type {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
}

.record-meta {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #9ca3af;
}

.record-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.record-time {
  font-size: 36rpx;
  font-weight: 800;
  color: #1f2937;
  font-variant-numeric: tabular-nums;
}

.status-pill {
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.status-pill.status-normal {
  background: #dcfce7;
  color: #16a34a;
}

.status-pill.status-late {
  background: #fef2f2;
  color: #ef4444;
}

.status-pill.status-early {
  background: #fffbeb;
  color: #f59e0b;
}
</style>
