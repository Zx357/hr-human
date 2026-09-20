<template>
  <view class="dept-members">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">{{ deptName }}</text>
      </view>
    </tn-navbar>

    <view :style="{paddingTop: vuex_custom_bar_height + 'px'}">
    <view v-if="loading" class="tn-padding-xl">
      <view class="tn-text-center tn-color-gray">加载中...</view>
    </view>

    <view v-else-if="!members.length" class="tn-padding-xl">
      <view class="tn-text-center" style="font-size: 160rpx;padding-top: 60rpx;">
        <text class="tn-icon-clip tn-color-gray--light"></text>
      </view>
      <view class="tn-color-gray--disabled tn-text-center tn-text-lg">该部门暂无成员</view>
    </view>

    <view v-else class="tn-bg-white">
      <view
        v-for="item in members"
        :key="item.id"
        class="member-item tn-flex tn-flex-col-center tn-padding"
        @click="openUser(item)"
      >
        <image class="member-avatar" :src="formatAvatar(item.avatar)" mode="aspectFill" />
        <view class="tn-padding-left-sm tn-flex-1">
          <view class="tn-text-lg tn-text-bold">{{ item.name || item.employeeName || '未命名员工' }}</view>
          <view class="tn-color-gray tn-padding-top-xs tn-text-sm">
            {{ item.position || item.post || '暂无职位' }}
          </view>
        </view>
        <tn-icon class="tn-color-gray" name="right"></tn-icon>
      </view>
    </view>
    <view class='tn-tabbar-height'></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getEmployeeList } from '@/api/employee'

// 自定义导航栏高度与返回方法
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'DeptMembers'
})

const deptId = ref(null)
const deptName = ref('部门成员')
const members = ref([])
const loading = ref(true)

const defaultAvatar = '/static/author.jpg'

const formatAvatar = (avatar) => {
  if (!avatar) return defaultAvatar
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

const loadMembers = async () => {
  if (!deptId.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await getEmployeeList({ deptId: deptId.value, status: 1 })
    members.value = Array.isArray(res.data) ? res.data : res.data?.records || []
  } catch (error) {
    members.value = []
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

const openUser = (item) => {
  if (!item?.id) return
  uni.navigateTo({
    url: `/partnerPages/user?id=${item.id}`
  })
}

onLoad((options) => {
  deptId.value = options?.id || null
  if (options?.name) {
    deptName.value = decodeURIComponent(options.name)
  }
  loadMembers()
})

onPullDownRefresh(() => {
  loadMembers()
})
</script>

<style lang="scss" scoped>
.dept-members {
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

.tn-tabbar-height {
  min-height: 60rpx;
  height: calc(80rpx + env(safe-area-inset-bottom) / 2);
}

.member-item {
  border-bottom: 1rpx solid #f3f2f7;
}

.member-avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 15rpx;
  background-color: #f4f5f9;
}
</style>
