<template>
  <view class="template-wallpaper">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :placeholder="false" back-icon="" home-icon="" :bottom-shadow="false" :bg-color="navBarBackgroundColor" :font-color="navBarStyle.color" id="navbar">
      <view class="custom-nav__back" @click="goBack">
        <tn-icon name="left"></tn-icon>
      </view>
      <view class="custom-nav__search">
        <tn-icon name="search"></tn-icon>
        <text class="tn-padding-left-xs">搜索你想要的</text>
      </view>
    </tn-navbar>
    
    <swiper class="card-swiper" :circular="true"
      :autoplay="true" duration="500" interval="18000" @change="cardSwiper"> 
      <swiper-item v-for="(item,index) in swiperList" :key="index" :class="cardCur==index?'cur':''">
        <view class="swiper-item image-banner">
          <image :src="item.url" mode="aspectFill" v-if="item.type=='image'" style="height: 100%;"></image>
        </view>
        <view class="swiper-item-text">
          <view class="tn-text-bold tn-color-white" style="font-size: 50rpx; margin-left: -20rpx;"> photo</view>
          <view class=" tn-color-white tn-padding-top" style="font-size: 30rpx;">
            <text class="tn-padding-right-lg tn-padding-left-xs">摄影</text>
            <tn-icon name="camera-fill"></tn-icon>
            <text class="tn-padding-left-xs">图片广场</text>
          </view>
        </view>
      </swiper-item>
    </swiper>
    <view class="indication">
        <block v-for="(item,index) in swiperList" :key="index">
            <view class="spot" :class="cardCur==index?'active':''"></view>
        </block>
    </view>
    
    <view class=" tn-padding-bottom-xl">
      <view class="nav-list  tn-margin-bottom-lg">
        <block v-for="(item, index) in navList" :key="index">
          <view class="nav-list-item tn-shadow-blur tn-cool-bg-image" :class="['nav-list-item'+(index+1)]">
            <view class="nav-link">
              <view class='title'>{{ item.title }}</view>
              <view class='join'>{{ item.join }}</view>
            </view>
          </view>
        </block>
      </view>
      
      <tn-water-fall ref="waterfall" :data="list" @finish="handleWaterFallFinish">
        <template #left="{ item }">
          <view class="image-data">
            <image class="image" :src="item.url" mode="widthFix" />
          </view>
        </template>
        <template #right="{ item }">
          <view class="image-data">
            <image class="image" :src="item.url" mode="widthFix" />
          </view>
        </template>
      </tn-water-fall>
      <tn-loadmore :status="loadStatus" v-if="list.length > 0"></tn-loadmore>
      <view class="tn-padding-bottom-lg"></view>
    </view>
    
    <tn-modal v-model="show1" :custom="true">
      <view class="custom-modal-content">
        <image class="gif-image" src="https://resource.tuniaokj.com/images/advertise/5c4a19545a0bc4f92d3d21eb0ad341f8d2a45af6.gif" mode="aspectFit"></image>
        <view class="tn-text-center tn-text-lg tn-text-bold tn-color-black tn-padding-top">欢迎加入图鸟UI</view>
        <view class="tn-text-center tn-padding-top tn-padding-bottom-xl tn-text-xs">
          <text class="tn-color-red">点击下方的加入群聊，即可申请加入群聊</text>
        </view>
        <tn-button
          bg-color="#01BEFF"
          :custom-style="{padding:'20rpx 0'}"
          width="40%"
          :fontSize="28"
          text-color="#FFFFFF"
          shape="round"
          @click="previewQRCodeImage"
        >
          <text class="">加入群聊</text>
        </tn-button>
      </view>
    </tn-modal>
    <!-- 回到首页悬浮按钮 TODO-->
    <!-- <tn-home-btn :bottom="200" bgColor="#3668FC" badgeColor="#FF0000" backBtnIcon="home"></tn-home-btn> -->
  </view>
</template>

<script setup>
  import { ref,nextTick, onMounted } from 'vue'
  import { onLoad,onReady, onReachBottom, onPageScroll } from '@dcloudio/uni-app'
  import { useGoBack,useGetRect } from '@/libs/composables'
  const { goBack } = useGoBack()
  const { _tGetRect } = useGetRect()

  const show1 = ref(false)
  const cardCur = ref(0)
  const swiperList = ref([
    { id: 0, type: 'image', url: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664015047023-assets/web-upload/147b0b7f-8253-4b92-bc1d-e28db7f54096.jpeg' },
    { id: 1, type: 'image', url: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1671437658295-assets/web-upload/05620a1f-452e-4a14-9d30-f6c66ee4be1c.jpeg' },
    { id: 2, type: 'image', url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116254-assets/web-upload/96b7cdd7-cbf6-44fa-bf1a-326f35262f0c.jpeg' },
    { id: 3, type: 'image', url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116261-assets/web-upload/4fd332a1-171f-4b54-9423-41b08513b8ab.jpeg' },
    { id: 4, type: 'image', url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116300-assets/web-upload/b46d51a8-aaa9-4600-9394-818216e7dc8c.jpeg' }
  ])
  const navBarRectInfo = ref({})
  const navBarChangebaseLineHeight = ref(0)
  const navBarStyle = ref({ color: '#FFFFFF', opacity: 1, display: 'flex' })
  const navBarStyle2 = ref({ color: 'rgba(255,255,255,0)', opacity: 1, display: 'flex' })
  const navBarBackgroundColor = ref('rgba(255, 255, 255, 0)')
  const loadStatus = ref('loadmore')
  const list = ref([])
  
  const navList = ref([
    { title: '最新', join: '321人' },
    { title: '最热', join: '987人' },
    { title: '免费', join: '65人' },
    { title: '赞赏', join: '18人' },
  ])

  const data = ref([
    { title: '酷炫外卖首页', userName: '试试就逝世', mainImage: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1671437658295-assets/web-upload/05620a1f-452e-4a14-9d30-f6c66ee4be1c.jpeg', userImage: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699098-assets/web-upload/e8b29292-72fc-4c1e-9d7c-fd9dba31cb62.jpeg', storeType: 1, newProduct: true, tags: ['首页','美食'], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 338 }},
    { title: '内容发布页', userName: '你的名字', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683185481047-assets/web-upload/1659a1f2-d66c-4eb9-9545-1419ee65f158.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg', storeType: 1, newProduct: false, tags: ['表单','新建'], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 289 }},
    { title: '商品详情，带购物车动画', userName: '青梅煮马', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116287-assets/web-upload/62bcb362-2f09-403e-8381-286e9f74487c.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 1, newProduct: true, tags: ['男生头像','情侣头像'], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 381 }},
    { title: '简约个人中心页', userName: '你的名字', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116230-assets/web-upload/a67a2574-41a1-447b-8b4c-cdaf69a14a53.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: true, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 526 }},
    { title: '消息通知页面', userName: '坟头草三米高', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116251-assets/web-upload/808c7cd2-2aa5-49ac-a2d4-5ba8b6e4475c.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 372 }},
    { title: '弹窗领红包', userName: '不许凶我', mainImage: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664179989916-assets/web-upload/eda197eb-42ce-44b1-9b14-fce3481db603.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg', storeType: 2, newProduct: false, tags: ['弹窗','模态窗'], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 694 }},
    { title: '系统设置', userName: 'seventeen', mainImage: 'https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664015047023-assets/web-upload/147b0b7f-8253-4b92-bc1d-e28db7f54096.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 2, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 508 }},
    { title: '金融理财首页', userName: '你的名字', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116291-assets/web-upload/c4c0d31c-88ee-42c7-99ba-139ef206aaed.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 923 }},
    { title: '图片视频编辑上传', userName: '图鸟东东', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116288-assets/web-upload/fc1aa566-9be3-4185-be44-1d36cf84c1f9.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 989 }},
    { title: '路线导航', userName: '此处凶姐承包', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116293-assets/web-upload/15169708-1cc1-4699-89ad-5e675351698f.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 129 }},
    { title: '系统设置', userName: 'seventeen', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116322-assets/web-upload/e8c4a23d-a981-4d07-9711-d4aee997438a.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 2, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 508 }},
    { title: '金融理财首页', userName: '你的名字', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116292-assets/web-upload/6ade2579-cbd2-4755-8552-02e8d9e9056e.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 923 }},
    { title: '图片视频编辑上传', userName: '图鸟东东', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116283-assets/web-upload/ffb5ec95-a1f9-40ba-954b-790723fa860f.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 989 }},
    { title: '路线导航', userName: '此处凶姐承包', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116300-assets/web-upload/b46d51a8-aaa9-4600-9394-818216e7dc8c.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 129 }},
    { title: '系统设置', userName: 'seventeen', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116302-assets/web-upload/18c27289-a7f1-4870-8d7d-77b72b280db9.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 2, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 508 }},
    { title: '金融理财首页', userName: '你的名字', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116261-assets/web-upload/4fd332a1-171f-4b54-9423-41b08513b8ab.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 923 }},
    { title: '图片视频编辑上传', userName: '图鸟东东', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116279-assets/web-upload/f9c0ca30-93d7-4b7c-86fb-ff899d7cfd26.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 989 }},
    { title: '路线导航', userName: '此处凶姐承包', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116283-assets/web-upload/00db3659-b29f-4964-8d32-13f3789ccd5a.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 129 }},
    { title: '路线导航', userName: '此处凶姐承包', mainImage: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116254-assets/web-upload/96b7cdd7-cbf6-44fa-bf1a-326f35262f0c.jpeg', userImage: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg', storeType: 1, newProduct: false, tags: [], viewUser: { latestUserAvatar: [{src: 'https://resource.tuniaokj.com/images/blogger/avatar_1.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_2.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_3.jpeg'},{src: 'https://resource.tuniaokj.com/images/blogger/avatar_4.jpeg'}], viewUserCount: 129 }}
  ])

  function tn(e) {
    uni.navigateTo({ url: e })
  }
  function cardSwiper(e) {
    cardCur.value = e.detail.current
  }
  function previewQRCodeImage() {
    uni.previewImage({ urls: ['https://resource.tuniaokj.com/images/advertise/qrcode.jpg'] })
  }
  function showModal(event) {
    openModal()
  }
  function openModal() {
    show1.value = true
  }
  async function initNavBarRectInfo() {
    const navBarRectInfoRes = await _tGetRect('#navbar')
    const pageTipsRectInfo = await _tGetRect('#page_tips')
    if (!navBarRectInfoRes.hasOwnProperty('top') || !pageTipsRectInfo.hasOwnProperty('top')) {
      setTimeout(() => { initNavBarRectInfo() }, 10)
      return
    }
    navBarRectInfo.value = { top: navBarRectInfoRes.top }
    navBarChangebaseLineHeight.value = pageTipsRectInfo.top - navBarRectInfoRes.top
  }
  function updateNavBarRectInfo() {
    _tGetRect('#page_tips').then((res) => {
      const top = res?.top || 0
      if (!top) return
      const differHeight = top - navBarRectInfo.value.top
      const opacity = differHeight / navBarChangebaseLineHeight.value
      if (opacity < 0) {
        navBarStyle.value.color = 'rgba(0, 0, 0, ${opacity})'
        navBarStyle2.value.color = 'rgba(0, 0, 0, ${opacity})'
        navBarBackgroundColor.value = 'rgba(255, 255, 255, 1)'
      } else {
        navBarStyle.value.color = 'rgba(255, 255, 255, 1)'
        navBarStyle2.value.color = 'rgba(255, 255, 255, 0)'
        navBarBackgroundColor.value = `rgba(255, 255, 255, ${1 - opacity})`
      }
    })
  }
  function getRandomData() {
    loadStatus.value = 'loading'
    for (let i = 0; i < 10; i++) {
      let index = i
      let item = JSON.parse(JSON.stringify(data.value[index]))
      item.id = i
      list.value.push(item)
    }
  }
  function handleWaterFallFinish() {
    loadStatus.value = 'loadmore'
  }

  //onMounted(() => { nextTick(() => initNavBarRectInfo()) })
  onLoad(() => getRandomData())
  onReachBottom(() => getRandomData())
  onPageScroll(() => updateNavBarRectInfo())

  defineOptions({})
</script>

<style lang="scss" scoped>
  .template-wallpaper { max-height: 100vh; }
  .custom-nav__back { font-size: 40rpx; margin: 0 20rpx; }
  .custom-nav__search { padding: 12rpx 30rpx; border-radius: 50rpx; background-color: rgba(0,0,0,0.2); display: flex; align-items: center; color: #FFFFFF; font-size: 26rpx; flex: 1; margin-right: 20rpx; }
  .card-swiper { height: 500rpx !important; }
  .card-swiper swiper-item { width: 750rpx !important; box-sizing: border-box; }
  .card-swiper swiper-item .swiper-item { width: 100%; display: block; height: 100%; border-radius: 0; transform: scale(1); transition: all 0.2s ease-in 0s; overflow: hidden; }
  .card-swiper swiper-item.cur .swiper-item { transform: scale(1); transition: all 0.2s ease-in 0s; }
  .card-swiper swiper-item .swiper-item-text { position: absolute; bottom: 80rpx; left: 40rpx; z-index: 100; }
  .indication { z-index: 9999; width: 100%; height: 36rpx; position: absolute; display: flex; flex-direction: row; align-items: center; justify-content: center; }
  .spot { background-color: #FFFFFF; opacity: 0.6; width: 10rpx; height: 10rpx; border-radius: 20rpx; top: -80rpx; margin: 0 8rpx !important; position: relative; }
  .spot.active { opacity: 1; width: 30rpx; background-color: #FFFFFF; }
  .nav-list { display: flex; flex-wrap: wrap; padding: 0rpx 12rpx 0rpx; justify-content: space-between; }
  .nav-list-item { margin: 30rpx 16rpx 0rpx; width: 45%; border-radius: 12rpx; position: relative; z-index: 1; }
  .nav-list-item::after { content: ""; position: absolute; width: 100%; height: 100%; background-color: inherit; border-radius: 10rpx; opacity: 1; box-shadow: 0rpx 10rpx 30rpx -10rpx; z-index: -1; transition: 0.25s; top: 0rpx; }
  .nav-link { position: relative; display: flex; justify-content: space-between; align-items: center; padding: 20rpx; color: #ffffff; font-size: 28rpx; }
  .nav-list-item1 { background-color: #3668FC; }
  .nav-list-item2 { background-color: #3DDEA7; }
  .nav-list-item3 { background-color: #F6A65F; }
  .nav-list-item4 { background-color: #F64F6F; }
  .nav-list-item5 { background-color: #7D3DFF; }
  .product__item { background-color: #FFFFFF; overflow: hidden; margin: 0 10rpx; margin-bottom: 40rpx; border-radius: 12rpx; box-shadow: 0rpx 0rpx 20rpx 0rpx rgba(0, 0, 0, 0.05); }
  .item__image { width: 100%; }
  .item__data { padding: 14rpx 0rpx 14rpx 14rpx; }
  .item__title-container { display: flex; flex-direction: row; align-items: center; }
  .item__store-type { border: 1rpx solid #AAAAAA; color: #AAAAAA; font-size: 18rpx; padding: 0 4rpx; border-radius: 8rpx; margin-right: 8rpx; }
  .item__title { font-size: 26rpx; color: #333333; flex: 1; }
  .item__tags-container { display: flex; flex-direction: row; flex-wrap: wrap; margin-top: 10rpx; }
  .item__tag { border: 1rpx solid #F4F4F4; color: #AAAAAA; font-size: 20rpx; padding: 2rpx 8rpx; border-radius: 8rpx; margin-right: 8rpx; }
  .custom-modal-content { display: flex; flex-direction: column; align-items: center; }
  .gif-image { width: 200rpx; height: 200rpx; }
</style>
