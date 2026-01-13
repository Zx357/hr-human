<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="80px">
        <uni-forms-item label="加班日期" name="overtimeDate" required>
          <uni-datetime-picker type="date" v-model="formData.overtimeDate" placeholder="请选择加班日期" />
        </uni-forms-item>
        
        <uni-forms-item label="开始时间" name="startTime" required>
          <uni-datetime-picker type="time" v-model="formData.startTime" placeholder="请选择开始时间" />
        </uni-forms-item>
        
        <uni-forms-item label="结束时间" name="endTime" required>
          <uni-datetime-picker type="time" v-model="formData.endTime" placeholder="请选择结束时间" />
        </uni-forms-item>
        
        <uni-forms-item label="加班时长" name="duration">
          <uni-easyinput v-model="formData.duration" placeholder="自动计算(小时)" disabled />
        </uni-forms-item>
        
        <uni-forms-item label="加班原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入加班原因" :maxlength="200" />
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
        overtimeDate: '',
        startTime: '',
        endTime: '',
        duration: '',
        reason: ''
      },
      rules: {
        overtimeDate: { rules: [{ required: true, errorMessage: '请选择加班日期' }] },
        startTime: { rules: [{ required: true, errorMessage: '请选择开始时间' }] },
        endTime: { rules: [{ required: true, errorMessage: '请选择结束时间' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入加班原因' }] }
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
    'formData.startTime': 'calcHours',
    'formData.endTime': 'calcHours'
  },
  methods: {
    calcHours() {
      if (this.formData.startTime && this.formData.endTime) {
        const [sh, sm] = this.formData.startTime.split(':').map(Number)
        const [eh, em] = this.formData.endTime.split(':').map(Number)
        const diff = (eh * 60 + em - sh * 60 - sm) / 60
        this.formData.duration = diff > 0 ? diff.toFixed(1) : ''
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
        
        const startDateTime = `${this.formData.overtimeDate} ${this.formData.startTime}:00`
        const endDateTime = `${this.formData.overtimeDate} ${this.formData.endTime}:00`
        
        const data = {
          employeeId: this.employeeId,
          appType: 'overtime',
          title: '加班申请',
          startTime: startDateTime,
          endTime: endDateTime,
          duration: parseFloat(this.formData.duration) || 0,
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
