<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="90px">
        <uni-forms-item label="转正日期" name="regularDate" required>
          <uni-datetime-picker type="date" v-model="formData.regularDate" placeholder="请选择转正日期" />
        </uni-forms-item>
        
        <uni-forms-item label="工作总结" name="evaluation" required>
          <uni-easyinput type="textarea" v-model="formData.evaluation" placeholder="请输入试用期工作总结" :maxlength="500" />
        </uni-forms-item>
        
        <uni-forms-item label="申请说明" name="reason">
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入申请说明（选填）" :maxlength="300" />
        </uni-forms-item>
      </uni-forms>
    </view>
    
    <view class="btn-group">
      <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">提交申请</button>
    </view>
  </view>
</template>

<script>
import { submitApplication } from '@/api/application'
import { mapGetters } from 'vuex'

export default {
  data() {
    return {
      formData: {
        regularDate: '',
        evaluation: '',
        reason: ''
      },
      rules: {
        regularDate: { rules: [{ required: true, errorMessage: '请选择转正日期' }] },
        evaluation: { rules: [{ required: true, errorMessage: '请输入工作总结' }] }
      },
      submitting: false
    }
  },
  computed: {
    ...mapGetters(['id']),
    employeeId() {
      if (this.id) return this.id
      const userInfo = uni.getStorageSync('userInfo')
      return userInfo?.id
    }
  },
  methods: {
    async handleSubmit() {
      try {
        await this.$refs.form.validate()
        
        if (!this.employeeId) {
          this.$modal.msgError('请先登录')
          return
        }
        
        this.submitting = true
        this.$modal.loading('提交中...')
        
        const data = {
          employeeId: this.employeeId,
          appType: 'regularization',
          title: '转正申请',
          regularDate: this.formData.regularDate,
          evaluation: this.formData.evaluation,
          reason: this.formData.reason,
          status: 0
        }
        
        const res = await submitApplication(data)
        this.$modal.closeLoading()
        
        if (res.code === 200) {
          this.$modal.showToast('提交成功')
          setTimeout(() => uni.navigateBack(), 1500)
        } else {
          this.$modal.msgError(res.msg || '提交失败')
        }
      } catch (e) {
        this.$modal.closeLoading()
        if (e !== 'validate') {
          this.$modal.msgError(e || '提交失败')
        }
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.apply-container {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 120rpx;
}

.form-section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.btn-group {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.btn {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
}

.btn-primary {
  background-color: #2d8cf0;
  color: #fff;
}
</style>
