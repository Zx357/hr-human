<template>
  <app-page padding="0">
    <view class="login-hero">
      <view class="brand">
        <view class="brand-text">
          <text class="title">HR 人事管理</text>
          <text class="subtitle">移动端</text>
        </view>
      </view>
    </view>

    <view class="content">
      <app-card class="card">
        <view class="form-group">
          <text class="form-label">工号</text>
          <view class="input-wrapper">
            <input
              class="form-input"
              v-model="loginForm.employeeNo"
              placeholder="请输入工号"
              placeholder-class="input-placeholder"
            />
            <view v-if="loginForm.employeeNo" class="input-clear" @click="loginForm.employeeNo = ''">
              <text class="clear-icon">×</text>
            </view>
          </view>
        </view>
        <view class="form-group">
          <text class="form-label">密码</text>
          <view class="input-wrapper">
            <input
              class="form-input"
              v-model="loginForm.password"
              :password="!showPassword"
              placeholder="请输入密码"
              placeholder-class="input-placeholder"
            />
            <view class="input-suffix" @click="showPassword = !showPassword">
              <u-icon :name="showPassword ? 'eye-off' : 'eye-fill'" size="20" color="#909399"></u-icon>
            </view>
          </view>
        </view>

        <u-button type="primary" shape="circle" :loading="loading" @click="handleLogin">登录</u-button>

        <view class="agreements">
          <text class="muted">登录即代表同意</text>
          <text class="link" @click="handleUserAgrement">《用户协议》</text>
          <text class="link" @click="handlePrivacy">《隐私协议》</text>
        </view>
      </app-card>
    </view>
  </app-page>
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
        },
        loading: false,
        showPassword: false
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
          this.loading = true
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
          this.loading = false
        })
      },
      // 登录成功后，处理函数
      loginSuccess(result) {
        // 设置用户信息
        this.$store.dispatch('GetInfo').then(res => {
          console.log('获取用户信息成功，跳转主页', res)
          this.$modal.closeLoading()
          this.$tab.reLaunch('/pages/index')
          this.loading = false
        }).catch(err => {
          console.log('获取用户信息失败，但仍跳转主页', err)
          this.$modal.closeLoading()
          this.$tab.reLaunch('/pages/index')
          this.loading = false
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "@/static/scss/tokens.scss";

  .login-hero {
    padding: 120rpx 32rpx 56rpx;
    background: linear-gradient(135deg, $app-primary 0%, $app-primary-2 100%);
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  .brand-text {
    display: flex;
    flex-direction: column;
  }

  .title {
    font-size: 40rpx;
    font-weight: 700;
    color: #fff;
    letter-spacing: 1rpx;
  }

  .subtitle {
    margin-top: 8rpx;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.85);
  }

  .content {
    padding: 0 24rpx;
    margin-top: -28rpx;
  }

  .card {
    padding: 28rpx;
  }

  .form-group {
    margin-bottom: 28rpx;
  }

  .form-label {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #303133;
    margin-bottom: 12rpx;
  }

  .input-wrapper {
    display: flex;
    align-items: center;
    border: 1rpx solid #dcdfe6;
    border-radius: 12rpx;
    padding: 0 20rpx;
    height: 80rpx;
    background-color: #f7f8fa;
    transition: border-color 0.2s;
  }

  .form-input {
    flex: 1;
    height: 80rpx;
    font-size: 28rpx;
    color: #303133;
    background: transparent;
  }

  .input-placeholder {
    color: #c0c4cc;
    font-size: 28rpx;
  }

  .input-clear {
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 8rpx;
  }

  .clear-icon {
    font-size: 32rpx;
    color: #c0c4cc;
    line-height: 1;
  }

  .input-suffix {
    margin-left: 8rpx;
    padding: 8rpx;
  }

  .agreements {
    margin-top: 18rpx;
    text-align: center;
    font-size: 24rpx;
  }

  .muted {
    color: $app-text-2;
  }

  .link {
    color: $app-primary;
  }
</style>
