<template>
  <view class="apply-container">
    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-icon">
        <text class="iconfont">&#xe635;</text>
      </view>
      <view class="header-info">
        <text class="header-title">出差申请</text>
        <text class="header-desc">填写出差信息，提交后等待审批</text>
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
      <view class="section-title">出差信息</view>
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="85px" label-align="right">
        <uni-forms-item label="出差地点" name="destination" required>
          <uni-easyinput v-model="formData.destination" placeholder="请输入出差目的地" />
        </uni-forms-item>

        <uni-forms-item label="开始日期" name="startDate" required>
          <uni-datetime-picker type="date" v-model="formData.startDate" placeholder="请选择开始日期" />
        </uni-forms-item>

        <uni-forms-item label="结束日期" name="endDate" required>
          <uni-datetime-picker type="date" v-model="formData.endDate" placeholder="请选择结束日期" />
        </uni-forms-item>

        <uni-forms-item label="出差天数" name="duration">
          <view class="duration-display">
            <text v-if="travelDays > 0" class="duration-text">{{ travelDays }} 天</text>
            <text v-else class="placeholder-text">选择日期后自动计算</text>
          </view>
        </uni-forms-item>

        <uni-forms-item label="出差事由" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入出差事由" :maxlength="200" autoHeight />
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
import { submitApplication, calculateLeaveHours } from '@/api/application'
import { mapGetters } from 'vuex'

export default {
  data() {
    return {
      formData: {
        destination: '',
        startDate: '',
        endDate: '',
        reason: ''
      },
      rules: {
        destination: { rules: [{ required: true, errorMessage: '请输入出差地点' }] },
        startDate: { rules: [{ required: true, errorMessage: '请选择开始日期' }] },
        endDate: { rules: [{ required: true, errorMessage: '请选择结束日期' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入出差事由' }] }
      },
      submitting: false,
      travelDays: 0,
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
  watch: {
    'formData.startDate': 'calcDays',
    'formData.endDate': 'calcDays'
  },
  onLoad() {
    const info = uni.getStorageSync('userInfo')
    if (info) this.userInfo = info
  },
  methods: {
    calcDays() {
      if (this.formData.startDate && this.formData.endDate) {
        const start = new Date(this.formData.startDate)
        const end = new Date(this.formData.endDate)
        const diff = (end - start) / (1000 * 60 * 60 * 24) + 1
        this.travelDays = diff > 0 ? diff : 0
      } else {
        this.travelDays = 0
      }
    },
    handleReset() {
      this.formData = { destination: '', startDate: '', endDate: '', reason: '' }
      this.travelDays = 0
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate()

        if (!this.employeeId) {
          this.$modal.msgError('请先登录')
          return
        }

        if (this.travelDays <= 0) {
          this.$modal.msgError('结束日期不能早于开始日期')
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
          duration: this.travelDays,
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
  background: linear-gradient(135deg, #0984e3, #74b9ff);
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
  border-left: 6rpx solid #0984e3;
}

.info-grid { display: flex; flex-wrap: wrap; }
.info-item { width: 50%; padding: 12rpx 16rpx; }
.info-label { font-size: 24rpx; color: #999; display: block; }
.info-value { font-size: 28rpx; color: #333; font-weight: 500; margin-top: 4rpx; display: block; }

.duration-display {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 20rpx;
  background-color: #f7f8fa;
  border-radius: 12rpx;
}
.duration-text { font-size: 30rpx; color: #0984e3; font-weight: 600; }
.placeholder-text { font-size: 26rpx; color: #c0c4cc; }

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
.btn-primary { background-color: #0984e3; color: #fff; }
.btn-primary[disabled] { opacity: 0.6; }
</style>
