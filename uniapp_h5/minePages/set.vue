<template>
  <view class="profile-page">
    <tn-navbar fixed bg-color="#FFFFFF" home-icon="" :bottom-shadow="true" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">个人信息</text>
      </view>
    </tn-navbar>

    <scroll-view scroll-y class="profile-scroll" :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }">
      <view class="profile-card">
        <view class="profile-head">
          <view>
            <view class="field-label">用户昵称</view>
            <view class="field-value">{{ displayName }}</view>
          </view>
          <view class="avatar-wrap" @click="changeAvatar">
            <image v-if="avatarUrl" :src="avatarUrl" mode="aspectFill" class="avatar-image" />
            <view v-else class="avatar-fallback">{{ firstChar(displayName) }}</view>
            <view class="avatar-edit">更换</view>
          </view>
        </view>
      </view>

      <view class="info-list">
        <view v-for="item in profileRows" :key="item.label" class="info-row" @click="readonlyTip">
          <view class="info-main">
            <view class="row-label">{{ item.label }}</view>
            <view class="row-value">{{ item.value }}</view>
          </view>
          <tn-icon name="right" class="row-arrow"></tn-icon>
        </view>
      </view>
    </scroll-view>

    <view class="footer-actions">
      <tn-button
        bg-color="#FE871B"
        :custom-style="{ padding: '20rpx' }"
        width="100%"
        :font-size="28"
        :plain="true"
        text-color="#FE871B"
        shape="round"
        size="xl"
        @click="handleLogout"
      >
        <text>切换账号</text>
      </tn-button>
      <tn-button
        bg-color="#FB6A67"
        :custom-style="{ padding: '20rpx' }"
        width="100%"
        :font-size="28"
        text-color="#FFFFFF"
        shape="round"
        size="xl"
        @click="handleLogout"
      >
        <text>退出登录</text>
      </tn-button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { uploadEmployeeAvatar } from '@/api/employee'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

const employee = computed(() => store.state.user.employeeInfo || uni.getStorageSync('userInfo') || {})
const displayName = computed(() => pick(employee.value.name, store.state.user.name, employee.value.employeeNo, '未填写'))
const avatarUrl = computed(() => pick(store.state.user.avatar, employee.value.avatar, ''))

const profileRows = computed(() => [
  { label: '绑定手机号', value: pick(employee.value.phone, '未填写') },
  { label: '姓名', value: pick(employee.value.name, '未填写') },
  { label: '性别', value: formatGender(employee.value.gender) },
  { label: '生日', value: formatDate(employee.value.birthDate) },
  { label: '职业', value: pick(employee.value.position, employee.value.duty, employee.value.occupation, employee.value.post, employee.value.jobTitle, '未填写') },
  { label: '工号', value: pick(employee.value.employeeNo, '未填写') },
  { label: '部门', value: pick(employee.value.deptName, '未填写') },
  { label: '公司', value: pick(employee.value.companyName, '未填写') },
  { label: '入职日期', value: formatDate(employee.value.entryDate) }
])

onShow(() => {
  store.dispatch('GetInfo')
})

// 头像上传:选图后经 /file/upload/employee/avatar 上传,成功后刷新资料
const uploadingAvatar = ref(false)

function changeAvatar() {
  if (uploadingAvatar.value) return
  const employeeNo = employee.value.employeeNo
  if (!employeeNo) {
    uni.showToast({ title: '缺少工号信息,暂不能更换头像', icon: 'none' })
    return
  }
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const filePath = res.tempFilePaths && res.tempFilePaths[0]
      if (!filePath) return
      uploadingAvatar.value = true
      uni.showLoading({ title: '上传中', mask: true })
      uploadEmployeeAvatar(filePath, employeeNo)
        .then(() => {
          uni.hideLoading()
          uni.showToast({ title: '头像已更新', icon: 'success' })
          store.dispatch('GetInfo')
        })
        .catch(() => {
          uni.hideLoading()
          uni.showToast({ title: '头像上传失败，请重试', icon: 'none' })
        })
        .finally(() => {
          uploadingAvatar.value = false
        })
    }
  })
}

function pick(...values) {
  return values.find((value) => value !== undefined && value !== null && String(value).trim() !== '') || ''
}

function firstChar(value) {
  return String(value || '?').slice(0, 1)
}

function formatDate(value) {
  if (!value) return '未填写'
  return String(value).replace('T', ' ').slice(0, 10)
}

function formatGender(value) {
  const map = {
    0: '未知',
    1: '男',
    2: '女',
    male: '男',
    female: '女',
    M: '男',
    F: '女',
    男: '男',
    女: '女'
  }
  return map[value] || pick(value, '未填写')
}

function readonlyTip() {
  uni.showToast({ title: '员工档案信息请在后台维护', icon: 'none' })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出当前账号吗？',
    success: async (res) => {
      if (!res.confirm) return
      uni.showLoading({ title: '退出中...' })
      try {
        await store.dispatch('LogOut')
        uni.hideLoading()
        uni.reLaunch({ url: '/pages/login' })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ icon: 'none', title: error || '退出失败' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F8F7F8;
}

.nav-back {
  width: 72rpx;
  height: 54rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.12);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-scroll {
  box-sizing: border-box;
  height: 100vh;
  padding-bottom: 180rpx;
}

.profile-card,
.info-list {
  background: #fff;
}

.profile-card {
  margin-top: 16rpx;
  border-bottom: 20rpx solid #f1f3f7;
}

.profile-head {
  min-height: 150rpx;
  padding: 34rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label,
.row-label {
  color: #111827;
  font-size: 30rpx;
  font-weight: 800;
}

.field-value,
.row-value {
  margin-top: 12rpx;
  color: #8a93a3;
  font-size: 27rpx;
}

.avatar-wrap,
.avatar-image,
.avatar-fallback {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
}

.avatar-wrap {
  position: relative;
  overflow: hidden;
  background: #f3f6fb;
}

.avatar-edit {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 3rpx 0;
  background: rgba(29, 37, 65, 0.55);
  color: #fff;
  font-size: 18rpx;
  text-align: center;
}

.avatar-image {
  display: block;
}

.avatar-fallback {
  color: #fff;
  background: linear-gradient(135deg, #8eb0d8, #b7c7d9);
  font-size: 36rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-list {
  margin-top: 0;
}

.info-row {
  min-height: 118rpx;
  padding: 26rpx 30rpx;
  border-bottom: 1rpx solid #eef1f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-main {
  flex: 1;
  min-width: 0;
}

.row-value {
  word-break: break-all;
}

.row-arrow {
  margin-left: 20rpx;
  color: #8ca0b3;
  font-size: 32rpx;
}

.footer-actions {
  position: fixed;
  left: 50%;
  bottom: calc(28rpx + env(safe-area-inset-bottom));
  z-index: 20;
  width: calc(100% - 52rpx);
  max-width: 588px;
  transform: translateX(-50%);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22rpx;
}
</style>
