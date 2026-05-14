<template>
  <view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#ffffff00" :placeholder="false" customBack>
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left"></tn-icon>
        <tn-icon class='icon' name="home-capsule-fill"></tn-icon>
      </view>
    </tn-navbar>
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <!-- 图文信息 -->
      <block v-for="(item,index) in content" :key="index">
        <view class="blogger__item">
          <view class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify__author__info" @click="tn('/pageA/author/author')">
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
              <tn-icon class="tn-color-gray--disabled tn-text-bold tn-text-xxl" name="more-vertical"></tn-icon>
            </view>
          </view>
         
          <view class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left">
            <view v-for="(label_item,label_index) in item.label" :key="label_index" class="blogger__desc__label tn-float-left tn-margin-right">
              <tn-icon class="blogger__desc__label--prefix" name="topics-fill"></tn-icon> 
              <text class="tn-text-df">{{ label_item }}</text>
            </view>
            <text v-if="!item.label || item.label.length < 4" class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df">{{ item.desc }}</text>  
          </view>
          
          <block v-if="item.mainImage">
            <view v-if="[1,2,4].indexOf(item.mainImage.length) != -1" class="tn-padding-top-xs">
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
            <view v-else class="tn-padding-top-xs" style="margin-left: -10rpx;">
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
         
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top">
            <view class="justify-content-item tn-flex tn-flex-col-center">
              <view style="margin-left: -10rpx;margin-right: 6rpx;transform: scale(0.85);">
                <tn-avatar-group :lists="item.viewUser.latestUserAvatar" size="sm"></tn-avatar-group>
              </view>
              <text class="tn-color-gray">{{ item.viewUser.viewUserCount }}人</text>
            </view>

            <view class="justify-content-item tn-color-gray tn-text-center" style="padding-top: 5rpx;">
              <tn-icon class="tn-text-lg" name="fire" style="padding-right: 5rpx;"></tn-icon>
              <text class="tn-padding-right tn-text-df">{{ item.collectionCount }}</text>
              <tn-icon class="tn-text-lg" name="message" style="padding-right: 5rpx;"></tn-icon>
              <text class="tn-padding-right tn-text-df">{{ item.commentCount }}</text>
              <tn-icon class="tn-text-lg" name="like-lack" style="padding-right: 5rpx;"></tn-icon>
              <text class="tn-text-df">{{ item.likeCount }}</text>
            </view>
          </view>
        </view>
      </block>
      
      <!-- 按钮-->
      <view class="tn-flex tn-flex-row-between" style="margin: 40rpx 0 60rpx 0;">
        <view class="tn-flex-1 justify-content-item tn-text-center tn-margin-xs">
          <tn-button
            bg-color="#FBBD12"
            :custom-style="{padding:'40rpx 0'}"
            width="90%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            @click="tn('')"
          >
            <text class="">点赞支持</text>
          </tn-button>
        </view>
        <view class="tn-flex-1 justify-content-item tn-text-center tn-margin-xs">
          <tn-button
            bg-color="#05C160"
            :custom-style="{padding:'40rpx 0'}"
            width="90%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            open-type="share"
          >
            <text class="">分享好友</text>
          </tn-button>
        </view>
      </view>
      
      
    </view>
    
    <!-- 评论 -->
    <view class="tn-margin" style="padding-bottom: 120rpx;">
      <!-- 图标logo/头像 -->
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xl" @click="tn('/circlePages/blogger_other')">
        <view class="justify-content-item">
          <view class="tn-flex tn-flex-col-center tn-flex-row-left">
            <view class="logo-pic tn-shadow">
              <view class="logo-image">
                <view class="tn-shadow-blur" style="background-image:url('https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg');width: 60rpx;height: 60rpx;background-size: cover;">
                </view>
              </view>
            </view>
            <view class="tn-padding-right tn-padding-left-sm">
              <view class="tn-padding-right tn-text-df tn-text-bold tn-color-black">
                抓住那只高产母猪
              </view>
              <view class="tn-padding-right tn-text-ellipsis tn-text-xs tn-color-gray" style="padding-top: 5rpx;">
                2022年5月26日
              </view>
            </view>
          </view>
        </view>
        <view class="justify-content-item tn-flex-row-center tn-flex-col-center tn-color-gray">
          <view class="tn-text-center">
            <tn-icon class="tn-padding-xs" name="like-lack"></tn-icon>
          </view>
          <view class="tn-text-center">
            <text class="tn-text-xs">26</text>
          </view>
        </view>
      </view>
      
      <view class="" style="margin: 20rpx 30rpx 30rpx 80rpx;">
        说好的带我飞呢？？就这？？
      </view>
      <view class="tn-bg-gray--light tn-padding-sm" style="margin: 20rpx 30rpx 30rpx 80rpx;border-radius: 10rpx;">
        <text class="tn-text-bold tn-padding-right-xs">博主回复: </text>
        <text style="line-height: 40rpx;">设计师何必难为设计师？冤冤相报何时了，不如喝杯奶茶忘了那个仔</text>
        <view class="tn-flex tn-flex-row-between tn-margin-top-xs">
          <view class="justify-content-item tn-text-xs tn-color-gray" style="padding-top: 5rpx;">
            2022年5月26日
          </view>
          <view class="justify-content-item tn-text-xs tn-color-gray">
            <text class="tn-padding-xs">16</text>
            <tn-icon name="like-lack"></tn-icon>
          </view>
        </view>
      </view>
      
      <!-- 评论2-->
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xl" @click="tn('/circlePages/blogger_other')">
        <view class="justify-content-item">
          <view class="tn-flex tn-flex-col-center tn-flex-row-left">
            <view class="logo-pic tn-shadow">
              <view class="logo-image">
                <view class="tn-shadow-blur" style="background-image:url('https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg');width: 60rpx;height: 60rpx;background-size: cover;">
                </view>
              </view>
            </view>
            <view class="tn-padding-right tn-padding-left-sm">
              <view class="tn-padding-right tn-text-df tn-text-bold tn-color-black">
                北染陌人
              </view>
              <view class="tn-padding-right tn-text-ellipsis tn-text-xs tn-color-gray" style="padding-top: 5rpx;">
                2022年5月25日
              </view>
            </view>
          </view>
        </view>
        <view class="justify-content-item tn-flex-row-center tn-flex-col-center tn-color-gray">
          <view class="tn-text-center">
            <tn-icon class="tn-padding-xs" name="like-lack"></tn-icon>
          </view>
          <view class="tn-text-center">
            <text class="tn-text-xs">68</text>
          </view>
        </view>
      </view>
      
      <view class="" style="margin: 20rpx 30rpx 30rpx 80rpx;">
        求摄影师微信，谢谢
      </view>
      
      
      <!-- 评论3-->
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xl" @click="tn('/circlePages/blogger_other')">
        <view class="justify-content-item">
          <view class="tn-flex tn-flex-col-center tn-flex-row-left">
            <view class="logo-pic tn-shadow">
              <view class="logo-image">
                <view class="tn-shadow-blur" style="background-image:url('https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg');width: 60rpx;height: 60rpx;background-size: cover;">
                </view>
              </view>
            </view>
            <view class="tn-padding-right tn-padding-left-sm">
              <view class="tn-padding-right tn-text-df tn-text-bold tn-color-black">
                原来是吖释鸭
              </view>
              <view class="tn-padding-right tn-text-ellipsis tn-text-xs tn-color-gray">
                2022年5月25日
              </view>
            </view>
          </view>
        </view>
        <view class="justify-content-item tn-flex-row-center tn-flex-col-center tn-color-gray">
          <view class="tn-text-center">
            <tn-icon class="tn-padding-xs" name="like-lack"></tn-icon>
          </view>
          <view class="tn-text-center">
            <text class="tn-text-xs">43</text>
          </view>
        </view>
      </view>
      
      <view class="" style="margin: 20rpx 30rpx 30rpx 80rpx;">
        吃瓜群众到此一游，阿巴阿巴
      </view>
      
    </view>
    
    
    <view class="tabbar footerfixed tn-bg-white">
      <view class="tn-flex tn-flex-row-between tn-flex-col-center">
        <view class="justify-content-item tn-margin-top">
          <view class="tn-flex tn-flex-row-center tn-flex-col-center">
            
            <view class="tn-flex tn-flex-row-center tn-flex-col-center tn-padding-right tn-padding-left-sm">
              <view class="avatar-all">
                <view class="tn-shadow-blur" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg');width: 60rpx;height: 60rpx;background-size: cover;">
                </view>
              </view>
            </view>
            
            
            <view class="topic__info__item__input tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-left">
              <view class="topic__info__item__input__left-icon">
                <tn-icon class="tn-color-gray--disabled" name="emoji-good"></tn-icon>
              </view>
              <view class="topic__info__item__input__content">
                <input maxlength="20" class="justify-content-item" placeholder="不说点啥子吗？" name="input" placeholder-style="color:#E6E6E6" style="width: 90%;"></input>
              </view>
            </view>
            
          </view>
        </view>
        <view class="justify-content-item tn-flex-row-center tn-flex-col-center tn-margin-top tn-margin-right">
          <view class="topic__info__item__sure">
            <view class="tn-flex-1 justify-content-item tn-text-center">
              <tn-button
                bg-color="#3668fc"
                :custom-style="{padding:'20rpx 0'}"
                width="100%"
                :fontSize="28"
                text-color="#FFFFFF"
                shape="round"
                @click="tn('')"
              >
                <text class="">发 送</text>
              </tn-button>
            </view>
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

defineOptions({
  name: 'TemplateDetails'
})

const content = ref([
  {
    userAvatar: 'https://resource.tuniaokj.com/images/blogger/blogger_beibei.jpg',
    userName: '可我会像',
    date: '2022年5月20日',
    label: ['猛犸空间','打卡'],
    desc: '佛山乐从家具城一日游',
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
    likeCount: 102
  }
])

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
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
      background-color: #F8F7F8;
      min-height: 100vh;
      padding-bottom: 60rpx;
      padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
      padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
    }
    
    /* 文章内容 start*/
      .blogger {
        &__item {
          padding: 30rpx;
        }
        
        &__author {
          &__btn {
            margin-right: -12rpx;
            padding: 0 20rpx;
          }
        }
        
        &__desc {
          line-height: 30rpx;
          
          &__label {
            
            color: #1D2541;
            background-color: #F3F2F7;
            border-radius: 10rpx;
            font-size: 22rpx;
            
            padding: 5rpx 15rpx;
            margin: 5rpx 18rpx 0 0;
            
            &--prefix {
              font-size: 24rpx;
              color: #1D2541;
              padding-right: 10rpx;
            }
          }
          &__content {
            line-height: 50rpx;
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
       
    /* 头像 start */
    .logo-image {
      width: 60rpx;
      height: 60rpx;
      position: relative;
    }
    
    .logo-pic {
      background-size: cover;
      background-repeat: no-repeat;
      background-position: top;
      box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
      border-radius: 50%;
      overflow: hidden;
    }
  
  
  /* 底部 start*/
  .footerfixed{
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    width: 100%;
    bottom: 0;
    z-index: 999;
    background-color: rgba(255,255,255,0.5);
    box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.07);
  }
  
  .tabbar {
    align-items: center;
    min-height: 130rpx;
  	padding: 0;
  	height: calc(130rpx + env(safe-area-inset-bottom) / 2);
  	padding-bottom: calc(30rpx + env(safe-area-inset-bottom) / 2);
    padding-left: 10rpx;
    padding-right: 10rpx;
  }
  
    /* 毛玻璃*/
  .dd-glass {
     width: 100%;
     backdrop-filter: blur(20rpx);
    -webkit-backdrop-filter: blur(20rpx);
  }
  
  /* 头像*/
  .avatar-all {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid rgba(255,255,255,0.05);
    border-radius: 50%;
    overflow: hidden;
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
  }
  
  /* 内容*/
  .topic {
    position: relative;
    height: 100%;
    z-index: 1;
    margin-bottom: 120rpx;
    
    
    /* 表单信息 start */
    &__info {
      margin: 0 50rpx;
      margin-top: 105rpx;
      padding: 30rpx 51rpx;
      border-radius: 20rpx;
      background-color: rgba(255,255,255,1);
      border: 2rpx solid rgba(255, 255, 255, 0.1);
      box-shadow: 0rpx 10rpx 50rpx 0rpx rgba(0, 3, 72, 0.1);
      
      &__item {
        
        &__input {
          width: 400rpx;
          height: 60rpx;
          border: 1rpx solid #E6E6E6;
          border-radius: 39rpx;
          
          &__left-icon {
            width: 10%;
            font-size: 44rpx;
            margin-left: 20rpx;
            margin-right: 5rpx;
          }
          
          &__content {
            width: 80%;
            padding-left: 10rpx;
            
            &--verify-code {
              width: 56%;
            }
            
            input {
              font-size: 30rpx;
            }
          }
          
          &__right-icon {
            width: 10%;
            font-size: 34rpx;
            margin-right: 20rpx;
            color: #78909C;
          }
          
          &__right-verify-code {
            width: 34%;
            margin-right: 20rpx;
          }
        }
        
        &__button {
          width: 100%;
          height: 60rpx;
          text-align: center;
          font-size: 31rpx;
          font-weight: bold;
          line-height: 77rpx;
          border-radius: 100rpx;
          color: #FFFFFF;
          background-color: rgba(255,255,255,0.2);
        }
        
        &__sure {
          height: 60rpx;
          width: 140rpx;
        }
        
      }
    }
    /* 表单信息 end */
    
    /* 内容 end */
    
  }
  
  :deep(.input-placeholder) {
    font-size: 30rpx;
    color: #C6D1D8;
  }
  
</style>
