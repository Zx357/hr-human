<template>
  <view class="password-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">修改密码</text>
      </view>
    </tn-navbar>

    <scroll-view scroll-y class="password-scroll" :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }">
      <view class="form-card">
        <view class="field-block">
          <view class="field-label">原密码</view>
          <input
            v-model="form.oldPassword"
            class="field-input"
            type="password"
            password
            maxlength="20"
            placeholder="请输入原密码"
            placeholder-class="placeholder"
          />
        </view>

        <view class="field-block">
          <view class="field-label">新密码</view>
          <input
            v-model="form.newPassword"
            class="field-input"
            type="password"
            password
            maxlength="20"
            placeholder="6-20位,须同时包含字母和数字"
            placeholder-class="placeholder"
          />
        </view>

        <view class="field-block">
          <view class="field-label">确认新密码</view>
          <input
            v-model="form.confirmPassword"
            class="field-input"
            type="password"
            password
            maxlength="20"
            placeholder="请再次输入新密码"
            placeholder-class="placeholder"
          />
        </view>

        <view class="tips">修改成功后需要使用新密码重新登录。</view>
      </view>

      <tn-button
        bg-color="#3668FC"
        text-color="#FFFFFF"
        shape="round"
        width="90%"
        height="88rpx"
        :fontSize="30"
        :disabled="submitting"
        @click="submit"
      >
        <text>{{ submitting ? '提交中...' : '确认修改' }}</text>
      </tn-button>
    </scroll-view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { useStore } from 'vuex'
import { mobileChangePassword } from '@/api/login'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const submitting = ref(false)

const validate = () => {
  if (!form.oldPassword) return '请输入原密码'
  if (!form.newPassword || form.newPassword.length < 6 || form.newPassword.length > 20) {
    return '新密码长度须为6-20位'
  }
  if (!/[A-Za-z]/.test(form.newPassword) || !/\d/.test(form.newPassword)) {
    return '新密码须同时包含字母和数字'
  }
  if (form.newPassword === form.oldPassword) {
    return '新密码不能与原密码相同'
  }
  if (form.newPassword !== form.confirmPassword) {
    return '两次输入的新密码不一致'
  }
  return ''
}

const submit = async () => {
  const error = validate()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await mobileChangePassword(form.oldPassword, form.newPassword)
    uni.showToast({ title: '密码修改成功，请重新登录', icon: 'none' })
    // 改密后后端已吊销会话,前端清理本地登录态并回到登录页
    setTimeout(async () => {
      await store.dispatch('ClearSession').catch(() => {})
      uni.reLaunch({ url: '/pages/login' })
    }, 1200)
  } catch (error) {
    uni.showToast({ title: (error && error.msg) || '修改失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.password-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #f8f7f8;
}

.nav-back {
  width: 72rpx;
  height: 54rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.12);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-scroll {
  box-sizing: border-box;
  height: 100vh;
  padding: 0 24rpx;
}

.form-card {
  margin-bottom: 40rpx;
  padding: 12rpx 26rpx 26rpx;
  background: #fff;
  border-radius: 18rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.field-block {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f3f2f7;

  &:last-of-type {
    border-bottom: none;
  }
}

.field-label {
  margin-bottom: 14rpx;
  color: #1d2541;
  font-size: 28rpx;
  font-weight: 700;
}

.field-input {
  width: 100%;
  height: 76rpx;
  padding: 0 22rpx;
  box-sizing: border-box;
  background: #f4f5f9;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.placeholder {
  color: #b9c0ca;
}

.tips {
  margin-top: 18rpx;
  color: #9aa4b2;
  font-size: 24rpx;
  line-height: 1.6;
}
</style>
