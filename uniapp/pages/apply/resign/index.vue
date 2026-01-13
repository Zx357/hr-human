<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="90px">
        <uni-forms-item label="离职类型" name="resignType" required>
          <uni-data-select v-model="formData.resignType" :localdata="resignTypes" placeholder="请选择离职类型"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item label="期望离职日" name="resignDate" required>
          <uni-datetime-picker type="date" v-model="formData.resignDate" placeholder="请选择期望离职日期" />
        </uni-forms-item>
        
        <uni-forms-item label="离职原因" name="resignReason" required>
          <uni-data-select v-model="formData.resignReason" :localdata="resignReasons" placeholder="请选择离职原因"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item label="详细说明" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请详细说明离职原因" :maxlength="500" />
        </uni-forms-item>
        
        <uni-forms-item label="工作交接" name="handover">
          <uni-easyinput type="textarea" v-model="formData.handover" placeholder="请说明工作交接安排" :maxlength="300" />
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
        resignType: '',
        resignDate: '',
        resignReason: '',
        reason: '',
        handover: ''
      },
      resignTypes: [
        { value: '1', text: '主动离职' },
        { value: '2', text: '协商离职' }
      ],
      resignReasons: [
        { value: '1', text: '个人发展' },
        { value: '2', text: '薪资待遇' },
        { value: '3', text: '工作环境' },
        { value: '4', text: '家庭原因' },
        { value: '5', text: '健康原因' },
        { value: '6', text: '其他原因' }
      ],
      rules: {
        resignType: { rules: [{ required: true, errorMessage: '请选择离职类型' }] },
        resignDate: { rules: [{ required: true, errorMessage: '请选择期望离职日期' }] },
        resignReason: { rules: [{ required: true, errorMessage: '请选择离职原因' }] },
        reason: { rules: [{ required: true, errorMessage: '请详细说明离职原因' }] }
      }
    }
  },
  methods: {
    getResignReasonText(value) {
      const item = this.resignReasons.find(r => r.value === value)
      return item ? item.text : ''
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate()
        
        const userInfo = uni.getStorageSync('userInfo')
        const employeeId = userInfo?.id || this.$store.state.user.id
        
        if (!employeeId) {
          this.$modal.msgError('请先登录')
          return
        }
        
        this.$modal.loading('提交中...')
        
        const data = {
          employeeId: employeeId,
          appType: 'resignation',
          title: '离职申请',
          resignType: this.formData.resignType === '1' ? '主动离职' : '协商离职',
          lastWorkDate: this.formData.resignDate,
          reason: `离职原因: ${this.getResignReasonText(this.formData.resignReason)}\n详细说明: ${this.formData.reason}`,
          remark: this.formData.handover ? `工作交接: ${this.formData.handover}` : ''
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
