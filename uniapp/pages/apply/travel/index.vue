<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="80px">
        <uni-forms-item label="出差地点" name="destination" required>
          <uni-easyinput v-model="formData.destination" placeholder="请输入出差地点" />
        </uni-forms-item>
        
        <uni-forms-item label="开始日期" name="startDate" required>
          <uni-datetime-picker type="date" v-model="formData.startDate" placeholder="请选择开始日期" />
        </uni-forms-item>
        
        <uni-forms-item label="结束日期" name="endDate" required>
          <uni-datetime-picker type="date" v-model="formData.endDate" placeholder="请选择结束日期" />
        </uni-forms-item>
        
        <uni-forms-item label="出差天数" name="duration">
          <uni-easyinput v-model="formData.duration" placeholder="自动计算" disabled />
        </uni-forms-item>
        
        <uni-forms-item label="出差事由" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入出差事由" :maxlength="200" />
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
        destination: '',
        startDate: '',
        endDate: '',
        duration: '',
        reason: ''
      },
      rules: {
        destination: { rules: [{ required: true, errorMessage: '请输入出差地点' }] },
        startDate: { rules: [{ required: true, errorMessage: '请选择开始日期' }] },
        endDate: { rules: [{ required: true, errorMessage: '请选择结束日期' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入出差事由' }] }
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
  watch: {
    'formData.startDate': 'calcDays',
    'formData.endDate': 'calcDays'
  },
  methods: {
    calcDays() {
      if (this.formData.startDate && this.formData.endDate) {
        const start = new Date(this.formData.startDate)
        const end = new Date(this.formData.endDate)
        const diff = (end - start) / (1000 * 60 * 60 * 24) + 1
        this.formData.duration = diff > 0 ? diff : ''
      }
    },
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
          appType: 'business',
          title: `出差申请-${this.formData.destination}`,
          startTime: `${this.formData.startDate} 00:00:00`,
          endTime: `${this.formData.endDate} 23:59:59`,
          duration: parseFloat(this.formData.duration) || 0,
          reason: this.formData.reason,
          remark: this.formData.destination,
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
