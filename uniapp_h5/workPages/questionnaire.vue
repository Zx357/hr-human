<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">问卷调查</text>
      </view>
    </tn-navbar>
    
    <view class="tn-padding-bottom-lg" :style="{paddingTop: vuex_custom_bar_height + 10 +'px'}">
      <view class="content-bg tn-margin tn-padding" v-for="(item, index) in questionList" :key="index" @click="tn('/workPages/question')">
        <view class="tn-text-center" style="width: 70rpx;height: 30rpx;position: absolute;top: 0;right:0;border-radius: 0 12rpx 0 12rpx;font-size: 14rpx;background-color: #4B98FE50;">
          <view class="tn-color-white" style="margin-top: 6rpx;">已回答</view>
        </view>
        <view class="tn-text-justify clamp-text-1 tn-text-lg tn-text-bold">
          {{ item.title }}
        </view>
        <view class="tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between tn-padding-top-sm">
          <view class="tn-flex">
            <view class="tn-flex user-pic">
              <view class="user-image" :style="'background-image:url('+ item.userImage +');width: 35rpx;height: 35rpx;background-size: cover;'">
              </view>
            </view>
            <view class="tn-flex tn-margin-left-xs" style="width: 300rpx;">
              <text class="clamp-text-1 tn-color-gray--dark">{{ item.userName }}</text>
            </view>
          </view>
          <view class="tn-color-gray">
            {{ item.number }} 人已回答
          </view>
        </view>
        <view class="tn-text-justify clamp-text-2 tn-padding-top-sm tn-color-gray--dark">
          {{ item.content }}
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

const questionList = ref([
  {
    title: '关于图鸟开源生态的满意度调查',
    content: '为倾听广大用户群体的心声、需求与建议，请大佬们认真填写图鸟开源生态的满意度调查问卷',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242423-assets/web-upload/6d82b929-67e4-4919-a826-c931940f5872.jpeg',
    userName: '那只猪-图鸟猪猪',
    number: '129',
  }, {
    title: '2024年年度模板开放方向调查',
    content: '这里是一大堆瞎逼逼的描述，为了凑字数，想不出来什么文案了',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1696519175593-assets/web-upload/b6b5c625-f3ba-4d0a-9506-2dc079a89997.jpeg',
    userName: '你的名字',
    number: '208',
  }, {
    title: '图鸟OA模板满意度调查',
    content: '这里是一大堆瞎逼逼的描述，为了凑字数，想不出来什么文案了',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103627542-assets/web-upload/8d373c70-a1cb-4395-86e8-7e499856d45e.jpeg',
    userName: '不许凶我吖',
    number: '236',
  }, {
    title: '凑数的文案，凑够一行超出隐藏，不喜勿喷',
    content: '这里是一大堆瞎逼逼的描述，为了凑字数，想不出来什么文案了',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103627675-assets/web-upload/7f0fc488-330d-46fa-970e-ef19756ddef4.jpeg',
    userName: '图鸟西西',
    number: '102',
  }, {
    title: '凑数的文案，凑够一行超出隐藏，不喜勿喷',
    content: '这里是一大堆瞎逼逼的描述，为了凑字数，想不出来什么文案了',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692948628847-assets/web-upload/34ed894b-97ef-4fd0-a1f1-fb7507ce3ac7.jpeg',
    userName: '猪猪打杂',
    number: '98',
  }, {
    title: '2023年年度模板开放方向调查',
    content: '这里是一大堆瞎逼逼的描述，为了凑字数，想不出来什么文案了',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg',
    userName: '抓住那只猪',
    number: '602',
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
  name: 'TemplateQuestionnaire',
  
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
  
  .oa-content{
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
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
  
  /* 背景阴影 start*/
  .content-bg {
    position: relative;
    border-radius: 15rpx;
    background-color: #FFFFFF;
    // box-shadow: 0rpx 0rpx 50rpx 0rpx rgba(0, 0, 0, 0.07);
  }
   
   /* 用户头像 start */
   .user-image {
     width: 35rpx;
     height: 35rpx;
     position: relative;
     overflow: hidden;
     border-radius: 50%;
   }
   
   .user-pic {
     background-size: cover;
     background-repeat: no-repeat;
     // background-attachment:fixed;
     background-position: top;
     border: 1rpx solid rgba(255,255,255,0.05);
     box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
     border-radius: 50%;
     overflow: hidden;
     // background-color: #FFFFFF;
   }
    
</style>
