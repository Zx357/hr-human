<template>
  <view class="apply-container">
    <view class="form-section">
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="80px">
        <uni-forms-item label="补卡日期" name="cardDate" required>
          <uni-datetime-picker type="date" v-model="formData.cardDate" placeholder="请选择补卡日期" />
        </uni-forms-item>
        
        <uni-forms-item label="补卡类型" name="cardType" required>
          <uni-data-select v-model="formData.cardType" :localdata="cardTypes" placeholder="请选择补卡类型"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item label="补卡时间" name="cardTime" required>
          <uni-datetime-picker type="time" v-model="formData.cardTime" placeholder="请选择补卡时间" />
        </uni-forms-item>
        
        <uni-forms-item label="补卡原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入补卡原因" :maxlength="200" />
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
        cardDate: '',
        cardType: '',
        cardTime: '',
        reason: ''
      },
      cardTypes: [
        { value: '上班补卡', text: '上班补卡' },
        { value: '下班补卡', text: '下班补卡' }
      ],
      rules: {
        cardDate: { rules: [{ required: true, errorMessage: '请选择补卡日期' }] },
        cardType: { rules: [{ required: true, errorMessage: '请选择补卡类型' }] },
        cardTime: { rules: [{ required: true, errorMessage: '请选择补卡时间' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入补卡原因' }] }
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
        
        const dateTime = `${this.formData.cardDate} ${this.formData.cardTime}:00`
        
        const data = {
          employeeId: this.employeeId,
          appType: 'makeup',
          title: `${this.formData.cardType}申请`,
          startTime: dateTime,
          endTime: dateTime,
          reason: this.formData.reason,
          category: this.formData.cardType,
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
