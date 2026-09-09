<template>
  <view class="start-index">
    <view v-show="currentIndex === 0" class="tabbar-page-wrap">
      <scroll-view class="custom-tabbar-page" scroll-y enable-back-to-top @scrolltolower="tabbarPageScrollLower">
        <Home ref="homeRef"></Home>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 1" class="tabbar-page-wrap">
      <scroll-view class="custom-tabbar-page" scroll-y enable-back-to-top @scrolltolower="tabbarPageScrollLower">
        <Moment ref="momentRef"></Moment>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 2" class="tabbar-page-wrap">
      <scroll-view class="custom-tabbar-page" scroll-y enable-back-to-top @scrolltolower="tabbarPageScrollLower">
        <Work ref="workRef"></Work>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 3" class="tabbar-page-wrap">
      <scroll-view class="custom-tabbar-page" scroll-y enable-back-to-top :scroll-top="pageScrollTop[3]" @scroll="tabbarPageScroll" @scrolltolower="tabbarPageScrollLower">
        <Partner ref="partnerRef" @key-select="contactsKeysSelectEvent"></Partner>
      </scroll-view>
    </view>
    <view v-show="currentIndex === 4" class="tabbar-page-wrap">
      <scroll-view class="custom-tabbar-page" scroll-y enable-back-to-top @scrolltolower="tabbarPageScrollLower">
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
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Home from './home/home.vue'
import Moment from './moment/moment.vue'
import Work from './work/work.vue'
import Partner from './partner/partner.vue'
import Mine from './mine/mine.vue'

// TnTabbar 和 TnTabbarItem 已通过全局组件导入，无需手动引入

// 组件引用
const homeRef = ref(null)
const momentRef = ref(null)
const workRef = ref(null)
const partnerRef = ref(null)
const mineRef = ref(null)

// 底部tabbar菜单数据
const tabbarList = ref([
  {
    title: '首页',
    activeIcon: 'company-fill',
    inactiveIcon: 'company-fill',
    iconSize: '50rpx'
  },
  {
    title: '时光',
    activeIcon: 'discover-fill',
    inactiveIcon: 'discover-fill',
    iconSize: '50rpx'
  },
  {
    title: '工作台',
    activeIcon: 'menu-circle-fill',
    inactiveIcon: 'menu-circle-fill',
    iconSize: '50rpx'
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
// 保存每个子页面scrollTop的值
const pageScrollTop = ref([0, 0, 0, 0, 0])

// 切换导航
const switchTabbar = (index) => {
  _switchTabbarPage(index)
}

// 瀑布流导航页面滚动到底部
const tabbarPageScrollLower = (e) => {
  // 滚动到底部事件
}

// 子页面滚动事件
const tabbarPageScroll = (e) => {
  const scrollTop = e.detail.scrollTop
  pageScrollTop.value[currentIndex.value] = scrollTop
  if (currentIndex.value === 3) {
    partnerRef.value?._updateScrollTopValue?.(scrollTop)
  }
}

// 通讯录点击索引事件
const contactsKeysSelectEvent = (scrollTop) => {
  pageScrollTop.value[3] = scrollTop - 125
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
</script>

<style lang="scss" scoped>
.start-index {
  min-height: 100vh;
  background-color: #FFFFFF;
}

.tabbar-page-wrap {
  height: calc(100vh - 120rpx);
  overflow: hidden;
}

.custom-tabbar-page {
  height: 100%;
}
</style>
