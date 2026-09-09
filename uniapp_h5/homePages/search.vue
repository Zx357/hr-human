<template>
  <view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="rgba(0,0,0,0)" home-icon="" :bottom-shadow="false" :placeholder="false">
     
    </tn-navbar>
    
    <view class="tn-search-fixed" style="background: linear-gradient(180deg, #D8E5FF, #FFFFFF);">
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin" :style="{paddingTop: '100px'}">
        
        <view class="justify-content-item align-content-item" style="width: 100vw;">
          <view class="tn-flex tn-flex-col-center" style="border: 1rpx solid #3668fc;border-radius: 100rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 90%;">
            <tn-icon name="search" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
            <input v-model="inputValue" class="justify-content-item" placeholder="搜索同事姓名" name="input" placeholder-style="color:#AAAAAA" style="width: 90%;" confirm-type="search" @confirm="doSearch"></input>
          </view>
        </view>
        
        <view class="align-content-item">
          <view class="justify-content-item tn-text-center">
            <!-- <tn-button backgroundColor="#3668fc" shape="round" padding="20rpx 20rpx" width="150rpx" @tap="">
              <text class="tn-color-white">搜 索</text>
            </tn-button> -->
             <tn-button
              bg-color="#3668fc"
              :custom-style="{padding:'20rpx 40rpx'}"
              width="150rpx"
              :fontSize="24"
              text-color="#ffffff"
              shape="round"
              @click="doSearch"
            >
              <text class="">搜 索</text>
            </tn-button>
          </view>
        </view>
      </view>
    </view>
    
    <view class=""  :style="{paddingTop:'100px'}">
      <view class="tn-flex tn-flex-row-between tn-margin" >
        <view class="justify-content-item tn-text-bold">
          <text class="tn-text-df tn-color-black">最近搜索</text>
        </view>
        <view class="justify-content-item tn-text-df tn-color-grey">
          <text class="tn-padding-xs">删除</text>
          <tn-icon name="delete"></tn-icon>
        </view>
      </view>
    </view>
    
    <view class="">
      <view class="tn-tag-search tn-margin tn-text-justify">
        <view v-for="(item, index) in tagList" :key="index" class="tn-tag-search__item tn-margin-right tn-round tn-text-sm tn-bg-gray--light tn-color-gray">
          <text class="tn-tag-search__item--prefix">#</text> {{ item.title }}
        </view>
      </view>
    </view>
    
    <view class="tn-flex tn-flex-row-between tn-padding-top-xl tn-margin tn-padding-bottom">
      <view class="justify-content-item tn-text-bold">
        <text class="tn-text-df tn-color-black">搜索结果</text>
      </view>
      <view class="justify-content-item tn-text-df tn-color-grey">
        <text class="tn-padding-xs">筛选</text>
        <tn-icon name="filter"></tn-icon>
      </view>
    </view>
    
    
    <view v-if="searching" class="tn-text-center tn-color-gray tn-padding-xl">搜索中...</view>
    <view v-else-if="!content.length" class="tn-text-center tn-color-gray--disabled tn-padding-xl">
      {{ inputValue ? '未找到相关同事' : '输入姓名搜索同事' }}
    </view>
    <view class="">
      <block v-for="(item, index) in content" :key="index">
        <view class="article-shadow tn-margin" @click="openContact(item)">
          <view class="tn-flex">
            <view class="image-pic tn-margin-sm" :style="'background-image:url(' + item.userAvatar + ')'">
              <view class="image-article">
              </view>
            </view>
            <view class="tn-margin-sm tn-padding-top-xs" style="width: 100%;">
              <view class="tn-text-lg tn-text-bold clamp-text-1 tn-text-justify">
                <text class="">{{ item.title }}</text>
              </view>
              <view class="tn-padding-top-xs">
                <text class="tn-text-df tn-color-gray clamp-text-2 tn-text-justify">
                  {{ item.desc }}
                </text>
              </view>
              <view class="tn-flex tn-flex-row-between tn-flex-col-between tn-margin-top-xs">
                <view v-for="(label_item,label_index) in item.label" :key="label_index"
                  class="justify-content-item tn-tag-content__item tn-margin-right tn-text-sm tn-text-bold">
                  <text class="tn-tag-content__item--prefix">#</text> {{ label_item }}
                </view>
              </view>
            </view>
          </view>
        </view>
      </block>
    </view>

      </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { searchContacts } from '@/api/contact'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 输入值
const inputValue = ref('')

// 搜索结果
const content = ref([])
const searching = ref(false)

// 执行搜索(后端联系人搜索)
const doSearch = async () => {
  const keyword = inputValue.value.trim()
  if (!keyword) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }
  searching.value = true
  try {
    const res = await searchContacts({ name: keyword })
    const list = Array.isArray(res.data) ? res.data : res.data?.records || []
    content.value = list.map((item) => ({
      id: item.id,
      title: item.name || item.employeeName || '未命名员工',
      desc: item.deptName || item.companyName || '暂无部门信息',
      label: [item.position || item.post || '同事'].filter(Boolean),
      userAvatar: formatAvatar(item.avatar),
      raw: item
    }))
  } catch (error) {
    console.log('搜索失败', error)
  } finally {
    searching.value = false
  }
}

// 打开同事详情
const openContact = (item) => {
  if (!item?.id) return
  uni.navigateTo({ url: `/partnerPages/user?id=${item.id}` })
}

const formatAvatar = (avatar) => {
  if (!avatar) return '/static/author.jpg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

// 跳转
const tn = (e) => {
  if (!e) return
  uni.navigateTo({
    url: e,
  })
}
</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    width: 100%;
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
    
    &:before {
      content: " ";
      width: 1rpx;
      height: 110%;
      position: absolute;
      top: 22.5%;
      left: 0;
      right: 0;
      margin: auto;
      transform: scale(0.5);
      transform-origin: 0 0;
      pointer-events: none;
      box-sizing: border-box;
      opacity: 0.7;
      background-color: #FFFFFF;
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
  
  .tn-search-fixed{
    position: fixed;
    top: 0rpx;
    width: 100%;
    max-width: 640px;
    margin: 0 auto;
    transition: all 0.25s ease-out;
    z-index: 1;
  }

  
  /* 搜索标签 start*/
  .tn-tag-search {
    &__item {
      display: inline-block;
      line-height: 45rpx;
      padding: 10rpx 30rpx;
      margin: 20rpx 20rpx 5rpx 0rpx;
      
      &--prefix {
        padding-right: 10rpx;
      }  
    }
  }
  /* 标签内容 end*/
  
  /* 标题 start */
  .nav_title {
    -webkit-background-clip: text;
    color: transparent;
    
    &--wrap {
      position: relative;
      display: flex;
      height: 120rpx;
      font-size: 42rpx;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      background-image: url(https://resource.tuniaokj.com/images/title_bg/title00.png);
      background-size: cover;
    }
  }
  /* 标题 end */
  
  /* 富文本图示意 start */
  .news-img {
    z-index: -1;
    padding-bottom: 40rpx;
  
    image {
      width: 100%;
      margin: 20rpx 0;
      // height: 3373rpx;
      // z-index: -1;
    }
  }
  
  /* 资讯主图 start*/
  .image-article {
    border-radius: 8rpx;
    border: 1rpx solid #F8F7F8;
    width: 200rpx;
    height: 200rpx;
    position: relative;
  }
  
  .image-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: top;
    border-radius: 10rpx;
  }
  
  .article-shadow {
    border-radius: 15rpx;
    box-shadow: 0rpx 0rpx 50rpx 0rpx rgba(0, 0, 0, 0.07);
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
  
  /* 标签内容 start*/
  .tn-tag-content {
    &__item {
      display: inline-block;
      line-height: 35rpx;
      color: #1D2541;
      background-color: #F3F2F7;
      border-radius: 10rpx;
      font-size: 22rpx;
      padding: 5rpx 15rpx;
  
      &--prefix {
        padding-right: 10rpx;
      }
    }
  }
</style>
