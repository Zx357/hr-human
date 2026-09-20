<template>
  <view class="start-index">
    <view v-show="currentIndex === 0" class="tabbar-page-wrap">
      <scroll-view
        class="custom-tabbar-page"
        scroll-y
        enable-back-to-top
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="tabbarPageScrollLower"
      >
        <Home ref="homeRef"></Home>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 1" class="tabbar-page-wrap">
      <scroll-view
        class="custom-tabbar-page"
        scroll-y
        enable-back-to-top
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="tabbarPageScrollLower"
      >
        <Moment ref="momentRef"></Moment>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 2" class="tabbar-page-wrap">
      <scroll-view
        class="custom-tabbar-page"
        scroll-y
        enable-back-to-top
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="tabbarPageScrollLower"
      >
        <Work ref="workRef"></Work>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 3" class="tabbar-page-wrap">
      <scroll-view
        class="custom-tabbar-page"
        scroll-y
        enable-back-to-top
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="tabbarPageScrollLower"
      >
        <Partner ref="partnerRef"></Partner>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 4" class="tabbar-page-wrap">
      <scroll-view
        class="custom-tabbar-page"
        scroll-y
        enable-back-to-top
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="tabbarPageScrollLower"
      >
        <Mine ref="mineRef"></Mine>
      </scroll-view>
    </view>

    <tn-tabbar v-model="currentIndex" fixed active-color="#3668FC" inactive-color="#C5CAD5" @change="switchTabbar">
      <tn-tabbar-item
        v-for="(item, index) in tabbarList"
        :key="index"
        :icon="item.inactiveIcon"
        :active-icon="item.activeIcon"
        :text="item.title"
        :badge="item.badge"
        :icon-size="item.iconSize"
      />
    </tn-tabbar>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import Home from './home/home.vue'
import Moment from './moment/moment.vue'
import Work from './work/work.vue'
import Partner from './partner/partner.vue'
import Mine from './mine/mine.vue'

// TnTabbar 和 TnTabbarItem 已通过全局组件导入，无需手动引入

const store = useStore()

// 组件引用
const homeRef = ref(null)
const momentRef = ref(null)
const workRef = ref(null)
const partnerRef = ref(null)
const mineRef = ref(null)

// 下拉刷新状态
const refreshing = ref(false)

// 角标格式化:0 不显示,超过 99 显示 99+
const formatBadge = (value) => {
  const count = Number(value || 0)
  if (count <= 0) return ''
  return count > 99 ? '99+' : String(count)
}

// 底部tabbar菜单数据(角标来自 vuex:聊天未读数 / 时光未读数 / 工作台待办数)
const tabbarList = computed(() => [
  {
    title: '首页',
    activeIcon: 'company-fill',
    inactiveIcon: 'company-fill',
    iconSize: '50rpx',
    badge: formatBadge(store.state.unreadBadge?.chatUnread)
  },
  {
    title: '时光',
    activeIcon: 'discover-fill',
    inactiveIcon: 'discover-fill',
    iconSize: '50rpx',
    badge: formatBadge(store.state.unreadBadge?.momentUnread)
  },
  {
    title: '工作台',
    activeIcon: 'menu-circle-fill',
    inactiveIcon: 'menu-circle-fill',
    iconSize: '50rpx',
    badge: formatBadge(store.state.unreadBadge?.workTodo)
  },
  {
    title: '通讯录',
    activeIcon: 'address-fill',
    inactiveIcon: 'address-fill',
    iconSize: '50rpx'
  },
  {
    title: '我的',
    activeIcon: 'my-job-fill',
    inactiveIcon: 'my-job-fill',
    iconSize: '50rpx'
  }
])

// tabbar当前被选中的序号
const currentIndex = ref(0)

// 获取当前激活 tab 对应的子组件实例
const currentTabComponent = () => {
  const refs = [homeRef, momentRef, workRef, partnerRef, mineRef]
  return refs[currentIndex.value]?.value || null
}

// 刷新当前激活 tab 的子组件数据
const refreshCurrentTab = async () => {
  const component = currentTabComponent()
  try {
    await component?.refresh?.()
  } catch (error) {
  }
}

// 切换导航(切换后刷新目标子组件)
const switchTabbar = (index) => {
  _switchTabbarPage(index)
  refreshCurrentTab()
}

// 下拉刷新
const onRefresh = async () => {
  if (refreshing.value) return
  refreshing.value = true
  await refreshCurrentTab()
  refreshing.value = false
}

// 瀑布流导航页面滚动到底部(转发给当前激活 tab 的子组件)
const tabbarPageScrollLower = () => {
  if (currentIndex.value === 1) {
    // 时光动态触底加载更多
    momentRef.value?.loadMore?.()
  }
}

// 切换导航页面
const _switchTabbarPage = (index) => {
  // #ifdef MP-WEIXIN
  wx.vibrateShort()
  // #endif
  const pageIndex = normalizeTabbarIndex(index)
  currentIndex.value = pageIndex
}

const normalizeTabbarIndex = (value) => {
  const index = Number.parseInt(value, 10)
  if (Number.isInteger(index) && index >= 0 && index < tabbarList.value.length) {
    return index
  }
  return 0
}

onLoad((options = {}) => {
  switchTabbar(options.index)
})

// 页面显示时只刷新当前激活 tab(各子组件首次数据在自身 onMounted 中加载)
onShow(() => {
  refreshCurrentTab()
})
</script>

<style lang="scss" scoped>
.start-index {
  min-height: 100vh;
  background-color: #F8F7F8;
}

.tabbar-page-wrap {
  height: calc(100vh - 120rpx - env(safe-area-inset-bottom));
  overflow: hidden;
}

.custom-tabbar-page {
  height: 100%;
}
</style>
