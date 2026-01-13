<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="80px">
        <uni-forms-item label="请假类型" name="category" required>
          <uni-data-select v-model="formData.category" :localdata="leaveTypes" placeholder="请选择请假类型"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item label="开始时间" name="startTime" required>
          <uni-datetime-picker type="datetime" v-model="formData.startTime" placeholder="请选择开始时间" />
        </uni-forms-item>
        
        <uni-forms-item label="结束时间" name="endTime" required>
          <uni-datetime-picker type="datetime" v-model="formData.endTime" placeholder="请选择结束时间" />
        </uni-forms-item>
        
        <uni-forms-item label="请假天数" name="duration">
          <uni-easyinput v-model="formData.duration" type="number" placeholder="自动计算" disabled />
        </uni-forms-item>
        
        <uni-forms-item label="请假原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入请假原因" :maxlength="200" />
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
        category: '',
        startTime: '',
        endTime: '',
        duration: '',
        reason: ''
      },
      leaveTypes: [
        { value: '事假', text: '事假' },
        { value: '病假', text: '病假' },
        { value: '年假', text: '年假' },
        { value: '婚假', text: '婚假' },
        { value: '产假', text: '产假' },
        { value: '陪产假', text: '陪产假' },
        { value: '丧假', text: '丧假' }
      ],
      rules: {
        category: { rules: [{ required: true, errorMessage: '请选择请假类型' }] },
        startTime: { rules: [{ required: true, errorMessage: '请选择开始时间' }] },
        endTime: { rules: [{ required: true, errorMessage: '请选择结束时间' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入请假原因' }] }
      },
      submitting: false
    }
  },
  computed: {
    ...mapGetters(['id']),
    employeeId() {
      // 优先从 store 获取，其次从 storage 获取
      if (this.id) return this.id
      const userInfo = uni.getStorageSync('userInfo')
      return userInfo?.id
    }
  },
  watch: {
    'formData.startTime': 'calcDays',
    'formData.endTime': 'calcDays'
  },
  methods: {
    calcDays() {
      if (this.formData.startTime && this.formData.endTime) {
        const start = new Date(this.formData.startTime)
        const end = new Date(this.formData.endTime)
        const diff = (end - start) / (1000 * 60 * 60 * 24)
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
        
        const data = {
          employeeId: this.employeeId,
          appType: 'leave',
          title: `${this.formData.category}申请`,
          startTime: this.formData.startTime,
          endTime: this.formData.endTime,
          duration: parseFloat(this.formData.duration) || 0,
          reason: this.formData.reason,
          category: this.formData.category,
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
