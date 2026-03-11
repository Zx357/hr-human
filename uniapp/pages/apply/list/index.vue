<template>
    <view class="page-wrap">
        <view class="top-bg"></view>
        <view class="main-content">
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
                    <view class="apply-card" v-for="(item, index) in list" :key="index">
                        <view class="card-header">
                            <view class="type-tag" :class="'type-' + item.appType">
                                {{ getTypeName(item.appType) }}
                            </view>
                            <view class="status-pill" :class="'status-' + item.status">
                                <view class="status-dot" :class="'dot-' + item.status"></view>
                                <text>{{ getStatusName(item.status) }}</text>
                            </view>
                        </view>
                        <view class="card-body">
                            <view class="info-row" v-if="item.startTime">
                                <text class="label">开始时间</text>
                                <text class="value">{{ item.startTime }}</text>
                            </view>
                            <view class="info-row" v-if="item.endTime">
                                <text class="label">结束时间</text>
                                <text class="value">{{ item.endTime }}</text>
                            </view>
                            <view class="info-row" v-if="item.reason">
                                <text class="label">事由</text>
                                <text class="value reason-text">{{ item.reason }}</text>
                            </view>
                        </view>
                        <view class="card-footer">
                            <text class="time-text">{{ formatTime(item.createdTime) }}</text>
                            <view class="action-btn" v-if="item.status === 0" @click="handleCancel(item)">
                                <text>撤销</text>
                            </view>
                        </view>
                    </view>
                </view>
                <view class="empty-state" v-else-if="!loading">
                    <u-icon name="file-text" size="64" color="#d1d5db"></u-icon>
                    <text class="empty-text">暂无申请记录</text>
                </view>
                <view class="load-more" v-if="list.length > 0">
                    <text v-if="loading">加载中...</text>
                    <text v-else-if="finished">没有更多了</text>
                </view>
            </scroll-view>
        </view>
    </view>
</template>

<script>
import { getMyApplications, cancelApplication } from '@/api/application'

export default {
    data() {
        return {
            currentTab: null,
            tabs: [
                { label: '全部', value: null },
                { label: '待审批', value: 0 },
                { label: '已通过', value: 1 },
                { label: '已拒绝', value: 2 },
                { label: '已撤销', value: 3 }
            ],
            list: [],
            pageNum: 1,
            pageSize: 10,
            loading: false,
            finished: false,
            refreshing: false
        }
    },
    onLoad(options) {
        if (options.status !== undefined && options.status !== '') {
            this.currentTab = Number(options.status)
        }
        this.loadList()
    },
    methods: {
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
                const info = this.$store.state.user.employeeInfo || uni.getStorageSync('userInfo')
                if (!info || !info.id) {
                    console.log('employeeInfo为空，无法加载申请列表')
                    return
                }
                const params = {
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    employeeId: info.id
                }
                if (this.currentTab !== null) {
                    params.status = this.currentTab
                }
                const res = await getMyApplications(params)
                if (res.code === 200 && res.data) {
                    const records = res.data.records || []
                    this.list = this.list.concat(records)
                    if (this.list.length >= res.data.total) {
                        this.finished = true
                    }
                    this.pageNum++
                }
            } catch (e) {
                console.log('加载申请列表失败', e)
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
        async handleCancel(item) {
            uni.showModal({
                title: '提示',
                content: '确定撤销该申请吗？',
                success: async (res) => {
                    if (res.confirm) {
                        try {
                            const result = await cancelApplication(item.id)
                            if (result.code === 200) {
                                uni.showToast({ title: '已撤销', icon: 'success' })
                                this.onRefresh()
                            } else {
                                uni.showToast({ title: result.msg || '撤销失败', icon: 'none' })
                            }
                        } catch (e) {
                            uni.showToast({ title: '操作失败', icon: 'none' })
                        }
                    }
                }
            })
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
            return map[status] ?? '未知'
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
    padding-top: calc(var(--status-bar-height, 44px) + 16rpx);
}

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

.list-scroll {
    height: calc(100vh - var(--status-bar-height, 44px) - 140rpx);
}

.apply-card {
    background: #fff;
    border-radius: 20rpx;
    margin-bottom: 20rpx;
    padding: 28rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
}

.type-tag {
    font-size: 26rpx;
    font-weight: 600;
    color: #4f46e5;
    padding: 6rpx 20rpx;
    background: rgba(79, 70, 229, 0.08);
    border-radius: 12rpx;
}

.type-leave {
    color: #0284c7;
    background: rgba(2, 132, 199, 0.08);
}

.type-overtime {
    color: #ea580c;
    background: rgba(234, 88, 12, 0.08);
}

.type-business {
    color: #db2777;
    background: rgba(219, 39, 119, 0.08);
}

.type-makeup {
    color: #4f46e5;
    background: rgba(79, 70, 229, 0.08);
}

.type-resignation {
    color: #dc2626;
    background: rgba(220, 38, 38, 0.08);
}

.status-pill {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    padding: 6rpx 16rpx;
    border-radius: 20rpx;
}

.status-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    margin-right: 8rpx;
}

.status-0 {
    color: #f59e0b;
    background: rgba(245, 158, 11, 0.1);
}

.dot-0 {
    background: #f59e0b;
}

.status-1 {
    color: #10b981;
    background: rgba(16, 185, 129, 0.1);
}

.dot-1 {
    background: #10b981;
}

.status-2 {
    color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
}

.dot-2 {
    background: #ef4444;
}

.status-3 {
    color: #6b7280;
    background: rgba(107, 114, 128, 0.1);
}

.dot-3 {
    background: #6b7280;
}

.card-body {
    border-top: 1rpx solid #f3f4f6;
    padding-top: 16rpx;
}

.info-row {
    display: flex;
    margin-bottom: 12rpx;
    font-size: 26rpx;
}

.info-row .label {
    color: #9ca3af;
    width: 140rpx;
    flex-shrink: 0;
}

.info-row .value {
    color: #374151;
    flex: 1;
}

.reason-text {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid #f3f4f6;
}

.time-text {
    font-size: 24rpx;
    color: #9ca3af;
}

.action-btn {
    padding: 8rpx 24rpx;
    border-radius: 12rpx;
    background: rgba(239, 68, 68, 0.08);
    color: #ef4444;
    font-size: 24rpx;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 200rpx;
}

.empty-text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #9ca3af;
}

.load-more {
    text-align: center;
    padding: 24rpx 0;
    font-size: 24rpx;
    color: #9ca3af;
}
</style>
