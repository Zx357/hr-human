<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
      <tn-navbar :placeholder="false" fixed :bottom-shadow="false" bg-color="#FFFFFF00">
        <view slot="back" class='tn-custom-nav-bar__back'
          @click="goBack">
          <tn-icon name="left-arrow" class='icon'></tn-icon>
        </view>
        <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
          <text class="tn-text-bold tn-text-xl tn-color-white">客户管理</text>
        </view>
      </tn-navbar>
    
    <view class="" :style="{height: vuex_custom_bar_height + 66 +'px;background-image:url(https://cdn.nlark.com/yuque/0/2022/png/280373/1646056140125-assets/web-upload/5511fd0a-fc44-4b35-8f73-3bb44e953021.png);width: 100vw;position: fixed;background-size: cover;overflow: hidden;z-index: 9;'}">
      <view class="top-fixed" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top tn-padding-bottom-xs"
          style="background-color: #FFFFFF00;">
          <view class="justify-content-item" style="width: 75vw;overflow: hidden;">
            <tn-tabs v-model="current1" scroll :bar="false" activeColor="#4B98FE" bold fontSize="22" bgColor="#FFFFFF00" height="50">
              <tn-tabs-item
                v-for="(item, index) in selectList"
                :key="index"
                :title="item.name"
              />
            </tn-tabs>
          </view>
          <view class="justify-content-item" style="padding-right: 30rpx;">
            <picker @change="bindPickerChange" :value="index" :range="array">
              <view class="tn-color-white tn-text-sm tn-padding-xs" style="margin-left: 15rpx;">
                <text class="tn-padding-top-xs">
                  {{array[index]}}
                </text>
                <tn-icon name="down-triangle" class="tn-padding-left-xs"></tn-icon>
              </view>
            </picker>
          </view>
        
        </view>
      </view>
    </view>
    
    <view class="tn-padding-bottom-lg" :style="{paddingTop: vuex_custom_bar_height + 70 +'px'}">
      
      <view class="tn-margin client-shadow tn-bg-white"  style="border-radius: 24rpx;">
        <view class="tn-color-black tn-padding">
          <view class="tn-flex tn-flex-col-center tn-text-center">
            <view class="justify-content-item tn-flex-1">
              <view class="tn-text-bold" style="font-size: 50rpx;">68</view>
              <view class="tn-text-sm tn-padding-top-xs tn-color-gray">今日新增</view>
            </view>
            <view class="justify-content-item tn-flex-1">
              <view class="tn-text-bold" style="font-size: 50rpx;">1,029</view>
              <view class="tn-text-sm tn-padding-top-xs tn-color-gray">本月新增</view>
            </view>
            <view class="justify-content-item tn-flex-1">
              <view class="tn-text-bold" style="font-size: 50rpx;">86,209</view>
              <view class="tn-text-sm tn-padding-top-xs tn-color-gray">累计客户</view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="" style="background-color: #FFFFFF;border-radius: 24rpx;margin: 0 30rpx 30rpx 30rpx;" v-for="(item, index) in 6" :key="index">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center" style="background-color: #FFFFFF;border-radius: 24rpx;">
          <view class="justify-content-item">
            <view class="tn-flex tn-flex-row-center">
              <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                <view class="image-pic tn-margin" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg');background-size: cover;background-position: center;" @click="tn('/workPages/portrait')">
                  <view class="image-product">
                  </view>
                </view>
                <view class="tn-margin-right" style="width: 100%;">
                  <view class="tn-text-bold clamp-text-1 tn-text-justify" @click="tn('/workPages/portrait')">
                    <text class="">抓住那只猪</text>
                  </view>
                  <view class="tn-padding-top-xs tn-color-gray" @click="callPhoneNumber" data-number="18266666666">
                    182 1912 8888 <tn-icon name="phone" class="tn-color-gray tn-padding-left-xs"></tn-icon>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <!-- <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-margin-right tn-bg-blue--light tn-round tn-text-xs tn-color-blue" style="padding: 10rpx 16rpx 10rpx 20rpx;" @click="tn('/workPages/portrait')">
            <text class="tn-text-bold">详情</text>
            <tn-icon name="send-fill" class="tn-padding-left-xs"></tn-icon>
          </view> -->
          
        </view>
        
        <view class="" style="padding: 0 30rpx 30rpx 30rpx;" @click="tn('/workPages/portrait')">
          <view class="tn-padding-top-xs">累计购买次数<text class="tn-color-gray tn-padding-left-sm">16次</text></view>
          <view class="tn-padding-top-xs">最近购买时间<text class="tn-color-gray tn-padding-left-sm">2023-12-23 10:20:15</text></view>
          <view class="tn-padding-top-xs">客户注册时间<text class="tn-color-gray tn-padding-left-sm">2023-12-23 16:09:06</text></view>
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

const index = ref(0)
const array = ref(['新客户', '老客户'])

const activeItemStyle = ref({
  borderRadius: '100rpx',
  border: "1rpx solid #4B98FE",
  marginLeft: "30rpx",
  backgroundColor: '#F7ECEB',
})

const inactiveItemStyle = ref({
  borderRadius: '100rpx',
  border: "1rpx solid #F2F2F2",
  marginLeft: "30rpx",
  backgroundColor: '#F2F2F2',
})

const current1 = ref(0)
const selectList = ref([
  {name: '全部'},
  {name: '今日'},
  {name: '本周'},
  {name: '当月'}
])

function bindPickerChange(e) {
  index.value = e.detail.value
}



//拨打固定电话
function callPhoneNumber() {
  wx.vibrateShort();
  uni.makePhoneCall({
    phoneNumber: "18219128888",
  });
}

// 跳转
function tn(e) {
  uni.navigateTo({
    url: e,
  });
}
</script>

<script>
export default {
  name: 'TemplateContent',
  
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
  
  .top-fixed{
    position: relative;
    // background-color: rgba(255,255,255,1);
    width: 100%;
    transition: all 0.25s ease-out;
    z-index: 100;
    padding-bottom: 20rpx;
  }
  
  /* 间隔线 start*/
  .tn-strip-bottom-min {
    width: 100%;
    border-bottom: 1rpx solid #F8F9FB;
  }
  
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid rgba(241, 241, 241, 0.8);
  }
   /* 间隔线 end*/
   
   /* 商品 start*/
   .image-product {
     border-radius: 1000rpx;
     width: 80rpx;
     height: 80rpx;
     position: relative;
   }
   
   .image-good{
     width: 100%;
     height: 200rpx;
     font-size: 40rpx;
     font-weight: 300;
     position: relative;
   }
   
   .image-pic{
     background-size: cover;
     background-repeat:no-repeat;
     // background-attachment:fixed;
     background-position:top;
     border-radius: 1000rpx;
   }
   
   /* 文字截取*/
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
   
   /* 间隔线 start*/
   .tn-strip-bottom-min {
     width: 100%;
     border-bottom: 1rpx solid #F8F9FB;
   }
   
   .tn-strip-bottom {
    width: 100%;
    border-bottom: 20rpx solid rgba(241, 241, 241, 0.8);
   }
    /* 间隔线 end*/
</style>
