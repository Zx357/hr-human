<template>
  <view class="nav-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">快捷导航</text>
      </view>
    </tn-navbar>

    <scroll-view scroll-y class="nav-scroll" :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }">
      <view class="summary-card">
        <view>
          <view class="summary-title">常用功能入口</view>
          <view class="summary-desc">把打卡、申请、审批、通知和个人资料集中到这里，少绕路。</view>
        </view>
        <view class="summary-icon">
          <tn-icon name="menu-fill"></tn-icon>
        </view>
      </view>

      <view v-for="section in sections" :key="section.title" class="section">
        <view class="section-head">
          <view>
            <view class="section-title">{{ section.title }}</view>
            <view class="section-desc">{{ section.desc }}</view>
          </view>
        </view>

        <view class="nav-grid">
          <view v-for="item in section.items" :key="item.title" class="nav-item" @click="openItem(item)">
            <view class="item-icon" :style="{ color: item.color, backgroundColor: item.bg }">
              <tn-icon :name="item.icon"></tn-icon>
            </view>
            <view class="item-main">
              <view class="item-title">{{ item.title }}</view>
              <view class="item-desc">{{ item.desc }}</view>
            </view>
            <tn-icon name="right" class="item-arrow"></tn-icon>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const blue = { color: '#3D7EFF', bg: 'rgba(61, 126, 255, 0.1)' }
const cyan = { color: '#00C8B0', bg: 'rgba(0, 200, 176, 0.12)' }
const orange = { color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.13)' }
const red = { color: '#FB6A67', bg: 'rgba(251, 106, 103, 0.12)' }
const purple = { color: '#957BFE', bg: 'rgba(149, 123, 254, 0.12)' }
const green = { color: '#00D05E', bg: 'rgba(0, 208, 94, 0.12)' }

const sections = [
  {
    title: '考勤',
    desc: '打卡和考勤异常处理',
    items: [
      routeItem('考勤打卡', '进入定位打卡页', 'time-fill', '/workPages/time', blue),
      routeItem('补卡申请', '漏打卡时提交说明', 'clock-fill', '/workPages/replace', green),
      routeItem('换休申请', '调休换休提交审批', 'menu-grille-fill', '/workPages/exchange', cyan)
    ]
  },
  {
    title: '申请',
    desc: '常用办公申请',
    items: [
      routeItem('请假申请', '年假、事假、病假', 'calendar-fill', '/workPages/leave', blue),
      routeItem('加班申请', '填写加班时间和原因', 'clock-fill', '/workPages/overtime', orange),
      routeItem('出差申请', '提交行程与事由', 'location-fill', '/workPages/travel', purple),
      routeItem('离职申请', '发起离职流程', 'reduce-circle-fill', '/workPages/resign', red)
    ]
  },
  {
    title: '审批与消息',
    desc: '处理待办和查看通知',
    items: [
      routeItem('待办事项', '需要你处理的审批', 'flag-fill', '/homePages/pending', orange),
      routeItem('审批进程', '查看已提交申请', 'seal', '/homePages/approval', purple),
      routeItem('通知公告', '系统通知和公告', 'notice-fill', '/homePages/notice', red)
    ]
  },
  {
    title: '个人',
    desc: '个人资料与辅助入口',
    items: [
      routeItem('个人信息', '查看员工档案', 'identity-fill', '/minePages/set', blue),
      routeItem('帮助中心', '常见问题说明', 'help-fill', '/minePages/help', purple),
      mainItem('工作台', '回到工作台首页', 'home-fill', '/pages/index?index=2', cyan)
    ]
  }
]

function routeItem(title, desc, icon, url, theme) {
  return {
    title,
    desc,
    icon,
    url,
    type: 'page',
    ...theme
  }
}

function mainItem(title, desc, icon, url, theme) {
  return {
    title,
    desc,
    icon,
    url,
    type: 'main',
    ...theme
  }
}

function openItem(item) {
  if (item.type === 'main') {
    uni.reLaunch({ url: item.url })
    return
  }

  uni.navigateTo({ url: item.url })
}
</script>

<style scoped>
.nav-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F8F7F8;
}

.nav-back {
  width: 72rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 18rpx;
  border-radius: 999rpx;
  color: #3b4b68;
  background: rgba(20, 30, 50, 0.08);
}

.nav-scroll {
  height: 100vh;
  box-sizing: border-box;
  padding: 0 28rpx 48rpx;
}

.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-radius: 24rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #5a7ec8, #7fa1de);
  box-shadow: 0 18rpx 42rpx rgba(90, 126, 200, 0.22);
}

.summary-title {
  font-size: 36rpx;
  font-weight: 800;
}

.summary-desc {
  max-width: 440rpx;
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.82);
  font-size: 24rpx;
  line-height: 1.5;
}

.summary-icon {
  width: 94rpx;
  height: 94rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 30rpx;
  font-size: 48rpx;
  background: rgba(255, 255, 255, 0.18);
}

.section {
  margin-top: 28rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16rpx;
  padding: 0 4rpx;
}

.section-title {
  color: #101933;
  font-size: 32rpx;
  font-weight: 800;
}

.section-desc {
  margin-top: 6rpx;
  color: #9aa4b6;
  font-size: 23rpx;
}

.nav-grid {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 16rpx 46rpx rgba(55, 74, 105, 0.06);
}

.item-icon {
  width: 76rpx;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 24rpx;
  font-size: 38rpx;
}

.item-main {
  min-width: 0;
  flex: 1;
  padding: 0 20rpx;
}

.item-title {
  color: #111a32;
  font-size: 29rpx;
  font-weight: 700;
}

.item-desc {
  margin-top: 8rpx;
  color: #8d98aa;
  font-size: 23rpx;
}

.item-arrow {
  color: #b5bdca;
  font-size: 28rpx;
}
</style>
