<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF00" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">用户信息</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
      <view class="user-fixed">
        
      </view>
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding" style="position: relative;z-index: 9999;">
        <view class="justify-content-item">
          <view class="tn-flex tn-flex-col-center tn-flex-row-left">
            <view class="user-pic">
              <view class="user-image" :style="avatarStyle">
              </view>
            </view>
            <view class="tn-color-black" style="width: 68vw;margin-top: -10rpx;">
              <view class="tn-padding-right tn-padding-left-sm tn-text-xl tn-text-bold">
                {{ user.name || '未命名员工' }}
              </view>
              <view class="tn-padding-right tn-padding-left-sm clamp-text-2 tn-text-sm" style="opacity: 0.4;margin-top: 10rpx">
                {{ maskedPhone }}
              </view>
            </view>
          </view>
        </view>
        <view class="justify-content-item tn-flex-row-center">
          <view class="tn-padding-left-xs">
            <tn-icon name="edit" class="tn-text-xxl tn-color-gray"></tn-icon>
          </view>
        </view>
      </view>
      
      <view class="" style="margin-top: 40rpx;padding-bottom: 160rpx;">
        
        <view class="tn-padding-top" style="background-color: #FFFFFF;border-radius: 40rpx 40rpx 0 0;position: relative;z-index: 9999;">
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                备注
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <view class="tn-color-gray">
                {{ user.employeeNo || '-' }}
              </view>
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                所属部门
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <view class="tn-color-gray">
                {{ user.deptName || user.companyName || '-' }}
              </view>
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                职位
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <view class="tn-color-gray">
                {{ user.position || user.post || user.duty || '-' }}
              </view>
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                Email
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <view class="tn-color-gray">
                {{ user.email || '-' }}
              </view>
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                性别
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <view class="tn-color-gray">
                {{ genderText }}
              </view>
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                Ta的主页
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <tn-icon name="right" class="tn-color-gray"></tn-icon>
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-between tn-strip-bottom-min tn-padding">
            <view class="justify-content-item">
              <view class="tn-text-bold tn-text-lg">
                推荐给同事
              </view>
            </view>
            <view class="justify-content-item tn-color-gray tn-text-right">
              <tn-icon name="right" class="tn-color-gray"></tn-icon>
            </view>
          </view>
          
        </view>
      
      </view>
      
      
      <!-- 好友-->
      <!-- 悬浮按钮-->
      <view class="tn-flex tn-footerfixed tn-padding">
        <view class="tn-flex-1 justify-content-item tn-margin-right-xs tn-text-center tn-bg-white" style="border-radius: 100rpx;">
          <tn-button
            bg-color="#FFAC00"
            :custom-style="{padding:'40rpx 0'}"
            width="100%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            @click="callPhoneNumber"
          >
            <text class="">打电话</text>
          </tn-button>
        </view>
        <view class="tn-flex-1 justify-content-item tn-margin-left-xs tn-text-center">
          <tn-button
            bg-color="#00C8B0"
            :custom-style="{padding:'40rpx 0'}"
            width="100%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            @click="startChat"
          >
            <text class="">发消息</text>
          </tn-button>
        </view>
      </view>
      <!-- <view class="tn-footerfixed tn-padding" style="z-index: 9999;">
        <view class="tn-flex tn-flex-row-between tn-bg-white tn-padding-top-xs tn-padding-bottom-xs" style="border-radius: 100rpx;box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.07);">
          <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
            <tn-button shape="round" backgroundColor="tn-bg-orange" fontColor="#FFFFFF" :custom-style="{padding:'35rpx 0'}" width="90%" :fontSize="30" @click="tn('')">
              <text class="tn-icon-comment tn-padding-right-xs"></text>
              <text class="">发消息</text>
            </tn-button>
          </view>
          <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
            <tn-button shape="round" backgroundColor="#00C8B0" fontColor="#FFFFFF" :custom-style="{padding:'35rpx 0'}" width="90%" :fontSize="30" >
              <text class="tn-icon-phone tn-padding-right-xs"></text>
              <text class="">打电话</text>
            </tn-button>
          </view>
        </view>
      </view> -->
      
      <!-- 非好友-->
      <!-- <view class="tn-footerfixed tn-padding" style="z-index: 9999;">
        <view class="tn-flex tn-flex-row-between tn-bg-white tn-padding-top-xs tn-padding-bottom-xs" style="border-radius: 100rpx;box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.07);">
          <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
            <tn-button shape="round" backgroundColor="#00C8B0" fontColor="#FFFFFF" :custom-style="{padding:'35rpx 0'}" width="90%" :fontSize="30">
              <text class="tn-icon-add tn-padding-right-xs"></text>
              <text class="">加好友</text>
            </tn-button>
          </view>
        </view>
      </view> -->
      
    </view>
    
    
    <view class='tn-tabbar-height'></view>
    
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  import { useStore } from 'vuex'
  import config from '@/config'
  import { getEmployeeDetail } from '@/api/employee'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()

  const user = ref({
    name: '付衣衣',
    employeeNo: '付总-设计主管',
    deptName: '产品研发部',
    position: '设计总监',
    phone: '18219128888',
    email: 'fuyiyi@163.com',
    gender: '2',
    avatar: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg'
  })

  const avatarStyle = computed(() => ({
    backgroundImage: `url(${formatAvatar(user.value.avatar)})`,
    width: '110rpx',
    height: '110rpx',
    backgroundSize: 'cover'
  }))

  const maskedPhone = computed(() => {
    const phone = user.value.phone || ''
    if (phone.length < 7) return phone || '-'
    return `${phone.slice(0, 3)} **** ${phone.slice(-4)}`
  })

  const genderText = computed(() => {
    const gender = String(user.value.gender || '')
    if (gender === '1' || gender === '男') return '男'
    if (gender === '2' || gender === '女') return '女'
    return '-'
  })

  const formatAvatar = (avatar) => {
    if (!avatar) return '/static/author.jpg'
    if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
    return config.baseUrl + avatar
  }

  const loadUser = async (id) => {
    if (!id) return
    try {
      const res = await getEmployeeDetail(id)
      if (res.data) {
        user.value = {
          ...user.value,
          ...res.data
        }
      }
    } catch (error) {
      console.log('加载用户信息失败', error)
    }
  }
  
  // 发消息:进入单聊会话
  const startChat = () => {
    const info = user.value || {}
    const id = info.id
    if (!id) {
      uni.showToast({ icon: 'none', title: '暂无法发起会话' })
      return
    }
    const store = useStore()
    const mine = store.getters.employeeInfo || uni.getStorageSync('userInfo') || {}
    const myId = store.getters.id || mine.id
    if (myId && String(myId) === String(id)) {
      uni.showToast({ icon: 'none', title: '不能给自己发消息' })
      return
    }
    const name = info.name || info.employeeName || '同事'
    uni.navigateTo({
      url: `/partnerPages/chat?type=single&targetId=${id}&name=${encodeURIComponent(name)}`,
    })
  }

  // 跳转
  const tn = (e) => {
    uni.navigateTo({
      url: e,
    });
  }
  
  //拨打固定电话
  const callPhoneNumber = () => {
    if (!user.value.phone) {
      uni.showToast({
        title: '暂无手机号',
        icon: 'none'
      })
      return
    }
    uni.makePhoneCall({
      phoneNumber: user.value.phone,
    });
  }

  onLoad((options) => {
    loadUser(options?.id)
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
  
  .oa-content{
    max-width: 640px;
    margin: 0 auto;
    // background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  /* 用户头像 start */
  .user-image {
    width: 110rpx;
    height: 110rpx;
    position: relative;
  }
  
  .user-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: top;
    border: 6rpx solid rgba(255,255,255,0.25);
    // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 1000rpx;
    overflow: hidden;
    // background-color: #FFFFFF;
  }
  
  // 渐变底色
  .user-fixed{
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    background: linear-gradient(90deg, #DBF2FE, #F3FDF8);
    top: 0;
    width: 100%;
    transition: all 0.25s ease-out;
    z-index: 100;
    height: 600rpx;
  }
  .user-fixed:before{
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
  
  /* 间隔线 start*/
  .tn-strip-bottom-min {
    width: 100%;
    border-bottom: 1rpx solid #F8F9FB;
  }
  
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid rgba(241, 241, 241, 0.8);
  }
  
  .tn-strip-top {
   width: 100%;
   border-top: 20rpx solid rgba(241, 241, 241, 0.8);
  }
   /* 间隔线 end*/
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
  	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    width: 100%;
    bottom: calc(40rpx + env(safe-area-inset-bottom));
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
  }
  /* 底部悬浮按钮 end*/
</style>
