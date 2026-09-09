<template>
  <scroll-view class="template-work tn-safe-area-inset-bottom" scroll-y="true" @scroll="handleScroll" :style="{height: '100vh'}">
   
    

    <view class="top-backgroup">
      <image src='/static/aa7.jpg' mode='widthFix' class='backgroud-image'></image>
    </view>
    
    <view class="work-clock-panel">
      <view class="work-clock-user tn-flex tn-flex-row-between tn-flex-col-center">
        <view class="tn-flex tn-flex-col-center">
          <view class="work-clock-avatar">
            <image :src="userAvatar" mode="aspectFill"></image>
            <view class="work-clock-online"></view>
          </view>
          <view class="work-clock-userinfo">
            <view class="work-clock-greeting">
              {{ greetingText }}，<text>{{ userName }}</text>
            </view>
            <view class="work-clock-date">
              <text>{{ todayDateText }}</text>
              <text class="work-clock-entry">{{ entryDaysLabel }}</text>
            </view>
          </view>
        </view>
        <view class="work-clock-bell tn-flex tn-flex-row-center tn-flex-col-center" @click="tn('/homePages/application')">
          <tn-icon name="notice-fill"></tn-icon>
          <view v-if="pendingCount > 0" class="work-clock-bell-dot"></view>
        </view>
      </view>

      <view class="work-clock-card tn-flex tn-flex-row-between tn-flex-col-center" @click="goToClock">
        <view>
          <view class="work-clock-time">
            <text>{{ currentTime }}</text>
            <text class="work-clock-seconds">{{ currentSeconds }}</text>
          </view>
          <view class="work-clock-weekday">{{ weekdayText }}</view>
        </view>
        <view class="work-clock-action">
          <view class="work-clock-location tn-flex tn-flex-row-center tn-flex-col-center">
            <view class="work-clock-ring"></view>
            <view class="work-clock-ring work-clock-ring--delay"></view>
            <view class="work-clock-location-inner tn-flex tn-flex-row-center tn-flex-col-center">
              <tn-icon name="location-fill"></tn-icon>
            </view>
          </view>
          <view class="work-clock-action-text">去打卡</view>
        </view>
      </view>
    </view>
   
    
    
    <view class="top-menu-section">
      <swiper class="card-swiper" :circular="iconPages.length > 1"
        :autoplay="false" duration="500" interval="5000" @change="cardSwiper"> 
        <swiper-item v-for="(page, pageIndex) in iconPages" :key="pageIndex" :class="cardCur==pageIndex?'cur':''">
          <!-- 方式5 start-->
          <view class="top-menu-grid">
           <block v-for="(item, index) in page" :key="index">
            <view class="top-menu-item" @click="tn(item.url)">
              <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                <!-- 当然，如果你有图片，可以换成图片模式 -->
                <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                </view> -->
                <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" :style="'background-color:'+ item.color +';'" >
                  <tn-icon :name="item.icon" style="text-shadow: 5rpx 8rpx 10rpx rgba(0,0,0,0.16);">
                  
                  </tn-icon>
                </view>
                <view class="top-menu-label tn-color-gray--dark tn-text-center">
                  <text class="tn-text-ellipsis">{{ item.title }}</text>
                </view>
              </view>
            </view>
           </block>
          </view>
          <!-- 方式5 end-->
        </swiper-item>
      </swiper>
      <view class="indication" v-if="iconPages.length > 1">
          <block v-for="(item,index) in iconPages" :key="index">
              <view class="spot" :class="cardCur==index?'active':''"></view>
          </block>
      </view>
    </view>

    
    
    
    <view class="tn-strip-bottom"></view>
    
    <!-- 标题-->
    <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-sm">
      <view class="justify-content-item tn-margin tn-text-bold tn-text-xl blue-title">
        考勤统计
      </view>
      <view class="justify-content-item tn-margin-right tn-text-df tn-color-gray" @click="tn('/workPages/calendar')">
        <text class="tn-padding-xs">考勤日历</text>
        <text class="tn-icon-right"></text>
      </view>
    </view>
    
    <!-- 方式12 start-->
    <view class="tn-flex tn-flex-wrap tn-padding-top-sm tn-padding-bottom-sm tn-bg-white">
      <view v-for="(item, index) in attendance" :key="index" style="width: 25%;">
        <view class="tn-margin-bottom tn-margin-top-sm" @click="tn(item.url)">
          <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
            <!-- 当然，如果你有图片，可以换成图片模式 -->
            <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
            </view> -->
            <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" :style="'background-color:'+ item.color +';'" >
              <tn-icon :name="item.icon" style="text-shadow: 5rpx 8rpx 10rpx rgba(0,0,0,0.16);">
               
              </tn-icon>
             
            </view>
            <view class="tn-color-gray--dark tn-text-center tn-text-df">
              <text class="tn-text-ellipsis">{{ item.title }}</text>
            </view>
          </view>
        </view>
     </view>
    </view>
    <!-- 方式12 end-->
    
    
    
    
    
    
<view class="tn-tabbar-height"></view>

  </scroll-view>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { getClockInfo, getHomeStats } from '@/api/attendance'
import { getMobileMenus } from '@/api/menu'

const store = useStore()
const vuex_custom_bar_height = store.state.vuex_custom_bar_height

const navOpacity = ref(0)
const pendingCount = ref(0)
const weekAttendance = ref({})
const currentTime = ref('--:--')
const currentSeconds = ref('--')
const colorList = ['#4B98FE', '#FFAC00', '#00D05E', '#FB6A67', '#957BFE', '#00B9FE', '#CC52E2']
const topMenuOrder = ['请假申请', '加班申请', '补卡申请', '离职申请', '出差申请', '换休申请']
const topMenuColorMap = {
  '请假申请': '#4B98FE',
  '加班申请': '#FFAC00',
  '补卡申请': '#00D05E',
  '离职申请': '#FB6A67',
  '出差申请': '#957BFE',
  '换休申请': '#00B9FE'
}
let clockTimer = null

const userName = computed(() => store.state.user?.name || '同事')
const userAvatar = computed(() => store.state.user?.avatar || '/static/author.jpg')
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
const todayDateText = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})
const weekdayText = computed(() => {
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekDays[new Date().getDay()]
})
const entryDaysLabel = computed(() => {
  const info = store.state.user?.employeeInfo || {}
  const dateStr = info.entryDate || info.entry_date
  if (!dateStr) return '欢迎使用'
  const entry = new Date(String(dateStr).replace(/-/g, '/'))
  const diff = Math.floor((Date.now() - entry.getTime()) / (1000 * 60 * 60 * 24))
  return diff >= 0 ? `已入职 ${diff} 天` : '欢迎使用'
})

const handleScroll = (e) => {
  const scrollTop = e.detail.scrollTop
  if (scrollTop > 150) {
    navOpacity.value = 1
  } else {
    navOpacity.value = scrollTop / 150
  }
}

// 卡片轮播当前索引
const cardCur = ref(0)

// 金刚区图标(优先使用后端移动端菜单)
const icons = ref(getFallbackMenus())

// 考勤统计四宫格(由本周考勤数据构建)
const attendance = ref([])

const iconPages = computed(() => {
  const pageSize = 8
  const pages = []
  for (let index = 0; index < icons.value.length; index += pageSize) {
    pages.push(icons.value.slice(index, index + pageSize))
  }
  return pages.length ? pages : [[]]
})








attendance.value = buildAttendanceItems()

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
})

// tab 页常驻内存,每次切到工作台都刷新
onShow(() => {
  loadWorkbench()
})

onUnmounted(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
    clockTimer = null
  }
})

function loadWorkbench() {
  loadMenus()
  loadWeekAttendance()
  loadPendingCount()
}

async function loadPendingCount() {
  try {
    const res = await getHomeStats()
    if (res.code === 200 && res.data) {
      pendingCount.value = Number(res.data.approvalCount || 0)
    }
  } catch (error) {
    console.log('加载待办数量失败', error)
  }
}

async function loadMenus() {
  try {
    const res = await getMobileMenus()
    if (res.code === 200) {
      const menus = normalizeMenus(res.data)
      icons.value = menus.length ? mergeTopMenus(menus) : getFallbackMenus()
    }
  } catch (error) {
    icons.value = getFallbackMenus()
  }
}

async function loadWeekAttendance() {
  try {
    const res = await getClockInfo()
    if (res.code === 200 && res.data) {
      weekAttendance.value = res.data.weekStatus || {}
      attendance.value = buildAttendanceItems()
    }
  } catch (error) {
    weekAttendance.value = {}
    attendance.value = buildAttendanceItems()
  }
}

function updateClock() {
  const now = new Date()
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  currentSeconds.value = String(now.getSeconds()).padStart(2, '0')
}

function normalizeMenus(payload) {
  const rawMenus = []
  if (Array.isArray(payload)) {
    rawMenus.push(...payload)
  } else if (payload && typeof payload === 'object') {
    Object.keys(payload).forEach((key) => {
      if (Array.isArray(payload[key])) rawMenus.push(...payload[key])
    })
  }

  const seen = new Set()
  return rawMenus.filter(Boolean).map((item, index) => {
    const title = item.menuName || item.title || item.name || '功能'
    const supportedTitle = matchTopMenuTitle(title)
    if (!supportedTitle || seen.has(supportedTitle)) return null
    seen.add(supportedTitle)
    return {
      title: supportedTitle,
      icon: iconForName(supportedTitle),
      color: topMenuColorMap[supportedTitle] || colorList[index % colorList.length],
      url: normalizeMenuUrl(item.path || item.url, supportedTitle)
    }
  }).filter(Boolean).sort((a, b) => topMenuOrder.indexOf(a.title) - topMenuOrder.indexOf(b.title))
}

function getFallbackMenus() {
  return [
    { title: '请假申请', icon: 'calendar-fill', color: topMenuColorMap['请假申请'], url: '/workPages/leave' },
    { title: '加班申请', icon: 'time-fill', color: topMenuColorMap['加班申请'], url: '/workPages/overtime' },
    { title: '补卡申请', icon: 'edit-form', color: topMenuColorMap['补卡申请'], url: '/workPages/replace' },
    { title: '离职申请', icon: 'reduce-circle-fill', color: topMenuColorMap['离职申请'], url: '/workPages/resign' },
    { title: '出差申请', icon: 'suitcase-fill', color: topMenuColorMap['出差申请'], url: '/workPages/travel' },
    { title: '换休申请', icon: 'menu-grille-fill', color: topMenuColorMap['换休申请'], url: '/workPages/exchange' }
  ]
}

function mergeTopMenus(menus) {
  const menuMap = new Map(getFallbackMenus().map((item) => [item.title, item]))
  menus.forEach((item) => {
    const fallback = menuMap.get(item.title) || {}
    menuMap.set(item.title, { ...fallback, ...item })
  })
  return topMenuOrder.map((title) => menuMap.get(title)).filter(Boolean)
}

function matchTopMenuTitle(name) {
  if (name.includes('请假')) return '请假申请'
  if (name.includes('加班')) return '加班申请'
  if (name.includes('补卡') || name.includes('缺卡')) return '补卡申请'
  if (name.includes('离职')) return '离职申请'
  if (name.includes('出差')) return '出差申请'
  if (name.includes('换休') || name.includes('调休')) return '换休申请'
  return ''
}

function iconForName(name) {
  if (name.includes('假')) return 'calendar-fill'
  if (name.includes('加班')) return 'time-fill'
  if (name.includes('补卡')) return 'edit-form'
  if (name.includes('出差')) return 'suitcase-fill'
  if (name.includes('转正')) return 'my-job-fill'
  if (name.includes('离职')) return 'reduce-circle-fill'
  if (name.includes('换休') || name.includes('调休')) return 'menu-grille-fill'
  if (name.includes('调动')) return 'transfer-fill'
  if (name.includes('审批')) return 'seal'
  if (name.includes('报销')) return 'money-fill'
  return 'menu-fill'
}

function routeForName(name) {
  if (name.includes('请假')) return '/workPages/leave'
  if (name.includes('加班')) return '/workPages/overtime'
  if (name.includes('补卡')) return '/workPages/replace'
  if (name.includes('离职')) return '/workPages/resign'
  if (name.includes('出差')) return '/workPages/travel'
  if (name.includes('换休') || name.includes('调休')) return '/workPages/exchange'
  return ''
}

function normalizeMenuUrl(url, title) {
  const route = routeForName(title)
  if (!url) return route
  if (url.includes('/pages/apply/') || url.includes('pages/apply/')) return route
  return url.startsWith('/') ? url : `/${url}`
}

function buildAttendanceItems() {
  const summary = getWeekSummary()
  return [
    { title: `正常${summary.normal}天`, icon: 'reload-planet-fill', color: '#4B98FE', url: '/workPages/calendar' },
    { title: `迟到${summary.late}次`, icon: 'rocket-fill', color: '#FB6A67', url: '/workPages/calendar' },
    { title: `缺勤${summary.absent}次`, icon: 'warning-fill', color: '#FFAC00', url: '/workPages/leave-record' },
    { title: `待确认${summary.unknown}天`, icon: 'notebook-fill', color: '#00B9FE', url: '/workPages/calendar' }
  ]
}

function getWeekSummary() {
  return getWeekDays().reduce(
    (summary, day) => {
      const status = weekAttendance.value[day]
      if (status === 'normal') summary.normal += 1
      else if (status === 'late') summary.late += 1
      else if (status === 'absent') summary.absent += 1
      else summary.unknown += 1
      return summary
    },
    { normal: 0, late: 0, absent: 0, unknown: 0 }
  )
}

function getWeekDays() {
  const now = new Date()
  const dayOfWeek = now.getDay() || 7
  const days = []
  for (let i = 1; i <= 7; i++) {
    const date = new Date(now)
    date.setDate(now.getDate() - dayOfWeek + i)
    days.push(formatDateKey(date))
  }
  return days
}

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 卡片轮播切换
const cardSwiper = (e) => {
  cardCur.value = e.detail.current
}

function goToClock() {
  uni.navigateTo({
    url: '/workPages/time',
    fail() {
      uni.showToast({ icon: 'none', title: '打卡功能待迁移' })
    }
  })
}

// 跳转
const tn = (e) => {
  if (!e) {
    uni.showToast({ icon: 'none', title: '功能待迁移' })
    return
  }
  uni.navigateTo({
    url: e,
    fail() {
      uni.showToast({ icon: 'none', title: '功能待迁移' })
    }
  })
}
</script>

<style lang="scss" scoped>
  .template-work {
    max-height: 100vh;
    max-width: 640px;
    margin: 0 auto;
    }
    
  /* 自定义导航栏内容 start */
  .custom-nav {
    position: fixed;
    top:0;
    left: 0;
    right: 0;
    height: 200rpx;
    z-index:99999;
    &__back {
      margin: auto 5rpx;
      font-size: 40rpx;
      margin-right: 10rpx;
      margin-left: 30rpx;
    }
  }
  
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
  
  // 四个角渐变底色
  .work-fixed{
    max-width: 640px; 
    position: fixed;
    top: 0;
    width: 100%;
  }
  /* 底部安全边距 start*/
  .tn-tabbar-height {
  	min-height: 120rpx;
  	height: calc(140rpx + env(safe-area-inset-bottom) / 2);
    height: calc(140rpx + constant(safe-area-inset-bottom));
  }
  
  /* 间隔线 start*/
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid #F8F7F8;
  }
  /* 间隔线 start*/
  .tn-strip-bottom-min {
   width: 100%;
   border-bottom: 1rpx solid #F8F7F8;
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
        border-radius: 20rpx;
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
  
            
  /* 企业列表头像 start */
  .company-image {
    width: 80rpx;
    height: 80rpx;
    position: relative;
  }
  
  .company-pic {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    border-radius: 20rpx;
    overflow: hidden;
    border: 1rpx solid #F8F7F8;
  }


  /* 头像*/
   .logo-image{
     width: 65rpx;
     height: 65rpx;
     position: relative;
   }
   .logo-pic{
     width: 65rpx;
     height: 65rpx;
      border-radius: 50%;
     background-size: cover;
     background-repeat:no-repeat;
     // background-attachment:fixed;
     background-position:top;
   }
   /* 自定义导航栏内容 end */
   
  /* 搜索栏 start */
   .tn-classify {
     
     &__search {
       &--wrap {
       }
       
       &__box {
         flex: 1;
         text-align: center;
         padding: 20rpx 30rpx;
         margin: 0 30rpx;
         border-radius: 60rpx;
         font-size: 30rpx;
       }
       
       &__icon {
         padding-right: 10rpx;
       }
       &__text {
         padding-right: 10rpx;
       }
     }
     /* 搜索栏 end */
   }
   
   /* 顶部背景图 start */
   .top-backgroup {
     height: 424rpx;
     max-height: 500rpx;
     overflow: hidden;
     z-index: -1;
   
     .backgroud-image {
       width: 100%;
       height: 424rpx;
       z-index: -1;
     }
   }
   /* 顶部背景图 end */
   
   /* 旧版打卡模块 start */
   .work-clock-panel {
     position: relative;
     z-index: 2;
     min-height: 424rpx;
     margin-top: -424rpx;
     padding: 54rpx 24rpx 28rpx;
     box-sizing: border-box;
     background: linear-gradient(135deg, #ECF4FF 0%, #D9E8F8 52%, #C7D7EE 100%);
     overflow: hidden;
   }

   .work-clock-panel::before {
     content: "";
     position: absolute;
     right: -110rpx;
     top: -90rpx;
     width: 360rpx;
     height: 360rpx;
     border-radius: 50%;
     background: rgba(255, 255, 255, 0.36);
   }

   .work-clock-user,
   .work-clock-card {
     position: relative;
     z-index: 1;
   }

   .work-clock-avatar {
     position: relative;
     width: 92rpx;
     height: 92rpx;
     flex-shrink: 0;
     border: 4rpx solid rgba(255, 255, 255, 0.9);
     border-radius: 50%;
     overflow: visible;
     box-shadow: 0 12rpx 30rpx rgba(64, 91, 132, 0.16);
   }

   .work-clock-avatar image {
     width: 100%;
     height: 100%;
     border-radius: 50%;
   }

   .work-clock-online {
     position: absolute;
     right: 2rpx;
     bottom: 0;
     width: 18rpx;
     height: 18rpx;
     border: 3rpx solid #DCE9F7;
     border-radius: 50%;
     background: #20d780;
   }

   .work-clock-userinfo {
     margin-left: 20rpx;
   }

   .work-clock-greeting {
     color: #5F7898;
     font-size: 28rpx;
     line-height: 1.35;
   }

   .work-clock-greeting text {
     color: #223554;
     font-size: 36rpx;
     font-weight: 900;
   }

   .work-clock-date {
     display: flex;
     align-items: center;
     flex-wrap: wrap;
     gap: 12rpx;
     margin-top: 6rpx;
     color: #6E83A0;
     font-size: 24rpx;
   }

   .work-clock-entry {
     padding: 4rpx 16rpx;
     border-radius: 999rpx;
     background: rgba(244, 249, 255, 0.62);
     color: #5A7395;
   }

   .work-clock-bell {
     position: relative;
     width: 76rpx;
     height: 76rpx;
     border-radius: 50%;
     color: #55719A;
     font-size: 42rpx;
     background: rgba(239, 246, 255, 0.62);
     box-shadow: 0 12rpx 28rpx rgba(76, 105, 150, 0.14);
     backdrop-filter: blur(10px);
   }

   .work-clock-bell-dot {
     position: absolute;
     right: 18rpx;
     top: 16rpx;
     width: 12rpx;
     height: 12rpx;
     border-radius: 50%;
     background: #ff4d6d;
   }

   .work-clock-card {
     margin-top: 36rpx;
     padding: 34rpx 34rpx 28rpx;
     border-radius: 24rpx;
     background: rgba(255, 255, 255, 0.96);
     box-shadow: 0 24rpx 70rpx rgba(67, 95, 138, 0.16);
   }

   .work-clock-time {
     display: flex;
     align-items: baseline;
     color: #172642;
     font-variant-numeric: tabular-nums;
   }

   .work-clock-time > text:first-child {
     font-size: 66rpx;
     font-weight: 900;
     letter-spacing: 1rpx;
   }

   .work-clock-seconds {
     margin-left: 12rpx;
     color: #7890B0;
     font-size: 28rpx;
     font-weight: 800;
   }

   .work-clock-weekday {
     margin-top: 8rpx;
     color: #607894;
     font-size: 28rpx;
   }

   .work-clock-action {
     display: flex;
     flex-direction: column;
     align-items: center;
     min-width: 116rpx;
   }

   .work-clock-location {
     position: relative;
     width: 96rpx;
     height: 96rpx;
   }

   .work-clock-ring {
     position: absolute;
     inset: 6rpx;
     border: 2rpx solid rgba(79, 111, 164, 0.2);
     border-radius: 50%;
     animation: workClockPulse 2.4s ease-out infinite;
   }

   .work-clock-ring--delay {
     animation-delay: 1.2s;
   }

   .work-clock-location-inner {
     position: relative;
     z-index: 1;
     width: 70rpx;
     height: 70rpx;
     border-radius: 50%;
     color: #ffffff;
     font-size: 42rpx;
     background: linear-gradient(135deg, #7FA0D2, #526FA6);
     box-shadow: 0 12rpx 28rpx rgba(82, 111, 166, 0.24);
   }

   .work-clock-action-text {
     margin-top: 6rpx;
     color: #526FA6;
     font-size: 26rpx;
     font-weight: 800;
   }

   @keyframes workClockPulse {
     0% {
       opacity: 1;
       transform: scale(0.78);
     }
     100% {
       opacity: 0;
       transform: scale(1.35);
     }
   }
   /* 旧版打卡模块 end */
   
   /* 轮播视觉差 start */
   .capsule-swiper {
     height: 260rpx !important;
     max-width: 640px;
   }
     
   .capsule-swiper swiper-item {
     width: 750rpx !important;
     left: 0rpx;
     box-sizing: border-box;
     overflow: initial;
     max-width: 640px;
   }
   
   @media screen and (max-width:400px) {
  	.capsule-swiper swiper-item {
  	  padding: 30rpx 10rpx 30rpx 10rpx;
  	}
   }
   
   @media screen and (min-width:400px) {
     .capsule-swiper {
       margin: 40rpx 0 0 0;
     }
   }
     
   .capsule-swiper swiper-item .swiper-item {
     width: 100%;
     display: block;
     height: 100%;
     border-radius: 10rpx;
     transform: scale(0.8);
     transition: all 0.6s ease-in 0s;
     will-change: transform;
     // overflow: hidden;
   }
     
   .capsule-swiper swiper-item.cur .swiper-item {
     transform: none;
     transition: all 0.6s ease-in 0s;
     will-change: transform;
   }
     
   .capsule-swiper swiper-item .swiper-item-text {
     margin-top: -220rpx;
     text-align: center;
     width: 100%;
     display: block;
     height: 50%;
     border-radius: 10rpx;
     transform: translate(100rpx, 0rpx) scale(0.9, 0.9);
     transition: all 0.6s ease 0s;
     will-change: transform;
     overflow: hidden;
   }
     
   .capsule-swiper swiper-item.cur .swiper-item-text {
     margin-top: -220rpx;
     width: 100%;
     transform: translate(0rpx, 0rpx) scale(0.9, 0.9);
     transition: all 0.6s ease 0s;
     will-change: transform;
   }
   
   
   /* 轮播指示点 start*/
   .indication3{
     max-width: 640px;
     z-index: 9999;
     width: 100%;
     height: 36rpx;
     position: absolute;
     display:flex;
     flex-direction:row;
     align-items:center;
     justify-content:center;
   }
   
   .spot3{
     background-color: #000000;
     opacity: 0.1;
     width: 10rpx;
     height: 10rpx;
     border-radius: 20rpx;
     top: -40rpx;
     margin: 0 8rpx !important;
     position: relative;
   }
   
   .spot3.active{
     opacity: 1;
     width: 30rpx;
     background-color: #000000;
     opacity: 0.1;
   }
   
  
   /* 金刚区轮播 start */
   .top-menu-section {
     background: #FFFFFF;
     padding: 18rpx 0 8rpx;
   }

   .top-menu-grid {
     display: flex;
     flex-wrap: wrap;
     padding-bottom: 18rpx;
   }

   .top-menu-item {
     width: 25%;
     padding: 12rpx 0 18rpx;
     box-sizing: border-box;
   }

   .top-menu-section .icon12__item--icon {
     width: 86rpx;
     height: 86rpx;
     margin-bottom: 12rpx;
     font-size: 46rpx;
   }

   .top-menu-label {
     width: 100%;
     padding: 0 4rpx;
     box-sizing: border-box;
     font-size: 25rpx;
     line-height: 1.25;
   }

   .card-swiper {
     height: 350rpx !important;
     max-width: 640px;
   }
     
   .card-swiper swiper-item {
     width: 100% !important;
     left: 0rpx;
     box-sizing: border-box;
     // padding: 0rpx 30rpx 90rpx 30rpx;
     overflow: initial;
   }
     
   .card-swiper swiper-item .swiper-item {
     width: 100%;
     display: block;
     height: 100%;
     transform: scale(1);
     transition: all 0.2s ease-in 0s;
     will-change: transform;
     overflow: hidden;
   }
     
   .card-swiper swiper-item.cur .swiper-item {
     transform: none;
     transition: all 0.2s ease-in 0s;
     will-change: transform;
   }
     
   .card-swiper swiper-item .swiper-item-text {
     margin-top: -300rpx;
     text-align: center;
     width: 100%;
     display: block;
     height: 50%;
     border-radius: 10rpx;
     transform: translate(100rpx, 0rpx) scale(0.9, 0.9);
     transition: all 0.6s ease 0s;
     will-change: transform;
     overflow: hidden;
   }
     
   .card-swiper swiper-item.cur .swiper-item-text {
     margin-top: -300rpx;
     width: 100%;
     transform: translate(0rpx, 0rpx) scale(0.9, 0.9);
     transition: all 0.6s ease 0s;
     will-change: transform;
   }
   
   .image-banner{
     display: flex;
     align-items: center;
     justify-content: center;
   }
   .image-banner image{
     width: 100%;
     height: 100%;
   }
   
   /* 轮播指示点 start*/
   .indication{
     max-width: 640px;
     z-index: 9999;
     width: 100%;
     height: 36rpx;
     position: absolute;
     display:flex;
     flex-direction:row;
     align-items:center;
     justify-content:center;
   }
   
   .spot{
     background-color: #000000;
     opacity: 0.1;
     width: 10rpx;
     height: 10rpx;
     border-radius: 20rpx;
     top: -70rpx;
     margin: 0 8rpx !important;
     position: relative;
   }
   
   .spot.active{
     opacity: 0.15;
     width: 30rpx;
     background-color: #000000;
   }
   
   .image-pic{
     // border: 1rpx solid #F8F7F8;
     background-size: cover;
     background-repeat:no-repeat;
     // background-attachment:fixed;
     background-position:top;
     border-radius: 10rpx;
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
   
   .blue-title::before {
     content: "";
     position: absolute;
     display: block;
     width: 80rpx;
     height: 26rpx;
     background: #269EFC;
     margin-top: 20rpx;
     margin-left: 70rpx;
     opacity: 0.09;
     z-index: -1;
     border-radius: 4rpx;
   }
   
   
  .icon12 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 56rpx;
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
          transform: scale(1, 1) rotate(19deg);
          background-size: 100% 100%;
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg4.png);
        }
      }
      
    }
  }
  
  .icon13 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 56rpx;
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
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg4.png);
            
        }
       
      }
      
    }
  }
  
   
   /* 工作区展示 start */
   .tn-info {
     
     &__container {
       margin-top: 10rpx;
       margin-bottom: 30rpx;
     }
     
     &__item {
       width: 47.7%;
       margin: 15rpx 0rpx 15rpx 0rpx;
       padding: 40rpx 30rpx;
       border-radius: 10rpx;
       
   
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
           background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/2.png);
         }
       
       &__left {
         
         &--icon {
           width: 80rpx;
           height: 80rpx;
           border-radius: 50%;
           font-size: 40rpx;
           margin-right: 20rpx;
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
         
         &__content {
           font-size: 25rpx;          
           
           &--data {
             color: rgba(255,255,255,0.5);
             margin-top: 5rpx;
             // font-weight: bold;
           }
         }
       }
       
       &__right {
         &--icon {
           position: absolute;
           right: 0rpx;
           top: 50rpx;
           font-size: 80rpx;
           width: 108rpx;
           height: 108rpx;
           text-align: center;
           line-height: 60rpx;
           opacity: 0.6;  
         }
       }
       // &__bottom {
       //   box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.12);
       //   border-radius: 0 0 10rpx 10rpx;
       //   position: absolute;
       //   width: 85%;
       //   line-height: 15rpx;
       //   left: 50%;
       //   bottom: -15rpx;
       //   transform: translateX(-50%);
       //   z-index: -1;
       //   text-align: center;
       // }
     }
   }
   /* 工作区展示 end */
   
   
   /* 广告*/
   
   .button-shake {
     animation: shake 4s infinite;
   }
   
   @keyframes shake {
     5%, 50% {
       transform: scale(1);
     }
     10% {
       transform: scale(0.9);
     }
     15% {
       transform: scale(1.15);
     }
     20% {
       transform: scale(1.15) rotate(-5deg);
     }
     25% {
       transform: scale(1.15) rotate(5deg);
     }
     30% {
       transform: scale(1.15) rotate(-3deg);
     }
     35% {
       transform: scale(1.15) rotate(2deg);
     }
     40% {
       transform: scale(1.15) rotate(0);
     }
   }
   
   
   
   /* 背景波浪高度 */
     .button-number {
       width: 100%;
       height: 150rpx;
       border-radius: 15rpx;
       position: relative;
       z-index: 1;
     }
   
   /* 动态背景波浪*/
   @keyframes move_wave {
       0% {
           transform: translateX(0) translateZ(0) scaleY(1)
       }
       50% {
           transform: translateX(-25%) translateZ(0) scaleY(1)
       }
       100% {
           transform: translateX(-50%) translateZ(0) scaleY(1)
       }
   }
   .tnwave {
       overflow: hidden;
       position: absolute;
       left: 0;
       right: 0;
       bottom: 0;
       top: 0;
       margin: auto;
       z-index: -1;
       border-radius: 15rpx;
   }
   .waveWrapperInner {
       position: absolute;
       width: 100%;
       overflow: hidden;
       height: 100%;
   }
   .wave {
       position: absolute;
       left: 0;
       width: 200%;
       height: 100%;
       background-repeat: repeat no-repeat;
       background-position: 0 bottom;
       transform-origin: center bottom;
   }
   
   .bgTop {
       opacity: 0.1;
   }
   .waveTop {
       background-size: 50% 45px;
   }
   .waveAnimation .waveTop {
     animation: move_wave 4s linear infinite;
   }
   
   .bgMiddle {
       opacity: 0.2;
   }
   .waveMiddle {
       background-size: 50% 40px;
   }
   .waveAnimation .waveMiddle {
       animation: move_wave 3.5s linear infinite;
   }
   
   .bgBottom {
       opacity: 0.3;
   }
   .waveBottom {
       background-size: 50% 35px;
   }
   .waveAnimation .waveBottom {
       animation: move_wave 2s linear infinite;
   }
    
</style>
