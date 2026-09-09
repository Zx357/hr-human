<template>
  <view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#ffffff00" :placeholder="false" customBack>
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left"></tn-icon>
        <tn-icon class='icon' name="home-capsule-fill"></tn-icon>
      </view></template>
    </tn-navbar>
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">

    
      <block v-for="(item, index) in navList" :key="index">
        <view class="nav_title--wrap tn-margin-bottom-sm">
          <view class="nav_title tn-main-gradient-aquablue">{{ titleFilter(item.title) }}</view>
        </view>
        
        <view class='nav-list'>
          <block v-for="(content_item, content_index) in item.list" :key="content_index">
            <navigator
              open-type="navigate"
              hover-class='none'
              :url="content_item.url"
              class="nav-list-item tn-shadow-blur tn-cool-bg-image tn-main-gradient-aquablue--light"
            >
            <!-- :class="[
              getRandomCoolBg(content_index)
            ]" -->
              <view class="nav-link tn-color-blue">
                <view class='title'>{{ content_item.title }}</view>
                <!-- <view class='author'> <tn-icon class="tn-padding-right-xs" name="code"></tn-icon> {{ content_item.author }}</view> -->
              </view>
              <!-- <view class="icon">
                <tn-icon :name="content_item.icon"></tn-icon>
              </view> -->
            </navigator>
          </block>
        </view>
      </block>
    
    </view>
    
    
    <!-- 回到首页悬浮按钮-->
    <nav-index-button></nav-index-button>
    
    
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()
  import vipListData from '@/libs/navigation/navigation.js'
  import NavIndexButton from '@/libs/components/nav-index-button.vue'
  
  // nav菜单列表
  const navList = ref(vipListData.data)
  
  // 标题过滤器
  function titleFilter(value) {
    if (value.length === 0) {
      return ''
    }
    let newString = ''
    for (let i = 0; i < value.length; i++) {
      if (i !== 0) {
        newString += ' / '
      }
      newString += value[i]
    }
    return newString
  }
  
  function getRandomCoolBg() {
    // 注意：需要确保 $t 对象可用
    // return this.$t.color.getRandomCoolBgClass()
    return ''
  }
</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    width: 100%;
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
    
    &:before {
      content: " ";
      width: 1rpx;
      height: 110%;
      position: absolute;
      top: 22.5%;
      left: 0;
      right: 0;
      margin: auto;
      transform: scale(0.5);
      transform-origin: 0 0;
      pointer-events: none;
      box-sizing: border-box;
      opacity: 0.7;
      background-color: #FFFFFF;
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
  
  /* 标题start */
  .nav_title {
    -webkit-background-clip: text;
    color: transparent;
    
    &--wrap {
      position: relative;
      display: flex;
      height: 120rpx;
      font-size: 46rpx;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      background-image: url(https://resource.tuniaokj.com/images/title_bg/title44.png);
      background-size: cover;
    }
  }
  /* 标题end */
  
  /* 组件导航列表 start*/
  .nav-list {
    display: flex;
    flex-wrap: wrap;
    padding: 0rpx 12rpx 0rpx;
    justify-content: space-between;
    
    /* 列表元素 start */
    .nav-list-item {
      padding: 0rpx 30rpx 15rpx 30rpx;
      border-radius: 12rpx;
      width: 45%;
      margin: 0 18rpx 40rpx;
      background-size: cover;
      background-position: center;
      position: relative;
      z-index: 99;
      
      
      
      /* 元素标题 start */
      .nav-link {
        font-size: 32rpx;
        text-transform: capitalize;
        padding: 0 0 10rpx 0;
        position: relative;
        
        .title {
          font-weight: bold;
          font-size: 36rpx;
          // color: #FFFFFF;
          margin-top: 30rpx;
          text-align: center;
        }
        .author {
          font-size: 25rpx;
          // color: #FFFFFF;
          margin-top: 10rpx;
          margin-left: -10rpx;
          text-align: center;
        }
      }
      /* 元素标题 end */
      
      /* 元素图标 start */
      .icon {
        font-variant: small-caps;
        position: absolute;
        top: 20rpx;
        right: 50rpx;
        left: 37%;
        width: 90rpx;
        height: 90rpx;
        line-height: 90rpx;
        margin: 0;
        padding: 0;
        display: inline-flex;
        text-align: center;
        justify-content: center;
        vertical-align: middle;
        font-size: 50rpx;
        color: #FFFFFF;
        white-space: nowrap;
        opacity: 0.9;
        background-color: rgba(0, 0, 0, 0.05);
        background-size: cover;
        background-position: 50%;
        border-radius: 5000rpx;
      }
      /* 元素图标 end */
    }
    /* 列表元素 end */
  }
  /* 组件导航列表 end*/
</style>
