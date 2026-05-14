<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">硬件管理</text>
      </view>
    </tn-navbar>
    
    
    <view class="tn-padding-bottom-lg" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
      
      <view class="tn-flex tn-flex-wrap" style="margin: 15rpx;">
        <block v-for="(item, index) in data" :key="index">
          <view class="" style="width: 50%;">
            <view class="content-bg tn-padding" style="margin: 15rpx;">
              <view class="tn-flex tn-flex-row-between tn-flex-row-center">
                <view class=""  @click="tn('/workPages/operate')">
                  <view class="clamp-text-1 tn-text-bold">
                    {{item.title}}
                  </view>
                  <view class="tn-text-xs tn-color-gray" v-if="item.online===0">
                    <text class="">离线</text>
                    <tn-icon class="tn-padding-left-xs" name="circle-fill"></tn-icon>
                  </view>
                  <view class="tn-text-xs oa-green" v-if="item.online===1">
                    <text class="">在线</text>
                    <tn-icon class="tn-padding-left-xs" name="circle-fill"></tn-icon>
                  </view>
                </view>
                <view class="tn-text-xs">
                  <tn-switch v-model="value" leftIcon="creative" rightIcon="creative-stop" activeColor="#00D05E" inactiveColor="#E6E6E6"></tn-switch>
                </view>
              </view>
              
              <view class="tn-color-gray tn-text-sm tn-padding-top-sm tn-text-justify clamp-text-1"  @click="tn('/workPages/operate')">
                <text class="">{{item.text}}</text>  
              </view>
              
            </view>
          </view>
        </block>
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

const value = ref(false)
const data = ref([
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 1,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  },
  {
    title: '网关',
    text: '公司ZigBee蜂鸣器网关',
    online: 0,
  }
])

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
  
  /* 背景阴影 start*/
  .content-bg {
    position: relative;
    border-radius: 15rpx;
    background-color: #FFFFFF;
    // box-shadow: 0rpx 0rpx 50rpx 0rpx rgba(0, 0, 0, 0.07);
  }
  
  /* 新增OA色系，自行调用，或者拿色值去用，多种方式*/
  .oa-black{
    color: #1D2541;
  }
  .oa-blue{
    color: #4B98FE;
  }
  .oa-orangeyellow{
    color: #FFAC00;
  }
  .oa-green{
    color: #00D05E;
  }
  .oa-orange{
    color: #FE871B;
  }
  .oa-cyan{
    color: #00C8B0;
  }
  .oa-indigo{
    color: #00B9FE;
  }
  .oa-orangered{
    color: #FB6A67;
  }
  .oa-purple{
    color: #957BFE;
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
  
</style>
