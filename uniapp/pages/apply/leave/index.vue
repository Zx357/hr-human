<template>
  <view class="apply-container">
    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-icon">
        <text class="iconfont">&#xe62b;</text>
      </view>
      <view class="header-info">
        <text class="header-title">请假申请</text>
        <text class="header-desc">请填写请假信息，提交后等待审批</text>
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
      <view class="section-title">请假信息</view>
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="85px" label-align="right">
        <uni-forms-item label="请假类型" name="leaveType" required>
          <uni-data-select v-model="formData.leaveType" :localdata="leaveTypes" placeholder="请选择请假类型"></uni-data-select>
        </uni-forms-item>

        <uni-forms-item label="开始时间" name="startTime" required>
          <uni-datetime-picker type="datetime" v-model="formData.startTime" :hide-second="true" placeholder="请选择开始时间" />
        </uni-forms-item>

        <uni-forms-item label="结束时间" name="endTime" required>
          <uni-datetime-picker type="datetime" v-model="formData.endTime" :hide-second="true" placeholder="请选择结束时间" />
        </uni-forms-item>

        <uni-forms-item label="请假时长" name="duration">
          <view class="duration-display">
            <text v-if="calculating" class="calc-text">计算中...</text>
            <text v-else-if="leaveHours > 0" class="duration-text">{{ leaveHours }} 小时</text>
            <text v-else class="placeholder-text">选择时间后自动计算</text>
          </view>
          <view v-if="leaveHours === 0 && formData.startTime && formData.endTime && !calculating" class="warn-tip">
            提示：请假时长为0，可能该时段无排班
          </view>
        </uni-forms-item>

        <uni-forms-item label="请假原因" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入请假原因" :maxlength="200" autoHeight />
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
        leaveType: '',
        startTime: '',
        endTime: '',
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
        leaveType: { rules: [{ required: true, errorMessage: '请选择请假类型' }] },
        startTime: { rules: [{ required: true, errorMessage: '请选择开始时间' }] },
        endTime: { rules: [{ required: true, errorMessage: '请选择结束时间' }] },
        reason: { rules: [{ required: true, errorMessage: '请输入请假原因' }] }
      },
      submitting: false,
      leaveHours: 0,
      calculating: false,
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
    'formData.startTime': 'calcHours',
    'formData.endTime': 'calcHours'
  },
  onLoad() {
    const info = uni.getStorageSync('userInfo')
    if (info) this.userInfo = info
  },
  methods: {
    async calcHours() {
      if (!this.formData.startTime || !this.formData.endTime || !this.employeeId) {
        this.leaveHours = 0
        return
      }
      this.calculating = true
      try {
        const start = this.formData.startTime.length === 16 ? this.formData.startTime + ':00' : this.formData.startTime
        const end = this.formData.endTime.length === 16 ? this.formData.endTime + ':00' : this.formData.endTime
        const res = await calculateLeaveHours(this.employeeId, start, end)
        this.leaveHours = res.data || 0
      } catch (e) {
        // 如果后端接口不可用，回退到本地计算
        const start = new Date(this.formData.startTime)
        const end = new Date(this.formData.endTime)
        const diffHours = (end - start) / (1000 * 60 * 60)
        this.leaveHours = diffHours > 0 ? Math.round(diffHours * 10) / 10 : 0
      } finally {
        this.calculating = false
      }
    },
    handleReset() {
      this.formData = { leaveType: '', startTime: '', endTime: '', reason: '' }
      this.leaveHours = 0
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

        const start = this.formData.startTime.length === 16 ? this.formData.startTime + ':00' : this.formData.startTime
        const end = this.formData.endTime.length === 16 ? this.formData.endTime + ':00' : this.formData.endTime

        const data = {
          employeeId: this.employeeId,
          appType: 'leave',
          title: this.formData.leaveType,
          startTime: start,
          endTime: end,
          duration: this.leaveHours,
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
  background: linear-gradient(135deg, #646cff, #7c83ff);
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
.header-info {
  flex: 1;
}
.header-title {
  font-size: 36rpx;
  font-weight: 700;
  display: block;
}
.header-desc {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 6rpx;
  display: block;
}

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
  border-left: 6rpx solid #646cff;
}

.info-grid {
  display: flex;
  flex-wrap: wrap;
}
.info-item {
  width: 50%;
  padding: 12rpx 16rpx;
}
.info-label {
  font-size: 24rpx;
  color: #999;
  display: block;
}
.info-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-top: 4rpx;
  display: block;
}

.duration-display {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 20rpx;
  background-color: #f7f8fa;
  border-radius: 12rpx;
}
.duration-text {
  font-size: 30rpx;
  color: #646cff;
  font-weight: 600;
}
.calc-text {
  font-size: 26rpx;
  color: #999;
}
.placeholder-text {
  font-size: 26rpx;
  color: #c0c4cc;
}
.warn-tip {
  font-size: 22rpx;
  color: #e6a23c;
  margin-top: 8rpx;
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
.btn-default {
  background-color: #f4f5f7;
  color: #666;
}
.btn-primary {
  background-color: #646cff;
  color: #fff;
}
.btn-primary[disabled] {
  opacity: 0.6;
}
</style>
