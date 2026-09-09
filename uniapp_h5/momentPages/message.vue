<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-padding-left">
        <text class="tn-text-bold tn-text-xl tn-color-black">消息互动</text>
      </view>
    </tn-navbar>

    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <!-- 互动统计 -->
      <view class="tn-margin-top tn-bg-white">
        <view
          v-for="(item, index) in summaryList"
          :key="index"
          class="summary-item tn-flex tn-flex-row-between tn-flex-col-center tn-padding tn-margin-left tn-margin-right"
          :class="{ 'tn-border-solid-bottom': index !== summaryList.length - 1 }"
        >
          <view class="tn-flex tn-flex-col-center">
            <view class="summary-icon tn-flex tn-flex-row-center tn-flex-col-center" :style="{ backgroundColor: item.bgColor }">
              <tn-icon :name="item.icon" class="tn-color-white tn-text-lg"></tn-icon>
            </view>
            <text class="tn-padding-left-sm tn-text-lg">{{ item.name }}</text>
          </view>
          <view class="tn-flex tn-flex-col-center">
            <text class="tn-text-lg tn-text-bold" :class="item.count > 0 ? 'tn-color-red' : 'tn-color-gray'">{{ item.count }}</text>
            <tn-icon class="tn-color-gray tn-padding-left-xs" name="right"></tn-icon>
          </view>
        </view>
      </view>

      <view v-if="totalCount === 0" class="tn-padding-xl">
        <view class="tn-text-center" style="font-size: 180rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-center tn-text-lg">暂无互动消息</view>
      </view>

      <view v-else class="tn-color-gray tn-text-center tn-padding">
        <text class="tn-text-xs">评论与私信功能即将上线，敬请期待</text>
      </view>
    </view>

    <view class='tn-tabbar-height'></view>

  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMomentMessages } from '@/api/moment'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'TemplateMessage'
})

const summary = ref({
  unreadCount: 0,
  likeCount: 0,
  commentCount: 0,
  mentionCount: 0
})

const summaryList = computed(() => [
  { name: '收到的赞', icon: 'like-fill', bgColor: '#FB6A67', count: Number(summary.value.likeCount || 0) },
  { name: '收到的评论', icon: 'comment-fill', bgColor: '#4B98FE', count: Number(summary.value.commentCount || 0) },
  { name: '提到的我', icon: 'at-sign', bgColor: '#FFAC00', count: Number(summary.value.mentionCount || 0) },
  { name: '未读消息', icon: 'email-fill', bgColor: '#00D05E', count: Number(summary.value.unreadCount || 0) }
])

const totalCount = computed(() =>
  summaryList.value.reduce((total, item) => total + item.count, 0)
)

const loadSummary = async () => {
  try {
    const res = await getMomentMessages()
    if (res.data) {
      summary.value = { ...summary.value, ...res.data }
    }
  } catch (error) {
    console.log('加载互动消息失败', error)
  }
}

onMounted(() => {
  loadSummary()
})
</script>

<style lang="scss" scoped>
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
    color: #FFFFFF;
    font-size: 18px;

    .icon {
      display: block;
      flex: 1;
      margin: auto;
      text-align: center;
    }

  }

  .oa-content{
    max-width: 640px;
    margin: 0 auto;
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }

  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }

  .summary-item {
    background-color: #FFFFFF;
  }

  .summary-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 16rpx;
  }

  .tn-border-solid-bottom {
    border-bottom: 1rpx solid #F3F2F7;
  }
</style>
