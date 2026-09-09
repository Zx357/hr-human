<template>
  <view class="template-moment tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottomShadow="false" bg-color="#FFFFFF"  backText="" backIcon="" homeIcon="">
      <template v-slot:back>
        <view class="custom-nav tn-flex tn-flex-col-center tn-flex-row-left"></view>
      </template>
      <template v-slot:default>
       <view class="" style="overflow: hidden;">
          <tn-tabs height="50rpx" :bar="false" v-model="current" activeColor="#000" bold fontSize="36" @change="tabChange">
            <tn-tabs-item
              v-for="(item, index) in scrollList"
              :key="index"
              :title="item.name"
            />
          </tn-tabs>
        </view>
      </template>
    </tn-navbar>
    
    <view class="" :style="{paddingTop: 20 + 'px'}">
      
      <view class="tn-flex tn-flex-row-center">
        <view class="tn-flex tn-flex-col-center tn-bg-gray--light" style="border-radius: 100rpx;" @click="tn('/momentPages/message')">
          <view class="" style="padding: 10rpx;">
            <view class="message-pic">
              <view class="message-image" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1671437658295-assets/web-upload/05620a1f-452e-4a14-9d30-f6c66ee4be1c.jpeg');width: 50rpx;height: 50rpx;background-size: cover;">
              </view>
            </view>
          </view>
          <view class="" style="padding: 10rpx 30rpx 10rpx 10rpx;">
            <view class="tn-flex tn-flex-row-between tn-flex-col-between">
              <view class="justify-content-item">
            <text class="tn-color-bdhook tn-text-df">{{ messageCount }} 条新消息</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="tn-flex tn-flex-direction-column tn-margin-top-sm tn-margin-bottom" v-if="currentContent.length">
           
        <!-- 图文信息 -->
        <block v-for="(item,index) in currentContent" :key="item.id || index">
          <view class="blogger__item tn-margin-bottom-sm tn-margin-top-sm">
            <view class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center">
              <view class="justify__author__info" @click="tn('/momentPages/blogger_other')">
                <view class="tn-flex tn-flex-row-center">
                  <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                    <view class="logo-pic">
                      <view class="logo-image" :style="'background-image:url(' + item.userAvatar + ');width: 70rpx;height: 70rpx;background-size: cover;'">
                      </view>
                    </view>
                    
                    <view class="tn-padding-right tn-text-ellipsis">
                      <view class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg">{{ item.userName }}</view>
                      <view class="tn-padding-right tn-padding-left-sm tn-padding-top-xs tn-color-gray tn-text-xs">{{ item.post }}</view>
                    </view>
                  </view>
                </view>
              </view>
              <view class="blogger__author__btn justify-content-item tn-flex-col-center tn-flex-row-center">
                <!-- 为什么不放关注按钮，因为快餐文化的世界，关注按钮放在外面没必要 -->
                <text class="tn-icon-more-vertical tn-color-gray tn-text-bold tn-text-xxl"></text>
              </view>
            </view>
            
            <view class="" style="margin-left: 90rpx;">
           
              <view class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left" @click="goDetail(item)">
                <view v-for="(label_item,label_index) in item.label" :key="label_index" class="blogger__desc__label tn-float-left tn-margin-right">
                  <text class="blogger__desc__label--prefix tn-icon-topics-fill"></text> 
                  <text class="tn-text-df">{{ label_item }}</text>
                </view>
                <!-- 不用限制长度了，因为发布的时候限制长度了-->
                 
              </view>
              <text v-if="!item.label || item.label.length < 4" class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df">{{ item.desc }}</text> 
              
              <block v-if="item.mainImage">
                <view v-if="[1,2,4].indexOf(item.mainImage.length) != -1" class="tn-padding-top-xs" @click="goDetail(item)">
                  <image v-for="(image_item,image_index) in item.mainImage" :key="image_index" 
                    class="blogger__main-image"
                    :class="{
                      'blogger__main-image--1 tn-margin-bottom-sm': item.mainImage.length === 1,
                      'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm': item.mainImage.length === 2 || item.mainImage.length === 4
                    }"
                    :src="image_item"
                    mode="aspectFill"
                  ></image>
                </view>
                <view v-else class="tn-padding-top-xs" style="" @click="goDetail(item)">
                  <view class="blogger__grid-image">
                    <block v-for="(image_item,image_index) in item.mainImage" :key="image_index">
                      <image
                        class="blogger__main-image blogger__main-image--3"
                        :src="image_item"
                        mode="aspectFill"
                      ></image>
                    </block>
                  </view>
                </view>
              </block>
             
              <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xs">
                <view class="justify-content-item tn-flex tn-flex-col-center">
                  <text class="tn-color-gray">{{ item.date }}</text>
                </view>
                <view class="justify-content-item tn-color-gray tn-text-center" style="padding-top: 5rpx;">
                  <text class="tn-icon-message tn-text-lg" style="padding-right: 5rpx;"></text>
                  <text class="tn-padding-right tn-text-df">{{ item.commentCount }}</text>
                  <text
                    :class="item.liked ? 'tn-icon-like-fill' : 'tn-icon-like-lack'"
                    class="tn-text-lg"
                    style="padding-right: 5rpx;"
                    @click.stop="toggleLike(item)"
                  ></text>
                  <text class="tn-text-df">{{ item.likeCount }}</text>
                </view>

              </view>
            
            </view>
            
          </view>
          
          <!-- 边距间隔 -->
          <view class="tn-strip-bottom-min" v-if="index != currentContent.length - 1"></view>
        </block>
        
        <!-- 边距间隔 -->
        <view class="tn-strip-bottom-min"></view>
        
        <!-- 图文信息 -->
        <block v-for="(item,index) in currentContent" :key="'second-' + (item.id || index)">
          <view class="blogger__item tn-margin-bottom-sm tn-margin-top-sm">
            <view class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center">
              <view class="justify__author__info" @click="tn('/momentPages/blogger_other')">
                <view class="tn-flex tn-flex-row-center">
                  <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                    <view class="logo-pic">
                      <view class="logo-image" :style="'background-image:url(' + item.userAvatar + ');width: 70rpx;height: 70rpx;background-size: cover;'">
                      </view>
                    </view>
                    
                    <view class="tn-padding-right tn-text-ellipsis">
                      <view class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg">{{ item.userName }}</view>
                      <view class="tn-padding-right tn-padding-left-sm tn-padding-top-xs tn-color-gray tn-text-xs">{{ item.post }}</view>
                    </view>
                  </view>
                </view>
              </view>
              <view class="blogger__author__btn justify-content-item tn-flex-col-center tn-flex-row-center">
                <text class="tn-icon-more-vertical tn-color-gray tn-text-bold tn-text-xxl"></text>
              </view>
            </view>
            
            <view class="" style="margin-left: 90rpx;">
           
              <view class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left" @click="goDetail(item)">
                <view v-for="(label_item,label_index) in item.label" :key="'label-' + label_index" class="blogger__desc__label tn-float-left tn-margin-right">
                  <text class="blogger__desc__label--prefix tn-icon-topics-fill"></text> 
                  <text class="tn-text-df">{{ label_item }}</text>
                </view>
                <text v-if="!item.label || item.label.length < 4" class="blogger__desc__content tn-flex-1 tn-text-justify tn-text-df">{{ item.desc }}</text>  
              </view>
              
              <block v-if="item.mainImage">
                <view v-if="[1,2,4].indexOf(item.mainImage.length) != -1" class="tn-padding-top-xs" @click="goDetail(item)">
                  <image v-for="(image_item,image_index) in item.mainImage" :key="'img-' + image_index" 
                    class="blogger__main-image"
                    :class="{
                      'blogger__main-image--1 tn-margin-bottom-sm': item.mainImage.length === 1,
                      'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm': item.mainImage.length === 2 || item.mainImage.length === 4
                    }"
                    :src="image_item"
                    mode="aspectFill"
                  ></image>
                </view>
                <view v-else class="tn-padding-top-xs" style="" @click="goDetail(item)">
                  <view class="blogger__grid-image">
                    <block v-for="(image_item,image_index) in item.mainImage" :key="'img-grid-' + image_index">
                      <image
                        class="blogger__main-image blogger__main-image--3"
                        :src="image_item"
                        mode="aspectFill"
                      ></image>
                    </block>
                  </view>
                </view>
              </block>
             
              <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top-xs">
                <view class="justify-content-item tn-flex tn-flex-col-center">
                  <text class="tn-color-gray">{{ item.date }}</text>
                </view>
                <view class="justify-content-item tn-color-gray tn-text-center" style="padding-top: 5rpx;">
                  <text class="tn-icon-message tn-text-lg" style="padding-right: 5rpx;"></text>
                  <text class="tn-padding-right tn-text-df">{{ item.commentCount }}</text>
                  <text
                    :class="item.liked ? 'tn-icon-like-fill' : 'tn-icon-like-lack'"
                    class="tn-text-lg"
                    style="padding-right: 5rpx;"
                    @click.stop="toggleLike(item)"
                  ></text>
                  <text class="tn-text-df">{{ item.likeCount }}</text>
                </view>
        
              </view>
            
            </view>
            
          </view>
          
          <!-- 边距间隔 -->
          <view class="tn-strip-bottom-min" v-if="index != currentContent.length - 1"></view>
        </block>
         
            
      </view>
      
      <view class="tn-flex tn-flex-direction-column tn-margin-top-sm tn-margin-bottom" v-if="!currentContent.length">
        <view class="" style="padding: 15vh 20rpx;">
          <view class="tn-text-center" style="font-size: 200rpx;padding-top: 30rpx;">
            <text class="tn-icon-clip tn-color-gray--light"></text>
          </view>
          <view class="tn-color-gray--disabled tn-text-center tn-text-lg">{{ emptyText }}</view>
        </view>      
      </view>
    
    </view>
    

    
    
    <view class="">
      <view class="icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-shadow-blur button-1" @click="tn('/momentPages/edit')">
        <tn-icon name="camera-fill" class="tn-color-white"></tn-icon>
      </view>
    </view>  
    

    <view class='tn-tabbar-height'></view>
    
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue'
  import { onShow } from '@dcloudio/uni-app'
  import config from '@/config'
  import { getMomentMessages, getMomentPosts, toggleMomentLike } from '@/api/moment'
 
  // 当前tab索引
const current = ref(0)

// tab列表
const scrollList = ref([
  {name: '最新'},
  {name: '推荐'},
  {name: '热门'}
])

// 内容默认隐藏显示的高度
const contentHideShowHeight = ref(0)

// 内容列表
const content = ref([])

const messageSummary = ref({
  unreadCount: 0
})

const messageCount = computed(() => messageSummary.value.unreadCount || 0)

const currentContent = computed(() => {
  if (current.value === 1) {
    return content.value.filter((item, index) => item.mainImage?.length || index % 2 === 0)
  }
  if (current.value === 2) {
    return [...content.value].sort((a, b) => b.likeCount - a.likeCount)
  }
  return content.value
})

const emptyText = computed(() => {
  if (current.value === 1) return '暂无推荐内容'
  if (current.value === 2) return '暂无热门内容'
  return '暂无动态，点击右下角发布第一条动态吧'
})

const getTabKey = () => {
  if (current.value === 1) return 'recommend'
  if (current.value === 2) return 'hot'
  return 'latest'
}

const splitField = (value) => {
  if (!value) return []
  if (Array.isArray(value)) return value
  return String(value).split(',').map((item) => item.trim()).filter(Boolean)
}

const formatAvatar = (avatar) => {
  if (!avatar) return 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
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

const normalizePost = (item) => {
  const images = splitField(item.images)
  const labels = splitField(item.labels)
  return {
    id: item.id,
    userAvatar: formatAvatar(item.authorAvatar),
    userName: item.authorName || '未命名员工',
    post: item.post || item.deptName || '团队成员',
    date: formatDate(item.createdTime),
    label: labels,
    desc: item.content || '',
    content: item.content || '',
    mainImage: images,
    commentCount: Number(item.commentCount || 0),
    likeCount: Number(item.likeCount || 0),
    liked: !!item.liked,
    raw: item
  }
}

const loadMoments = async () => {
  try {
    const res = await getMomentPosts({
      pageNum: 1,
      pageSize: 20,
      tab: getTabKey()
    })
    const list = normalizeList(res.data)
    content.value = list.map(normalizePost)
  } catch (error) {
    console.log('加载时光动态失败', error)
  }
}

const loadMomentMessages = async () => {
  try {
    const res = await getMomentMessages()
    messageSummary.value = {
      ...messageSummary.value,
      ...(res.data || {})
    }
  } catch (error) {
    console.log('加载时光消息失败', error)
  }
}

// tab选项卡切换
const tabChange = (index) => {
  current.value = index
  loadMoments()
}

const toggleLike = async (item) => {
  const oldLiked = !!item.liked
  const oldLikeCount = Number(item.likeCount || 0)
  item.liked = !oldLiked
  item.likeCount = Math.max(0, oldLikeCount + (item.liked ? 1 : -1))

  try {
    const res = await toggleMomentLike(item.id)
    if (res.data) {
      item.liked = !!res.data.liked
      item.likeCount = Number(res.data.likeCount || 0)
    }
  } catch (error) {
    item.liked = oldLiked
    item.likeCount = oldLikeCount
    console.log('点赞失败', error)
  }
}

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  })
}

// 帖子详情（携带真实id）
const goDetail = (item) => {
  if (!item || !item.id) return
  uni.navigateTo({
    url: `/momentPages/details?id=${item.id}`
  })
}

// tab 页常驻内存,每次切到时光都刷新
onShow(() => {
  loadMoments()
  loadMomentMessages()
})
</script>

<style lang="scss" scoped>
  .template-moment{
    max-height: 100vh;
    max-width: 640px;
    margin: 0 auto;
  }
  
/* 自定义导航栏内容 start */
  .custom-nav {
    height: 100%;
    max-width: 640px;
    &__back {
      margin: auto 5rpx;
      font-size: 45rpx;
      margin-right: 10rpx;
      margin-left: 30rpx;
      flex-basis: 5%;
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
  
  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
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
  
  /* 按钮 */
  .button-1 {
    background-color: rgba(0, 0, 0, 0.15);
    position: fixed;
    /* bottom:200rpx;
      right: 20rpx; */
    bottom: 24%;
    right: 30rpx;
    z-index: 1001;
    border-radius: 100px;
  }
  .button-2 {
    background-color: rgba(0, 0, 0, 0.15);
    position: fixed;
    /* bottom:200rpx;
      right: 20rpx; */
    bottom: 15%;
    right: 30rpx;
    z-index: 1001;
    border-radius: 100px;
  }
  
  /* 图标容器15 start */
  .icon15 {
    &__item {
      width: 30%;
      
      border-radius: 10rpx;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 90rpx;
        height: 90rpx;
        font-size: 50rpx;
        border-radius: 50%;
        margin-bottom: 18rpx;
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
  
  /* 悬浮 */
  .tnxuanfu{
    animation: suspension 3s ease-in-out infinite;
  }
  
  @keyframes suspension {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-0.8rem);
    }
  }
  /* 悬浮按钮 */
  .button-shop {
    width: 90rpx;
    height: 90rpx;
    display: flex;
    flex-direction: row;
    position: fixed;
    /* bottom:200rpx;
      right: 20rpx; */
    left: 5rpx;
    top: 5rpx;
    z-index: 1001;
    border-radius: 100px;
    opacity: 0.9;
  }
  
  
  /* 文章内容 start*/
    .blogger {
      &__item {
        padding: 30rpx;
      }
      
      &__author {
        &__btn {
          margin-right: -12rpx;
          opacity: 0.5;
        }
      }
      
      &__desc {
        line-height: 30rpx;
        
        &__label {
          
          color: #1D2541;
          background-color: #F3F2F7;
          border-radius: 10rpx;
          font-size: 22rpx;
          
          padding: 5rpx 15rpx;
          margin: 5rpx 18rpx 0 0;
          
          &--prefix {
            font-size: 24rpx;
            color: #1D2541;
            padding-right: 10rpx;
          }
        }
        &__content {
          line-height: 50rpx;
        }
      }
      
      &__content {
        margin-top: 18rpx;
        padding-right: 18rpx;
        
        &__data {
          line-height: 46rpx;
          text-align: justify;
          overflow: hidden;
          transition: all 0.25s ease-in-out;
  
        }
        
        &__status {
          margin-top: 10rpx;
          font-size: 26rpx;
          color: #82B2FF;
        }
      }
      
      &__main-image {
        border-radius: 16rpx;
        
        &--1 {
          max-width: 80%;
          max-height: 300rpx;
        }
        
        &--2 {
          max-width: 260rpx;
          max-height: 260rpx;
        }
        
        &--3 {
          height: 174rpx;
          width: 100%;
        }
      }

      &__grid-image {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 10rpx;
        margin-bottom: 10rpx;

        image {
          width: 100%;
          height: 174rpx;
          border-radius: 16rpx;
        }
      }
      
      &__count-icon {
        font-size: 40rpx;
        padding-right: 5rpx;
      }
      
      &__ad {
        width: 100%;
        height: 500rpx;
        transform: translate3d(0px, 0px, 0px) !important;
        
        ::v-deep .uni-swiper-slide-frame {
          transform: translate3d(0px, 0px, 0px) !important;
        }
        .uni-swiper-slide-frame {
          transform: translate3d(0px, 0px, 0px) !important;
        }
        
        &__item {
          position: absolute;
          width: 100%;
          height: 100%;
          transform-origin: left center;
          transform: translate3d(100%, 0px, 0px) scale(1) !important;
          transition: transform 0.25s ease-in-out;
          z-index: 1;
          
          &--0 {
            transform: translate3d(0%, 0px, 0px) scale(1) !important;
            z-index: 4;
          }
          &--1 {
            transform: translate3d(13%, 0px, 0px) scale(0.9) !important;
            z-index: 3;
          }
          &--2 {
            transform: translate3d(26%, 0px, 0px) scale(0.8) !important;
            z-index: 2;
          }
        }
        
        &__content {
          border-radius: 40rpx;
          width: 640rpx;
          height: 500rpx;
          overflow: hidden;
        }
        
        &__image {
          width: 100%;
          height: 100%;
        }
      }
    }
    /* 文章内容 end*/
    
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
      background-position: center;
      // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
      border-radius: 15rpx;
      overflow: hidden;
      // background-color: #FFFFFF;
    }
     
    /* 间隔线 start*/
    .tn-strip-bottom-min {
      width: 100%;
      border-bottom: 1rpx solid #F8F7F8;
    } 
     
    .tn-strip-bottom {
     width: 100%;
     border-bottom: 20rpx solid #F8F7F8;
    }
     /* 间隔线 end*/
  
 
  /* 最新消息-头像 start */
  .message-image {
    width: 50rpx;
    height: 50rpx;
    position: relative;
  }
  
  .message-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: center;
    // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 100rpx;
    overflow: hidden;
    // background-color: #FFFFFF;
  }
  
</style>
