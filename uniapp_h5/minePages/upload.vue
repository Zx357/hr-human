<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name='left-arrow' class='icon'></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">文件上传</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 30 + 'px'}" style="padding-bottom: 240rpx;">
      
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
        <view class="tn-flex justify-content-item">
          <view class="tn-text-bold tn-text-lg">
            文件信息
          </view>
        </view>
        <view class="tn-flex justify-content-item tn-color-gray">
          信息传达
        </view>
      </view>
      <view class="tn-bg-gray--light" style="border-radius: 10rpx;padding: 20rpx 30rpx;margin: 0 30rpx 30rpx 30rpx;">
      	<input placeholder="请填写文件用途" name="input" placeholder-style="color:#AAAAAA" ></input>
      </view>
      
      <view class="tn-bg-gray--light tn-padding" style="border-radius: 10rpx;margin: 0 30rpx 30rpx 30rpx;">
        <textarea maxlength="500" placeholder="请输入文件简单备注" placeholder-style="color:#AAAAAA" style="height: 160rpx;"></textarea>
      </view>
      
      <!-- <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding tn-strip-top">
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
      
      </view> -->
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding tn-strip-top">
        <view class="tn-flex justify-content-item">
          <view class="tn-text-bold tn-text-lg">
            上传附件 <text class="tn-color-red tn-padding-left-xs">*</text>
          </view>
        </view>
        <view class="justify-content-item tn-text-xl tn-color-grey">
          <tn-icon name="add-circle"></tn-icon>
        </view>
      </view>
      
      <view class="tn-margin-left tn-margin-right tn-margin-bottom" style="background-color: #00C8B008;color: #00C8B0;border-radius: 10rpx;" @click="tn('')">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
          <view class="justify-content-item tn-text-bold tn-text-left" style="width: 80%;">
            <view class="">
              <text class="">月报通用表格.xls</text>
            </view>
            <view class="tn-padding-top-xs tn-text-sm">
              <tn-icon name="folder-fill"></tn-icon>
              <text class="tn-padding-left-xs">109.23 KB</text>
            </view>
          </view>
          <view class="justify-content-item">
            <tn-icon name="close-fill"></tn-icon>
          </view>
        </view>
      </view>
      
      
      <!-- 悬浮按钮-->
      <view class="tn-flex tn-footerfixed tn-padding">
        <view class="tn-flex-1 justify-content-item tn-text-center">
          <tn-button
  bg-color="#00C8B0"
  :custom-style="{padding:'40rpx 0'}"
  width="60%"
  :fontSize="28"
  text-color="#FFFFFF"
  shape="round"
  @click="tn('')"
>
            <text class="">提交上传</text>
          </tn-button>
        </view>
      </view>
      
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()
  
  const index = ref(99)
  const array = ref(['日报', '周报', '月报', '单纯想汇报'])
  
  const action = ref('https://www.hualigs.cn/api/upload')
  // action: '',
  const formData = ref({
    apiType: 'this,ali',
    token: 'dffc1e06e636cff0fdf7d877b6ae6a2e',
    image: null
  })
  const fileList = ref([{url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103619950-assets/web-upload/31e51c70-c0d4-4814-91d6-c740d45b014d.jpeg'},{url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103627372-assets/web-upload/4dbd5977-14d8-4720-9ec4-752ced51c39a.jpeg'}])
  const showUploadList = ref(true)
  const customBtn = ref(false)
  const autoUpload = ref(true)
  const showProgress = ref(false)
  const deleteable = ref(true)
  const customStyle = ref(false)
  const maxCount = ref(6)
  const disabled = ref(false)
  
  function bindPickerChange(e) {
    index.value = e.detail.value
  }
  // 跳转
  function tn(e) {
    uni.navigateTo({
      url: e,
    });
  }
  
  // 手动上传文件
  function upload() {
    // 需要使用 ref 获取组件引用
    // this.$refs.imageUpload.upload()
  }
  // 手动清空列表
  function clear() {
    // 需要使用 ref 获取组件引用
    // this.$refs.imageUpload.clear()
  }
  // 图片拖拽重新排序
  function onSortList(list) {
    console.log(list);
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
