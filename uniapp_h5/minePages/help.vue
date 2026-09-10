<template>
  <view class="help-page tn-safe-area-inset-bottom">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">帮助中心</text>
      </view>
    </tn-navbar>

    <view class="help-content" :style="{ paddingTop: vuex_custom_bar_height + 18 + 'px' }">
      <view class="hint-card">
        <view class="hint-icon">
          <tn-icon name="help"></tn-icon>
        </view>
        <view class="hint-main">
          <text class="hint-title">常用帮助</text>
          <text class="hint-desc">只保留和当前系统相关的问题，其他说明以后需要再加。</text>
        </view>
      </view>

      <view class="faq-list">
        <view v-for="(item, index) in helpList" :key="item.title" class="faq-item" @click="toggle(index)">
          <view class="faq-head">
            <view class="faq-icon" :style="{ color: item.color, backgroundColor: item.bg }">
              <tn-icon :name="item.icon"></tn-icon>
            </view>
            <view class="faq-title">{{ item.title }}</view>
            <tn-icon :name="activeIndex === index ? 'up' : 'down'" class="faq-arrow"></tn-icon>
          </view>
          <view v-if="activeIndex === index" class="faq-answer">
            {{ item.answer }}
          </view>
        </view>
      </view>

      <view class="contact-card">
        <view>
          <text class="contact-title">还是解决不了？</text>
          <text class="contact-desc">请联系管理员检查账号、员工档案或审批权限。</text>
        </view>
        <view class="contact-icon">
          <tn-icon name="service-fill"></tn-icon>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const activeIndex = ref(0)

const helpList = [
  {
    title: '登录不上怎么办',
    icon: 'login',
    color: '#3D7EFF',
    bg: 'rgba(61, 126, 255, 0.1)',
    answer: '先确认工号和密码是否正确。员工默认密码为空时通常按 123456 处理；如果仍无法登录，请让管理员检查员工状态是否为在职。'
  },
  {
    title: '打卡定位不出来',
    icon: 'location-fill',
    color: '#00C8B0',
    bg: 'rgba(0, 200, 176, 0.12)',
    answer: '请在手机系统和浏览器里允许定位权限，然后回到考勤打卡页点击刷新定位。进入打卡范围后才可以打卡。'
  },
  {
    title: '申请和审批在哪里',
    icon: 'seal',
    color: '#957BFE',
    bg: 'rgba(149, 123, 254, 0.12)',
    answer: '请假、加班、补卡等申请在工作台提交；待我处理的审批在首页“待办”，我的申请进度在首页“审批”。'
  },
  {
    title: '个人信息不对',
    icon: 'identity-fill',
    color: '#FFAC00',
    bg: 'rgba(255, 172, 0, 0.14)',
    answer: '个人信息来自后台员工档案，手机端暂不直接修改。姓名、手机号、部门、职位等需要管理员在后台维护。'
  },
  {
    title: '怎么退出登录',
    icon: 'logout',
    color: '#FB6A67',
    bg: 'rgba(251, 106, 103, 0.12)',
    answer: '进入“我的”，点击底部“退出登录”。退出后会清空当前账号状态并回到登录页。'
  }
]

function toggle(index) {
  activeIndex.value = activeIndex.value === index ? -1 : index
}
</script>

<style lang="scss" scoped>
.help-page {
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

.help-content {
  padding-left: 24rpx;
  padding-right: 24rpx;
  padding-bottom: 60rpx;
}

.hint-card,
.faq-item,
.contact-card {
  background: #fff;
  border-radius: 18rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.hint-card {
  padding: 26rpx;
  display: flex;
  align-items: center;
}

.hint-icon,
.contact-icon {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.1);
  font-size: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hint-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.hint-title,
.contact-title {
  display: block;
  color: #1d2541;
  font-size: 31rpx;
  font-weight: 800;
}

.hint-desc,
.contact-desc {
  display: block;
  margin-top: 8rpx;
  color: #8a93a3;
  font-size: 25rpx;
  line-height: 1.5;
}

.faq-list {
  margin-top: 24rpx;
}

.faq-item {
  margin-bottom: 18rpx;
  overflow: hidden;
}

.faq-head {
  min-height: 104rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
}

.faq-icon {
  width: 62rpx;
  height: 62rpx;
  border-radius: 50%;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.faq-title {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
  color: #1d2541;
  font-size: 29rpx;
  font-weight: 800;
}

.faq-arrow {
  color: #9aa4b2;
  font-size: 30rpx;
}

.faq-answer {
  padding: 0 26rpx 26rpx 104rpx;
  color: #657189;
  font-size: 26rpx;
  line-height: 1.65;
}

.contact-card {
  margin-top: 26rpx;
  padding: 26rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.contact-card > view:first-child {
  flex: 1;
  min-width: 0;
  padding-right: 20rpx;
}
</style>
