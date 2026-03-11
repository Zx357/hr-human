<template>
  <app-page padding="24rpx">
    <app-card padding="8rpx">
      <u-cell-group :border="false" v-if="loaded">
        <u-cell title="姓名" :value="user.name || '-'" />
        <u-cell title="工号" :value="user.employeeNo || '-'" />
        <u-cell title="手机号码" :value="user.phone || '-'" />
        <u-cell title="邮箱" :value="user.email || '-'" />
        <u-cell title="部门" :value="user.deptName || '-'" />
        <u-cell title="公司" :value="user.companyName || '-'" />
        <u-cell title="岗位" :value="user.post || user.position || '-'" />
        <u-cell title="入职日期" :value="formatDate(user.entryDate)" />
        <u-cell title="创建时间" :value="user.createdTime || '-'" />
      </u-cell-group>
      <view v-else class="loading-wrap">
        <u-loading-icon mode="circle" />
        <text class="loading-text">加载中...</text>
      </view>
    </app-card>
  </app-page>
</template>

<script>
  import { getCurrentEmployee } from '@/api/login'

  export default {
    data() {
      return {
        user: {},
        loaded: false
      }
    },
    onLoad() {
      this.getUser()
    },
    methods: {
      formatDate(val) {
        if (!val) return '-'
        if (typeof val === 'string') return val
        return val
      },
      getUser() {
        getCurrentEmployee().then(res => {
          if (res.code === 200 && res.data) {
            this.user = res.data
          } else {
            this.$modal.msgError(res.msg || '获取个人信息失败')
          }
        }).catch(() => {
          this.$modal.msgError('获取个人信息失败')
        }).finally(() => {
          this.loaded = true
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "@/static/scss/tokens.scss";

  .loading-wrap {
    padding: 80rpx 0;
    text-align: center;
    color: $app-text-2;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
  }

  .loading-text {
    font-size: 24rpx;
  }
</style>
