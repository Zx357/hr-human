<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF00" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="home-loading-fill" class='icon'></tn-icon>
      </view>
      <!-- <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">注册登录</text>
      </view> -->
    </tn-navbar>
    
    <view class="login login-fixed">
      <!-- 顶部背景图片-->
      <!-- <view class="login__bg login__bg--top">
        <image class="bg" src="https://resource.tuniaokj.com/images/login/2/login-top2.png" mode="widthFix"></image>
      </view> -->
      <!-- <view class="login__bg login__bg--top">
        <image class="rocket login-sussuspension" src="https://resource.tuniaokj.com/images/login/1/login_top3.png" mode="widthFix"></image>
      </view> -->
      <view class="login__bg login__bg--top">
        <image class="rocket login-sussuspension" src="https://cdn.nlark.com/yuque/0/2023/png/280373/1679469229310-assets/web-upload/bb996531-e579-47a2-8f50-7209f6d655a4.png" mode="widthFix"></image>
      </view>

      <view class="login__wrapper">
        <!-- <view class="tn-margin-left tn-margin-right tn-text-bold" style="font-size: 48rpx;">
          图鸟OA
        </view>
        <view class="tn-margin tn-color-gray--dark tn-text-sm">
          这是一句很厉害的广告语
        </view> -->
        
        <!-- 登录/注册切换 -->
        <!-- <view class="login-sussuspension login__mode tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-center">
          <view class="login__mode__item tn-flex-1" :class="[{'login__mode__item--active': currentModeIndex === 0}]" @tap.stop="modeSwitch(0)">
            登录
          </view>
          <view class="login__mode__item tn-flex-1" :class="[{'login__mode__item--active': currentModeIndex === 1}]" @tap.stop="modeSwitch(1)">
            注册
          </view>
          <view class="login__mode__slider tn-cool-bg-color-15--reverse" :style="[modeSliderStyle]"></view>
        </view> -->
        
        <!-- 输入框内容-->
        <view class="login__info tn-flex tn-flex-direction-column tn-flex-col-center tn-flex-row-center">
          <!-- 登录 -->
          <block v-if="currentModeIndex === 0">
            <view class="login__info__item__input tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-left">
              <view class="login__info__item__input__left-icon">
                <tn-icon name="my"></tn-icon>
              </view>
              <view class="login__info__item__input__content">
                <input
                  v-model="loginForm.employeeNo"
                  maxlength="32"
                  placeholder-class="input-placeholder"
                  placeholder="请输入工号"
                  confirm-type="next"
                />
              </view>
            </view>
            
            <view class="login__info__item__input tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-left">
              <view class="login__info__item__input__left-icon">
                <tn-icon name="lock"></tn-icon>
              </view>
              <view class="login__info__item__input__content">
                <input
                  v-model="loginForm.password"
                  maxlength="32"
                  :password="true"
                  placeholder-class="input-placeholder"
                  placeholder="请输入密码"
                  confirm-type="done"
                  @confirm="handleLogin"
                />
              </view>
            </view>
          </block>
          <!-- 注册 -->
          <block v-if="currentModeIndex === 1">
            <view class="login__info__item__input tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-left">
              <view class="login__info__item__input__left-icon">
                <tn-icon name="my"></tn-icon>
              </view>
              <view class="login__info__item__input__content">
                <input maxlength="16" placeholder-class="input-placeholder" placeholder="请输入账号" />
              </view>
            </view>
            
            <view class="login__info__item__input tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-left">
              <view class="login__info__item__input__left-icon">
                <tn-icon name="safe"></tn-icon>
              </view>
              <view class="login__info__item__input__content login__info__item__input__content--verify-code">
                <input maxlength="6" placeholder-class="input-placeholder" placeholder="请输入验证码" />
              </view>
              <view class="login__info__item__input__right-verify-code tn-flex-row-right" @tap.stop="getCode">
                <tn-button
                    bg-color="#00C8B0"
                    text-color="#FFFFFF"
                    size="sm"
                    :custom-style="{padding:'5rpx 10rpx'}"
                    width="180rpx"
                    shape="round"
                  >{{ tips }}</tn-button>
              </view>
            </view>
            
            <view class="login__info__item__input tn-flex tn-flex-direction-row tn-flex-nowrap tn-flex-col-center tn-flex-row-left">
              <view class="login__info__item__input__left-icon">
                <tn-icon name="lock"></tn-icon>
              </view>
              <view class="login__info__item__input__content">
                <input maxlength="12" :password="!showPassword" :class="[showPassword ? '' : 'tn-color-gray tn-text-sm']" placeholder-class="input-placeholder" placeholder="请输入密码" />
              </view>
              <view class="login__info__item__input__right-icon" @click="showPassword = !showPassword">
                <view :class="[showPassword ? 'tn-icon-eye' : 'tn-icon-eye-hide']"></view>
              </view>
            </view>
          </block>
          
          <!-- 悬浮按钮-->
          <view class="tn-margin-top-lg" v-if="currentModeIndex === 0" style="width: 100%;position: relative;">
            <view class="tn-margin-top-lg">
              <tn-button
              shape="round"
              bg-color="#3668FC80"
              :custom-style="{padding:'46rpx 0'}"
              width="100%"
              :fontSize="30"
              text-color="#FFFFFF"
              @click="handleLogin"
            >
              <text class="">{{ loading ? '登录中...' : '登 录' }}</text>
            </tn-button>
            </view>
            <!-- <view class="tn-margin-top-lg">
              <tn-button
              shape="round"
              bg-color="#F2F2F2"
              :custom-style="{padding:'46rpx 0'}"
              width="100%"
              :fontSize="30"
              fontColor="tn-color-gray"
              @click="tn('')"
            >
              <text class="">登 录</text>
            </tn-button>
            </view> -->
          </view>
          
          <!-- 悬浮按钮-->
          <view class="tn-margin-top-lg" v-if="currentModeIndex === 1" style="width: 100%;position: relative;">
            <view class="tn-margin-top-lg">
              <tn-button
              shape="round"
              bg-color="#00C8B080"
              :custom-style="{padding:'46rpx 0'}"
              width="100%"
              :fontSize="30"
              text-color="#FFFFFF"
              @click="emptyFeature('注册')"
            >
              <text class="">注 册</text>
            </tn-button>
            </view>
            <!-- <view class="tn-margin-top-lg">
              <tn-button
              shape="round"
              bg-color="#F2F2F2"
              :custom-style="{padding:'46rpx 0'}"
              width="100%"
              :fontSize="30"
              fontColor="tn-color-gray"
              @click="tn('')"
            >
              <text class="">注 册</text>
            </tn-button>
            </view> -->
          </view>
          
          <view v-if="currentModeIndex === 1" :class="[{'login__info__item__tips': currentModeIndex === 0}]">
            <view class="tn-flex tn-flex-row-between tn-padding-xl">
              <view class="tn-color-gray" @tap.stop="modeSwitch(0)">已有账号？前往登录</view>
            </view>
          </view>
          <view v-if="currentModeIndex === 0" :class="[{'login__info__item__tips': currentModeIndex === 1}]">
            <view class="tn-flex tn-flex-row-between tn-padding-xl">
              <view class="tn-padding-right tn-color-gray" @tap.stop="modeSwitch(1)">新账号注册</view>
              <view class="tn-padding-left-lg tn-color-gray" @click="emptyFeature('找回密码')">忘记密码？</view>
            </view>
          </view>
          
        </view>
        
        
        <view class="tn-footerfixed">
          
          <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-text-center">
            <view class="" style="border-bottom: 1rpx solid #F8F7F8;width: 160rpx;"></view>
            <view class="tn-padding tn-text-sm tn-color-gray--disabled">快捷登录方式</view>
            <view class="" style="border-bottom: 1rpx solid #F8F7F8;width: 160rpx;"></view>
          </view>
          
          <!-- 方式13 start-->
          <view class="tn-flex tn-flex-row-center">
            <view class="tn-padding-sm tn-margin-sm tn-radius">
              <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-shadow-blur tn-main-gradient-green--light tn-color-green">
                  <tn-icon name="wechat-fill" class="tn-three"></tn-icon>
                </view>  
                <!-- <view class="tn-color-gray tn-text-center">
                  <text class="tn-text-ellipsis">微 信</text>
                </view> -->
              </view>
            </view>
            <view class="tn-padding-sm tn-margin-sm tn-radius">
              <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-shadow-blur tn-main-gradient-blue--light tn-color-blue">
                  <tn-icon name="qq" class="tn-three"></tn-icon>
                </view>  
                <!-- <view class="tn-color-gray tn-text-center">
                  <text class="tn-text-ellipsis">企 鹅</text>
                </view> -->
              </view>
            </view>
            <view class="tn-padding-sm tn-margin-sm tn-radius">
              <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-shadow-blur tn-main-gradient-red--light tn-color-red">
                  <tn-icon name="huawei" class="tn-three"></tn-icon>
                </view>  
                <!-- <view class="tn-color-gray tn-text-center">
                  <text class="tn-text-ellipsis">华 为</text>
                </view> -->
              </view>
            </view>
            <view class="tn-padding-sm tn-margin-sm tn-radius">
              <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-shadow-blur tn-main-gradient-cyan--light tn-color-cyan">
                  <tn-icon name="phone-fill" class="tn-three"></tn-icon>
                </view>  
                <!-- <view class="tn-color-gray tn-text-center">
                  <text class="tn-text-ellipsis">手 机</text>
                </view> -->
              </view>
            </view>
          </view>
          <!-- 方式13 end-->
          
        </view>
        
        
        
        
        
      </view>
      
      <!-- 底部背景图片-->
      <!-- <view class="login__bg login__bg--bottom">
        <image src="https://resource.tuniaokj.com/images/login/2/login-bottom2.png" mode="widthFix"></image>
      </view> -->
  
    </view>
    
    <!-- 验证码倒计时 TODO -->
    <!-- <tn-verification-code
      ref="code"
      uniqueKey="login-demo-4"
      :seconds="60"
      @change="codeChange">
    </tn-verification-code> -->
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useGoBack } from '@/libs/composables'
import { getToken } from '@/utils/auth'
import { resolveRedirectUrl, WORKBENCH_PAGE } from '@/utils/auth-guard'

const store = useStore()
const { goBack } = useGoBack()

const currentModeIndex = ref(0)
const modeSliderStyle = ref({ left: 0 })
const showPassword = ref(false)
const tips = ref('获取验证码')
const loading = ref(false)
const redirectUrl = ref(WORKBENCH_PAGE)
const loginForm = ref({
  employeeNo: '',
  password: ''
})

onLoad((options = {}) => {
  redirectUrl.value = resolveRedirectUrl(options.redirect)

  if (getToken()) {
    uni.reLaunch({ url: redirectUrl.value })
  }
})

watch(currentModeIndex, (value) => {
  const sliderWidth = uni.upx2px(476 / 2)
  modeSliderStyle.value.left = `${sliderWidth * value}px`
})

function modeSwitch(index) {
  currentModeIndex.value = index
  showPassword.value = false
  if (index === 1) {
    emptyFeature('注册')
  }
}

function getCode() {
  emptyFeature('验证码')
}

function codeChange(event) {
  tips.value = event
}

function emptyFeature(name) {
  uni.showToast({
    icon: 'none',
    title: `${name}暂未接入`
  })
}

async function handleLogin() {
  const employeeNo = loginForm.value.employeeNo.trim()
  if (!employeeNo) {
    uni.showToast({ icon: 'none', title: '请输入工号' })
    return
  }

  loading.value = true
  uni.showLoading({ title: '登录中...' })

  try {
    await store.dispatch('Login', {
      employeeNo,
      password: loginForm.value.password
    })
    await store.dispatch('GetInfo')
    uni.hideLoading()
    uni.reLaunch({ url: redirectUrl.value })
  } catch (error) {
    uni.hideLoading()
    uni.showToast({
      icon: 'none',
      title: error || '登录失败'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    width: 38%;
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
    // padding-bottom: 60rpx;
    // padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    // padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  /* 悬浮 */
  .login-sussuspension{
    animation: suspension 3s ease-in-out infinite;
  }
  
  @keyframes suspension {
    0%, 100% {
      transform: translate(0 , 0);
    }
    50% {
      transform: translate(0rem , 1rem);
    }
  }
  
  // 渐变底色
  .login-fixed{
    position: fixed;
    // background: linear-gradient(0deg, #F6DB4E00, #F6DB4EF2, #F6DB4E);
    background: linear-gradient(90deg, #FFFFFF, #FFFFFF, #DBF2FE, #DBF2FE);
    // background: linear-gradient(90deg, #FFFFFF, #FFFFFF, #FC3A7230, #FC3A7280);
    top: 0;
    width: 100%;
    transition: all 0.25s ease-out;
    z-index: 100;
    height: 400rpx;
  }
  .login-fixed:before{
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    right: 0;
    z-index: -1;
    mask-image: linear-gradient(to bottom, transparent, black);
    background: linear-gradient(90deg, #FFFFFF, #FFFFFF);	
  
  }
  
  .login {
    position: relative;
    z-index: 1;
    
    /* 背景图片 start */
    &__bg {
      z-index: -1;
      position: fixed;
      
      &--top {
        top: 0;
        left: 0;
        right: 0;
        width: 100%;
        
        .bg {
          width: 750rpx;
          will-change: transform;
        }
        .rocket {
          margin: 30% 36%;
          width: 220rpx;
          height: 220rpx;
          will-change: transform;
        }
      }
      
      &--bottom {
        bottom: -10rpx;
        left: 0;
        right: 0;
        width: 100%;
        // height: 144px;
        margin-bottom: env(safe-area-inset-bottom);
        
        image {
          width: 750rpx;
          will-change: transform;
        }
      }
    }
    /* 背景图片 end */
    
    /* 内容 start */
    &__wrapper {
      padding-top: 420rpx;
      width: 100%;
    }
    
    /* 切换 start */
    &__mode {
      position: relative;
      margin: 0 auto;
      width: 476rpx;
      height: 77rpx;
      margin-top: 100rpx;
      background-color: rgba(255,255,255,0.6);
      box-shadow: 0rpx 10rpx 50rpx 0rpx rgba(0, 3, 72, 0.1);
      border-radius: 39rpx;
      
      &__item {
        height: 77rpx;
        width: 100%;
        line-height: 77rpx;
        text-align: center;
        font-size: 31rpx;
        color: #080808;
        letter-spacing: 1em;
        text-indent: 1em;
        z-index: 2;
        transition: all 0.4s;
        
        &--active {
          font-weight: bold;
          color: #FFFFFF;
        }
      }
      
      &__slider {
        position: absolute;
        height: inherit;
        width: calc(476rpx / 2);
        border-radius: inherit;
        box-shadow: 0rpx 18rpx 72rpx 18rpx rgba(0, 195, 255, 0.1);
        z-index: 1;
        transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
      }
    }
    /* 切换 end */
    
    /* 登录注册信息 start */
    &__info {
      margin: 80rpx 30rpx 10rpx 30rpx;
      padding-bottom: 0;
      border-radius: 20rpx;
      
      &__item {
        
        // &__input {
        //   margin-top: 59rpx;
        //   width: 100%;
        //   height: 77rpx;
        //   border-bottom: 1rpx solid #E6E6E6;
          
        //   &__left-icon {
        //     font-size: 44rpx;
        //     padding: 10rpx 0rpx 30rpx 0rpx;
        //     color: #AAAAAA;
        //   }
          
        //   &__content {
        //     width: 90%;
        //     padding: 10rpx 0rpx 30rpx 10rpx;
        //     // padding-left: 10rpx;
            
        //     &--verify-code {
        //       width: 90%;
        //     }
            
        //     input {
        //       font-size: 32rpx;
        //       // letter-spacing: 0.1em;
        //     }
        //   }
          
        //   &__right-icon {
        //     width: 10%;
        //     font-size: 44rpx;
        //     margin-right: 0rpx;
        //     color: #AAAAAA;
        //     padding: 10rpx 0rpx 30rpx 0rpx;
        //   }
          
        //   &__right-verify-code {
        //     width: 28%;
        //     padding: 10rpx 0rpx 30rpx 0rpx;
        //   }
        // }
        
        &__input {
          margin-top: 59rpx;
          width: 100%;
          height: 90rpx;
          background-color: #F9F9F9;
          border-radius: 100rpx;
          
          &__left-icon {
            width: 10%;
            font-size: 36rpx;
            margin-left: 20rpx;
            margin-top: -4rpx;
            color: #AAAAAA;
          }
          
          &__content {
            width: 80%;
            padding-left: 10rpx;
            
            &--verify-code {
              width: 56%;
            }
            
            input {
              font-size: 32rpx;
              // letter-spacing: 0.1em;
            }
          }
          
          &__right-icon {
            width: 10%;
            font-size: 36rpx;
            margin-top: -4rpx;
            margin-right: 30rpx;
            color: #AAAAAA;
          }
          
          &__right-verify-code {
            width: 28%;
          }
        }
        
        &__tips {
          margin: 30rpx 0;
          color: #AAAAAA;
        }
      }
    }
    /* 登录注册信息 end */
    /* 内容 end */
    
  }
  
  .tn-three{
      position: absolute;
      top: 50%;
      right: 50%;
      bottom: 50%;
      left: 50%;
      transform: translate(-32rpx, -18rpx) rotateX(30deg) rotateY(20deg) rotateZ(-30deg);
      text-shadow: -1rpx 2rpx 0 #f0f0f0, -2rpx 4rpx 0 #f0f0f0, -10rpx 20rpx 30rpx rgba(0, 0, 0, 0.2);
  }
  
  
  /* 图标容器13 start */
  .icon13 {
    &__item {
      width: 30%;
      background-color: #FFFFFF;
      border-radius: 10rpx;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 90rpx;
        height: 90rpx;
        font-size: 50rpx;
        border-radius: 50%;
        margin-bottom: 18rpx;
        position: relative;
        z-index: 1;
        
        &::after {
          content: " ";
          position: absolute;
          z-index: -1;
          width: 100%;
          height: 100%;
          left: 0;
          bottom: 0;
          border-radius: inherit;
          opacity: 1;
          transform: scale(1, 1);
          background-size: 100% 100%;
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg.png);
  
            
        }
      }
    }
  }
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
  	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
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
  
  :deep(.input-placeholder) {
    font-size: 32rpx;
    color: #AAAAAA;
  }
  
</style>
