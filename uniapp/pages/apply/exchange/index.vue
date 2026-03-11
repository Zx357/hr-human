<template>
  <view class="apply-container">
    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-icon">
        <text class="iconfont">&#xe636;</text>
      </view>
      <view class="header-info">
        <text class="header-title">换休申请</text>
        <text class="header-desc">使用加班时长兑换休息时间</text>
      </view>
    </view>

    <!-- 申请人信息 -->
    <view class="section-card" v-if="userInfo.name">
      <view class="section-title">申请人信息</view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">姓名</text>
          <text class="info-value">{{ userInfo.name }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">部门</text>
          <text class="info-value">{{ userInfo.deptName || '-' }}</text>
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="section-card">
      <view class="section-title">换休信息</view>
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="85px" label-align="right">
        <uni-forms-item label="换休日期" name="exchangeDate" required>
          <uni-datetime-picker type="date" v-model="formData.exchangeDate" placeholder="请选择换休日期" />
        </uni-forms-item>

        <uni-forms-item label="换休时长" name="hours" required>
          <view class="hours-selector">
            <view
              class="hours-option"
              :class="{ active: formData.hours === 4 }"
              @click="formData.hours = 4"
            >
              <text class="hours-value">4</text>
              <text class="hours-unit">小时</text>
              <text class="hours-label">半天</text>
            </view>
            <view
              class="hours-option"
              :class="{ active: formData.hours === 8 }"
              @click="formData.hours = 8"
            >
              <text class="hours-value">8</text>
              <text class="hours-unit">小时</text>
              <text class="hours-label">一天</text>
            </view>
          </view>
        </uni-forms-item>

        <uni-forms-item label="换休原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入换休原因" :maxlength="200" autoHeight />
        </uni-forms-item>
      </uni-forms>
    </view>

    <!-- 换休说明 -->
    <view class="section-card tip-card">
      <view class="tip-title">换休说明</view>
      <view class="tip-list">
        <text class="tip-item">· 换休需有足够的加班时长余额</text>
        <text class="tip-item">· 半天换休：09:00 - 13:00</text>
        <text class="tip-item">· 全天换休：09:00 - 18:00</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="btn-group">
      <button class="btn btn-default" @click="handleReset">重置</button>
      <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
        {{ submitting ? '提交中...' : '提交申请' }}
      </button>
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
        exchangeDate: '',
        hours: '',
        reason: ''
      },
      rules: {
        exchangeDate: { rules: [{ required: true, errorMessage: '请选择换休日期' }] },
        hours: { rules: [{ required: true, errorMessage: '请选择换休时长' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入换休原因' }] }
      },
      submitting: false,
      userInfo: {}
    }
  },
  computed: {
    ...mapGetters(['id']),
    employeeId() {
      if (this.id) return this.id
      const info = uni.getStorageSync('userInfo')
      return info?.id
    }
  },
  onLoad() {
    const info = uni.getStorageSync('userInfo')
    if (info) this.userInfo = info
  },
  methods: {
    handleReset() {
      this.formData = { exchangeDate: '', hours: '', reason: '' }
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
          this.$modal.msgError(e?.message || e || '提交失败')
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
  background-color: #f4f5f7;
  padding-bottom: 140rpx;
}

.header-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #00b894, #55efc4);
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 36rpx 32rpx;
  color: #fff;
}
.header-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  font-size: 40rpx;
}
.header-info { flex: 1; }
.header-title { font-size: 36rpx; font-weight: 700; display: block; }
.header-desc { font-size: 24rpx; opacity: 0.85; margin-top: 6rpx; display: block; }

.section-card {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 20rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #00b894;
}

.info-grid { display: flex; flex-wrap: wrap; }
.info-item { width: 50%; padding: 12rpx 16rpx; }
.info-label { font-size: 24rpx; color: #999; display: block; }
.info-value { font-size: 28rpx; color: #333; font-weight: 500; margin-top: 4rpx; display: block; }

.hours-selector {
  display: flex;
  gap: 24rpx;
}
.hours-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28rpx 0;
  border-radius: 16rpx;
  background-color: #f7f8fa;
  border: 2rpx solid transparent;
  transition: all 0.2s;
}
.hours-option.active {
  background-color: #eafff8;
  border-color: #00b894;
}
.hours-value {
  font-size: 48rpx;
  font-weight: 700;
  color: #999;
  line-height: 1;
}
.hours-option.active .hours-value {
  color: #00b894;
}
.hours-unit {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}
.hours-option.active .hours-unit {
  color: #00b894;
}
.hours-label {
  font-size: 24rpx;
  color: #bbb;
  margin-top: 8rpx;
}
.hours-option.active .hours-label {
  color: #00b894;
  font-weight: 500;
}

.tip-card {
  background-color: #f0faf6;
}
.tip-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #00b894;
  margin-bottom: 12rpx;
}
.tip-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.tip-item {
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
}

.btn-group {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
}
.btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  text-align: center;
  border: none;
}
.btn-default { background-color: #f4f5f7; color: #666; }
.btn-primary { background-color: #00b894; color: #fff; }
.btn-primary[disabled] { opacity: 0.6; }
</style>
