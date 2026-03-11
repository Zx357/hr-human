<template>
  <view class="apply-container">
    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-icon">
        <text class="iconfont">&#xe63a;</text>
      </view>
      <view class="header-info">
        <text class="header-title">离职申请</text>
        <text class="header-desc">提交离职申请，请妥善安排工作交接</text>
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
          <text class="info-label">在职时长</text>
          <text class="info-value">{{ workDays }}</text>
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="section-card">
      <view class="section-title">离职信息</view>
      <uni-forms ref="form" :modelValue="formData" :rules="rules" label-width="95px" label-align="right">
        <uni-forms-item label="离职类型" name="resignType" required>
          <uni-data-select v-model="formData.resignType" :localdata="resignTypes" placeholder="请选择离职类型"></uni-data-select>
        </uni-forms-item>

        <uni-forms-item label="最后工作日" name="lastWorkDate" required>
          <uni-datetime-picker type="date" v-model="formData.lastWorkDate" placeholder="请选择最后工作日" />
        </uni-forms-item>

        <uni-forms-item label="离职原因" name="resignReason" required>
          <uni-data-select v-model="formData.resignReason" :localdata="resignReasons" placeholder="请选择离职原因"></uni-data-select>
        </uni-forms-item>

        <uni-forms-item label="详细说明" name="reason" required>
          <uni-easyinput type="textarea" v-model="formData.reason" placeholder="请详细说明离职原因" :maxlength="500" autoHeight />
        </uni-forms-item>

        <uni-forms-item label="工作交接" name="handover">
          <uni-easyinput type="textarea" v-model="formData.handover" placeholder="请说明工作交接安排及交接人（选填）" :maxlength="300" autoHeight />
        </uni-forms-item>
      </uni-forms>
    </view>

    <!-- 温馨提示 -->
    <view class="section-card tip-card">
      <view class="tip-title">温馨提示</view>
      <view class="tip-list">
        <text class="tip-item">· 离职申请提交后需要上级审批</text>
        <text class="tip-item">· 请提前30天提交离职申请</text>
        <text class="tip-item">· 请妥善安排工作交接事宜</text>
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
        resignType: '',
        lastWorkDate: '',
        resignReason: '',
        reason: '',
        handover: ''
      },
      resignTypes: [
        { value: '主动离职', text: '主动离职' },
        { value: '协商离职', text: '协商离职' }
      ],
      resignReasons: [
        { value: '个人发展', text: '个人发展' },
        { value: '薪资待遇', text: '薪资待遇' },
        { value: '工作环境', text: '工作环境' },
        { value: '家庭原因', text: '家庭原因' },
        { value: '健康原因', text: '健康原因' },
        { value: '其他原因', text: '其他原因' }
      ],
      rules: {
        resignType: { rules: [{ required: true, errorMessage: '请选择离职类型' }] },
        lastWorkDate: { rules: [{ required: true, errorMessage: '请选择最后工作日' }] },
        resignReason: { rules: [{ required: true, errorMessage: '请选择离职原因' }] },
        reason: { rules: [{ required: true, errorMessage: '请详细说明离职原因' }] }
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
    },
    workDays() {
      if (!this.userInfo.entryDate) return '-'
      const entry = new Date(this.userInfo.entryDate)
      const now = new Date()
      const diffMs = now - entry
      const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))
      if (days >= 365) {
        const years = Math.floor(days / 365)
        const months = Math.floor((days % 365) / 30)
        return `${years}年${months}个月`
      }
      const months = Math.floor(days / 30)
      return months > 0 ? `${months}个月` : `${days}天`
    }
  },
  onLoad() {
    const info = uni.getStorageSync('userInfo')
    if (info) this.userInfo = info
  },
  methods: {
    handleReset() {
      this.formData = { resignType: '', lastWorkDate: '', resignReason: '', reason: '', handover: '' }
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
          appType: 'resignation',
          title: '离职申请',
          resignType: this.formData.resignType,
          lastWorkDate: this.formData.lastWorkDate,
          reason: `离职原因: ${this.formData.resignReason}\n详细说明: ${this.formData.reason}`,
          remark: this.formData.handover ? `工作交接: ${this.formData.handover}` : '',
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
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
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
  border-left: 6rpx solid #6c5ce7;
}

.info-grid { display: flex; flex-wrap: wrap; }
.info-item { width: 50%; padding: 12rpx 16rpx; }
.info-label { font-size: 24rpx; color: #999; display: block; }
.info-value { font-size: 28rpx; color: #333; font-weight: 500; margin-top: 4rpx; display: block; }

.tip-card {
  background-color: #fef9ef;
}
.tip-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #e6a23c;
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
.btn-primary { background-color: #6c5ce7; color: #fff; }
.btn-primary[disabled] { opacity: 0.6; }
</style>
