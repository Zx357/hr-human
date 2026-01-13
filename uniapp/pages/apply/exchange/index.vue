<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="80px">
        <uni-forms-item label="换休日期" name="exchangeDate" required>
          <uni-datetime-picker type="date" v-model="formData.exchangeDate" placeholder="请选择换休日期" />
        </uni-forms-item>
        
        <uni-forms-item label="换休时长" name="hours" required>
          <uni-data-select v-model="formData.hours" :localdata="hourOptions" placeholder="请选择换休时长"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item label="关联加班" name="overtimeId">
          <uni-easyinput v-model="formData.overtimeName" placeholder="请选择关联的加班记录" disabled @click="selectOvertime" />
        </uni-forms-item>
        
        <uni-forms-item label="换休原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入换休原因" :maxlength="200" />
        </uni-forms-item>
      </uni-forms>
    </view>
    
    <view class="btn-group">
      <button class="btn btn-primary" @click="handleSubmit">提交申请</button>
    </view>
  </view>
</template>

<script>
import { submitApplication } from '@/api/application'

export default {
  data() {
    return {
      formData: {
        exchangeDate: '',
        hours: '',
        overtimeId: '',
        overtimeName: '',
        reason: ''
      },
      hourOptions: [
        { value: 4, text: '半天(4小时)' },
        { value: 8, text: '一天(8小时)' }
      ],
      rules: {
        exchangeDate: { rules: [{ required: true, errorMessage: '请选择换休日期' }] },
        hours: { rules: [{ required: true, errorMessage: '请选择换休时长' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入换休原因' }] }
      }
    }
  },
  computed: {
    employeeId() {
      const userInfo = uni.getStorageSync('userInfo')
      return userInfo?.id || this.$store.state.user.id
    }
  },
  methods: {
    selectOvertime() {
      // TODO: 弹窗选择加班记录
      this.$modal.showToast('选择加班记录功能开发中')
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate()
        
        if (!this.employeeId) {
          this.$modal.msgError('请先登录')
          return
        }
        
        this.$modal.loading('提交中...')
        
        // 换休日期转换为开始和结束时间
        const startTime = `${this.formData.exchangeDate} 09:00:00`
        const endHour = this.formData.hours === 4 ? '13:00:00' : '18:00:00'
        const endTime = `${this.formData.exchangeDate} ${endHour}`
        
        const data = {
          employeeId: this.employeeId,
          appType: 'exchange',
          title: '换休申请',
          startTime: startTime,
          endTime: endTime,
          duration: this.formData.hours,
          reason: this.formData.reason,
          remark: this.formData.overtimeId ? `关联加班ID: ${this.formData.overtimeId}` : ''
        }
        
        await submitApplication(data)
        this.$modal.closeLoading()
        this.$modal.showToast('提交成功')
        setTimeout(() => uni.navigateBack(), 1500)
      } catch (e) {
        this.$modal.closeLoading()
        console.log('提交失败', e)
        this.$modal.showToast(e.message || '提交失败')
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
