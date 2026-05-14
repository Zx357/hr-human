<template>
	<view class="template-home tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottomShadow="false" bg-color="#FFFFFF00" backText="" backIcon="" homeIcon="" >
      <template v-slot:back>
        <view class="custom-nav tn-flex tn-flex-col-center tn-flex-row-left">
          <view class="custom-nav__back">
            <tn-icon name="search-menu" @click="tn('/homePages/search')"></tn-icon>
            <tn-icon name="discover" class="tn-padding-left" @click="tn('/minePages/navigation')"></tn-icon>
          </view>
        </view>
      </template>
    </tn-navbar>
    
    <!-- 方式1 start-->
    <view class="tn-flex home-fixed" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <view
        v-for="item in shortcutList"
        :key="item.title"
        class="home-shortcut tn-padding-sm tn-margin-xs tn-radius"
        @click="tn(item.url)"
      >
        <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
          <view class="icon1__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-bg-white tn-color-black">
            <tn-badge v-if="item.badge" :value="item.badge" type="danger">
              <tn-icon :name="item.icon"></tn-icon>
            </tn-badge>
            <tn-icon v-else :name="item.icon"></tn-icon>
          </view>  
          <view class="tn-color-gray--dark tn-text-center">
            <text class="tn-text-ellipsis">{{ item.title }}</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 方式1 end-->
    
    <view class="tn-margin-top-sm" :style="{paddingTop: vuex_custom_bar_height +'px'}" style="max-width: 640px; margin: 0 auto;">
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
  
    </view>
    
    <view class="tn-tabbar-height"></view>
    
	</view>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue'
  import { onShow } from '@dcloudio/uni-app'
  import { useStore } from 'vuex'
  import { getHomeStats, getHomeMessages } from '@/api/home'
  import { getNoticeList } from '@/api/system/notice'
  
  const store = useStore()
  // 使用 computed 保持响应式
  const vuex_custom_bar_height = computed(() => store.state.vuex_custom_bar_height)

  const stats = ref({
    pendingCount: 0,
    approvalCount: 0,
    approvedCount: 0
  })
  const notices = ref([])
  const backendMessages = ref([])

  const fallbackMessages = [
    {
      id: 'article',
      title: '行业资讯',
      desc: '熟读《2023年劳动合同法》',
      time: '12:06',
      color: '#FFAC00',
      icon: 'image-text-fill',
      badge: 2,
      url: '/homePages/article'
    },
    {
      id: 'approval',
      title: '审批通知',
      desc: '你的设备费用报销已通过审批',
      time: '08:06',
      color: '#00C8B0',
      icon: 'ticket-fill',
      badge: 2,
      url: '/homePages/approval'
    },
    {
      id: 'chat',
      title: '蔡东东',
      desc: '麻烦帮我处理一下这个申请',
      time: '10:38',
      color: '#4B98FE',
      icon: 'my-simple-fill',
      url: '/homePages/chat'
    },
    {
      id: 'daily',
      title: '团队提醒',
      desc: '今日请记得完成日报和下班打卡',
      time: '5月20日',
      avatar: 'https://resource.tuniaokj.com/images/simple/image2.jpg',
      url: '/homePages/chat'
    }
  ]

  const formatBadge = (value) => {
    const count = Number(value || 0)
    if (count <= 0) return ''
    return count > 99 ? '99+' : count
  }

  const shortcutList = computed(() => [
    {
      title: '互动',
      icon: 'topics-fill',
      badge: formatBadge(3),
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
      return [...apiMessages, ...fallbackMessages].slice(0, 12)
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
      ...noticeMessages,
      ...fallbackMessages
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
      const [statsResult, messageResult, noticeResult] = await Promise.allSettled([
        getHomeStats(),
        getHomeMessages(),
        getNoticeList({ status: 1 })
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
      font-size: 45rpx;
      margin-right: 10rpx;
      margin-left: 30rpx;
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
  
  
  // 四个角渐变底色
  .home-fixed{
    max-width: 640px;
    position: fixed;
    background: linear-gradient(90deg, #DBF2FE, #FFE1E1);
    top: 0;
    width: 100%;
    left: 50%;
    transform: translateX(-50%);
    justify-content: space-around;
    box-sizing: border-box;
    transition: all 0.25s ease-out;
    z-index: 100;
  }
  .home-shortcut {
    flex: 0 0 20%;
    max-width: 128px;
    box-sizing: border-box;
  }

  .home-message-item {
    height: 110rpx;
    margin: 24rpx 30rpx;
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
  .home-fixed:before{
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    right: 0;
    z-index: -1;
    mask-image: linear-gradient(to bottom, transparent, black);
    background: linear-gradient(90deg, #FFFFFF, #FFFFFF);	

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
