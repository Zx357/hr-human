<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-padding-left">
        <text class="tn-text-bold tn-text-xl tn-color-black">好友申请</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <view class="tn-text-justify" v-if="requests.length">
        <view class="tn-margin" v-for="item in requests" :key="item.id">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" :style="{ backgroundImage: `url(${formatAvatar(item.requesterAvatar)})` }">
                  <view class="logo-image"></view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="tn-color-black">{{ item.requesterName || '同事' }}</text>
                      </view>
                      <view v-if="item.status === 0" class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view>
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;">
                      <text class="tn-color-black">{{ item.remark || '申请添加为好友' }}</text>
                    </view>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">{{ formatTime(item.createdTime) }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-row-right">
              <template v-if="item.status === 0">
                <view class="justify-content-item tn-margin-right-xs">
                  <tn-button
                    bg-color="#EA5E55"
                    size="lg"
                    :fontSize="24"
                    text-color="#FFFFFF"
                    @click="handle(item, 2)"
                  >
                    <text>拒 绝</text>
                  </tn-button>
                </view>
                <view class="justify-content-item">
                  <tn-button
                    bg-color="#00D05E"
                    size="lg"
                    :fontSize="24"
                    text-color="#FFFFFF"
                    @click="handle(item, 1)"
                  >
                    <text>同 意</text>
                  </tn-button>
                </view>
              </template>
              <view v-else :class="item.status === 1 ? 'tn-color-gray' : 'tn-color-red--disabled'">
                {{ statusText(item.status) }}
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="tn-text-justify" v-if="!requests.length">
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242480-assets/web-upload/f09e233b-953b-4b1a-8a77-2a75f2445655.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="tn-color-black" style="">抓住那只猪</text>
                      </view>
                      <view class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view>
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;">
                      <text class="tn-color-black">你好，我是研发部的没道李</text>
                    </view>
                    <!-- <view class="">
                      <text class="tn-color-red tn-icon-like-fill tn-text-xxl"></text>
                    </view> -->
                  </view>
                  <!-- <view class="" style="max-width: 50vw;margin: 0 20rpx 15rpx 20rpx;">
                    <text class="tn-color-gray tn-text-sm">部门：产品研发部</text>
                  </view> -->
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">5月21日 18:09</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-row-right">
              <view class="justify-content-item tn-margin-right-xs">
                <tn-button
                  bg-color="#EA5E55"
                   size="lg"
                  :fontSize="24"
                  text-color="#FFFFFF"
                  @click="tn('')"
                >
                  <text class="">拒 绝</text>
                </tn-button>
              </view>
              <view class="justify-content-item">
                <tn-button
                  bg-color="#00D05E"
                  size="lg"
                  :fontSize="24"
                  text-color="#FFFFFF"
                  @click="tn('')"
                >
                  <text class="">同 意</text>
                </tn-button>
              </view>
            </view>
          </view>
        </view>
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242480-assets/web-upload/f09e233b-953b-4b1a-8a77-2a75f2445655.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="tn-color-black" style="">抓住那只猪</text>
                      </view>
                      <view class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view>
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;">
                      <text class="tn-color-black">主管你好，我是新来的蔡东东</text>
                    </view>
                    <!-- <view class="">
                      <text class="tn-color-red tn-icon-like-fill tn-text-xxl"></text>
                    </view> -->
                  </view>
                  <view class="" style="max-width: 50vw;margin: 0 20rpx 15rpx 20rpx;">
                    <text class="tn-color-gray tn-text-sm">部门：产品研发部</text>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">5月20日 12:29</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-row-right">
              <view class="justify-content-item tn-margin-right-xs">
                <tn-button
                  bg-color="#EA5E55"
                   size="lg"
                  :fontSize="24"
                  text-color="#FFFFFF"
                  @click="tn('')"
                >
                  <text class="">拒 绝</text>
                </tn-button>
              </view>
              <view class="justify-content-item">
                <tn-button
                  bg-color="#00D05E"
                  size="lg"
                  :fontSize="24"
                  text-color="#FFFFFF"
                  @click="tn('')"
                >
                  <text class="">同 意</text>
                </tn-button>
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242480-assets/web-upload/f09e233b-953b-4b1a-8a77-2a75f2445655.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="tn-color-black" style="">抓住那只猪</text>
                      </view>
                      <!-- <view class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view> -->
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;">
                      <text class="tn-color-black">申请添加为好友</text>
                    </view>
                    <!-- <view class="">
                      <text class="tn-color-red tn-icon-like-fill tn-text-xxl"></text>
                    </view> -->
                  </view>
                  <view class="" style="max-width: 50vw;margin: 0 20rpx 15rpx 20rpx;">
                    <text class="tn-color-gray tn-text-sm">部门：产品研发部</text>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">5月18日 09:56</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-row-right">
              <!-- <view class="justify-content-item tn-margin-right-xs">
                <tn-button backgroundColor="#EA5E55" :custom-style="{padding:'22rpx 0'}" size="sm" width="80rpx">
                  <text class="tn-color-white">拒 绝</text>
                </tn-button>
              </view>
              <view class="justify-content-item">
                <tn-button backgroundColor="#00D05E" :custom-style="{padding:'22rpx 0'}" size="sm" width="80rpx">
                  <text class="tn-color-white">同 意</text>
                </tn-button>
              </view> -->
              <view class="tn-color-red--disabled">
                已拒绝
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242480-assets/web-upload/f09e233b-953b-4b1a-8a77-2a75f2445655.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="tn-color-black" style="">抓住那只猪</text>
                      </view>
                      <!-- <view class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view> -->
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;">
                      <text class="tn-color-black">同事你好，我是吃人陈</text>
                    </view>
                    <!-- <view class="">
                      <text class="tn-color-red tn-icon-like-fill tn-text-xxl"></text>
                    </view> -->
                  </view>
                  <view class="" style="max-width: 50vw;margin: 0 20rpx 15rpx 20rpx;">
                    <text class="tn-color-gray tn-text-sm">部门：产品研发部</text>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">5月16日 10:02</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item tn-flex tn-flex-row-right">
              <view class="tn-color-gray">
                已通过
              </view>
              <!-- <view class="justify-content-item tn-margin-right-xs">
                <tn-button backgroundColor="#EA5E55" :custom-style="{padding:'22rpx 0'}" size="sm" width="80rpx">
                  <text class="tn-color-white">拒 绝</text>
                </tn-button>
              </view>
              <view class="justify-content-item">
                <tn-button backgroundColor="#00D05E" :custom-style="{padding:'22rpx 0'}" size="sm" width="80rpx">
                  <text class="tn-color-white">同 意</text>
                </tn-button>
              </view> -->
            </view>
          </view>
        </view>
        
      </view>
      
    </view>
    
    <view class='tn-tabbar-height'></view>
    
  </view>
</template>

<script setup>
  import { onMounted, ref } from 'vue'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  import config from '@/config'
  import { getContactRequests, handleContactRequest } from '@/api/contact'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()
  const requests = ref([])

  const formatAvatar = (avatar) => {
    if (!avatar) return '/static/author.jpg'
    if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
    return config.baseUrl + avatar
  }

  const formatTime = (value) => {
    if (!value) return ''
    const date = String(value).replace('T', ' ')
    return date.length > 10 ? date.slice(5, 16) : date
  }

  const statusText = (status) => {
    if (status === 1) return '已通过'
    if (status === 2) return '已拒绝'
    return '待处理'
  }

  const loadRequests = async () => {
    try {
      const res = await getContactRequests({ type: 'received' })
      requests.value = Array.isArray(res.data) ? res.data : []
    } catch (error) {
      console.log('加载好友申请失败', error)
    }
  }

  const handle = async (item, status) => {
    try {
      await handleContactRequest(item.id, { status })
      item.status = status
      uni.showToast({
        title: status === 1 ? '已同意' : '已拒绝',
        icon: 'success'
      })
    } catch (error) {
      console.log('处理好友申请失败', error)
    }
  }

  onMounted(() => {
    loadRequests()
  })
  // 处理返回事件
 
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
  
  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }
  
  /* 用户头像 start */
  .logo-image {
    width: 70rpx;
    height: 70rpx;
    position: relative;
  }
  
  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    border: 1rpx solid #F8F7F8;
    background-position: center;
    // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 100rpx;
    overflow: hidden;
    // background-color: #FFFFFF;
  }
  
</style>
