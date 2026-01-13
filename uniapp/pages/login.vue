<template>
  <view class="normal-login-container">
    <view class="logo-content align-center justify-center flex">
      <image style="width: 100rpx;height: 100rpx;" :src="globalConfig.appInfo.logo" mode="widthFix">
      </image>
      <text class="title">HR人事管理系统</text>
    </view>
    <view class="login-form-content">
      <view class="input-item flex align-center">
        <view class="iconfont icon-user icon"></view>
        <input v-model="loginForm.employeeNo" class="input" type="text" placeholder="请输入工号" maxlength="30" />
      </view>
      <view class="input-item flex align-center">
        <view class="iconfont icon-password icon"></view>
        <input v-model="loginForm.password" type="password" class="input" placeholder="请输入密码（默认123456）" maxlength="20" />
      </view>
      <view class="action-btn">
        <button @click="handleLogin" class="login-btn cu-btn block bg-blue lg round">登录</button>
      </view>
      <view class="xieyi text-center">
        <text class="text-grey1">登录即代表同意</text>
        <text @click="handleUserAgrement" class="text-blue">《用户协议》</text>
        <text @click="handlePrivacy" class="text-blue">《隐私协议》</text>
      </view>
    </view>
  </view>
</template>

<script>
  import { getToken } from '@/utils/auth'

  export default {
    data() {
      return {
        globalConfig: getApp().globalData.config,
        loginForm: {
          employeeNo: "",
          password: ""
        }
      }
    },
    onLoad() {
      //#ifdef H5
      if (getToken()) {
        this.$tab.reLaunch('/pages/index')
      }
      //#endif
    },
    methods: {
      // 隐私协议
      handlePrivacy() {
        let site = this.globalConfig.appInfo.agreements[0]
        this.$tab.navigateTo(`/pages/common/webview/index?title=${site.title}&url=${site.url}`)
      },
      // 用户协议
      handleUserAgrement() {
        let site = this.globalConfig.appInfo.agreements[1]
        this.$tab.navigateTo(`/pages/common/webview/index?title=${site.title}&url=${site.url}`)
      },
      // 登录方法
      async handleLogin() {
        if (this.loginForm.employeeNo === "") {
          this.$modal.msgError("请输入工号")
        } else {
          this.$modal.loading("登录中，请耐心等待...")
          this.pwdLogin()
        }
      },
      // 密码登录
      async pwdLogin() {
        console.log('开始登录...', this.loginForm)
        this.$store.dispatch('Login', this.loginForm).then(() => {
          console.log('登录成功，开始获取用户信息')
          this.loginSuccess()
        }).catch((err) => {
          console.log('登录失败', err)
          this.$modal.closeLoading()
          this.$modal.msgError(err || '登录失败')
        })
      },
      // 登录成功后，处理函数
      loginSuccess(result) {
        // 设置用户信息
        this.$store.dispatch('GetInfo').then(res => {
          console.log('获取用户信息成功，跳转主页', res)
          this.$modal.closeLoading()
          this.$tab.reLaunch('/pages/index')
        }).catch(err => {
          console.log('获取用户信息失败，但仍跳转主页', err)
          this.$modal.closeLoading()
          this.$tab.reLaunch('/pages/index')
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  page {
    background-color: #ffffff;
  }

  .normal-login-container {
    width: 100%;

    .logo-content {
      width: 100%;
      font-size: 21px;
      text-align: center;
      padding-top: 15%;

      image {
        border-radius: 4px;
      }

      .title {
        margin-left: 10px;
      }
    }

    .login-form-content {
      text-align: center;
      margin: 20px auto;
      margin-top: 15%;
      width: 80%;

      .input-item {
        margin: 20px auto;
        background-color: #f5f6f7;
        height: 45px;
        border-radius: 20px;

        .icon {
          font-size: 38rpx;
          margin-left: 10px;
          color: #999;
        }

        .input {
          width: 100%;
          font-size: 14px;
          line-height: 20px;
          text-align: left;
          padding-left: 15px;
        }
      }

      .login-btn {
        margin-top: 40px;
        height: 45px;
      }
      
      .xieyi {
        color: #333;
        margin-top: 20px;
      }
    }
  }
</style>
