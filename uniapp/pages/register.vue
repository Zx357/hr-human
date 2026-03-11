<template>
  <app-page padding="0">
    <view class="login-hero">
      <view class="brand">
        <image class="logo" :src="globalConfig.appInfo.logo" mode="aspectFit" />
        <view class="brand-text">
          <text class="title">注册</text>
          <text class="subtitle">创建账号后即可登录</text>
        </view>
      </view>
    </view>

    <view class="content">
      <app-card class="card">
        <u-form ref="uForm" :model="registerForm" labelPosition="top">
          <u-form-item label="账号" prop="username">
            <u--input v-model="registerForm.username" placeholder="请输入账号" clearable />
          </u-form-item>
          <u-form-item label="密码" prop="password">
            <u--input v-model="registerForm.password" placeholder="请输入密码" type="password" clearable password />
          </u-form-item>
          <u-form-item label="确认密码" prop="confirmPassword">
            <u--input v-model="registerForm.confirmPassword" placeholder="请再次输入密码" type="password" clearable password />
          </u-form-item>
          <u-form-item v-if="captchaEnabled" label="验证码" prop="code">
            <view class="captcha-row">
              <u--input v-model="registerForm.code" placeholder="请输入验证码" clearable />
              <image class="captcha-img" :src="codeUrl" @click="getCode" mode="aspectFit" />
            </view>
          </u-form-item>
        </u-form>

        <u-button type="primary" shape="circle" :loading="loading" @click="handleRegister()">注册</u-button>

        <view class="footer">
          <text class="link" @click="handleUserLogin">使用已有账号登录</text>
        </view>
      </app-card>
    </view>
  </app-page>
</template>

<script>
  import { getCodeImg, register } from '@/api/login'

  export default {
    data() {
      return {
        codeUrl: "",
        captchaEnabled: true,
        globalConfig: getApp().globalData.config,
      loading: false,
        registerForm: {
          username: "",
          password: "",
          confirmPassword: "",
          code: "",
          uuid: ""
        }
      }
    },
    created() {
      this.getCode()
    },
    methods: {
      // 用户登录
      handleUserLogin() {
        this.$tab.navigateTo(`/pages/login`)
      },
      // 获取图形验证码
      getCode() {
        getCodeImg().then(res => {
          this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
          if (this.captchaEnabled) {
            this.codeUrl = 'data:image/gif;base64,' + res.img
            this.registerForm.uuid = res.uuid
          }
        })
      },
      // 注册方法
      async handleRegister() {
        if (this.registerForm.username === "") {
          this.$modal.msgError("请输入您的账号")
        } else if (this.registerForm.password === "") {
          this.$modal.msgError("请输入您的密码")
        } else if (this.registerForm.confirmPassword === "") {
          this.$modal.msgError("请再次输入您的密码")
        } else if (this.registerForm.password !== this.registerForm.confirmPassword) {
          this.$modal.msgError("两次输入的密码不一致")
        } else if (this.registerForm.code === "" && this.captchaEnabled) {
          this.$modal.msgError("请输入验证码")
        } else {
          this.loading = true
          this.$modal.loading("注册中，请耐心等待...")
          this.register()
        }
      },
      // 用户注册
      async register() {
        register(this.registerForm).then(res => {
          this.loading = false
          this.$modal.closeLoading()
          uni.showModal({
          	title: "系统提示",
          	content: "恭喜你，您的账号 " + this.registerForm.username + " 注册成功！",
          	success: function (res) {
          		if (res.confirm) {
                uni.redirectTo({ url: `/pages/login` });
          		}
          	}
          })
        }).catch(() => {
          this.loading = false
          if (this.captchaEnabled) {
            this.getCode()
          }
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

  .logo {
    width: 88rpx;
    height: 88rpx;
    border-radius: 18rpx;
    background-color: rgba(255, 255, 255, 0.2);
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

  .captcha-row {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .captcha-img {
    width: 220rpx;
    height: 72rpx;
    border-radius: 12rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.0);
    background-color: #fff;
  }

  .footer {
    margin-top: 18rpx;
    text-align: center;
    font-size: 24rpx;
  }

  .link {
    color: $app-primary;
  }

</style>
