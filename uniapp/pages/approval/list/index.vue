<template>
    <view class="page-wrap">
        <view class="top-bg"></view>
        <view class="main-content">
            <!-- 自定义导航栏 -->
            <view class="custom-nav">
                <view class="nav-back" @click="goBack">
                    <u-icon name="arrow-left" size="18" color="#ffffff"></u-icon>
                </view>
                <text class="nav-title">待审批</text>
                <view class="nav-placeholder"></view>
            </view>

            <view class="tabs">
                <view v-for="(tab, idx) in tabs" :key="idx" class="tab-item"
                    :class="{ active: currentTab === tab.value }" @click="switchTab(tab.value)">
                    <text>{{ tab.label }}</text>
                    <view class="tab-line" v-if="currentTab === tab.value"></view>
                </view>
            </view>

            <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore" :refresher-enabled="true"
                :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
                <view class="apply-list" v-if="list.length > 0">
                    <view class="apply-card" v-for="(item, index) in list" :key="index" @click="goToDetail(item)">
                        <view class="card-header">
                            <view class="type-tag" :class="'type-' + item.appType">
                                {{ getTypeName(item.appType) }}
                            </view>
                            <view class="applicant-info">
                                <text class="applicant-name">{{ item.employeeName || '未知' }}</text>
                                <text class="applicant-no">{{ item.employeeNo }}</text>
                            </view>
                        </view>
                        <view class="card-body">
                            <view class="info-row" v-if="item.startTime">
                                <text class="label">开始时间</text>
                                <text class="value">{{ formatTime(item.startTime) }}</text>
                            </view>
                            <view class="info-row" v-if="item.endTime">
                                <text class="label">结束时间</text>
                                <text class="value">{{ formatTime(item.endTime) }}</text>
                            </view>
                            <view class="info-row" v-if="item.duration">
                                <text class="label">时长</text>
                                <text class="value">{{ item.duration }}小时</text>
                            </view>
                            <view class="info-row" v-if="item.reason">
                                <text class="label">事由</text>
                                <text class="value reason-text">{{ item.reason }}</text>
                            </view>
                        </view>
                        <view class="card-footer">
                            <text class="time-text">{{ formatTime(item.createdTime) }}</text>
                            <view class="action-area">
                                <view class="reject-btn" @click.stop="handleReject(item)">拒绝</view>
                                <view class="approve-btn" @click.stop="handleApprove(item)">通过</view>
                            </view>
                        </view>
                    </view>
                </view>
                <view class="empty-state" v-else-if="!loading">
                    <u-icon name="checkmark-circle" size="64" color="#d1d5db"></u-icon>
                    <text class="empty-text">暂无待审批事项</text>
                </view>
                <view class="load-more" v-if="list.length > 0">
                    <text v-if="loading">加载中...</text>
                    <text v-else-if="finished">没有更多了</text>
                </view>
            </scroll-view>
        </view>

        <!-- 拒绝原因弹窗 -->
        <u-popup :show="showRejectPopup" mode="center" round="24" @close="showRejectPopup = false">
            <view class="reject-popup">
                <text class="popup-title">拒绝原因</text>
                <u-textarea v-model="rejectRemark" placeholder="请输入拒绝原因" count maxlength="200"
                    height="120"></u-textarea>
                <view class="popup-actions">
                    <view class="popup-cancel" @click="showRejectPopup = false">取消</view>
                    <view class="popup-confirm" @click="confirmReject">确认拒绝</view>
                </view>
            </view>
        </u-popup>
    </view>
</template>

<script>
import { getPendingApprovals, approveApplication } from '@/api/application'

export default {
    data() {
        return {
            currentTab: 'all',
            tabs: [
                { label: '全部', value: 'all' },
                { label: '请假', value: 'leave' },
                { label: '加班', value: 'overtime' },
                { label: '出差', value: 'business' },
                { label: '补卡', value: 'makeup' },
                { label: '其他', value: 'other' }
            ],
            list: [],
            pageNum: 1,
            pageSize: 10,
            loading: false,
            finished: false,
            refreshing: false,
            showRejectPopup: false,
            rejectRemark: '',
            currentItem: null
        }
    },
    onLoad() {
        this.loadList()
    },
    onShow() {
        // 从详情页返回时刷新
        if (this.list.length > 0) {
            this.onRefresh()
        }
    },
    methods: {
        goBack() {
            const pages = getCurrentPages()
            if (pages.length > 1) {
                uni.navigateBack()
            } else {
                uni.switchTab({ url: '/pages/index' })
            }
        },
        switchTab(value) {
            this.currentTab = value
            this.pageNum = 1
            this.list = []
            this.finished = false
            this.loadList()
        },
        async loadList() {
            if (this.loading || this.finished) return
            this.loading = true
            try {
                const params = {
                    pageNum: this.pageNum,
                    pageSize: this.pageSize
                }
                if (this.currentTab === 'other') {
                    // "其他"包含：换休、转正、调动、奖惩、离职，但API不能传多个
                    // 不传appType，前端过滤
                } else if (this.currentTab !== 'all') {
                    params.appType = this.currentTab
                }
                const res = await getPendingApprovals(params)
                if (res.code === 200 && res.data) {
                    let records = res.data.records || []
                    if (this.currentTab === 'other') {
                        const mainTypes = ['leave', 'overtime', 'business', 'makeup']
                        records = records.filter(r => !mainTypes.includes(r.appType))
                    }
                    this.list = this.list.concat(records)
                    if (this.list.length >= res.data.total) {
                        this.finished = true
                    }
                    this.pageNum++
                }
            } catch (e) {
                console.log('加载待审批列表失败', e)
            } finally {
                this.loading = false
            }
        },
        loadMore() {
            this.loadList()
        },
        onRefresh() {
            this.refreshing = true
            this.pageNum = 1
            this.list = []
            this.finished = false
            this.loadList().then(() => {
                this.refreshing = false
            })
        },
        goToDetail(item) {
            uni.navigateTo({
                url: '/pages/approval/detail/index?id=' + item.id
            })
        },
        handleApprove(item) {
            uni.showModal({
                title: '确认审批',
                content: `确定通过 ${item.employeeName || ''} 的${this.getTypeName(item.appType)}申请吗？`,
                success: async (res) => {
                    if (res.confirm) {
                        try {
                            const result = await approveApplication(item.id, 1, '')
                            if (result.code === 200) {
                                uni.showToast({ title: '已通过', icon: 'success' })
                                this.onRefresh()
                            } else {
                                uni.showToast({ title: result.msg || '操作失败', icon: 'none' })
                            }
                        } catch (e) {
                            uni.showToast({ title: '操作失败', icon: 'none' })
                        }
                    }
                }
            })
        },
        handleReject(item) {
            this.currentItem = item
            this.rejectRemark = ''
            this.showRejectPopup = true
        },
        async confirmReject() {
            if (!this.rejectRemark.trim()) {
                uni.showToast({ title: '请输入拒绝原因', icon: 'none' })
                return
            }
            try {
                const result = await approveApplication(this.currentItem.id, 2, this.rejectRemark)
                if (result.code === 200) {
                    uni.showToast({ title: '已拒绝', icon: 'success' })
                    this.showRejectPopup = false
                    this.onRefresh()
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
}

.top-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 200rpx;
    background: linear-gradient(145deg, #4f46e5 0%, #6366f1 40%, #818cf8 100%);
}

.main-content {
    position: relative;
    z-index: 1;
    padding: 0 24rpx;
}

// === 自定义导航栏 ===
.custom-nav {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: calc(var(--status-bar-height, 44px) + 10rpx) 8rpx 20rpx;

    .nav-back {
        width: 64rpx;
        height: 64rpx;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;

        &:active {
            background: rgba(255, 255, 255, 0.35);
        }
    }

    .nav-title {
        font-size: 34rpx;
        font-weight: 700;
        color: #ffffff;
    }

    .nav-placeholder {
        width: 64rpx;
    }
}

// === 标签栏 ===
.tabs {
    display: flex;
    background: #fff;
    border-radius: 20rpx;
    padding: 8rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    font-size: 26rpx;
    color: #6b7280;
    position: relative;
    transition: all 0.2s;

    &.active {
        color: #4f46e5;
        font-weight: 600;
    }
}

.tab-line {
    position: absolute;
    bottom: 4rpx;
    left: 50%;
    transform: translateX(-50%);
    width: 40rpx;
    height: 6rpx;
    background: #4f46e5;
    border-radius: 3rpx;
}

// === 列表滚动区 ===
.list-scroll {
    height: calc(100vh - var(--status-bar-height, 44px) - 180rpx);
}

.apply-list {
    padding-bottom: 40rpx;
}

// === 卡片 ===
.apply-card {
    background: #fff;
    border-radius: 20rpx;
    margin-bottom: 20rpx;
    padding: 28rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .type-tag {
        font-size: 24rpx;
        font-weight: 600;
        padding: 6rpx 20rpx;
        border-radius: 12rpx;
        margin-right: 20rpx;
    }

    .type-leave {
        color: #2563eb;
        background: #dbeafe;
    }

    .type-overtime {
        color: #d97706;
        background: #fef3c7;
    }

    .type-business {
        color: #db2777;
        background: #fce7f3;
    }

    .type-makeup {
        color: #4f46e5;
        background: #e0e7ff;
    }

    .type-exchange {
        color: #059669;
        background: #d1fae5;
    }

    .type-regularization {
        color: #059669;
        background: #d1fae5;
    }

    .type-transfer {
        color: #7c3aed;
        background: #ede9fe;
    }

    .type-reward {
        color: #d97706;
        background: #fef3c7;
    }

    .type-punish {
        color: #dc2626;
        background: #fee2e2;
    }

    .type-resignation {
        color: #dc2626;
        background: #fee2e2;
    }

    .applicant-info {
        display: flex;
        align-items: center;
        flex: 1;

        .applicant-name {
            font-size: 30rpx;
            font-weight: 700;
            color: #111827;
            margin-right: 12rpx;
        }

        .applicant-no {
            font-size: 24rpx;
            color: #9ca3af;
        }
    }
}

.card-body {
    border-top: 1rpx solid #f3f4f6;
    padding-top: 16rpx;

    .info-row {
        display: flex;
        margin-bottom: 12rpx;
        font-size: 26rpx;

        .label {
            color: #9ca3af;
            width: 140rpx;
            flex-shrink: 0;
        }

        .value {
            color: #374151;
            flex: 1;
        }

        .reason-text {
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
    }
}

.card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid #f3f4f6;

    .time-text {
        font-size: 24rpx;
        color: #9ca3af;
    }

    .action-area {
        display: flex;
        gap: 16rpx;

        .reject-btn {
            padding: 12rpx 36rpx;
            border-radius: 40rpx;
            font-size: 26rpx;
            font-weight: 600;
            background: rgba(239, 68, 68, 0.08);
            color: #ef4444;

            &:active {
                background: rgba(239, 68, 68, 0.15);
            }
        }

        .approve-btn {
            padding: 12rpx 36rpx;
            border-radius: 40rpx;
            font-size: 26rpx;
            font-weight: 600;
            background: #4f46e5;
            color: #ffffff;

            &:active {
                background: #4338ca;
            }
        }
    }
}

// === 空状态 ===
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 200rpx;

    .empty-text {
        margin-top: 20rpx;
        font-size: 28rpx;
        color: #9ca3af;
    }
}

.load-more {
    text-align: center;
    padding: 24rpx 0;
    font-size: 24rpx;
    color: #9ca3af;
}

// === 拒绝弹窗 ===
.reject-popup {
    width: 600rpx;
    padding: 40rpx;

    .popup-title {
        display: block;
        font-size: 32rpx;
        font-weight: 700;
        color: #111827;
        text-align: center;
        margin-bottom: 30rpx;
    }

    .popup-actions {
        display: flex;
        justify-content: space-between;
        margin-top: 30rpx;
        gap: 20rpx;

        .popup-cancel,
        .popup-confirm {
            flex: 1;
            text-align: center;
            padding: 20rpx 0;
            border-radius: 40rpx;
            font-size: 28rpx;
            font-weight: 600;
        }

        .popup-cancel {
            background: #f3f4f6;
            color: #6b7280;
        }

        .popup-confirm {
            background: #ef4444;
            color: #ffffff;
        }
    }
}
</style>
