<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">设备申请</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 30 + 'px'}" style="padding-bottom: 240rpx;">
      
      <picker @change="bindPickerChange" :value="index" :range="array">
        <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
          <view class="justify-content-item tn-flex-column">
            <view class="tn-text-bold tn-text-lg">
              设备类型 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
            <view class="tn-color-gray tn-padding-top-xs" v-if="index===99">
              请选择
            </view>
            <view class="tn-color-black tn-padding-top-xs" v-else>
              {{array[index]}}
            </view>
          </view>
          <view class="justify-content-item tn-text-lg tn-color-grey tn-margin-left">
            <tn-icon class="tn-padding-top" name='right'></tn-icon>
          </view>
        </view>
      </picker>
      
      <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
        <view class="justify-content-item tn-flex-column">
          <view class="tn-text-bold tn-text-lg">
            设备名称 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
          <view class="tn-color-gray tn-padding-top-xs tn-color-black">
            <input placeholder="请输入" name="input" placeholder-style="color:#AAAAAA" value=""></input>
          </view>
        </view>
        <view class="justify-content-item tn-text-xl tn-color-grey tn-margin-left">
          <tn-icon class="tn-padding-top" name='edit'></tn-icon>
        </view>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
        <view class="justify-content-item tn-flex-column">
          <view class="tn-text-bold tn-text-lg">
            设备数量 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
          <view class="tn-color-gray tn-padding-top-xs tn-color-black">
            <input placeholder="请输入" name="input" placeholder-style="color:#AAAAAA" value=""></input>
          </view>
        </view>
        <view class="justify-content-item tn-text-xl tn-color-grey tn-margin-left">
          <tn-icon class="tn-padding-top" name='edit'></tn-icon>
        </view>
      </view>
      
      
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
        <view class="tn-flex justify-content-item">
          <view class="tn-text-bold tn-text-lg">
            设备用途 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
        </view>
        <view class="tn-flex justify-content-item tn-color-gray">
          200字内
        </view>
      </view>
      <view class="tn-bg-gray--light tn-padding tn-text-justify" style="border-radius: 10rpx;margin: 0 30rpx 30rpx 30rpx;">
        <textarea maxlength="500" placeholder="请简单写一下设备事由" placeholder-style="color:#AAAAAA" style="height: 160rpx;width: 100%;"></textarea>
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
          <tn-icon name='add-circle'></tn-icon>
        </view>
      </view>
      
      <view class="tn-margin-left tn-margin-right tn-margin-bottom" style="background-color: #00C8B008;color: #00C8B0;border-radius: 10rpx;" v-for="(item, index) in 1" :key="index" @click="tn('')">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
          <view class="justify-content-item tn-text-bold tn-text-left" style="width: 80%;">
            <view class="">
              <text class="">设备损坏情况.pdf</text>
            </view>
            <view class="tn-padding-top-xs tn-text-sm">
              <tn-icon name='folder-fill'></tn-icon>
              <text class="tn-padding-left-xs">12 MB</text>
            </view>
          </view>
          <view class="justify-content-item">
            <tn-icon name='close-fill'></tn-icon>
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
          <tn-icon class="tn-padding-top" name='my-add'></tn-icon>
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
          <tn-icon class="tn-padding-top" name='my-add'></tn-icon>
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

defineOptions({ name: 'TemplateDevice' })

const imageUpload = ref<any>(null)
const index = ref<number>(99)
const array = ref<string[]>(['个人设备', '部门设备', '公司设备', '其他用途'])

const action = ref<string>('https://www.hualigs.cn/api/upload')
const formData = ref<{
  apiType: string
  token: string
  image: any
}>({
  apiType: 'this,ali',
  token: 'dffc1e06e636cff0fdf7d877b6ae6a2e',
  image: null
})
const fileList = ref<{url: string}[]>([{url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1693830798032-assets/web-upload/aa49b472-f60a-45cc-83cb-d56255e21a96.jpeg'},{url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242436-assets/web-upload/05220af3-0aa1-4bdc-8e8b-d3d562870398.jpeg'}])
const showUploadList = ref<boolean>(true)
const customBtn = ref<boolean>(false)
const autoUpload = ref<boolean>(true)
const showProgress = ref<boolean>(false)
const deleteable = ref<boolean>(true)
const customStyle = ref<boolean>(false)
const maxCount = ref<number>(6)
const disabled = ref<boolean>(false)

const videoUrl = ref<string>('')

function bindPickerChange(e: { detail: { value: number } }) {
  index.value = e.detail.value
}

// 跳转
function tn(e: string) {
	uni.navigateTo({
		url: e,
	});
}

// 手动上传文件
function upload() {
  imageUpload.value?.upload()
}

// 图片拖拽重新排序
function onSortList(list: any) {
  console.log(list);
}

// 视频选择
function videoChooseHandle() {
  uni.chooseVideo({
    success: (res) => {
      // 上传完成后再把视频地址填写到这里，这里只是临时测试用
      videoUrl.value = res.tempFilePath
    }
  })
}

// 重新选择视频
function reChooseVideo() {
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
  
  .oa-content{
    max-width: 640px;
    margin: 0 auto;
    // background-color: #F8F7F8;
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
