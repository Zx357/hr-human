<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">每日词条</text>
      </view>
    </tn-navbar>
    
    
    <view class="">
      
      <!-- 卡片轮播，图片均为凶姐本人照片，请勿将图片用于奇奇怪怪的地方，谢谢大佬们-->
      <view class="swiper" style="height:90vh;margin: 0 60rpx;" :style="{paddingTop: vuex_custom_bar_height + 50 +'px'}" @click="tn('/pages/index?index=1')">
        <swiper style="height: 85%;" :vertical="true" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="500" :circular="true">
          <swiper-item v-for="(item, index) in list" :key="index">
            <image :src="item.image" mode="aspectFill" style="width: 100%; height: 100%; border-radius: 24rpx;"></image>
          </swiper-item>
        </swiper>
        <!-- <tn-stack-swiper :list="list" direction="vertical" height="85%" :switchRate="20" :scaleRate="0.1" :translateRate="7.2" :opacityRate="20"></tn-stack-swiper> -->
      </view>
      
      
      <view class="" @click="tn('/momentPages/catalog')">
        词条目录  卡片式，学习那个板块才放   抖音互动+视频改为图片背景式\
      </view>
      
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onReady, onShow, onHide } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 数据
const list = ref([
  {
    name: '不许凶我吖',
    sex: 'sex-female',
    color: 'red',
    age: '23',
    distance: '22.8',
    introduce: '晚风轻踩着云朵，月亮在贩售快乐',
    place: '浙江',
    cons: '双鱼座',
    job: 'UI设计',
    image: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116291-assets/web-upload/c4c0d31c-88ee-42c7-99ba-139ef206aaed.jpeg'
  },
  {
    name: '抓住那只猪',
    sex: 'sex-female',
    color: 'red',
    age: '24',
    distance: '16.2',
    introduce: '生活需要一些仪式感，这跟矫情无关',
    place: '广州',
    cons: '处女座',
    job: 'UE设计',
    image: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1671437658295-assets/web-upload/05620a1f-452e-4a14-9d30-f6c66ee4be1c.jpeg'
  },
  {
    name: '可我会像',
    sex: 'sex-male',
    color: 'blue',
    age: '26',
    distance: '12.9',
    introduce: '你从银河背后靠近我，我与星辉一同为你沉沦',
    place: '广州',
    cons: '水瓶座',
    job: '全栈开发',
    image: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699075-assets/web-upload/aaee3258-46b7-43ae-aaf2-02f3dff5f960.jpeg'
  }
])
const autoplay = ref(false)
const swiperContainerHeight = ref(0)

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
}

// 获取元素矩形信息
const tGetRect = (selector) => {
  return new Promise((resolve) => {
    const query = uni.createSelectorQuery()
    query.select(selector).boundingClientRect()
    query.exec((res) => {
      resolve(res[0] || {})
    })
  })
}

// 初始化轮播图容器
const initSwiperContainer = () => {
  // 获取底部tabbar信息
  tGetRect('.tabbar').then((res) => {
    if (!res || !res.height) {
      setTimeout(() => {
        initSwiperContainer()
      }, 10)
      return
    }
    // 获取系统信息
    const systemInfo = uni.getSystemInfoSync()
    swiperContainerHeight.value = systemInfo.safeArea.height - res.height - 10
  })
}

// 生命周期
onReady(() => {
  nextTick(() => {
    initSwiperContainer()
  })
})

onShow(() => {
  autoplay.value = true
})

onHide(() => {
  autoplay.value = false
})
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
  
  /* 卡片轮播图 start */
  .swiper {
    border-radius: 10rpx;
    overflow: hidden;
  }
  /* 轮播图 end */
</style>
