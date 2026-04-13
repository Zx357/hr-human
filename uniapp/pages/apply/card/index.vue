<template>
  <view class="apply-container">
    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-icon">
        <text class="iconfont">&#xe630;</text>
      </view>
      <view class="header-info">
        <text class="header-title">补卡申请</text>
        <text class="header-desc">忘记打卡？提交补卡申请</text>
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
      <view class="section-title">补卡信息</view>
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="85px" label-align="right">
        <uni-forms-item label="补卡日期" name="cardDate" required>
          <uni-datetime-picker type="date" v-model="formData.cardDate" placeholder="请选择补卡日期" />
        </uni-forms-item>

        <uni-forms-item label="补卡类型" name="cardType" required>
          <view class="type-selector">
            <view
              class="type-option"
              :class="{ active: formData.cardType === '上班补卡' }"
              @click="formData.cardType = '上班补卡'"
            >
              <text class="type-icon">&#xe638;</text>
              <text>上班补卡</text>
            </view>
            <view
              class="type-option"
              :class="{ active: formData.cardType === '下班补卡' }"
              @click="formData.cardType = '下班补卡'"
            >
              <text class="type-icon">&#xe639;</text>
              <text>下班补卡</text>
            </view>
          </view>
        </uni-forms-item>

        <uni-forms-item label="补卡时间" name="cardTime" required>
          <uni-datetime-picker type="time" v-model="formData.cardTime" placeholder="请选择补卡时间" :hideSecond="true" />
        </uni-forms-item>

        <uni-forms-item label="补卡原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入补卡原因，如忘记打卡、手机故障等" :maxlength="200" autoHeight />
        </uni-forms-item>
      </uni-forms>
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
        cardDate: '',
        cardType: '',
        cardTime: '',
        reason: ''
      },
      rules: {
        cardDate: { rules: [{ required: true, errorMessage: '请选择补卡日期' }] },
        cardType: { rules: [{ required: true, errorMessage: '请选择补卡类型' }] },
        cardTime: { rules: [{ required: true, errorMessage: '请选择补卡时间' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入补卡原因' }] }
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
    extractTime(val) {
      if (!val) return ''
      const str = String(val).trim()
      const timePart = str.includes(' ') ? str.split(' ').pop() : str
      const segments = timePart.split(':')
      if (segments.length === 2) return `${segments[0]}:${segments[1]}:00`
      if (segments.length >= 3) return `${segments[0]}:${segments[1]}:${segments[2]}`
      return timePart
    },
    handleReset() {
      this.formData = { cardDate: '', cardType: '', cardTime: '', reason: '' }
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

        const dateTime = `${this.formData.cardDate} ${this.extractTime(this.formData.cardTime)}`

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
  background: linear-gradient(135deg, #f0932b, #f6b93b);
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
  border-left: 6rpx solid #f0932b;
}

.info-grid { display: flex; flex-wrap: wrap; }
.info-item { width: 50%; padding: 12rpx 16rpx; }
.info-label { font-size: 24rpx; color: #999; display: block; }
.info-value { font-size: 28rpx; color: #333; font-weight: 500; margin-top: 4rpx; display: block; }

.type-selector {
  display: flex;
  gap: 20rpx;
}
.type-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
  border-radius: 16rpx;
  background-color: #f7f8fa;
  border: 2rpx solid transparent;
  transition: all 0.2s;
  font-size: 26rpx;
  color: #666;
}
.type-option.active {
  background-color: #fff7ed;
  border-color: #f0932b;
  color: #f0932b;
  font-weight: 600;
}
.type-icon {
  font-family: 'iconfont';
  font-size: 44rpx;
  margin-bottom: 8rpx;
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
.btn-primary { background-color: #f0932b; color: #fff; }
.btn-primary[disabled] { opacity: 0.6; }
</style>
