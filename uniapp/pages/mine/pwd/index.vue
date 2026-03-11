<template>
  <app-page padding="24rpx">
    <app-card padding="28rpx">
      <view class="form-group">
        <text class="form-label">旧密码</text>
        <view class="input-wrapper">
          <input
            class="form-input"
            v-model="form.oldPassword"
            type="password"
            :password="true"
            placeholder="请输入旧密码（默认123456）"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>
      <view class="form-group">
        <text class="form-label">新密码</text>
        <view class="input-wrapper">
          <input
            class="form-input"
            v-model="form.newPassword"
            type="password"
            :password="true"
            placeholder="请输入新密码（6-20位）"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>
      <view class="form-group">
        <text class="form-label">确认密码</text>
        <view class="input-wrapper">
          <input
            class="form-input"
            v-model="form.confirmPassword"
            type="password"
            :password="true"
            placeholder="请再次输入新密码"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <u-button type="primary" shape="circle" :loading="loading" @click="submit">提交</u-button>
    </app-card>
  </app-page>
</template>

<script>
  import { updateUserPwd } from "@/api/system/user"

  export default {
    data() {
      return {
        form: {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        },
        loading: false
      }
    },
    methods: {
      submit() {
        const { oldPassword, newPassword, confirmPassword } = this.form
        if (!oldPassword) return this.$modal.msgError('旧密码不能为空')
        if (!newPassword) return this.$modal.msgError('新密码不能为空')
        if (newPassword.length < 6 || newPassword.length > 20) return this.$modal.msgError('新密码长度需为 6-20 位')
        if (!confirmPassword) return this.$modal.msgError('确认密码不能为空')
        if (newPassword !== confirmPassword) return this.$modal.msgError('两次输入的密码不一致')

        this.loading = true
        updateUserPwd(oldPassword, newPassword)
          .then(() => {
            this.$modal.msgSuccess('修改成功')
            this.form.oldPassword = ''
            this.form.newPassword = ''
            this.form.confirmPassword = ''
          })
          .finally(() => {
            this.loading = false
          })
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "@/static/scss/tokens.scss";

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
</style>
