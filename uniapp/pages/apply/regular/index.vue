<template>
  <view class="apply-container">
    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-icon">
        <text class="iconfont">&#xe62c;</text>
      </view>
      <view class="header-info">
        <text class="header-title">转正申请</text>
        <text class="header-desc">试用期结束，申请转为正式员工</text>
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
        <view class="info-item">
          <text class="info-label">入职日期</text>
          <text class="info-value">{{ userInfo.entryDate || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">试用期止</text>
          <text class="info-value">{{ probationEndDate || '-' }}</text>
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="section-card">
      <view class="section-title">转正信息</view>
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="90px" label-align="right">
        <uni-forms-item label="转正日期" name="regularDate" required>
          <uni-datetime-picker type="date" v-model="formData.regularDate" placeholder="请选择转正日期" />
        </uni-forms-item>

        <uni-forms-item label="转正类型" name="newEmployeeType" required>
          <uni-data-select v-model="formData.newEmployeeType" :localdata="employeeTypes" placeholder="请选择转正后员工类型"></uni-data-select>
        </uni-forms-item>

        <uni-forms-item label="工作总结" name="evaluation" required>
          <uni-easyinput type="textarea" v-model="formData.evaluation" placeholder="请输入试用期工作总结与自我评价" :maxlength="500" autoHeight />
        </uni-forms-item>

        <uni-forms-item label="申请说明" name="reason">
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请输入申请说明（选填）" :maxlength="300" autoHeight />
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
        regularDate: '',
        newEmployeeType: '',
        evaluation: '',
        reason: ''
      },
      employeeTypes: [
        { value: 'formal', text: '正式员工' },
        { value: 'contract', text: '合同工' },
        { value: 'dispatch', text: '劳务派遣' }
      ],
      rules: {
        regularDate: { rules: [{ required: true, errorMessage: '请选择转正日期' }] },
        newEmployeeType: { rules: [{ required: true, errorMessage: '请选择转正后员工类型' }] },
        evaluation: { rules: [{ required: true, errorMessage: '请输入工作总结' }] }
      },
      submitting: false,
      userInfo: {},
      probationEndDate: ''
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
    if (info) {
      this.userInfo = info
      // 自动计算试用期结束日期（入职日期 + 3个月）
      if (info.entryDate) {
        const d = new Date(info.entryDate)
        d.setMonth(d.getMonth() + 3)
        this.probationEndDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      }
    }
  },
  methods: {
    handleReset() {
      this.formData = { regularDate: '', newEmployeeType: '', evaluation: '', reason: '' }
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
          appType: 'regularization',
          title: '转正申请',
          regularDate: this.formData.regularDate,
          newEmployeeType: this.formData.newEmployeeType,
          probationEndDate: this.probationEndDate,
          evaluation: this.formData.evaluation,
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
  background: linear-gradient(135deg, #36b37e, #57d9a3);
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
  border-left: 6rpx solid #36b37e;
}

.info-grid { display: flex; flex-wrap: wrap; }
.info-item { width: 50%; padding: 12rpx 16rpx; }
.info-label { font-size: 24rpx; color: #999; display: block; }
.info-value { font-size: 28rpx; color: #333; font-weight: 500; margin-top: 4rpx; display: block; }

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
.btn-primary { background-color: #36b37e; color: #fff; }
.btn-primary[disabled] { opacity: 0.6; }
</style>
