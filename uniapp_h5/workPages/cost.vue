<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">费用报销</text>
      </view>
    </tn-navbar>


    <view class="" :style="{paddingTop: vuex_custom_bar_height + 30 + 'px'}" style="padding-bottom: 240rpx;">

      <picker @change="bindPickerChange" :value="index" :range="array">
        <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
          <view class="justify-content-item tn-flex-column">
            <view class="tn-text-bold tn-text-lg">
              报销类型 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
            <view class="tn-color-gray tn-padding-top-xs" v-if="index===99">
              请选择
            </view>
            <view class="tn-color-black tn-padding-top-xs" v-else>
              {{array[index]}}
            </view>
          </view>
          <view class="justify-content-item tn-text-lg tn-color-grey tn-margin-left">
            <tn-icon name="right" class="tn-padding-top"></tn-icon>
          </view>
        </view>
      </picker>

      <view class="">
        <tn-picker mode="time" v-model="show" :params="params" :startYear="startYear" :endYear="endYear" @confirm="confirmPicker"></tn-picker>
      </view>

      <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding" @tap="showPicker">
        <view class="justify-content-item tn-flex-column">
          <view class="tn-text-bold tn-text-lg">
            发生时间 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
          <view class="tn-color-gray tn-padding-top-xs" v-if="result===''">
            请选择
          </view>
          <view class="tn-padding-top-xs" v-else>
            {{ result }}
          </view>
        </view>
        <view class="justify-content-item tn-text-lg tn-color-grey tn-margin-left">
          <tn-icon name="time" class="tn-padding-top"></tn-icon>
        </view>
      </view>

      <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
        <view class="justify-content-item tn-flex-column">
          <view class="tn-text-bold tn-text-lg">
            费用金额 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
          <view class="tn-color-gray tn-padding-top-xs tn-color-black">
            <input v-model="amount" type="digit" placeholder="请输入金额" name="input" placeholder-style="color:#AAAAAA" value=""></input>
          </view>
        </view>
        <view class="justify-content-item tn-text-xl tn-color-grey tn-margin-left">
          <tn-icon name="money" class="tn-padding-top"></tn-icon>
        </view>
      </view>



      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
        <view class="tn-flex justify-content-item">
          <view class="tn-text-bold tn-text-lg">
            费用说明 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
        </view>
        <view class="tn-flex justify-content-item tn-color-gray">
          200字内
        </view>
      </view>
      <view class="tn-bg-gray--light tn-padding tn-text-justify" style="border-radius: 10rpx;margin: 0 30rpx 30rpx 30rpx;">
        <textarea v-model="reason" maxlength="200" placeholder="请简单写一下报销事由" placeholder-style="color:#AAAAAA" style="height: 160rpx;width: 100%;"></textarea>
      </view>

      <!-- 悬浮按钮-->
      <view class="tn-flex tn-footerfixed justify-center">
        <tn-button
            bg-color="#00C8B0"
            width="60%"
            height="80"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            :loading="submitting"
            :disabled="submitting"
            @click="submitApply"
        >
            <text class="">{{ submitting ? '提交中...' : '提交申请' }}</text>
        </tn-button>
      </view>



    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { submitApplication } from '@/api/application'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 定义组件选项
defineOptions({ name: 'TemplateCost' })

// 只显示年月日时分，不显示秒
const params = ref({
  year: true,
  month: true,
  day: true,
  hour: true,
  minute: true,
  second: false
})
const show = ref(false)
const result = ref('')
const startYear = ref(2023)
const endYear = ref(2100)

const index = ref(99)
const array = ref(['餐饮费', '礼品费', '活动费', '物料费', '补助费', '交通费', '招待费', '医疗费', '出差费', '其他费用'])

const amount = ref('')
const reason = ref('')
const submitting = ref(false)

// 弹出Picker
function showPicker() {
  show.value = true
}

// 点击确认按钮
function confirmPicker(event) {
  result.value = `${event.year}-${event.month}-${event.day} ${params.value.hour ? event.hour + ':' : ''}${params.value.minute ? event.minute : ''}`
}

function bindPickerChange(e) {
  index.value = e.detail.value
}

// 提交报销申请(审批人由后台审批流配置决定)
async function submitApply() {
  if (index.value === 99) {
    uni.showToast({ title: '请选择报销类型', icon: 'none' })
    return
  }
  if (!result.value) {
    uni.showToast({ title: '请选择发生时间', icon: 'none' })
    return
  }
  if (!amount.value || Number(amount.value) <= 0) {
    uni.showToast({ title: '请填写费用金额', icon: 'none' })
    return
  }
  if (!reason.value.trim()) {
    uni.showToast({ title: '请填写费用说明', icon: 'none' })
    return
  }
  const store = useStore()
  const employeeInfo = store.getters.employeeInfo || uni.getStorageSync('userInfo') || {}
  const employeeId = store.getters.id || employeeInfo.id
  if (!employeeId) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  submitting.value = true
  uni.showLoading({ title: '提交中...' })
  try {
    const res = await submitApplication({
      employeeId,
      status: 0,
      appType: 'expense',
      title: `${array.value[index.value]}报销`,
      startTime: result.value,
      amount: Number(amount.value),
      reason: reason.value.trim()
    })
    uni.hideLoading()
    if (res.code === 200) {
      uni.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: '/workPages/prompt?result=success&title=费用报销' })
      }, 600)
    } else {
      uni.showToast({ title: res.msg || '提交失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error || '提交失败，请重试', icon: 'none' })
    setTimeout(() => {
      uni.redirectTo({ url: '/workPages/prompt?result=fail&title=费用报销' })
    }, 1200)
  } finally {
    submitting.value = false
  }
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

  /* 间隔线 start*/
  .tn-strip-bottom-min {
    width: 100%;
    border-bottom: 1rpx solid #F8F9FB;
  }

  .tn-strip-top {
   width: 100%;
   border-top: 20rpx solid rgba(241, 241, 241, 0.8);
  }
   /* 间隔线 end*/

   /* 底部悬浮按钮 start*/
   .tn-tabbar-height {
  	min-height: 160rpx;
	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
   }
   .tn-footerfixed {
     max-width: 640px;
     margin: 0 auto;
     position: fixed;
     width: 100%;
     bottom: calc(40rpx + env(safe-area-inset-bottom));
     z-index: 1024;
     box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
   }
   /* 底部悬浮按钮 end*/
</style>
