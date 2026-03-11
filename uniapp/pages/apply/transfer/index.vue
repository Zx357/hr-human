<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="90px">
        <uni-forms-item label="调动类型" name="transferType" required>
          <uni-data-select v-model="formData.transferType" :localdata="transferTypes" placeholder="请选择调动类型"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item label="原部门" name="fromDept">
          <uni-easyinput v-model="formData.fromDept" placeholder="当前部门" disabled />
        </uni-forms-item>
        
        <uni-forms-item label="目标部门" name="toDept" required>
          <uni-easyinput v-model="formData.toDept" placeholder="请输入目标部门" />
        </uni-forms-item>
        
        <uni-forms-item label="原岗位" name="fromPosition">
          <uni-easyinput v-model="formData.fromPosition" placeholder="当前岗位" disabled />
        </uni-forms-item>
        
        <uni-forms-item label="目标岗位" name="toPosition" required>
          <uni-easyinput v-model="formData.toPosition" placeholder="请输入目标岗位" />
        </uni-forms-item>
        
        <uni-forms-item label="期望生效日" name="effectDate" required>
          <uni-datetime-picker type="date" v-model="formData.effectDate" placeholder="请选择期望生效日期" />
        </uni-forms-item>
        
        <uni-forms-item label="调动原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入调动原因" :maxlength="300" />
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
        transferType: '',
        fromDept: '',
        toDept: '',
        fromPosition: '',
        toPosition: '',
        effectDate: '',
        reason: ''
      },
      transferTypes: [
        { value: '1', text: '部门调动' },
        { value: '2', text: '岗位调动' },
        { value: '3', text: '部门+岗位调动' }
      ],
      rules: {
        transferType: { rules: [{ required: true, errorMessage: '请选择调动类型' }] },
        toDept: { rules: [{ required: true, errorMessage: '请输入目标部门' }] },
        toPosition: { rules: [{ required: true, errorMessage: '请输入目标岗位' }] },
        effectDate: { rules: [{ required: true, errorMessage: '请选择期望生效日期' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入调动原因' }] }
      }
    }
  },
  onLoad() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        this.formData.fromDept = userInfo.deptName || ''
        this.formData.fromPosition = userInfo.position || ''
      }
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
          appType: 'transfer',
          title: '调动申请',
          transferType: this.formData.transferType,
          fromPosition: this.formData.fromDept,
          toPosition: this.formData.toPosition,
          effectDate: this.formData.effectDate,
          reason: this.formData.reason,
          remark: `目标部门: ${this.formData.toDept}`
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
  background-color: #646cff;
  color: #fff;
}
</style>
