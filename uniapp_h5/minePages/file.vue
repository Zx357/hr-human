<template>
  <view class="file-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">文件助手</text>
      </view>
    </tn-navbar>

    <scroll-view scroll-y class="file-scroll" :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }">
      <view class="hero-card">
        <view class="hero-icon">
          <tn-icon name="folder-fill"></tn-icon>
        </view>
        <view class="hero-main">
          <view class="hero-title">资料与附件中心</view>
          <view class="hero-desc">先放常用制度、申请说明和模板。后面接入文件接口后，这里可以直接下载真实附件。</view>
        </view>
      </view>

      <scroll-view scroll-x class="type-scroll" :show-scrollbar="false">
        <view class="type-list">
          <view
            v-for="item in types"
            :key="item.value"
            class="type-chip"
            :class="{ active: activeType === item.value }"
            @click="activeType = item.value"
          >
            {{ item.label }}
          </view>
        </view>
      </scroll-view>

      <view class="file-list">
        <view v-for="item in filteredFiles" :key="item.title" class="file-card" @click="openFile(item)">
          <view class="file-icon" :style="{ color: item.color, backgroundColor: item.bg }">
            <tn-icon :name="item.icon"></tn-icon>
          </view>
          <view class="file-main">
            <view class="file-title">{{ item.title }}</view>
            <view class="file-meta">
              <text>{{ item.typeName }}</text>
              <text>{{ item.format }}</text>
              <text>{{ item.date }}</text>
            </view>
          </view>
          <view class="file-action">
            <tn-icon name="download"></tn-icon>
          </view>
        </view>
      </view>

      <view v-if="filteredFiles.length === 0" class="empty-card">
        <tn-icon name="empty-list"></tn-icon>
        <text>暂无资料</text>
      </view>

      <view class="tips-card">
        <view class="tips-icon">
          <tn-icon name="notice"></tn-icon>
        </view>
        <view class="tips-text">
          <view class="tips-title">目前只是资料入口</view>
          <view class="tips-desc">没有真实文件下载接口的资料，点击后会提示联系管理员获取。</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const activeType = ref('all')

const types = [
  { label: '全部', value: 'all' },
  { label: '制度', value: 'policy' },
  { label: '模板', value: 'template' },
  { label: '个人资料', value: 'profile' }
]

const files = [
  {
    title: '考勤制度说明',
    type: 'policy',
    typeName: '制度',
    format: 'PDF',
    date: '2026-05-13',
    icon: 'time-fill',
    color: '#3D7EFF',
    bg: 'rgba(61, 126, 255, 0.1)'
  },
  {
    title: '请假与加班申请说明',
    type: 'policy',
    typeName: '制度',
    format: 'PDF',
    date: '2026-05-13',
    icon: 'calendar-fill',
    color: '#FFAC00',
    bg: 'rgba(255, 172, 0, 0.13)'
  },
  {
    title: '报销资料准备清单',
    type: 'policy',
    typeName: '制度',
    format: 'DOC',
    date: '2026-05-13',
    icon: 'receipt-fill',
    color: '#00C8B0',
    bg: 'rgba(0, 200, 176, 0.12)'
  },
  {
    title: '请假证明模板',
    type: 'template',
    typeName: '模板',
    format: 'DOC',
    date: '2026-05-13',
    icon: 'edit-write',
    color: '#957BFE',
    bg: 'rgba(149, 123, 254, 0.12)'
  },
  {
    title: '出差行程模板',
    type: 'template',
    typeName: '模板',
    format: 'XLS',
    date: '2026-05-13',
    icon: 'location-fill',
    color: '#FE871B',
    bg: 'rgba(254, 135, 27, 0.12)'
  },
  {
    title: '补卡情况说明模板',
    type: 'template',
    typeName: '模板',
    format: 'DOC',
    date: '2026-05-13',
    icon: 'clock-fill',
    color: '#4B98FE',
    bg: 'rgba(75, 152, 254, 0.12)'
  },
  {
    title: '入职资料清单',
    type: 'profile',
    typeName: '个人资料',
    format: 'PDF',
    date: '2026-05-13',
    icon: 'identity-fill',
    color: '#FB6A67',
    bg: 'rgba(251, 106, 103, 0.12)'
  },
  {
    title: '证书与合同附件说明',
    type: 'profile',
    typeName: '个人资料',
    format: 'PDF',
    date: '2026-05-13',
    icon: 'folder-upload-fill',
    color: '#00D05E',
    bg: 'rgba(0, 208, 94, 0.12)'
  }
]

const filteredFiles = computed(() => {
  if (activeType.value === 'all') return files
  return files.filter((item) => item.type === activeType.value)
})

function openFile(item) {
  uni.showToast({
    title: `${item.title} 暂未接入下载`,
    icon: 'none'
  })
}
</script>

<style scoped>
.file-page {
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

.file-scroll {
  height: 100vh;
  box-sizing: border-box;
  padding: 0 28rpx 48rpx;
}

.hero-card,
.tips-card,
.file-card,
.empty-card {
  background: #ffffff;
  border-radius: 22rpx;
  box-shadow: 0 16rpx 46rpx rgba(55, 74, 105, 0.06);
}

.hero-card {
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 30rpx;
  overflow: hidden;
  position: relative;
}

.hero-card::after {
  content: '';
  position: absolute;
  width: 220rpx;
  height: 220rpx;
  right: -70rpx;
  top: -80rpx;
  border-radius: 50%;
  background: rgba(61, 126, 255, 0.08);
}

.hero-icon {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 28rpx;
  color: #3d7eff;
  font-size: 48rpx;
  background: linear-gradient(145deg, rgba(61, 126, 255, 0.16), rgba(147, 190, 255, 0.12));
}

.hero-main {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.hero-title {
  color: #121b35;
  font-size: 34rpx;
  font-weight: 700;
}

.hero-desc {
  margin-top: 10rpx;
  color: #7c879b;
  font-size: 24rpx;
  line-height: 1.55;
}

.type-scroll {
  margin: 26rpx -28rpx 18rpx;
  white-space: nowrap;
}

.type-list {
  display: inline-flex;
  gap: 18rpx;
  padding: 0 28rpx;
}

.type-chip {
  height: 58rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 28rpx;
  border-radius: 999rpx;
  color: #7c879b;
  font-size: 25rpx;
  background: #ffffff;
}

.type-chip.active {
  color: #ffffff;
  background: linear-gradient(135deg, #5b8dff, #7ba7ff);
  box-shadow: 0 12rpx 26rpx rgba(61, 126, 255, 0.22);
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.file-card {
  display: flex;
  align-items: center;
  padding: 24rpx;
}

.file-icon {
  width: 78rpx;
  height: 78rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 24rpx;
  font-size: 38rpx;
}

.file-main {
  min-width: 0;
  flex: 1;
  padding: 0 20rpx;
}

.file-title {
  color: #111a32;
  font-size: 29rpx;
  font-weight: 700;
}

.file-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 12rpx;
  color: #929caf;
  font-size: 23rpx;
}

.file-action {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 50%;
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.09);
}

.empty-card {
  margin-top: 28rpx;
  padding: 56rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
  color: #9aa4b6;
  font-size: 25rpx;
}

.empty-card .tn-icon {
  font-size: 56rpx;
}

.tips-card {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
  padding: 24rpx;
  background: #fffaf0;
  box-shadow: none;
}

.tips-icon {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 50%;
  color: #fe871b;
  background: rgba(254, 135, 27, 0.12);
}

.tips-title {
  color: #8a4c08;
  font-size: 26rpx;
  font-weight: 700;
}

.tips-desc {
  margin-top: 8rpx;
  color: #b06b1c;
  font-size: 23rpx;
  line-height: 1.5;
}
</style>
