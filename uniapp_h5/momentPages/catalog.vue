<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
      <tn-navbar customBack :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
        <template #back><view class='tn-custom-nav-bar__back'
          @click="goBack">
          <tn-icon name="left-arrow" class='icon'></tn-icon>
        </view></template>
        <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
          <text class="tn-text-bold tn-text-xl" style="color: #0E2D6D;">词典目录</text>
        </view>
      </tn-navbar>
    
    <view class="search-fixed" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin">
        
        <view class="justify-content-item align-content-item" style="width: 100vw;">
          <view class="tn-flex tn-flex-col-center tn-bg-white" style="border: 2rpx solid #3467C2;border-radius: 100rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 90%;">
            <tn-icon name="search" class="justify-content-item tn-padding-right-xs tn-text-lg" style="color: #3467C2;"></tn-icon>
            <input class="justify-content-item" placeholder="请输入搜索关键词" name="input" placeholder-style="color:#3467C280" style="width: 90%;color: #0E2D6D;"></input>
          </view>
        </view>
        
        <view class="align-content-item">
          <view class="justify-content-item tn-text-center">
            <tn-button
              bg-color="#3467C2"
              :custom-style="{padding:'20rpx 20rpx'}"
              width="150rpx"
              :fontSize="28"
              text-color="#FFFFFF"
              shape="round"
              @tap=""
            >
              <text class="">搜 索</text>
            </tn-button>
          </view>
        </view>
        
      </view>
    </view>
    
    <view class="tn-padding-bottom-xl" :style="{paddingTop: vuex_custom_bar_height + 70 + 'px'}">
      
      <tn-index-list :data="listData">
        <template #default="{ data }">
          <view class="list-data" @click="tn('/momentPages/dictionary')">
            <view class="info">
              <view class="username">{{ data.username }}</view>
            </view>
          </view>
        </template>
      </tn-index-list>
      
      
    </view>
    
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { useStore } from 'vuex'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  
  const store = useStore()
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()
  
  // 索引列表数据
  const listData = ref({
    a: {
      title: 'A',
      data: [
        {
          id: 0,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/1.png',
          username: '图鸟UI-总监',
          office: '高级设计总监',
          star: true,
        },
        {
          id: 1,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/2.png',
          username: '图鸟UI-总监',
          office: '高级设计总监',
        },
        {
          id: 2,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/3.png',
          username: '图鸟UI-总监',
          office: '高级设计总监',
        },
      ],
    },
    c: {
      title: 'C',
      data: [
        {
          id: 3,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/4.png',
          username: '图鸟UI-总监',
          office: '高级设计总监',
        },
        {
          id: 4,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/5.png',
          username: '图鸟UI-总监',
          office: '高级设计总监',
        },
        {
          id: 5,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/6.png',
          username: '图鸟UI-总监',
          office: '高级设计总监',
        },
      ],
    },
    '#': {
      title: '#',
      data: [
        {
          id: 6,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/7.png',
          username: '图鸟UI-打杂',
          office: '高级打杂',
        },
        {
          id: 7,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/8.png',
          username: '图鸟UI-打杂',
          office: '高级打杂',
        },
        {
          id: 8,
          avatar: 'https://tnuiimage.tnkjapp.com/avatar/normal/9.png',
          username: '图鸟UI-打杂',
          office: '高级打杂',
        },
      ],
    },
  })
  
  // 跳转
  const tn = (e) => {
    if (!e) return
    uni.navigateTo({
      url: e,
    })
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
    // background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  // 搜索固定
  .search-fixed{
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    background-color: #FFFFFF;
    top: 0;
    width: 100%;
    transition: all 0.25s ease-out;
    z-index: 100;
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
  
  /* 列表数据样式 start */
  .list-data {
    display: flex;
    padding: 30rpx;
    
    .image {
      width: 90rpx;
      height: 90rpx;
      border-radius: 15rpx;
    }
    .info {
      margin-left: 20rpx;
      line-height: 1;
      flex: 1;
      .username {
        font-size: 34rpx;
        font-weight: bold;
      }
      .office {
        margin-top: 18rpx;
        font-size: 26rpx;
      }
    }
  }
  /* 列表数据样式 end */
  
</style>
