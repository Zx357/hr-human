<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF00" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="home-loading-fill" class='icon'></tn-icon>
      </view></template>
    </tn-navbar>

    <view class="login login-fixed">
      <!-- 顶部背景装饰:使用本地静态资源,避免外链图床不可达 -->
      <view class="login__bg login__bg--top">
        <image class="rocket login-sussuspension" src="/static/logo.png" mode="widthFix"></image>
      </view>

      <view class="login__wrapper">
        <!-- 输入框内容-->
        <view class="login__info tn-flex tn-flex-direction-column tn-flex-col-center tn-flex-row-center">
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

          <!-- 悬浮按钮-->
          <view class="tn-margin-top-lg" style="width: 100%;position: relative;">
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
          </view>

          <!-- 忘记密码提示(员工账号由管理员统一开通与重置) -->
          <view class="login__forgot tn-color-gray tn-text-sm" @tap.stop="showForgotTip">
            忘记密码？请联系管理员重置
          </view>
        </view>
      </view>

    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useGoBack } from '@/libs/composables'
import { getToken } from '@/utils/auth'
import { resolveRedirectUrl, WORKBENCH_PAGE } from '@/utils/auth-guard'

const store = useStore()
const { goBack } = useGoBack()

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

// 忘记密码提示
function showForgotTip() {
  uni.showModal({
    title: '忘记密码',
    content: '忘记密码？请联系管理员重置',
    showCancel: false,
    confirmText: '知道了'
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
    min-height: 100vh;
  }

  /* 忘记密码提示 */
  .login__forgot {
    margin-top: 36rpx;
    padding: 10rpx 0;
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
    background: linear-gradient(90deg, #FFFFFF, #FFFFFF, #DBF2FE, #DBF2FE);
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

    /* 登录信息 start */
    &__info {
      margin: 80rpx 30rpx 10rpx 30rpx;
      padding-bottom: 0;
      border-radius: 20rpx;

      &__item {

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
    /* 登录信息 end */
    /* 内容 end */

  }




  :deep(.input-placeholder) {
    font-size: 32rpx;
    color: #AAAAAA;
  }

</style>
