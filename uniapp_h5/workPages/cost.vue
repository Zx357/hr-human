<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view>
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
            <input placeholder="请输入" name="input" placeholder-style="color:#AAAAAA" value=""></input>
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
        <textarea maxlength="500" placeholder="请简单写一下报销事由" placeholder-style="color:#AAAAAA" style="height: 160rpx;width: 100%;"></textarea>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding tn-strip-top">
        <view class="tn-flex justify-content-item">
          <view class="tn-text-bold tn-text-lg">
            上传图片
          </view>
        </view>
        <view class="tn-flex justify-content-item tn-color-gray">
          最多6张
        </view>
      </view>
      
      <view class="tn-padding-left tn-padding-top-xs tn-padding-bottom-xs tn-strip-bottom-min">
        <tn-image-upload
          ref="imageUpload"
          :action="action"
          :width="236"
          :height="236"
          :formData="formData"
          :fileList="fileList"
          :disabled="disabled"
          :autoUpload="autoUpload"
          :maxCount="maxCount"
          :showUploadList="showUploadList"
          :showProgress="showProgress"
          :deleteable="deleteable"
          :customBtn="customBtn"
          @sort-list="onSortList"
        />
      
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding tn-strip-top">
        <view class="tn-flex justify-content-item">
          <view class="tn-text-bold tn-text-lg">
            上传附件
          </view>
        </view>
        <view class="justify-content-item tn-text-xl tn-color-grey">
          <tn-icon name="add-circle"></tn-icon>
        </view>
      </view>
      
      <view class="tn-margin-left tn-margin-right tn-margin-bottom" style="background-color: #00C8B008;color: #00C8B0;border-radius: 10rpx;" v-for="(item, index) in 3" :key="index" @click="tn('')">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
          <view class="justify-content-item tn-text-bold tn-text-left" style="width: 80%;">
            <view class="">
              <text class="">年中新年活动-费用报销.xls</text>
            </view>
            <view class="tn-padding-top-xs tn-text-sm">
              <tn-icon name="folder-fill"></tn-icon>
              <text class="tn-padding-left-xs">36.66 KB</text>
            </view>
          </view>
          <view class="justify-content-item">
            <tn-icon name="close-fill"></tn-icon>
          </view>
        </view>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding tn-strip-top">
        <view class="justify-content-item tn-flex-column">
          <view class="tn-text-bold tn-text-lg">
            审批人员 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
          <view class="tn-color-gray tn-padding-top-xs">
            请选择
          </view>
        </view>
        <view class="justify-content-item tn-text-lg tn-color-grey tn-margin-left">
          <tn-icon name="my-add" class="tn-padding-top"></tn-icon>
        </view>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-padding">
        <view class="justify-content-item tn-flex-column">
          <view class="tn-text-bold tn-text-lg">
            抄送人员 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
          <view class="tn-color-gray tn-padding-top-xs">
            请选择
          </view>
        </view>
        <view class="justify-content-item tn-text-lg tn-color-grey tn-margin-left">
          <tn-icon name="my-add" class="tn-padding-top"></tn-icon>
        </view>
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
            @click="tn('/workPages/prompt')"
          >
            <text class="">提交申请</text>
          </tn-button>
      </view>
      
      
      
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 定义组件选项
// @ts-ignore
defineOptions({ name: 'TemplateCost' })

// Picker 参数类型
interface PickerParams {
  year: boolean
  month: boolean
  day: boolean
  hour: boolean
  minute: boolean
  second: boolean
}

// 文件项类型
interface FileItem {
  url: string
}

// 表单数据类型
interface FormData {
  apiType: string
  token: string
  image: null
}

// Picker 确认事件类型
interface PickerConfirmEvent {
  year: string
  month: string
  day: string
  hour: string
  minute: string
}

// Picker 变更事件类型
interface PickerChangeEvent {
  detail: {
    value: number
  }
}

// 只显示年月日时分，不显示秒
const params = ref<PickerParams>({
  year: true,
  month: true,
  day: true,
  hour: true,
  minute: true,
  second: false
})
const show = ref<boolean>(false)
const result = ref<string>('')
const startYear = ref<number>(2023)
const endYear = ref<number>(2100)

const index = ref<number>(99)
const array = ref<string[]>(['餐饮费', '礼品费', '活动费', '物料费', '补助费', '交通费', '招待费', '医疗费', '出差费','其他乱糟糟的费'])

const action = ref<string>('https://www.hualigs.cn/api/upload')
const formData = ref<FormData>({
  apiType: 'this,ali',
  token: 'dffc1e06e636cff0fdf7d877b6ae6a2e',
  image: null
})
const fileList = ref<FileItem[]>([
  {url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103619950-assets/web-upload/31e51c70-c0d4-4814-91d6-c740d45b014d.jpeg'},
  {url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103627372-assets/web-upload/4dbd5977-14d8-4720-9ec4-752ced51c39a.jpeg'}
])
const showUploadList = ref<boolean>(true)
const customBtn = ref<boolean>(false)
const autoUpload = ref<boolean>(true)
const showProgress = ref<boolean>(false)
const deleteable = ref<boolean>(true)
const customStyle = ref<boolean>(false)
const maxCount = ref<number>(6)
const disabled = ref<boolean>(false)

const videoUrl = ref<string>('')

// 图片上传组件 ref
const imageUpload = ref<any>(null)

// 弹出Picker
function showPicker(event: any): void {
  openPicker()
}

// 打开Picker
function openPicker(): void {
  show.value = true
}

// 点击确认按钮
function confirmPicker(event: PickerConfirmEvent): void {
  result.value = `${event.year}-${event.month}-${event.day} ${params.value.hour ? event.hour+':' : ''}${params.value.minute ? event.minute : ''}`
}

function bindPickerChange(e: PickerChangeEvent): void {
  index.value = e.detail.value
}

// 跳转
function tn(e: string): void {
  uni.navigateTo({
    url: e,
  });
}

// 手动上传文件
function upload(): void {
  imageUpload.value?.upload()
}

// 图片拖拽重新排序
function onSortList(list: any): void {
  console.log(list);
}

// 视频选择
function videoChooseHandle(): void {
  uni.chooseVideo({
    success: (res: any) => {
      videoUrl.value = res.tempFilePath
    }
  })
}

// 重新选择视频
function reChooseVideo(): void {
  videoUrl.value = ''
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
    // background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  .video-choose {
    position: relative;
    width: calc(100% - 60rpx);
    height: 340rpx;
    background-color: #f8f7f8;
    color: #838383;
    border-radius: 10rpx;
    margin: 10rpx 0 30rpx 30rpx;
    
    .choose-operation {
      position: relative;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: inherit;
      
      .icon {
        font-size: 70rpx;
        line-height: 1;
        padding: 20rpx;
      }
      .tips {
        line-height: 1;
      }
    }
    
    .video-preview {
      position: relative;
      width: 100%;
      height: 100%;
      border-radius: inherit;
      .video-container {
        width: 100%;
        height: 100%;
        border-radius: inherit;
      }
      
      .remove-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        position: absolute;
        top: 0;
        right: 0;
        z-index: 10;
        border-top: 60rpx solid;
        border-left: 60rpx solid transparent;
        border-top-color: rgba(0,0,0,0.1);
        width: 0rpx;
        height: 0rpx;
        
        &--icon {
          position: absolute;
          top: -50rpx;
          right: 6rpx;
          color: #FFFFFF;
          font-size: 24rpx;
          line-height: 1;
        }
      }
    }
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
