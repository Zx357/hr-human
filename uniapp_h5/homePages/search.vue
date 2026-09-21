<template>
  <view class="search-page tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#FFFFFF" home-icon="" :bottom-shadow="false" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back' @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">全局搜索</text>
      </view>
    </tn-navbar>

    <!-- 搜索栏 -->
    <view class="search-bar-wrap tn-bg-white" :style="{ paddingTop: vuex_custom_bar_height + 'px' }">
      <view class="search-bar tn-flex tn-flex-col-center">
        <view class="search-input-wrap tn-flex tn-flex-col-center">
          <tn-icon name="search" class="tn-color-gray tn-padding-right-xs"></tn-icon>
          <input v-model="inputValue" class="search-input" placeholder="搜索同事、公告、功能" placeholder-style="color:#AAAAAA" confirm-type="search" @confirm="doSearch" />
        </view>
        <tn-button
          bg-color="#3668FC"
          :custom-style="{ padding: '14rpx 36rpx' }"
          :fontSize="26"
          text-color="#ffffff"
          shape="round"
          @click="doSearch"
        >
          搜索
        </tn-button>
      </view>
      <view class="result-tabs tn-flex">
        <view
          v-for="(tab, index) in resultTabs"
          :key="tab.key"
          class="result-tab"
          :class="{ 'result-tab--active': activeTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}{{ tabCount(index) ? ' (' + tabCount(index) + ')' : '' }}
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="search-result">
      <view v-if="searching" class="tn-text-center tn-color-gray tn-padding-xl">搜索中...</view>
      <view v-else-if="!searched" class="tn-text-center tn-color-gray--disabled tn-padding-xl">输入关键词搜索同事、公告、功能</view>
      <view v-else-if="!activeResults.length" class="tn-text-center tn-color-gray--disabled tn-padding-xl">未找到相关内容</view>
      <view v-else class="result-card">
        <view v-for="(item, index) in activeResults" :key="activeTab + '-' + index" class="result-item tn-flex tn-flex-col-center" @click="openResult(item)">
          <view v-if="item.icon" class="result-icon tn-flex tn-flex-row-center tn-flex-col-center">
            <tn-icon :name="item.icon" class="tn-color-white"></tn-icon>
          </view>
          <image v-else class="result-avatar" :src="item.userAvatar" mode="aspectFill" />
          <view class="result-info">
            <view class="tn-text-lg tn-text-bold">{{ item.title }}</view>
            <view class="result-desc tn-color-gray tn-text-sm tn-padding-top-xs">{{ item.desc }}</view>
          </view>
          <tn-icon class="tn-color-gray" name="right"></tn-icon>
        </view>
      </view>
    </view>

    <view class='tn-tabbar-height'></view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { searchContacts } from '@/api/contact'
import { getMobileNotices } from '@/api/system/notice'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 输入值
const inputValue = ref('')

// 三类结果:0-同事 1-公告 2-功能
const resultTabs = [
  { name: '同事', key: 'contact' },
  { name: '公告', key: 'notice' },
  { name: '功能', key: 'feature' }
]
const activeTab = ref(0)
const contactResults = ref([])
const noticeResults = ref([])
const featureResults = ref([])
const searched = ref(false)
const searching = ref(false)

const activeResults = computed(() => {
  if (activeTab.value === 1) return noticeResults.value
  if (activeTab.value === 2) return featureResults.value
  return contactResults.value
})

const tabCount = (index) => {
  if (!searched.value) return 0
  if (index === 1) return noticeResults.value.length
  if (index === 2) return featureResults.value.length
  return contactResults.value.length
}

const switchTab = (index) => {
  activeTab.value = index
}

// 工作台可直达功能(与后端菜单 path 对齐)
const FEATURE_PAGES = [
  { name: '请假申请', keywords: '请假 请假申请 leave', url: '/workPages/leave' },
  { name: '加班申请', keywords: '加班 加班申请 overtime', url: '/workPages/overtime' },
  { name: '补卡申请', keywords: '补卡 补卡申请 replace makeup', url: '/workPages/replace' },
  { name: '出差申请', keywords: '出差 出差申请 travel', url: '/workPages/travel' },
  { name: '换休申请', keywords: '换休 调休 换休申请 exchange', url: '/workPages/exchange' },
  { name: '离职申请', keywords: '离职 离职申请 resign', url: '/workPages/resign' },
  { name: '费用报销', keywords: '报销 费用 报销申请 cost', url: '/workPages/cost' },
  { name: '设备申请', keywords: '设备 设备申请 device', url: '/workPages/device' },
  { name: '考勤打卡', keywords: '打卡 考勤 上下班 clock time', url: '/workPages/time' },
  { name: '考勤日历', keywords: '考勤 日历 考勤记录 calendar', url: '/workPages/calendar' },
  { name: '请假记录', keywords: '请假 记录 申请记录 leave-record 我的申请', url: '/homePages/application' }
]

const searchContactsTab = async (keyword) => {
  try {
    const res = await searchContacts({ keyword })
    const list = Array.isArray(res.data) ? res.data : res.data?.records || []
    contactResults.value = list.map((item) => ({
      id: item.id,
      title: item.name || item.employeeName || '未命名员工',
      desc: [item.employeeNo, item.deptName || item.companyName].filter(Boolean).join(' · ') || '暂无部门信息',
      userAvatar: formatAvatar(item.avatar),
      url: `/partnerPages/user?id=${item.id}`
    }))
  } catch (error) {
    contactResults.value = []
  }
}

const searchNoticesTab = async (keyword) => {
  try {
    const res = await getMobileNotices({ pageNum: 1, pageSize: 20, keyword })
    const records = res.data?.records || []
    noticeResults.value = records.map((item) => ({
      id: item.id,
      title: item.noticeTitle || '未命名通知',
      desc: stripHtml(item.noticeContent).slice(0, 50) || '暂无内容',
      icon: 'notice-fill',
      url: '/homePages/notice'
    }))
  } catch (error) {
    noticeResults.value = []
  }
}

const searchFeaturesTab = (keyword) => {
  const kw = keyword.toLowerCase()
  featureResults.value = FEATURE_PAGES.filter(
    (page) => page.name.toLowerCase().includes(kw) || page.keywords.toLowerCase().includes(kw)
  ).map((page) => ({
    title: page.name,
    desc: '快速直达该功能',
    icon: 'rocket-fill',
    url: page.url
  }))
}

// 执行搜索(同事/公告/功能)
const doSearch = async () => {
  if (searching.value) return
  const keyword = inputValue.value.trim()
  if (!keyword) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }
  searching.value = true
  searched.value = true
  Promise.all([
    searchContactsTab(keyword),
    searchNoticesTab(keyword),
    Promise.resolve(searchFeaturesTab(keyword))
  ]).finally(() => {
    searching.value = false
  })
}

// 打开结果
const openResult = (item) => {
  if (!item?.url) return
  uni.navigateTo({ url: item.url })
}

function stripHtml(value) {
  if (!value) return ''
  return String(value).replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
}

const formatAvatar = (avatar) => {
  if (!avatar) return '/static/author.jpg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

// 跳转
const tn = (e) => {
  if (!e) return
  uni.navigateTo({
    url: e,
  })
}
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background-color: #f8f7f8;
}

/* 胶囊*/
.tn-custom-nav-bar__back {
  width: 60%;
  height: 100%;
  position: relative;
  display: flex;
  justify-content: space-evenly;
  align-items: center;
  box-sizing: border-box;
  background-color: rgba(0, 0, 0, 0.15);
  border-radius: 1000rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  color: #ffffff;
  font-size: 18px;

  .icon {
    display: block;
    flex: 1;
    margin: auto;
    text-align: center;
  }
}

.search-bar-wrap {
  padding-bottom: 20rpx;
}

.result-tabs {
  padding: 0 24rpx 6rpx;
}

.result-tab {
  position: relative;
  margin-right: 44rpx;
  padding: 12rpx 4rpx;
  color: #657189;
  font-size: 28rpx;
}

.result-tab--active {
  color: #3668fc;
  font-weight: 700;

  &::after {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 40rpx;
    height: 6rpx;
    border-radius: 6rpx;
    background: #3668fc;
    content: '';
  }
}

.result-icon {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #4b98fe 0%, #3668fc 100%);
  font-size: 42rpx;
  color: #fff;
}

.result-desc {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 420rpx;
}

.search-bar {
  padding: 10rpx 24rpx;
}

.search-input-wrap {
  flex: 1;
  height: 68rpx;
  margin-right: 20rpx;
  padding: 0 24rpx;
  background: #f4f5f9;
  border-radius: 100rpx;
}

.search-input {
  width: 100%;
  font-size: 26rpx;
}

.search-result {
  padding: 0 24rpx;
}

.result-card {
  background: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.result-item {
  padding: 26rpx;
  border-bottom: 1rpx solid #f3f2f7;
}

.result-item:last-child {
  border-bottom: none;
}

.result-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  background-color: #f4f5f9;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.clamp-text-1 {
  -webkit-line-clamp: 1;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  overflow: hidden;
}

.clamp-text-2 {
  -webkit-line-clamp: 2;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  overflow: hidden;
}

.tn-tabbar-height {
  min-height: 60rpx;
}
</style>
