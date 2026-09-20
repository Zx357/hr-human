<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF00">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">提交结果</text>
      </view>
    </tn-navbar>


    <view class="tn-text-center" :style="{paddingTop: vuex_custom_bar_height + 60 + 'px'}">
      <view v-if="isSuccess" class="tn-page-success" style="font-size: 420rpx;line-height: 420rpx"></view>
      <view v-else class="tn-page-error" style="font-size: 420rpx;line-height: 420rpx"></view>
      <view class="tn-margin-top">
        <view v-if="isSuccess" class="tn-text-lg tn-margin-bottom tn-text-bold" style="color: #00C8B0;">提交成功</view>
        <view v-else class="tn-text-lg tn-margin-bottom tn-text-bold" style="color: #FB6A67;">提交失败</view>
        <template v-if="isSuccess">
          <view class="tn-color-gray">{{ titleText }}已提交，等待审批</view>
          <view class="tn-color-gray">可在"我的申请"中查看审批进度</view>
        </template>
        <template v-else>
          <view class="tn-color-gray">{{ titleText }}未提交成功</view>
          <view class="tn-color-gray">请返回检查填写内容后重试</view>
        </template>
      </view>
    </view>

    <!-- 悬浮按钮-->
    <view class="tn-flex tn-padding tn-margin-top-lg">
      <view class="tn-flex-1 justify-content-item tn-margin-right-xs tn-text-center tn-bg-white" style="border-radius: 100rpx;">
        <tn-button
          bg-color="#00C8B0"
          :custom-style="{padding:'39rpx 0'}"
          width="48%"
          :fontSize="28"
          text-color="#00C8B0"
          shape="round"
          :plain="true"
          @click="tnindex('/pages/index?index=2')"
        >
          <text class="">返回首页</text>
        </tn-button>
      </view>
      <view class="tn-flex-1 justify-content-item tn-margin-right-xs tn-text-center tn-bg-white" style="border-radius: 100rpx;">
        <tn-button
          bg-color="#00C8B0"
          :custom-style="{padding:'39rpx 0'}"
          width="48%"
          :fontSize="28"
          text-color="#FFFFFF"
          shape="round"
          @click="tn('/homePages/application')"
        >
          <text class="">查看详情</text>
        </tn-button>
      </view>
    </view>


  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 提交结果: success-成功 fail-失败
const result = ref('success')
// 申请标题(用于结果文案)
const title = ref('')

const isSuccess = computed(() => result.value !== 'fail')
const titleText = computed(() => (title.value ? `${title.value}` : ''))

onLoad((options = {}) => {
  if (options.result) result.value = options.result
  if (options.title) title.value = decodeURIComponent(options.title)
})

// 跳转
function tn(e) {
  if (!e) return
  uni.navigateTo({
    url: e,
  });
}

// 返回首页
function tnindex(e) {
  uni.reLaunch({
    url: e,
  });
}
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
    font-size: 32rpx;

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
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }

  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    position: fixed;
    width: 100%;
    bottom: calc(40rpx + env(safe-area-inset-bottom));
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
  }
  /* 底部悬浮按钮 end*/

</style>
