<template>
	<view class="template-home tn-safe-area-inset-bottom">
    <!-- 顶部渐变欢迎区 -->
    <view class="home-banner">
      <tn-navbar fixed :bottomShadow="false" bg-color="#FFFFFF00" backText="" backIcon="" homeIcon="" >
        <template v-slot:back>
          <view class="custom-nav tn-flex tn-flex-col-center tn-flex-row-right">
            <view class="custom-nav__back">
              <tn-icon name="search-menu" class="tn-color-white" @click="tn('/homePages/search')"></tn-icon>
            </view>
          </view>
        </template>
      </tn-navbar>
      <view class="home-banner__greeting" :style="{ paddingTop: (vuex_custom_bar_height - 44) + 'px' }">
        <view class="tn-text-xl tn-text-bold tn-color-white">{{ greetingText }},{{ userName }}</view>
        <view class="tn-color-white tn-padding-top-xs home-banner__date">{{ todayText }}</view>
      </view>
      <view class="home-search" @click="tn('/homePages/search')">
        <tn-icon name="search" class="tn-color-gray"></tn-icon>
        <text class="tn-color-gray tn-padding-left-xs">搜索同事、公告、功能</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="home-shortcuts tn-radius tn-bg-white">
      <view
        v-for="(item, index) in shortcutList"
        :key="item.title"
        class="home-shortcut"
        @click="tn(item.url)"
      >
        <view class="home-shortcut__icon" :style="{ backgroundColor: shortcutColors[index % shortcutColors.length] }">
          <tn-badge v-if="item.badge" :value="item.badge" type="danger">
            <tn-icon :name="item.icon" class="tn-color-white"></tn-icon>
          </tn-badge>
          <tn-icon v-else :name="item.icon" class="tn-color-white"></tn-icon>
        </view>
        <text class="home-shortcut__title tn-color-gray--dark">{{ item.title }}</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <view class="home-messages tn-radius tn-bg-white">
      <view class="home-messages__head tn-text-bold">消息动态</view>
      <view class="">
        <view
          v-for="item in messageList"
          :key="item.id"
          class="home-message-item tn-flex tn-flex-col-center"
          @click="tn(item.url)"
        >
          <view class="home-message-icon-wrap">
            <view
              v-if="!item.avatar"
              class="icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white"
              :style="{ backgroundColor: item.color }"
            >
              <tn-icon :name="item.icon"></tn-icon>
            </view>
            <view v-else class="logo-pic">
              <view class="logo-image">
                <view :style="{ backgroundImage: `url(${item.avatar})`, width: '90rpx', height: '90rpx', backgroundSize: 'cover' }">
                </view>
              </view>
            </view>
          </view>
          <view class="home-message-main tn-padding-left-sm">
            <view class="tn-flex tn-flex-row-between tn-flex-col-between">
              <view class="justify-content-item home-message-title-wrap">
                <text class="home-message-title color-tnoa tn-text-lg tn-text-bold">{{ item.title }}</text>
              </view>
            </view>
            <view class="tn-padding-top-xs">
              <text class="home-message-desc tn-color-gray">{{ item.desc }}</text>
            </view>
          </view>
          <view class="home-message-meta">
            <view class="tn-flex tn-flex-row-right tn-margin-bottom-xs tn-margin-top-xs">
              <text class="tn-color-gray tn-text-sm">{{ item.time }}</text>
            </view>
            <view v-if="item.badge" class="message-dot tn-margin-top-xs">{{ item.badge }}</view>
          </view>
        </view>

      </view>
      <view v-if="!messageList.length" class="tn-padding-xl">
        <view class="tn-text-center tn-color-gray--disabled tn-text-lg">暂无消息</view>
      </view>

    </view>

    <view class="tn-tabbar-height"></view>
    
	</view>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue'
  import { onShow } from '@dcloudio/uni-app'
  import { useStore } from 'vuex'
  import { getHomeStats, getHomeMessages } from '@/api/home'
  import { getMomentMessages } from '@/api/moment'
  import { getNoticeList } from '@/api/system/notice'
  
  const store = useStore()
  // 使用 computed 保持响应式
  const vuex_custom_bar_height = computed(() => store.state.vuex_custom_bar_height)

  const userName = computed(() => store.state.user?.name || '同事')
  const greetingText = computed(() => {
    const hour = new Date().getHours()
    if (hour < 6) return '夜深了'
    if (hour < 9) return '早上好'
    if (hour < 12) return '上午好'
    if (hour < 14) return '中午好'
    if (hour < 18) return '下午好'
    return '晚上好'
  })
  const todayText = computed(() => {
    const now = new Date()
    const weeks = ['日', '一', '二', '三', '四', '五', '六']
    return `${now.getMonth() + 1}月${now.getDate()}日 星期${weeks[now.getDay()]}`
  })
  const shortcutColors = ['#4B98FE', '#00C8B0', '#FFAC00', '#957BFE']

  const stats = ref({
    pendingCount: 0,
    approvalCount: 0,
    approvedCount: 0
  })
  const notices = ref([])
  const backendMessages = ref([])
  const momentSummary = ref({
    unreadCount: 0,
    likeCount: 0,
    commentCount: 0,
    mentionCount: 0
  })

  const formatBadge = (value) => {
    const count = Number(value || 0)
    if (count <= 0) return ''
    return count > 99 ? '99+' : count
  }

  const momentBadgeCount = computed(() => {
    const summary = momentSummary.value
    return Number(summary.likeCount || 0) + Number(summary.commentCount || 0) +
      Number(summary.mentionCount || 0) + Number(summary.unreadCount || 0)
  })

  const shortcutList = computed(() => [
    {
      title: '互动',
      icon: 'topics-fill',
      badge: formatBadge(momentBadgeCount.value),
      url: '/momentPages/message'
    },
    {
      title: '待办',
      icon: 'flag-fill',
      badge: formatBadge(stats.value.pendingCount),
      url: '/homePages/pending'
    },
    {
      title: '审批',
      icon: 'seal',
      badge: formatBadge(stats.value.approvalCount),
      url: '/homePages/approval'
    },
    {
      title: '系统',
      icon: 'notice-fill',
      badge: formatBadge(stats.value.noticeCount ?? notices.value.length),
      url: '/homePages/notice'
    }
  ])

  const messageList = computed(() => {
    const apiMessages = backendMessages.value.map((item, index) => ({
      id: item.id || `message-${index}`,
      title: item.title || '消息提醒',
      desc: item.desc || '暂无内容',
      time: item.time || '',
      color: item.color || '#4B98FE',
      icon: item.icon || 'notice-fill',
      badge: item.badge || '',
      url: item.url || '/homePages/notice',
      avatar: item.avatar
    }))
    if (apiMessages.length) {
      return apiMessages
    }

    const noticeMessages = notices.value.slice(0, 3).map((item, index) => ({
      id: `notice-${item.id || index}`,
      title: item.noticeTitle || '系统通知',
      desc: stripHtml(item.noticeContent) || '暂无内容',
      time: formatDate(item.publishTime || item.createdTime),
      color: Number(item.noticeType) === 2 ? '#4B98FE' : '#00C8B0',
      icon: Number(item.noticeType) === 2 ? 'notice-fill' : 'image-text-fill',
      url: '/homePages/notice'
    }))

    return [
      {
        id: 'application',
        title: '应用消息',
        desc: stats.value.approvalCount
          ? `你有 ${stats.value.approvalCount} 条待审批事项需要处理`
          : '暂无新的待审批事项',
        time: '现在',
        color: '#4B98FE',
        icon: 'menu-fill',
        badge: formatBadge(stats.value.approvalCount),
        url: '/homePages/application'
      },
      ...noticeMessages
    ].slice(0, 12)
  })

  const stripHtml = (value) => {
    return String(value || '').replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
  }

  const formatDate = (value) => {
    if (!value) return ''
    const date = String(value).replace('T', ' ')
    return date.length > 10 ? date.slice(5, 16) : date
  }

  const normalizeList = (data) => {
    if (Array.isArray(data)) return data
    return data?.records || data?.rows || data?.list || []
  }

  const loadHomeData = async () => {
    try {
      const [statsResult, messageResult, noticeResult, momentResult] = await Promise.allSettled([
        getHomeStats(),
        getHomeMessages(),
        getNoticeList({ status: 1 }),
        getMomentMessages()
      ])
      if (statsResult.status === 'fulfilled') {
        stats.value = {
          ...stats.value,
          ...(statsResult.value.data || {})
        }
      }
      if (noticeResult.status === 'fulfilled') {
        notices.value = normalizeList(noticeResult.value.data)
      }
      if (messageResult.status === 'fulfilled') {
        backendMessages.value = normalizeList(messageResult.value.data)
      }
      if (momentResult.status === 'fulfilled') {
        momentSummary.value = { ...momentSummary.value, ...(momentResult.value.data || {}) }
      }
    } catch (error) {
      console.log('加载首页数据失败', error)
    }
  }
  
  // 跳转方法
  const tn = (e) => {
    if (!e) return
    uni.navigateTo({
      url: e,
    })
  }

  onShow(() => {
    loadHomeData()
  })

  onMounted(() => {
    loadHomeData()
  })
</script>

<style lang="scss" scoped>
	.template-home{
	  max-height: 100vh;
    max-width: 640px; 
    margin: 0 auto;
	}
  
  /* 自定义导航栏内容 start */
  .custom-nav {
    max-width: 640px; 
    height: 100%;
    
    &__back {
      margin: auto 5rpx;
      font-size: 40rpx;
      margin-right: 10rpx;
      margin-left: 30rpx;
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background-color: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  /* 自定义导航栏内容 end */
  
  /* 新增OA色系，自行调用，或者拿色值去用，多种方式*/
  .oa-black{
    color: #1D2541;
  }
  .oa-blue{
    color: #4B98FE;
  }
  .oa-orangeyellow{
    color: #FFAC00;
  }
  .oa-green{
    color: #00D05E;
  }
  .oa-orange{
    color: #FE871B;
  }
  .oa-cyan{
    color: #00C8B0;
  }
  .oa-indigo{
    color: #00B9FE;
  }
  .oa-orangered{
    color: #FB6A67;
  }
  .oa-purple{
    color: #957BFE;
  }
  
  /* 底部安全边距 start*/
  .tn-tabbar-height {
  	min-height: 120rpx;
  	height: calc(140rpx + env(safe-area-inset-bottom));
  	height: calc(140rpx + constant(safe-area-inset-bottom));
  }
  
  
  // 顶部渐变欢迎区
  .home-banner{
    max-width: 640px;
    margin: 0 auto;
    background: linear-gradient(135deg, #4B98FE 0%, #3668FC 100%);
    border-radius: 0 0 36rpx 36rpx;
    padding-bottom: 70rpx;
  }
  .home-banner__greeting{
    padding: 10rpx 36rpx 0;
  }
  .home-banner__date{
    font-size: 24rpx;
  }
  .home-search{
    margin: 30rpx 36rpx 0;
    height: 76rpx;
    background-color: #FFFFFF;
    border-radius: 100rpx;
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    font-size: 26rpx;
    box-shadow: 0 8rpx 24rpx rgba(29, 37, 65, 0.08);
  }
  // 快捷入口卡片
  .home-shortcuts{
    max-width: 600px;
    margin: -50rpx auto 0;
    position: relative;
    z-index: 2;
    display: flex;
    padding: 30rpx 10rpx 24rpx;
    box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
  }
  .home-shortcut{
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .home-shortcut__icon{
    width: 92rpx;
    height: 92rpx;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44rpx;
    margin-bottom: 12rpx;
  }
  .home-shortcut__title{
    font-size: 26rpx;
  }
  // 消息卡片
  .home-messages{
    max-width: 600px;
    margin: 24rpx auto 30rpx;
    padding: 26rpx 0 10rpx;
    box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
  }
  .home-messages__head{
    padding: 0 30rpx 10rpx;
    font-size: 30rpx;
    color: #1D2541;
  }

  .home-message-item {
    height: 110rpx;
    margin: 0 30rpx;
    border-bottom: 1rpx solid #F3F2F7;
    overflow: hidden;
  }

  .home-message-icon-wrap {
    flex: 0 0 90rpx;
    width: 90rpx;
  }

  .home-message-main {
    flex: 1;
    min-width: 0;
  }

  .home-message-title-wrap {
    min-width: 0;
    width: 100%;
  }

  .home-message-title,
  .home-message-desc {
    display: block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .home-message-title {
    line-height: 42rpx;
  }

  .home-message-desc {
    line-height: 36rpx;
  }

  .home-message-meta {
    flex: 0 0 76rpx;
    width: 76rpx;
    display: flex;
    flex-flow: column;
    align-items: flex-end;
  }
  /* OA黑色*/
  .color-tnoa{
    color: #1D2541;
  }
  
  /* 页面阴影 start*/
  .oa-shadow {
    border-radius: 15rpx;
    box-shadow: 0rpx 0rpx 50rpx 0rpx rgba(0, 0, 0, 0.07);
  }
  
  
 
  
  /* 图标容器1 start */
  .icon1 {
    &__item {
      width: 30%;
      background-color: #FFFFFF;
      border-radius: 10rpx;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 60rpx;
        border-radius: 50%;
        margin-bottom: 18rpx;
        position: relative;
        z-index: 1;
        
        &::after {
          content: " ";
          position: absolute;
          z-index: -1;
          width: 100%;
          height: 100%;
          left: 0;
          bottom: 0;
          border-radius: inherit;
          opacity: 1;
          transform: scale(1, 1);
          background-size: 100% 100%;
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg5.png);
        }
      }
    }
  }
  
  /* 图标容器15 start */
  .icon15 {
    &__item {
      width: 30%;
      background-color: #FFFFFF;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 90rpx;
        height: 90rpx;
        font-size: 60rpx;
        border-radius: 50%;
        position: relative;
        z-index: 1;
        
        &::after {
          content: " ";
          position: absolute;
          z-index: -1;
          width: 100%;
          height: 100%;
          left: 0;
          bottom: 0;
          border-radius: inherit;
          opacity: 1;
          transform: scale(1, 1);
          background-size: 100% 100%;
  
            
        }
      }
    }
  }
  
  /* 用户头像 start */
  .logo-image {
    width: 90rpx;
    height: 90rpx;
    position: relative;
  }
  
  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: center;
    // border: 1rpx solid rgba(255,255,255,0.05);
    // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 50%;
    overflow: hidden;
    // background-color: #FFFFFF;
  }
  
</style>
