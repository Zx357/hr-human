<template>
  <view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <view class="navbar-fixed">
      <tn-navbar :bottom-shadow="false" fixed bg-color="#ffffff00" :placeholder="false">
      <template #back><view class="tn-custom-nav-bar__back">
        <tn-icon class="icon" name="left" @click="goBack"></tn-icon>
        <tn-icon
          class="icon"
          name="message-fill"
          @click="tn('/momentPages/message')"
        ></tn-icon>
      </view></template>
    </tn-navbar>
    </view>

    <!-- 立体头像-->
    <view
      class="top-bg"
      :style="'background-image: url(https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664015047023-assets/web-upload/147b0b7f-8253-4b92-bc1d-e28db7f54096.jpeg);'"
    >
      <view
        class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center"
      >
        <view class="justify-content-item tn-flex-row-center">
          <view class="logo-pic tn-shadow">
            <view class="logo-image">
              <view
                class="tn-shadow-blur"
                style="
                  background-image: url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg');
                  width: 110rpx;
                  height: 110rpx;
                  background-size: cover;
                "
              >
              </view>
            </view>
          </view>
        </view>
        <view class="justify-content-item">
          <view class="tn-flex tn-flex-col-center">
            <view class="tn-color-white">
              <view class="tn-text-center tn-text-lg tn-text-bold tn-margin-sm">
                不许凶我吖
              </view>
              <view class="tn-text-ellipsis tn-text-sm">
                <text class="">请签收来自秋天的浪漫</text>
                <tn-icon class="tn-padding-xs" name="edit"></tn-icon>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 消息&数据 -->
    <view class="" style="margin-top: -80rpx">
      <view class="tn-bg-white" style="border-radius: 40rpx 40rpx 0 0">
        <view class="tn-flex tn-flex-row-center tn-padding-top-xl">
          <view
            class="tn-flex tn-flex-col-center tn-bg-gray--light"
            style="border-radius: 100rpx"
            @click="tn('/momentPages/message')"
          >
            <view class="" style="padding: 10rpx">
              <view class="message-pic">
                <view
                  class="message-image"
                  style="
                    background-image: url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1671437658295-assets/web-upload/05620a1f-452e-4a14-9d30-f6c66ee4be1c.jpeg');
                    width: 50rpx;
                    height: 50rpx;
                    background-size: cover;
                  "
                >
                </view>
              </view>
            </view>
            <view class="" style="padding: 10rpx 30rpx 10rpx 10rpx">
              <view class="tn-flex tn-flex-row-between tn-flex-col-between">
                <view class="justify-content-item">
                  <text class="tn-color-bdhook tn-text-df">3 条新消息</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view class="tn-padding-top-lg tn-flex">
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view
              class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center"
            >
              <view class="">
                <view class="tn-text-xxl tn-color-orange">{{
                  formatNumberAddPriceUnit(tipsDataMessage.likeCount)
                }}</view>
              </view>
              <view
                class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center"
              >
                <tn-icon name="like"></tn-icon>
                <text class="tn-padding-left-xs">爱心</text>
              </view>
            </view>
          </view>
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view
              class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center"
            >
              <view class="">
                <view class="tn-text-xxl tn-color-blue">{{
                  formatNumberAddPriceUnit(tipsDataMessage.hotReviewsCount)
                }}</view>
              </view>
              <view
                class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center"
              >
                <tn-icon name="message"></tn-icon>
                <text class="tn-padding-left-xs">热评</text>
              </view>
            </view>
          </view>
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view
              class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center"
            >
              <view class="">
                <view class="tn-text-xxl tn-color-red">{{
                  formatNumberAddPriceUnit(tipsDataMessage.fansCount)
                }}</view>
              </view>
              <view
                class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center"
              >
                <tn-icon name="fire"></tn-icon>
                <text class="tn-padding-left-xs">热度</text>
              </view>
            </view>
          </view>
          <view class="tn-flex-1 tn-padding-sm tn-margin-xs">
            <view
              class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center"
            >
              <view class="">
                <view class="tn-text-xxl tn-color-cyan">{{
                  formatNumberAddPriceUnit(tipsDataMessage.focusCount)
                }}</view>
              </view>
              <view
                class="tn-margin-top-xs tn-color-gray tn-text-df tn-text-center"
              >
                <tn-icon name="bookmark"></tn-icon>
                <text class="tn-padding-left-xs">帖子</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 内容 -->
    <view
      class="tn-flex tn-flex-direction-column tn-margin-top-sm tn-margin-bottom"
    >
      <view class="tn-strip-bottom"></view>

      <!-- 图文信息 -->
      <block v-for="(item, index) in content" :key="index">
        <view class="blogger__item">
          <view
            class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left"
            @click="tn('/circlePages/details')"
          >
            <view
              v-for="(label_item, label_index) in item.label"
              :key="label_index"
              class="blogger__desc__label tn-float-left tn-margin-right tn-bg-gray--light tn-round tn-text-sm tn-text-bold"
            >
              <text class="blogger__desc__label--prefix">#</text>
              <text class="tn-text-df">{{ label_item }}</text>
            </view>
          </view>
           <text
              v-if="!item.label || item.label.length < 4"
              class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df"
              >{{ item.desc }}</text
            >
          <block v-if="item.mainImage">
            <view
              v-if="[1, 2, 4].indexOf(item.mainImage.length) != -1"
              class="tn-padding-top-xs"
              @click="tn('/circlePages/details')"
            >
              <image
                v-for="(image_item, image_index) in item.mainImage"
                :key="image_index"
                class="blogger__main-image"
                :class="{
                  'blogger__main-image--1 tn-margin-bottom-sm':
                    item.mainImage.length === 1,
                  'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm':
                    item.mainImage.length === 2 || item.mainImage.length === 4,
                }"
                :src="image_item"
                mode="aspectFill"
              ></image>
            </view>
            <view
              v-else
              class="tn-padding-top-xs image-grid"
              @click="tn('/circlePages/details')"
            >
              <image
                v-for="(image_item, image_index) in item.mainImage"
                :key="image_index"
                class="blogger__main-image blogger__main-image--3"
                :src="image_item"
                mode="aspectFill"
              ></image>
            </view>
          </block>

          <view
            class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xs"
          >
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
              <view style="margin-right: 10rpx; margin-left: 20rpx">
                <tn-avatar-group
                  :lists="item.viewUser.latestUserAvatar"
                  size="sm"
                ></tn-avatar-group>
              </view>
              <text class="tn-color-gray"
                >{{ item.viewUser.viewUserCount }}人</text
              >
            </view>
          </view>
        </view>

        <view class="tn-strip-bottom" v-if="index != content.length - 1"></view>
      </block>

      <view class="tn-strip-bottom"></view>

      <!-- 广告 -->
      <view class="blogger__item" @click="tn('/circlePages/advertise')">
        <view
          class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-bottom"
        >
          <view class="justify-content-item">
            <view class="tn-flex tn-flex-col-center tn-flex-row-left">
              <view
                class="ad-pic tn-shadow-blur"
                style="
                  background-image: url('https://resource.tuniaokj.com/images/logo/logo2.png');
                "
              >
                <view class="ad-image"> </view>
              </view>
              <view class="tn-padding-right" style="max-width: 65vw">
                <view
                  class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg"
                  >抓住那只猪科技有限公司</view
                >
                <view
                  class="tn-padding-right tn-padding-left-sm tn-color-gray tn-text-ellipsis"
                  >我们都是好好孩子</view
                >
              </view>
            </view>
          </view>
          <view class="tn-color-gray">广告</view>
        </view>
        <view class="swiper">
          <swiper>
            <swiper-item v-for="(item, index) in adList" :key="index">
              <view
                class="swiper-item"
                :style="{ backgroundImage: `url(${item.image})` }"
              ></view>
            </swiper-item>
          </swiper>
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 图文信息 -->
      <block v-for="(item, index) in content" :key="index">
        <view class="blogger__item">
          <view
            class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center"
          >
            <view class="justify__author__info">
              <view class="tn-flex tn-flex-row-center">
                <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                  <view class="">
                    <tn-avatar
                      class=""
                      shape="circle"
                      :src="item.userAvatar"
                      size="lg"
                    >
                    </tn-avatar>
                  </view>
                  <view class="tn-padding-right tn-text-ellipsis">
                    <view
                      class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg"
                      >{{ item.userName }}</view
                    >
                    <view
                      class="tn-padding-right tn-padding-left-sm tn-padding-top-xs tn-color-gray"
                      >{{ item.date }}</view
                    >
                  </view>
                </view>
              </view>
            </view>
            <view
              class="blogger__author__btn justify-content-item tn-flex-col-center tn-flex-row-center"
            >
              <tn-icon
                class="tn-color-gray tn-text-bold tn-text-xxl"
                name="more-vertical"
              ></tn-icon>
            </view>
          </view>

          <view
            class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left"
            @click="tn('/circlePages/details')"
          >
            <view
              v-for="(label_item, label_index) in item.label"
              :key="label_index"
              class="blogger__desc__label tn-float-left tn-margin-right tn-bg-gray--light tn-round tn-text-sm tn-text-bold"
            >
              <text class="blogger__desc__label--prefix">#</text>
              <text class="tn-text-df">{{ label_item }}</text>
            </view>
           
          </view>
          <text
              v-if="!item.label || item.label.length < 4"
              class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df"
              >{{ item.desc }}</text >
          <block v-if="item.mainImage">
            <view
              v-if="[1, 2, 4].indexOf(item.mainImage.length) != -1"
              class="tn-padding-top-xs"
              @click="tn('/circlePages/details')"
            >
              <image
                v-for="(image_item, image_index) in item.mainImage"
                :key="image_index"
                class="blogger__main-image"
                :class="{
                  'blogger__main-image--1 tn-margin-bottom-sm':
                    item.mainImage.length === 1,
                  'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm':
                    item.mainImage.length === 2 || item.mainImage.length === 4,
                }"
                :src="image_item"
                mode="aspectFill"
              ></image>
            </view>
            <view
              v-else
              class="tn-padding-top-xs image-grid"
              @click="tn('/circlePages/details')"
            >
              <image
                v-for="(image_item, image_index) in item.mainImage"
                :key="image_index"
                class="blogger__main-image blogger__main-image--3"
                :src="image_item"
                mode="aspectFill"
              ></image>
            </view>
          </block>

          <view
            class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xs"
          >
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
              <view style="margin-right: 10rpx; margin-left: 20rpx">
                <tn-avatar-group
                  :lists="item.viewUser.latestUserAvatar"
                  size="sm"
                ></tn-avatar-group>
              </view>
              <text class="tn-color-gray"
                >{{ item.viewUser.viewUserCount }}人</text
              >
            </view>
          </view>
        </view>

        <view class="tn-strip-bottom" v-if="index != content.length - 1"></view>
      </block>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance } from "vue";
import { onLoad, onReady, onShow, onHide } from "@dcloudio/uni-app";
import { ref, reactive, nextTick } from "vue";
import { useGoBack } from "@/libs/composables";
const { goBack } = useGoBack();
// 内容默认隐藏显示的高度
const contentHideShowHeight = ref(0);

const tipsDataMessage = reactive({
  latestMessageUserAvatar:
    "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg",
  messageCount: 3,
  likeCount: 1290,
  hotReviewsCount: 896,
  fansCount: 962,
  focusCount: 86,
});

const content = ref([
  {
    userAvatar:
      "https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg",
    userName: "图鸟北北",
    date: "2021年12月20日",
    label: ["开源", "创意", "UI框架"],
    desc: "开源可商用组件，助你开发酷炫UI一臂之力",
    content:
      "基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了",
    viewUser: {
      latestUserAvatar: [
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg" },
      ],
      viewUserCount: 62,
    },
    collectionCount: 439,
    commentCount: 46,
    likeCount: 83,
  },
  {
    userAvatar:
      "https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg",
    userName: "图鸟北北",
    date: "2021年12月20日",
    label: ["开源", "创意", "UI框架"],
    desc: "开源可商用组件，助你开发酷炫UI一臂之力",
    content:
      "基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了",
    mainImage: ["https://resource.tuniaokj.com/images/blogger/content_1.jpeg"],
    viewUser: {
      latestUserAvatar: [
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg" },
      ],
      viewUserCount: 12,
    },
    collectionCount: 902,
    commentCount: 64,
    likeCount: 83,
  },
  {
    userAvatar:
      "https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg",
    userName: "图鸟北北",
    date: "2021年12月20日",
    label: [],
    desc: "",
    content: "",
    mainImage: [
      "https://resource.tuniaokj.com/images/shop/computer2.jpg",
      "https://resource.tuniaokj.com/images/shop/prototype2.jpg",
    ],
    viewUser: {
      latestUserAvatar: [
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg" },
      ],
      viewUserCount: 56,
    },
    collectionCount: 431,
    commentCount: 26,
    likeCount: 84,
  },
  {
    userAvatar:
      "https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg",
    userName: "图鸟北北",
    date: "2021年12月20日",
    label: ["开源", "创意"],
    desc: "开源可商用组件",
    content:
      "基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了 基础常用的布局元素，酷炫完善的配色体系，统一可增的图标 icon ，简便调用的功能组件，酷炫的前端页面，吖，编不下去了",
    mainImage: [
      "https://resource.tuniaokj.com/images/swiper/swiper2.jpg",
      "https://resource.tuniaokj.com/images/swiper/swiper3.jpg",
      "https://resource.tuniaokj.com/images/swiper/swiper4.jpg",
    ],
    viewUser: {
      latestUserAvatar: [
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg" },
      ],
      viewUserCount: 231,
    },
    collectionCount: 780,
    commentCount: 89,
    likeCount: 82,
  },
  {
    userAvatar:
      "https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg",
    userName: "图鸟北北",
    date: "2021年12月20日",
    label: ["开源", "链接"],
    desc: "https://www.yuque.com/tuniao",
    mainImage: [
      "https://resource.tuniaokj.com/images/shop/watch1.jpg",
      "https://resource.tuniaokj.com/images/shop/watch2.jpg",
      "https://resource.tuniaokj.com/images/shop/pillow2.jpg",
      "https://resource.tuniaokj.com/images/shop/pillow.jpg",
    ],
    viewUser: {
      latestUserAvatar: [
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg" },
      ],
      viewUserCount: 28,
    },
    collectionCount: 432,
    commentCount: 33,
    likeCount: 12,
  },
  {
    userAvatar:
      "https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg",
    userName: "图鸟北北",
    date: "2021年12月20日",
    label: ["开源", "创意"],
    desc: "开源可商用组件",
    mainImage: [
      "https://resource.tuniaokj.com/images/blogger/y11.jpg",
      "https://resource.tuniaokj.com/images/blogger/y33.jpg",
      "https://resource.tuniaokj.com/images/blogger/y22.jpg",
      "https://resource.tuniaokj.com/images/blogger/y44.jpg",
      "https://resource.tuniaokj.com/images/blogger/y55.jpg",
    ],
    viewUser: {
      latestUserAvatar: [
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg" },
        { src: "https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg" },
      ],
      viewUserCount: 65,
    },
    collectionCount: 265,
    commentCount: 22,
    likeCount: 62,
  },
]);

const adList = ref([
  { image: "https://resource.tuniaokj.com/images/swiper/ad1.jpg" },
  { image: "https://resource.tuniaokj.com/images/swiper/ad2.jpg" },
  { image: "https://resource.tuniaokj.com/images/swiper/ad3.jpg" },
  { image: "https://resource.tuniaokj.com/images/swiper/ad4.jpg" },
  { image: "https://resource.tuniaokj.com/images/swiper/ad5.jpg" },
]);
const adAutoplay = ref(false);

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
};

// 震动跳转
const navEdit = (e) => {
  wx.vibrateLong();
  uni.navigateTo({
    url: "/circlePages/edit",
  });
};

// 处理内容，给内容添加对应的标识信息
const initContentData = () => {
  content.value.forEach((item, index) => {
    item.hideContent = false;
    item.showAllContent = false;
    item.contentContainerHeight = 0;
    item.contentContainerInit = false;
  });
};

const formatNumberAddPriceUnit=function(){
  return ''
}

// 获取内容容器的信息
const getContentRectInfo = () => {
  const query = uni.createSelectorQuery().in(getCurrentInstance().proxy);
  content.value.forEach((item, index) => {
    if (item?.content) {
      query.select(`#blogger__content--${index}`).boundingClientRect();
    }
  });
  query.exec((res) => {
    if (!res) {
      setTimeout(() => {
        getContentRectInfo();
      }, 10);
      return;
    }
    res.map((item) => {
      const id = item.id;
      const idIndex = /blogger__content--(\d)/.exec(id)[1];
      let contentItem = content.value[idIndex];
      contentItem.hideContent = item.height > contentHideShowHeight.value;
      contentItem.showAllContent = false;
      contentItem.contentContainerHeight = item.height;
      contentItem.contentContainerInit = true;
    });
  });
};

// 切换内容的显示与隐藏
const switchContentShowStatus = (index) => {
  const contentItem = content.value[index];
  contentItem.showAllContent = !contentItem.showAllContent;
};

// 生命周期
onLoad(() => {
  initContentData();
  contentHideShowHeight.value = uni.upx2px(56) * 3;
});

onReady(() => {
  nextTick(() => {
    getContentRectInfo();
  });
});

onShow(() => {
  adAutoplay.value = true;
});

onHide(() => {
  adAutoplay.value = false;
});
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
  color: #ffffff;
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
    background-color: #ffffff;
  }
}

.oa-content {
  max-width: 640px;
  margin: 0 auto;
  min-height: 100vh;
  padding-bottom: 60rpx;
  padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
  padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
}

/* 顶部头像 */
.top-bg {
  background: #fff;
  background-repeat: no-repeat;
  background-size: cover;
  height: 550rpx;
  display: flex;
  justify-content: center;
  overflow: hidden;
  position: relative;
  flex-direction: column;
  align-items: center;
  z-index: -1;
}

/* 用户头像 start */
.logo-image {
  width: 110rpx;
  height: 110rpx;
  position: relative;
}

.logo-pic {
  background-size: cover;
  background-repeat: no-repeat;
  background-position: top;
  border: 6rpx solid rgba(255, 255, 255, 0.25);
  box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
  border-radius: 50%;
  overflow: hidden;
}

/* 最新消息-头像 start */
.message-image {
  width: 50rpx;
  height: 50rpx;
  position: relative;
}

.message-pic {
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  border-radius: 100rpx;
  overflow: hidden;
}

/* 图片网格 grid 布局 */
.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10rpx;
}

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
        color: #00ffc8;
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
      color: #82b2ff;
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
  position: fixed;
  width: 100%;
  bottom: calc(30rpx + env(safe-area-inset-bottom));
  z-index: 1024;
  box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
}
/* 底部悬浮按钮 end*/

/* 广告内容 start */
.ad-image {
  width: 80rpx;
  height: 80rpx;
  position: relative;
}
.ad-pic {
  background-size: cover;
  background-repeat: no-repeat;
  background-position: top;
  border-radius: 20%;
}
/* 自定义导航栏内容 end */

/* 悬浮 */
.tnxuanfu {
  animation: suspension 3s ease-in-out infinite;
}

@keyframes suspension {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-0.8rem);
  }
}
/* 悬浮按钮 */
.button-shop {
  width: 90rpx;
  height: 90rpx;
  display: flex;
  flex-direction: row;
  position: fixed;
  left: 5rpx;
  top: 5rpx;
  z-index: 1001;
  border-radius: 100px;
  opacity: 0.9;
}

/* 按钮 */
.edit {
  bottom: 300rpx;
  right: 75rpx;
  position: fixed;
  z-index: 9999;
}

.pa,
.pa0 {
  position: absolute;
}

.pa0 {
  left: 0;
  top: 0;
}

.bg0 {
  width: 100rpx;
  height: 100rpx;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.bg1 {
  width: 100%;
  height: 100%;
}

.hx-box {
  top: 50%;
  left: 50%;
  width: 100rpx;
  height: 100rpx;
  transform-style: preserve-3d;
  transform: translate(-50%, -50%) rotateY(75deg) rotateZ(10deg);
}

.hx-box .pr {
  width: 100rpx;
  height: 100rpx;
  transform-style: preserve-3d;
  animation: hxz 20s linear infinite;
}

@keyframes hxz {
  0% {
    transform: rotateX(0deg);
  }

  100% {
    transform: rotateX(-360deg);
  }
}

.hx-box .pr .pa0 {
  width: 100rpx;
  height: 100rpx;
  border-radius: 1000px;
}

.hx-box .pr .pa0 .span {
  display: block;
  width: 100%;
  height: 100%;
  background: url(https://resource.tuniaokj.com/images/cool_bg_image/arc4.png)
    no-repeat center center;
  background-size: 100% 100%;
  animation: hx 4s linear infinite;
}

@keyframes hx {
  to {
    transform: rotate(360deg);
  }
}

.hx-k1 {
  transform: rotateX(-60deg) rotateZ(-60deg);
}

.hx-k2 {
  transform: rotateX(-30deg) rotateZ(-30deg);
}

.hx-k3 {
  transform: rotateX(0deg) rotateZ(0deg);
}

.hx-k4 {
  transform: rotateX(30deg) rotateZ(30deg);
}

.hx-k5 {
  transform: rotateX(60deg) rotateZ(60deg);
}

.hx-k6 {
  transform: rotateX(90deg) rotateZ(90deg);
}

/* swiper start */
.swiper {
  position: relative;
  width: 100%;
  height: 360rpx;

  .swiper-item {
    position: relative;
    width: 100%;
    height: 100%;
    border-radius: 16rpx;
    background-size: cover;
    background-position: center;
  }
}
/* swiper end */
</style>
