<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">请假记录</text>
      </view>
    </tn-navbar>
    
    <view class="tn-padding-bottom-xl" :style="{paddingTop: vuex_custom_bar_height + 10 +'px'}">
      <view class="">
        <view class="content-bg tn-margin tn-padding" style="position: relative;" v-for="(item, index) in noticeList" :key="index" @click="tn('')">
          <view :class="['oa-' + item.color]" style="width: 15rpx;height: 100%;position: absolute;top: 0;left:0;border-radius: 15rpx 0 0 15rpx;font-size: 32rpx;">
          </view>
          <view class="tn-margin-left-xs">
            <view class="tn-flex tn-flex-col-center tn-flex-row-between" style="margin-top: -6rpx;">
              <view class="justify-content-item tn-flex tn-flex tn-flex-col-center tn-text-lg tn-text-bold">
                <view class="justify-content-item">
                  {{ item.type }}
                </view>
                <view class="justify-content-item">
                  <view class="tag-state" :class="['tn-bg-' + item.color + '--light oa-' + item.color]" style="font-size: 16rpx;">
                    <text class="">{{ item.state }}</text>
                  </view>
                </view>
              </view>
              <view class="justify-content-item tn-color-gray">
                {{ item.date }}
              </view>
            </view>
            <view class="tn-text-justify clamp-text-2 tn-padding-top-xs tn-color-gray--dark">
             事由：{{ item.title }}
            </view>
            <view class="tn-text-justify clamp-text-1 tn-padding-top-xs tn-color-gray--dark">
             开始：{{ item.date }}
            </view>
            <view class="tn-text-justify clamp-text-1 tn-padding-top-xs tn-color-gray--dark">
             结束：{{ item.date }}
            </view>
            <!-- <view class="tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between tn-padding-top-sm">
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
                {{ item.date }}
              </view>
            </view> -->
          </view>
          
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

const noticeList = ref([
  {
    color: 'orangered',
    type: '单纯想请假',
    state: '已拒绝',
    title: '给自己放个假',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242423-assets/web-upload/6d82b929-67e4-4919-a826-c931940f5872.jpeg',
    userName: '那只猪-图鸟人事部',
    date: '2024-06-26 10:12',
  }, {
    color: 'blue',
    type: '事假',
    state: '审批中',
    title: '请假一天用于搬家',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1696519175593-assets/web-upload/b6b5c625-f3ba-4d0a-9506-2dc079a89997.jpeg',
    userName: '你的名字',
    date: '2024-05-06 14:02',
  }, {
    color: 'green',
    type: '年假',
    state: '已通过',
    title: '回家过年哒，祝大家新年快乐',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103627542-assets/web-upload/8d373c70-a1cb-4395-86e8-7e499856d45e.jpeg',
    userName: '不许凶我吖',
    date: '2024-02-05 09:56',
  }, {
    color: 'blue',
    type: '事假',
    state: '审批中',
    title: '没有理由',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1694103627675-assets/web-upload/7f0fc488-330d-46fa-970e-ef19756ddef4.jpeg',
    userName: '图鸟西西',
    date: '2024-01-06 17:59',
  }, {
    color: 'green',
    type: '事假',
    state: '已通过',
    title: '和朋友出去吃饭',
    userImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692948628847-assets/web-upload/34ed894b-97ef-4fd0-a1f1-fb7507ce3ac7.jpeg',
    userName: '猪猪打杂',
    date: '2023-12-26 16:26',
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
  name: 'TemplateApproval',
  
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
    background-color: #1D2541;
  }
  .oa-blue{
    color: #4B98FE;
    background-color: #4B98FE;
  }
  .oa-orangeyellow{
    color: #FFAC00;
    background-color: #FFAC00;
  }
  .oa-green{
    color: #00D05E;
    background-color: #00D05E;
  }
  .oa-orange{
    color: #FE871B;
    background-color: #FE871B;
  }
  .oa-cyan{
    color: #00C8B0;
    background-color: #00C8B0;
  }
  .oa-indigo{
    color: #00B9FE;
    background-color: #00B9FE;
  }
  .oa-orangered{
    color: #FB6A67;
    background-color: #FB6A67;
  }
  .oa-purple{
    color: #957BFE;
    background-color: #957BFE;
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
    position: fixed;
    background-color: rgba(255,255,255,1);
    top: 0;
    width: 100%;
    transition: all 0.25s ease-out;
    z-index: 100;
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
   
   /* 标签 start*/
   .tag-state {
     display: inline-block;
     padding: 8rpx 12rpx 6rpx;
     margin: 12rpx 15rpx 15rpx 10rpx;
     border-radius: 10rpx;
   }
    
</style>
