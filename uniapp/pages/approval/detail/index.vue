<template>
    <view class="page-wrap">
        <view class="top-bg"></view>
        <view class="main-content" v-if="detail">
            <!-- 头部信息 -->
            <view class="header-card">
                <view class="type-tag" :class="'type-' + detail.appType">
                    {{ getTypeName(detail.appType) }}
                </view>
                <view class="applicant-row">
                    <view class="avatar">
                        <text class="avatar-text">{{ (detail.employeeName || '').substring(0, 1) }}</text>
                    </view>
                    <view class="applicant-info">
                        <text class="name">{{ detail.employeeName }}</text>
                        <text class="dept">{{ detail.deptName }} · {{ detail.employeeNo }}</text>
                    </view>
                </view>
            </view>

            <!-- 基本信息 -->
            <view class="info-card">
                <view class="card-title">申请信息</view>
                <view class="info-row" v-if="detail.startTime">
                    <text class="label">开始时间</text>
                    <text class="value">{{ formatTime(detail.startTime) }}</text>
                </view>
                <view class="info-row" v-if="detail.endTime">
                    <text class="label">结束时间</text>
                    <text class="value">{{ formatTime(detail.endTime) }}</text>
                </view>
                <view class="info-row" v-if="detail.duration">
                    <text class="label">时长</text>
                    <text class="value">{{ detail.duration }}小时</text>
                </view>

                <!-- 转正字段 -->
                <template v-if="detail.appType === 'regularization'">
                    <view class="info-row" v-if="detail.regularDate">
                        <text class="label">转正日期</text>
                        <text class="value">{{ detail.regularDate }}</text>
                    </view>
                    <view class="info-row" v-if="detail.probationEndDate">
                        <text class="label">试用期结束</text>
                        <text class="value">{{ detail.probationEndDate }}</text>
                    </view>
                    <view class="info-row" v-if="detail.evaluation">
                        <text class="label">自我评价</text>
                        <text class="value">{{ detail.evaluation }}</text>
                    </view>
                </template>

                <!-- 调动字段 -->
                <template v-if="detail.appType === 'transfer'">
                    <view class="info-row" v-if="detail.transferType">
                        <text class="label">调动类型</text>
                        <text class="value">{{ detail.transferType }}</text>
                    </view>
                    <view class="info-row" v-if="detail.fromDeptName">
                        <text class="label">原部门</text>
                        <text class="value">{{ detail.fromDeptName }}</text>
                    </view>
                    <view class="info-row" v-if="detail.toDeptName">
                        <text class="label">新部门</text>
                        <text class="value">{{ detail.toDeptName }}</text>
                    </view>
                    <view class="info-row" v-if="detail.fromPosition">
                        <text class="label">原职位</text>
                        <text class="value">{{ detail.fromPosition }}</text>
                    </view>
                    <view class="info-row" v-if="detail.toPosition">
                        <text class="label">新职位</text>
                        <text class="value">{{ detail.toPosition }}</text>
                    </view>
                    <view class="info-row" v-if="detail.effectDate">
                        <text class="label">生效日期</text>
                        <text class="value">{{ detail.effectDate }}</text>
                    </view>
                </template>

                <!-- 奖惩字段 -->
                <template v-if="detail.appType === 'reward' || detail.appType === 'punish'">
                    <view class="info-row" v-if="detail.category">
                        <text class="label">类别</text>
                        <text class="value">{{ detail.category }}</text>
                    </view>
                    <view class="info-row" v-if="detail.amount">
                        <text class="label">金额</text>
                        <text class="value">¥{{ detail.amount }}</text>
                    </view>
                </template>

                <!-- 离职字段 -->
                <template v-if="detail.appType === 'resignation'">
                    <view class="info-row" v-if="detail.resignType">
                        <text class="label">离职类型</text>
                        <text class="value">{{ detail.resignType }}</text>
                    </view>
                    <view class="info-row" v-if="detail.lastWorkDate">
                        <text class="label">最后工作日</text>
                        <text class="value">{{ detail.lastWorkDate }}</text>
                    </view>
                    <view class="info-row" v-if="detail.handoverToName">
                        <text class="label">交接人</text>
                        <text class="value">{{ detail.handoverToName }}</text>
                    </view>
                </template>

                <view class="info-row reason-row" v-if="detail.reason">
                    <text class="label">事由</text>
                    <text class="value">{{ detail.reason }}</text>
                </view>
                <view class="info-row" v-if="detail.remark">
                    <text class="label">备注</text>
                    <text class="value">{{ detail.remark }}</text>
                </view>
                <view class="info-row">
                    <text class="label">提交时间</text>
                    <text class="value">{{ formatTime(detail.createdTime) }}</text>
                </view>
            </view>

            <!-- 审批信息（已审批的才显示） -->
            <view class="info-card" v-if="detail.status !== 0">
                <view class="card-title">审批结果</view>
                <view class="info-row">
                    <text class="label">审批状态</text>
                    <view class="status-pill" :class="'status-' + detail.status">
                        {{ getStatusName(detail.status) }}
                    </view>
                </view>
                <view class="info-row" v-if="detail.approveTime">
                    <text class="label">审批时间</text>
                    <text class="value">{{ formatTime(detail.approveTime) }}</text>
                </view>
                <view class="info-row" v-if="detail.approveRemark">
                    <text class="label">审批意见</text>
                    <text class="value">{{ detail.approveRemark }}</text>
                </view>
            </view>

            <!-- 审批操作区 -->
            <view class="action-card" v-if="detail.status === 0">
                <view class="remark-section">
                    <text class="remark-label">审批意见（拒绝时必填）</text>
                    <u-textarea v-model="approveRemark" placeholder="请输入审批意见" count maxlength="200"
                        height="100"></u-textarea>
                </view>
                <view class="action-buttons">
                    <view class="reject-btn" @click="handleReject">
                        <u-icon name="close" color="#ef4444" size="18"></u-icon>
                        <text>拒绝</text>
                    </view>
                    <view class="approve-btn" @click="handleApprove">
                        <u-icon name="checkmark" color="#ffffff" size="18"></u-icon>
                        <text>通过</text>
                    </view>
                </view>
            </view>
        </view>

        <!-- 加载中 -->
        <view class="loading-state" v-else>
            <u-loading-icon></u-loading-icon>
            <text class="loading-text">加载中...</text>
        </view>
    </view>
</template>

<script>
import { getApplicationDetail, approveApplication } from '@/api/application'

export default {
    data() {
        return {
            id: null,
            detail: null,
            approveRemark: ''
        }
    },
    onLoad(options) {
        this.id = options.id
        this.loadDetail()
    },
    methods: {
        async loadDetail() {
            try {
                const res = await getApplicationDetail(this.id)
                if (res.code === 200 && res.data) {
                    this.detail = res.data
                } else {
                    uni.showToast({ title: '加载失败', icon: 'none' })
                }
            } catch (e) {
                uni.showToast({ title: '加载失败', icon: 'none' })
            }
        },
        handleApprove() {
            uni.showModal({
                title: '确认审批',
                content: `确定通过 ${this.detail.employeeName || ''} 的${this.getTypeName(this.detail.appType)}申请吗？`,
                success: async (res) => {
                    if (res.confirm) {
                        await this.doApprove(1)
                    }
                }
            })
        },
        handleReject() {
            if (!this.approveRemark.trim()) {
                uni.showToast({ title: '拒绝时请填写审批意见', icon: 'none' })
                return
            }
            uni.showModal({
                title: '确认拒绝',
                content: `确定拒绝 ${this.detail.employeeName || ''} 的${this.getTypeName(this.detail.appType)}申请吗？`,
                success: async (res) => {
                    if (res.confirm) {
                        await this.doApprove(2)
                    }
                }
            })
        },
        async doApprove(status) {
            try {
                const result = await approveApplication(this.id, status, this.approveRemark)
                if (result.code === 200) {
                    uni.showToast({
                        title: status === 1 ? '已通过' : '已拒绝',
                        icon: 'success'
                    })
                    // 刷新详情
                    setTimeout(() => {
                        this.loadDetail()
                    }, 500)
                } else {
                    uni.showToast({ title: result.msg || '操作失败', icon: 'none' })
                }
            } catch (e) {
                uni.showToast({ title: '操作失败', icon: 'none' })
            }
        },
        getTypeName(appType) {
            const map = {
                leave: '请假', overtime: '加班', business: '出差',
                makeup: '补卡', exchange: '换休', regularization: '转正',
                transfer: '调动', reward: '奖励', punish: '惩罚', resignation: '离职'
            }
            return map[appType] || appType || '申请'
        },
        getStatusName(status) {
            const map = { 0: '待审批', 1: '已通过', 2: '已拒绝', 3: '已撤销' }
            return map[status] || '未知'
        },
        formatTime(timeStr) {
            if (!timeStr) return ''
            return timeStr.substring(0, 16)
        }
    }
}
</script>

<style lang="scss" scoped>
.page-wrap {
    min-height: 100vh;
    background: #f3f4f6;
    position: relative;
    padding-bottom: 40rpx;
}

.top-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 240rpx;
    background: linear-gradient(145deg, #059669 0%, #10b981 40%, #34d399 100%);
}

.main-content {
    position: relative;
    z-index: 1;
    padding: 24rpx;
}

.header-card {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .type-tag {
        display: inline-block;
        padding: 8rpx 24rpx;
        border-radius: 12rpx;
        font-size: 24rpx;
        font-weight: 600;
        margin-bottom: 24rpx;
    }

    .type-leave {
        background: #dbeafe;
        color: #2563eb;
    }

    .type-overtime {
        background: #fef3c7;
        color: #d97706;
    }

    .type-business {
        background: #fce7f3;
        color: #db2777;
    }

    .type-makeup {
        background: #e0e7ff;
        color: #4f46e5;
    }

    .type-exchange {
        background: #d1fae5;
        color: #059669;
    }

    .type-regularization {
        background: #d1fae5;
        color: #059669;
    }

    .type-transfer {
        background: #ede9fe;
        color: #7c3aed;
    }

    .type-reward {
        background: #fef3c7;
        color: #d97706;
    }

    .type-punish {
        background: #fee2e2;
        color: #dc2626;
    }

    .type-resignation {
        background: #fee2e2;
        color: #dc2626;
    }

    .applicant-row {
        display: flex;
        align-items: center;

        .avatar {
            width: 80rpx;
            height: 80rpx;
            border-radius: 50%;
            background: linear-gradient(135deg, #059669, #34d399);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 20rpx;

            .avatar-text {
                color: #ffffff;
                font-size: 32rpx;
                font-weight: 700;
            }
        }

        .applicant-info {
            display: flex;
            flex-direction: column;

            .name {
                font-size: 32rpx;
                font-weight: 700;
                color: #111827;
            }

            .dept {
                font-size: 24rpx;
                color: #9ca3af;
                margin-top: 4rpx;
            }
        }
    }
}

.info-card {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .card-title {
        font-size: 30rpx;
        font-weight: 700;
        color: #111827;
        margin-bottom: 24rpx;
        padding-bottom: 20rpx;
        border-bottom: 1rpx solid #f3f4f6;
    }

    .info-row {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 16rpx 0;

        .label {
            width: 160rpx;
            font-size: 26rpx;
            color: #9ca3af;
            flex-shrink: 0;
        }

        .value {
            flex: 1;
            font-size: 26rpx;
            color: #374151;
            text-align: right;
        }

        .status-pill {
            padding: 6rpx 20rpx;
            border-radius: 20rpx;
            font-size: 24rpx;
            font-weight: 600;
        }

        .status-1 {
            background: #d1fae5;
            color: #059669;
        }

        .status-2 {
            background: #fee2e2;
            color: #ef4444;
        }

        .status-3 {
            background: #f3f4f6;
            color: #6b7280;
        }
    }

    .reason-row .value {
        text-align: left;
        word-break: break-all;
    }
}

.action-card {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .remark-section {
        margin-bottom: 30rpx;

        .remark-label {
            display: block;
            font-size: 28rpx;
            font-weight: 600;
            color: #111827;
            margin-bottom: 16rpx;
        }
    }

    .action-buttons {
        display: flex;
        gap: 24rpx;

        .reject-btn,
        .approve-btn {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24rpx 0;
            border-radius: 48rpx;
            font-size: 30rpx;
            font-weight: 700;
            gap: 8rpx;
        }

        .reject-btn {
            background: #fef2f2;
            color: #ef4444;
            border: 2rpx solid #fecaca;
        }

        .approve-btn {
            background: #059669;
            color: #ffffff;
        }
    }
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 400rpx;

    .loading-text {
        margin-top: 20rpx;
        font-size: 28rpx;
        color: #9ca3af;
    }
}
</style>
