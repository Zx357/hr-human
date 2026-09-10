<template>
  <view class="search-page tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#FFFFFF" home-icon="" :bottom-shadow="false" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back' @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">全局搜索</text>
      </view>
    </tn-navbar>

    <!-- 搜索栏 -->
    <view class="search-bar-wrap tn-bg-white" :style="{ paddingTop: vuex_custom_bar_height + 'px' }">
      <view class="search-bar tn-flex tn-flex-col-center">
        <view class="search-input-wrap tn-flex tn-flex-col-center">
          <tn-icon name="search" class="tn-color-gray tn-padding-right-xs"></tn-icon>
          <input v-model="inputValue" class="search-input" placeholder="搜索同事姓名" placeholder-style="color:#AAAAAA" confirm-type="search" @confirm="doSearch" />
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
    </view>

    <!-- 搜索结果 -->
    <view class="search-result">
      <view v-if="searching" class="tn-text-center tn-color-gray tn-padding-xl">搜索中...</view>
      <view v-else-if="!content.length" class="tn-text-center tn-color-gray--disabled tn-padding-xl">
        {{ inputValue ? '未找到相关同事' : '输入姓名搜索同事' }}
      </view>
      <view v-else class="result-card">
        <view v-for="(item, index) in content" :key="index" class="result-item tn-flex tn-flex-col-center" @click="openContact(item)">
          <image class="result-avatar" :src="item.userAvatar" mode="aspectFill" />
          <view class="result-info">
            <view class="tn-text-lg tn-text-bold">{{ item.title }}</view>
            <view class="tn-color-gray tn-text-sm tn-padding-top-xs">{{ item.desc }}</view>
          </view>
          <tn-icon class="tn-color-gray" name="right"></tn-icon>
        </view>
      </view>
    </view>

    <view class='tn-tabbar-height'></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { searchContacts } from '@/api/contact'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 输入值
const inputValue = ref('')

// 搜索结果
const content = ref([])
const searching = ref(false)

// 执行搜索(后端联系人搜索)
const doSearch = async () => {
  const keyword = inputValue.value.trim()
  if (!keyword) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }
  searching.value = true
  try {
    const res = await searchContacts({ name: keyword })
    const list = Array.isArray(res.data) ? res.data : res.data?.records || []
    content.value = list.map((item) => ({
      id: item.id,
      title: item.name || item.employeeName || '未命名员工',
      desc: item.deptName || item.companyName || '暂无部门信息',
      label: [item.position || item.post || '同事'].filter(Boolean),
      userAvatar: formatAvatar(item.avatar),
      raw: item
    }))
  } catch (error) {
    console.log('搜索失败', error)
  } finally {
    searching.value = false
  }
}

// 打开同事详情
const openContact = (item) => {
  if (!item?.id) return
  uni.navigateTo({ url: `/partnerPages/user?id=${item.id}` })
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
