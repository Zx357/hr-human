<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#ffffff00" :placeholder="false" customBack>
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left'></tn-icon>
        <tn-icon class='icon' name='home-capsule-fill'></tn-icon>
      </view>
    </tn-navbar>
    
    <!-- 立体头像-->
    <view class='cube' :style="'background-image: url(https://resource.tuniaokj.com/images/blogger/bg_image_1.jpg);'">
      <view class="cube__container">
        <view class="cube__container__body">
          <view class="cube__container__body__item cube__container__body__item--front" :style="{backgroundImage: `url(${userInfo.avatar[0]})`}"></view>
          <view class="cube__container__body__item cube__container__body__item--back" :style="{backgroundImage: `url(${userInfo.avatar[0]})`}"></view>
          <view class="cube__container__body__item cube__container__body__item--right" :style="{backgroundImage: `url(${userInfo.avatar[1]})`}"></view>
          <view class="cube__container__body__item cube__container__body__item--left" :style="{backgroundImage: `url(${userInfo.avatar[1]})`}"></view>
          <view class="cube__container__body__item cube__container__body__item--top" :style="{backgroundImage: `url(${userInfo.avatar[2]})`}"></view>
          <view class="cube__container__body__item cube__container__body__item--bottom" :style="{backgroundImage: `url(${userInfo.avatar[2]})`}"></view>
        </view>
      </view>
      
      
      <view class='tn-text-center tn-margin-top-lg'>
        <view class="tn-padding tn-text-bold tn-text-lg">{{ userInfo.username }}</view>
        <view class="tn-padding-bottom-xl tn-text-lg">{{ userInfo.desc }}</view>
      </view>
    </view>
    
    <!-- 消息&数据 -->
    <view class="blogger-tips-data">
      <view class="blogger-tips-data__wrap tn-bg-white">
        <view class="blogger-tips-data__info tn-flex">
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
              <view class="">
                <view class="tn-text-xxl tn-color-orange">{{ $t.number.formatNumberAddPriceUnit(tipsDataMessage.likeCount) }}</view>
              </view>
              <view class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center">
                <tn-icon name="like"></tn-icon>
                <text class="tn-padding-left-xs">爱心</text>
              </view>
            </view>
          </view>
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
              <view class="">
                <view class="tn-text-xxl tn-color-blue">{{ $t.number.formatNumberAddPriceUnit(tipsDataMessage.hotReviewsCount) }}</view>
              </view>
              <view class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center">
                <tn-icon name="message"></tn-icon>
                <text class="tn-padding-left-xs">热评</text>
              </view>
            </view>
          </view>
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
              <view class="">
                <view class="tn-text-xxl tn-color-red">{{ $t.number.formatNumberAddPriceUnit(tipsDataMessage.fansCount) }}</view>
              </view>
              <view class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center">
                <tn-icon name="vip"></tn-icon>
                <text class="tn-padding-left-xs">粉丝</text>
              </view>
            </view>
          </view>
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
              <view class="">
                <view class="tn-text-xxl tn-color-cyan">{{ $t.number.formatNumberAddPriceUnit(tipsDataMessage.focusCount) }}</view>
              </view>
              <view class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center">
                <tn-icon name="star"></tn-icon>
                <text class="tn-padding-left-xs">关注</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>      
    
    <!-- 内容 -->
    <view class="tn-flex tn-flex-direction-column tn-margin-top-sm tn-margin-bottom">
   
      <!-- 图文信息 -->
      <block v-for="(item,index) in content" :key="index">
        <view class="blogger__item">
          <view class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify__author__info">
              <view class="tn-flex tn-flex-row-center">
                <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                  <view class="">
                    <tn-avatar
                      class=""
                      shape="circle"
                      :src="item.userAvatar"
                      size="lg">
                    </tn-avatar>
                  </view>
                  <view class="tn-padding-right tn-text-ellipsis">
                    <view class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg">{{ item.userName }}</view>
                    <view class="tn-padding-right tn-padding-left-sm tn-padding-top-xs tn-color-gray">{{ item.date }}</view>
                  </view>
                </view>
              </view>
            </view>
            <view class="blogger__author__btn justify-content-item tn-flex-col-center tn-flex-row-center">
              <tn-icon class="tn-color-gray tn-text-bold tn-text-xxl" name="more-vertical"></tn-icon>
            </view>
          </view>
         
          <view class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left" @click="tn('/circlePages/details')">
            <view v-for="(label_item,label_index) in item.label" :key="label_index" class="blogger__desc__label tn-float-left tn-margin-right tn-bg-gray--light tn-round tn-text-sm tn-text-bold">
              <text class="blogger__desc__label--prefix">#</text> 
              <text class="tn-text-df">{{ label_item }}</text>
            </view>
            <text v-if="!item.label || item.label.length < 4" class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df">{{ item.desc }}</text>  
          </view>
          
          <block v-if="item.mainImage">
            <view v-if="[1,2,4].indexOf(item.mainImage.length) != -1" class="tn-padding-top-xs" @click="tn('/circlePages/details')">
              <image v-for="(image_item,image_index) in item.mainImage" :key="image_index" 
                class="blogger__main-image"
                :class="{
                  'blogger__main-image--1 tn-margin-bottom-sm': item.mainImage.length === 1,
                  'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm': item.mainImage.length === 2 || item.mainImage.length === 4
                }"
                :src="image_item"
                mode="aspectFill"
              ></image>
            </view>
            <view v-else class="tn-padding-top-xs" @click="tn('/circlePages/details')">
              <app-grid  hoverClass="none" :col="3">
                <block v-for="(image_item,image_index) in item.mainImage" :key="image_index">
                  <app-grid-item style="width: 30%;margin: 10rpx;">
                    <image
                      class="blogger__main-image blogger__main-image--3"
                      :src="image_item"
                      mode="aspectFill"
                    ></image>
                  </app-grid-item>
                </block>
              </app-grid>
            </view>
          </block>
         
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xs">
            <view class="justify-content-item tn-color-gray tn-text-center">
              <view class="">
                <tn-icon class="blogger__count-icon" name="footprint"></tn-icon> 
                <text class="tn-padding-right">{{ item.collectionCount }}</text>
                <tn-icon class="blogger__count-icon" name="message"></tn-icon>
                <text class="tn-padding-right">{{ item.commentCount }}</text>
                <tn-icon class="blogger__count-icon" name="like"></tn-icon>
                <text class="">{{ item.likeCount }}</text>
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-col-center">
              <view style="margin-right: 10rpx;margin-left: 20rpx;">
                <tn-avatar-group :lists="item.viewUser.latestUserAvatar" size="sm"></tn-avatar-group>
              </view>
              <text class="tn-color-gray">{{ item.viewUser.viewUserCount }}人</text>
            </view>
          </view>
        </view>
        
        <view class="tn-strip-bottom" v-if="index != content.length - 1"></view>
      </block>
      
      <view class="tn-strip-bottom"></view>
      
      <!-- 广告 -->
      <view class="blogger__item" @click="tn('/circlePages/advertise')">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-bottom">
          <view class="justify-content-item">
            <view class="tn-flex tn-flex-col-center tn-flex-row-left">
              <view class="ad-pic tn-shadow-blur" style="background-image:url('https://resource.tuniaokj.com/images/logo/logo2.png')">
                <view class="ad-image">
                </view>
              </view>
              <view class="tn-padding-right" style="max-width: 65vw;">
                <view class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg">抓住那只猪科技有限公司</view>
                <view class="tn-padding-right tn-padding-left-sm tn-color-gray tn-text-ellipsis">我们都是好好孩子</view>
              </view>
              
            </view>
          </view>
          <view class="tn-color-gray">广告</view>
        </view>
        <swiper class="swiper" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="500" :circular="true">
          <swiper-item v-for="(item, index) in adList" :key="index">
            <image class="swiper-image" :src="item.image" mode="aspectFill"></image>
          </swiper-item>
        </swiper>
      </view>
      
      <view class="tn-strip-bottom"></view>
      
      <!-- 图文信息 -->
      <block v-for="(item,index) in content" :key="index">
        <view class="blogger__item">
          <view class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify__author__info">
              <view class="tn-flex tn-flex-row-center">
                <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                  <view class="">
                    <tn-avatar
                      class=""
                      shape="circle"
                      :src="item.userAvatar"
                      size="lg">
                    </tn-avatar>
                  </view>
                  <view class="tn-padding-right tn-text-ellipsis">
                    <view class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg">{{ item.userName }}</view>
                    <view class="tn-padding-right tn-padding-left-sm tn-padding-top-xs tn-color-gray">{{ item.date }}</view>
                  </view>
                </view>
              </view>
            </view>
            <view class="blogger__author__btn justify-content-item tn-flex-col-center tn-flex-row-center">
              <tn-icon class="tn-color-gray tn-text-bold tn-text-xxl" name="more-vertical"></tn-icon>
            </view>
          </view>
         
          <view class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left" @click="tn('/circlePages/details')">
            <view v-for="(label_item,label_index) in item.label" :key="label_index" class="blogger__desc__label tn-float-left tn-margin-right tn-bg-gray--light tn-round tn-text-sm tn-text-bold">
              <text class="blogger__desc__label--prefix">#</text> 
              <text class="tn-text-df">{{ label_item }}</text>
            </view>
            <text v-if="!item.label || item.label.length < 4" class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df">{{ item.desc }}</text>  
          </view>
          
          <block v-if="item.mainImage">
            <view v-if="[1,2,4].indexOf(item.mainImage.length) != -1" class="tn-padding-top-xs" @click="tn('/circlePages/details')">
              <image v-for="(image_item,image_index) in item.mainImage" :key="image_index" 
                class="blogger__main-image"
                :class="{
                  'blogger__main-image--1 tn-margin-bottom-sm': item.mainImage.length === 1,
                  'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm': item.mainImage.length === 2 || item.mainImage.length === 4
                }"
                :src="image_item"
                mode="aspectFill"
              ></image>
            </view>
            <view v-else class="tn-padding-top-xs" @click="tn('/circlePages/details')">
              <app-grid  hoverClass="none" :col="3">
                <block v-for="(image_item,image_index) in item.mainImage" :key="image_index">
                  <app-grid-item style="width: 30%;margin: 10rpx;">
                    <image
                      class="blogger__main-image blogger__main-image--3"
                      :src="image_item"
                      mode="aspectFill"
                    ></image>
                  </app-grid-item>
                </block>
              </app-grid>
            </view>
          </block>
         
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xs">
            <view class="justify-content-item tn-color-gray tn-text-center">
              <view class="">
                <tn-icon class="blogger__count-icon" name="footprint"></tn-icon> 
                <text class="tn-padding-right">{{ item.collectionCount }}</text>
                <tn-icon class="blogger__count-icon" name="message"></tn-icon>
                <text class="tn-padding-right">{{ item.commentCount }}</text>
                <tn-icon class="blogger__count-icon" name="like"></tn-icon>
                <text class="">{{ item.likeCount }}</text>
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-col-center">
              <view style="margin-right: 10rpx;margin-left: 20rpx;">
                <tn-avatar-group :lists="item.viewUser.latestUserAvatar" size="sm"></tn-avatar-group>
              </view>
              <text class="tn-color-gray">{{ item.viewUser.viewUserCount }}人</text>
            </view>
          </view>
        </view>
        
        <view class="tn-strip-bottom" v-if="index != content.length - 1"></view>
      </block>
       
          
    </view>
    
    <!-- 悬浮按钮-->
    <view class="tn-flex tn-flex-row-between tn-footerfixed">
      <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
        <tn-button
          bg-color="#00FFC6"
          :custom-style="{padding:'40rpx 0'}"
          width="90%"
          :fontSize="28"
          text-color="#000000"
          shape="round"
          @click="tn('')"
        >
          <text class="">关 注</text>
        </tn-button>
      </view>
      <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
        <tn-button
          bg-color="#FFF00D"
          :custom-style="{padding:'40rpx 0'}"
          width="90%"
          :fontSize="28"
          text-color="#000000"
          shape="round"
          open-type="share"
        >
          <text class="">分 享</text>
        </tn-button>
      </view>
    </view>
    
    <view class='tn-tabbar-height'></view>
    
  </view>
</template>

<script setup>
import { onLoad, onReady, onShow, onHide } from '@dcloudio/uni-app'
import { ref, reactive, nextTick } from 'vue'
import { useGoBack } from '@/libs/composables'
const { goBack } = useGoBack()
defineOptions({
  name: 'TemplateBlogger2'
})

// 内容默认隐藏显示的高度
const contentHideShowHeight = ref(0)

const userInfo = reactive({
  avatar: [
    'https://resource.tuniaokj.com/images/blogger/blogger_avatar_1.jpeg',
    'https://resource.tuniaokj.com/images/blogger/blogger_avatar_2.jpeg',
    'https://resource.tuniaokj.com/images/blogger/blogger_avatar_3.jpeg',
  ],
  username: '菜的一撇的北北吖',
  desc: '你是不是傻，菜的一撇的北北'
})

const tipsDataMessage = reactive({
  latestMessageUserAvatar: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg',
  messageCount: 3,
  likeCount: 1290,
  hotReviewsCount: 896,
  fansCount: 962,
  focusCount: 86
})

const content = ref([
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '菜的一撇的北北吖',
    date: '2021年12月20日',
    label: ['开源','创意','UI框架'],
    desc: '开源可商用组件，助你开发酷炫UI一臂之力',
    content: '基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了',
    viewUser: {
      latestUserAvatar: [
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'},
      ],
      viewUserCount: 62
    },
    collectionCount: 439,
    commentCount: 46,
    likeCount: 83
  },
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '菜的一撇的北北吖',
    date: '2021年12月20日',
    label: ['开源','创意','UI框架'],
    desc: '开源可商用组件，助你开发酷炫UI一臂之力',
    content: '基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了',
    mainImage:[
      'https://resource.tuniaokj.com/images/blogger/content_1.jpeg'
    ],
    viewUser: {
      latestUserAvatar: [
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'},
      ],
      viewUserCount: 12
    },
    collectionCount: 902,
    commentCount: 64,
    likeCount: 83
  },
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '菜的一撇的北北吖',
    date: '2021年12月20日',
    label: [],
    desc: '',
    content: '',
    mainImage:[
      'https://resource.tuniaokj.com/images/shop/computer2.jpg',
      'https://resource.tuniaokj.com/images/shop/prototype2.jpg',
    ],
    viewUser: {
      latestUserAvatar: [
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},
      ],
      viewUserCount: 56
    },
    collectionCount: 431,
    commentCount: 26,
    likeCount: 84
  },
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '菜的一撇的北北吖',
    date: '2021年12月20日',
    label: ['开源','创意'],
    desc: '开源可商用组件',
    content: '基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了 基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了',
    mainImage:[
      'https://resource.tuniaokj.com/images/swiper/swiper2.jpg',
      'https://resource.tuniaokj.com/images/swiper/swiper3.jpg',
      'https://resource.tuniaokj.com/images/swiper/swiper4.jpg',
    ],
    viewUser: {
      latestUserAvatar: [
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},
      ],
      viewUserCount: 231
    },
    collectionCount: 780,
    commentCount: 89,
    likeCount: 82
  },
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '菜的一撇的北北吖',
    date: '2021年12月20日',
    label: ['开源','链接'],
    desc: 'https://www.yuque.com/tuniao',
    mainImage:[
      'https://resource.tuniaokj.com/images/shop/watch1.jpg',
      'https://resource.tuniaokj.com/images/shop/watch2.jpg',
      'https://resource.tuniaokj.com/images/shop/pillow2.jpg',
      'https://resource.tuniaokj.com/images/shop/pillow.jpg',
    ],
    viewUser: {
      latestUserAvatar: [
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},
      ],
      viewUserCount: 28
    },
    collectionCount: 432,
    commentCount: 33,
    likeCount: 12
  },
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '菜的一撇的北北吖',
    date: '2021年12月20日',
    label: ['开源','创意'],
    desc: '开源可商用组件',
    mainImage:[
      'https://resource.tuniaokj.com/images/blogger/y11.jpg',
      'https://resource.tuniaokj.com/images/blogger/y33.jpg',
      'https://resource.tuniaokj.com/images/blogger/y22.jpg',
      'https://resource.tuniaokj.com/images/blogger/y44.jpg',
      'https://resource.tuniaokj.com/images/blogger/y55.jpg',
    ],
    viewUser: {
      latestUserAvatar: [
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},
        {src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},
      ],
      viewUserCount: 65
    },
    collectionCount: 265,
    commentCount: 22,
    likeCount: 62
  }
])

const adList = ref([
  {image: 'https://resource.tuniaokj.com/images/swiper/ad1.jpg'},
  {image: 'https://resource.tuniaokj.com/images/swiper/ad2.jpg'},
  {image: 'https://resource.tuniaokj.com/images/swiper/ad3.jpg'},
  {image: 'https://resource.tuniaokj.com/images/swiper/ad4.jpg'},
  {image: 'https://resource.tuniaokj.com/images/swiper/ad5.jpg'}
])
const adAutoplay = ref(false)

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
}

// 处理内容，给内容添加对应的标识信息
const initContentData = () => {
  content.value.forEach((item, index) => {
    item.hideContent = false
    item.showAllContent = false
    item.contentContainerHeight = 0
    item.contentContainerInit = false
  })
}

// 获取内容容器的信息
const getContentRectInfo = () => {
  const query = uni.createSelectorQuery().in(getCurrentInstance().proxy)
  content.value.forEach((item, index) => {
    if (item?.content) {
      query.select(`#blogger__content--${index}`).boundingClientRect()
    }
  })
  query.exec(res => {
    if (!res) {
      setTimeout(() => {
        getContentRectInfo()
      }, 10)
      return
    }
    res.map((item) => {
      const id = item.id
      const idIndex = /blogger__content--(\d)/.exec(id)[1]
      let contentItem = content.value[idIndex]
      contentItem.hideContent = item.height > contentHideShowHeight.value
      contentItem.showAllContent = false
      contentItem.contentContainerHeight = item.height
      contentItem.contentContainerInit = true
    })
  })
}

// 切换内容的显示与隐藏
const switchContentShowStatus = (index) => {
  const contentItem = content.value[index]
  contentItem.showAllContent = !contentItem.showAllContent
}

// 生命周期
onLoad(() => {
  initContentData()
  contentHideShowHeight.value = uni.upx2px(56) * 3
})

onReady(() => {
  nextTick(() => {
    getContentRectInfo()
  })
})

onShow(() => {
  adAutoplay.value = true
})

onHide(() => {
  adAutoplay.value = false
})

import { getCurrentInstance } from 'vue'
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
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  $cube-size: 120rpx;
  $cube-split: 60rpx;
  
  /* 立体头像 start*/
  
  .cube {
    background: #fff;
    background-repeat: no-repeat;
    background-size: cover;
    height: 550rpx;
    display: flex;
    justify-content: center;
    padding-top: 40rpx;
    overflow: hidden;
    position: relative;
    flex-direction: column;
    align-items: center;
    font-weight: 300;
    
    &__container {
      margin-top: 180rpx;
      position: relative;
      width: $cube-size;
      height: $cube-size;
      -webkit-perspective: 500px;
      perspective: 500px;
      
      &:before {
        content: '';
        width: $cube-size;
        height: $cube-size;
        position: absolute;
        background-color: #3c6496;
        filter: blur(60px);
        opacity: .8;
      }
      
      &__body {
        position: absolute;
        width: 100%;
        height: 100%;
        transform-style: preserve-3d;
        transform: translateZ(-75px);
        animation: cubeFrame 35s cubic-bezier(0.36, -0.03, 0.46, 0.95) infinite alternate;
        will-change: transform;
        
        &__item {
          position: absolute;
          display: block;
          display: flex;
          align-items: center;
          justify-content: center;
          width: $cube-size;
          height: $cube-size;
          font-size: 30px;
          background-repeat: no-repeat;
          background-size: cover;
          
          &--front {
            transform: rotateY(0deg) translateZ($cube-split);
            background-color: #BEEBFF;
          }
          &--back {
            transform: rotateX(180deg) translateZ($cube-split);
            background-color: #BEEBFF;
          }
          &--right {
            transform: rotateY(90deg) translateZ($cube-split);
            background-color: #BEEBFF;
          }
          &--left {
            transform: rotateY(-90deg) translateZ($cube-split);
            background-color: #BEEBFF;
          }
          &--top {
            transform: rotateX(90deg) translateZ($cube-split);
            background-color: #BEEBFF;
          }
          &--bottom {
            transform: rotateX(-90deg) translateZ($cube-split);
            background-color: #BEEBFF;
          }
        }
      }
    }
  }
  
  @keyframes cubeFrame {
    10% {
      transform: translateZ(-75px) rotateX(40deg) rotateY(60deg);
    }
    15% {
      transform: translateZ(-75px) rotateX(80deg) rotateY(20deg);
    }
    20% {
      transform: translateZ(-75px) rotateX(-180deg) rotateY(-70deg);
    }
    30% {
      transform: translateZ(-75px) rotateX(90deg) rotateY(180deg);
    }
    40% {
      transform: translateZ(-75px) rotateX(-10deg) rotateY(-140deg);
    }
    45% {
      transform: translateZ(-75px) rotateX(-100deg) rotateY(20deg);
    }
    55% {
      transform: translateZ(-75px) rotateX(-10deg) rotateY(-35deg);
    }
    60% {
      transform: translateZ(-75px) rotateX(180deg) rotateY(360deg);
    }
    70% {
      transform: translateZ(-75px) rotateX(-180deg) rotateY(-360deg);
    }
    80% {
      transform: translateZ(-75px) rotateX(45deg) rotateY(-70deg);
    }
    90% {
      transform: translateZ(-75px) rotateX(-45deg) rotateY(70deg);
    }
    100% {
      transform: translateZ(-75px) rotateX(-360deg) rotateY(360deg);
    }
  }
  /* 立体头像 end*/
  
  /* 信息提示 start */
  .blogger-tips-data {
    background-color: #F8F7F3;
    
    &__wrap {
      border-radius: 40rpx 40rpx 0 0;
    }
    
    &__message {
      padding-top: 60rpx;
      
      &__container {
        padding: 5rpx;
        border-radius: 100rpx;
      }
      
      &__avatar {
        margin: 6rpx 0 0 6rpx;
      }
    }
    
    &__info {
      padding: 40rpx 0 0 0;
    }
  }
  /* 信息提示 end */
  
  /* 文章内容 start*/
  .blogger {
    &__item {
      padding: 30rpx;
    }
    
    &__author {
      &__btn {
        margin-right: -12rpx;
        opacity: 0.5;
      }
    }
    
    &__desc {
      line-height: 55rpx;
      
      &__label {
        padding: 0 20rpx;
        margin: 0rpx 18rpx 0 0;
        
        &--prefix {
          color: #00FFC8;
          padding-right: 10rpx;
        }
      }
    }
    
    &__content {
      margin-top: 18rpx;
      padding-right: 18rpx;
      
      &__data {
        line-height: 46rpx;
        text-align: justify;
        overflow: hidden;
        transition: all 0.25s ease-in-out;
      }
      
      &__status {
        margin-top: 10rpx;
        font-size: 26rpx;
        color: #82B2FF;
      }
    }
    
    &__main-image {
      border-radius: 16rpx;
      
      &--1 {
        max-width: 80%;
        max-height: 300rpx;
      }
      
      &--2 {
        max-width: 260rpx;
        max-height: 260rpx;
      }
      
      &--3 {
        height: 212rpx;
        width: 100%;
      }
    }
    
    &__count-icon {
      font-size: 40rpx;
      padding-right: 5rpx;
    }
    
    &__ad {
      width: 100%;
      height: 500rpx;
      transform: translate3d(0px, 0px, 0px) !important;
      
      ::v-deep .uni-swiper-slide-frame {
        transform: translate3d(0px, 0px, 0px) !important;
      }
      .uni-swiper-slide-frame {
        transform: translate3d(0px, 0px, 0px) !important;
      }
      
      &__item {
        position: absolute;
        width: 100%;
        height: 100%;
        transform-origin: left center;
        transform: translate3d(100%, 0px, 0px) scale(1) !important;
        transition: transform 0.25s ease-in-out;
        z-index: 1;
        
        &--0 {
          transform: translate3d(0%, 0px, 0px) scale(1) !important;
          z-index: 4;
        }
        &--1 {
          transform: translate3d(13%, 0px, 0px) scale(0.9) !important;
          z-index: 3;
        }
        &--2 {
          transform: translate3d(26%, 0px, 0px) scale(0.8) !important;
          z-index: 2;
        }
      }
      
      &__content {
        border-radius: 40rpx;
        width: 640rpx;
        height: 500rpx;
        overflow: hidden;
      }
      
      &__image {
        width: 100%;
        height: 100%;
      }
    }
  }
  /* 文章内容 end*/
   
   /* 间隔线 start*/
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid rgba(241, 241, 241, 0.8);
  }
   /* 间隔线 end*/
  /* 底部悬浮按钮 start*/ 
  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }
  .tn-footerfixed {
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    width: 100%;
    bottom: calc(30rpx + env(safe-area-inset-bottom));
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
    
  }
  /* 底部悬浮按钮 end*/
  
  /* 广告内容 start */
  .ad-image{
    width: 80rpx;
    height: 80rpx;
    position: relative;
  }
  .ad-pic{
    background-size: cover;
    background-repeat:no-repeat;
    background-position:top;
    border-radius: 20%;
  }
  /* 自定义导航栏内容 end */
  
  /* swiper start */
  .swiper {
    position: relative;
    width: 100%;
    height: 360rpx;

    .swiper-image {
      position: relative;
      width: 100%;
      height: 100%;
      border-radius: 16rpx;
    }
  }
  /* swiper end */
</style>
