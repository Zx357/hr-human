<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">请假记录</text>
      </view>
    </tn-navbar>

    <view class="tn-padding-bottom-xl" :style="{paddingTop: vuex_custom_bar_height + 10 +'px'}">
      <view v-if="loading && !recordList.length" class="tn-text-center tn-color-gray tn-padding-xl">加载中...</view>

      <view v-else-if="loadFailed && !recordList.length" class="tn-text-center tn-padding-xl">
        <view class="tn-text-center" style="font-size: 160rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-lg">记录加载失败</view>
        <view class="record-retry" @click="refreshRecords">点击重试</view>
      </view>

      <view v-else-if="!recordList.length" class="tn-text-center tn-padding-xl">
        <view class="tn-text-center" style="font-size: 160rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-lg">暂无请假记录</view>
      </view>

      <view class="">
        <view
          class="content-bg tn-margin tn-padding"
          style="position: relative;"
          v-for="item in recordList"
          :key="item.id"
        >
          <view :class="['oa-' + item.color]" style="width: 15rpx;height: 100%;position: absolute;top: 0;left:0;border-radius: 15rpx 0 0 15rpx;font-size: 32rpx;">
          </view>
          <view class="tn-margin-left-xs">
            <view class="tn-flex tn-flex-col-center tn-flex-row-between" style="margin-top: -6rpx;">
              <view class="justify-content-item tn-flex tn-flex tn-flex-col-center tn-text-lg tn-text-bold">
                <view class="justify-content-item">
                  {{ item.type }}
                </view>
                <view class="justify-content-item">
                  <view class="tag-state" :class="['tn-bg-' + item.color + '--light oa-' + item.color]" style="font-size: 16rpx;">
                    <text class="">{{ item.state }}</text>
                  </view>
                </view>
              </view>
              <view class="justify-content-item tn-color-gray">
                {{ item.date }}
              </view>
            </view>
            <view class="tn-text-justify clamp-text-2 tn-padding-top-xs tn-color-gray--dark">
             事由：{{ leaveTypeLabel(item.title) }}
            </view>
            <view class="tn-text-justify clamp-text-1 tn-padding-top-xs tn-color-gray--dark">
             开始：{{ item.startTime }}
            </view>
            <view class="tn-text-justify clamp-text-1 tn-padding-top-xs tn-color-gray--dark">
             结束：{{ item.endTime }}
            </view>
          </view>

        </view>
      </view>

      <!-- 加载更多状态 -->
      <view v-if="recordList.length" class="tn-text-center tn-padding tn-color-gray tn-text-sm">
        <text v-if="loadingMore">加载中...</text>
        <text v-else-if="finished">没有更多了</text>
        <text v-else>上拉加载更多</text>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMyApplications } from '@/api/application'
import { leaveTypeLabel } from '@/utils/common'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const store = useStore()
const employeeInfo = ref(store.state.user.employeeInfo || uni.getStorageSync('userInfo') || {})

const loading = ref(false)
const loadingMore = ref(false)
const recordList = ref([])
const loadFailed = ref(false)
// 分页状态(后端 /hr/application/page 支持 pageNum/pageSize)
const pageNum = ref(1)
const pageSize = 10
const finished = ref(false)
// 请求序号:刷新时丢弃旧的加载更多响应,避免竞态
let requestSeq = 0

const statusMap = {
  0: { name: '审批中', color: 'orangeyellow' },
  1: { name: '已通过', color: 'green' },
  2: { name: '已拒绝', color: 'orangered' },
  3: { name: '已撤销', color: 'black' }
}

const typeName = (appType) => {
  const map = {
    leave: '请假',
    overtime: '加班',
    makeup: '补卡',
    business: '出差',
    exchange: '换休',
    resignation: '离职',
    regularization: '转正',
    transfer: '调动'
  }
  return map[appType] || appType || '请假'
}

const formatTime = (value) => {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

const normalizeRecord = (item) => {
  const meta = statusMap[Number(item.status)] || statusMap[0]
  return {
    id: item.id,
    type: typeName(item.appType) + '申请',
    state: meta.name,
    color: meta.color,
    title: item.reason || item.remark || '未填写事由',
    startTime: formatTime(item.startTime),
    endTime: formatTime(item.endTime),
    date: formatTime(item.createdTime)
  }
}

const normalizeRecords = (data) => {
  if (Array.isArray(data)) return data
  return data?.records || data?.rows || data?.list || []
}

// 拉取一页记录(reset=true 为刷新/第一页)
const loadRecords = async (reset = false) => {
  if (!employeeInfo.value.id) {
    recordList.value = []
    finished.value = true
    loadFailed.value = false
    loading.value = false
    return
  }
  if (!reset && (loading.value || loadingMore.value || finished.value)) return
  const seq = ++requestSeq
  if (reset) {
    loading.value = true
    loadFailed.value = false
  } else {
    loadingMore.value = true
  }
  try {
    const page = reset ? 1 : pageNum.value + 1
    const res = await getMyApplications({
      pageNum: page,
      pageSize,
      appType: 'leave',
      employeeId: employeeInfo.value.id
    })
    // 请求期间又发起了刷新,丢弃旧响应
    if (seq !== requestSeq) return
    const records = normalizeRecords(res.data).map(normalizeRecord)
    if (reset) {
      recordList.value = records
      pageNum.value = 1
    } else {
      const seen = new Set(recordList.value.map((item) => String(item.id)))
      recordList.value = recordList.value.concat(records.filter((item) => !seen.has(String(item.id))))
      pageNum.value = page
    }
    // 服务端返回 total 时按 total 判断,否则按页大小判断
    const total = (!Array.isArray(res.data) && res.data?.total != null) ? Number(res.data.total) : null
    finished.value = records.length < pageSize || (total !== null && total <= recordList.value.length)
    loadFailed.value = false
  } catch (error) {
    if (seq !== requestSeq) return
    if (reset) {
      loadFailed.value = true
    } else {
      uni.showToast({ icon: 'none', title: '加载失败，请重试' })
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
      loadingMore.value = false
    }
  }
}

const refreshRecords = () => {
  pageNum.value = 1
  finished.value = false
  return loadRecords(true)
}

const loadMoreRecords = () => {
  loadRecords(false)
}

onShow(() => {
  refreshRecords()
})

onPullDownRefresh(async () => {
  try {
    await refreshRecords()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onReachBottom(() => {
  loadMoreRecords()
})
</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    width: 60%;
    height: 100%;
    position: relative;
    display: flex;
    justify-content: space-evenly;
    align-items: center;
    box-sizing: border-box;
    background-color: rgba(0, 0, 0, 0.15);
    border-radius: 1000rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.5);
    color: #FFFFFF;
    font-size: 18px;

    .icon {
      display: block;
      flex: 1;
      margin: auto;
      text-align: center;
    }
  }

  /* 新增OA色系，自行调用，或者拿色值去用，多种方式*/
  .oa-black{
    color: #1D2541;
    background-color: #1D2541;
  }
  .oa-blue{
    color: #4B98FE;
    background-color: #4B98FE;
  }
  .oa-orangeyellow{
    color: #FFAC00;
    background-color: #FFAC00;
  }
  .oa-green{
    color: #00D05E;
    background-color: #00D05E;
  }
  .oa-orange{
    color: #FE871B;
    background-color: #FE871B;
  }
  .oa-cyan{
    color: #00C8B0;
    background-color: #00C8B0;
  }
  .oa-indigo{
    color: #00B9FE;
    background-color: #00B9FE;
  }
  .oa-orangered{
    color: #FB6A67;
    background-color: #FB6A67;
  }
  .oa-purple{
    color: #957BFE;
    background-color: #957BFE;
  }

  .oa-content{
    max-width: 640px;
    margin: 0 auto;
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }

  /* 文字截取*/
  .clamp-text-1 {
    -webkit-line-clamp: 1;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .clamp-text-2 {
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  /* 失败重试按钮 */
  .record-retry {
    display: inline-block;
    margin-top: 30rpx;
    padding: 14rpx 60rpx;
    border-radius: 1000rpx;
    color: #3668FC;
    font-size: 27rpx;
    font-weight: 600;
    background-color: rgba(54, 104, 252, 0.1);
  }

  /* 背景阴影 start*/
  .content-bg {
    border-radius: 15rpx;
    background-color: #FFFFFF;
  }

  /* 标签 start*/
  .tag-state {
    display: inline-block;
    padding: 8rpx 12rpx 6rpx;
    margin: 12rpx 15rpx 15rpx 10rpx;
    border-radius: 10rpx;
  }

</style>
