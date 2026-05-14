<template>
	<view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#ffffff00" :placeholder="false" customBack>
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left'></tn-icon>
        <tn-icon class='icon' name='home-capsule-fill'></tn-icon>
      </view>
    </tn-navbar>
		
    <view class="tn-safe-area-inset-bottom" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top tn-margin">
        <view class="tn-flex justify-content-item">
          <view class="tn-bg-black tn-color-white tn-text-center" style="border-radius: 100rpx;margin-right: 8rpx;width: 45rpx;height: 45rpx;line-height: 45rpx;">
            <tn-icon class="" name="topics" style="font-size: 30rpx;"></tn-icon>
          </view>
          <view class="tn-text-lg tn-padding-right-xs tn-text-bold">想说点什么 *</view>
        </view>
        <view class="justify-content-item tn-text-df tn-color-grey">
          <text class="tn-padding-xs">500字内</text>
          <tn-icon name="keyboard-circle"></tn-icon>
        </view>
      </view>
      
      <view class="tn-margin tn-bg-gray--light tn-padding" style="border-radius: 10rpx;">
        <textarea v-model="postContent" maxlength="500" placeholder="说点什么 , 万一火了呢" placeholder-style="color:#AAAAAA" style="height: 160rpx;"></textarea>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top-xl tn-margin">
        <view class="tn-flex justify-content-item">
          <view class="tn-bg-black tn-color-white tn-text-center" style="border-radius: 100rpx;margin-right: 8rpx;width: 45rpx;height: 45rpx;line-height: 45rpx;">
            <tn-icon class="" name="image" style="font-size: 30rpx;"></tn-icon>
          </view>
          <view class="tn-text-lg tn-padding-right-xs tn-text-bold">发点什么图咧 *</view>
        </view>
        <view class="justify-content-item tn-text-df tn-color-grey" @tap="clear">
          <text class="tn-padding-xs">清空上传</text>
          <tn-icon name="delete"></tn-icon>
        </view>
      </view>
      
      
      
      
      <view class="tn-margin-left tn-padding-top-xs">
        <tn-image-upload
          ref="imageUpload"
          :action="action"
          :width="236"
          :height="236"
          :formData="formData"
          :fileList="fileList"
          :disabled="disabled"
          :autoUpload="autoUpload"
          :maxCount="maxCount"
          :showUploadList="showUploadList"
          :showProgress="showProgress"
          :deleteable="deleteable"
          :customBtn="customBtn"
          @sort-list="onSortList"
        />
      
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top-xl tn-margin">
        <view class="tn-flex justify-content-item">
          <view class="tn-bg-black tn-color-white tn-text-center" style="border-radius: 100rpx;margin-right: 8rpx;width: 45rpx;height: 45rpx;line-height: 45rpx;">
            <tn-icon class="" name="tag" style="font-size: 30rpx;"></tn-icon>
          </view>
          <view class="tn-text-lg tn-padding-right-xs tn-text-bold">话题标签</view>
        </view>
        <view class="justify-content-item tn-text-df tn-color-grey">
          <text class="tn-padding-xs">多选</text>
          <tn-icon name="constellation"></tn-icon>
        </view>
      </view>
      
      <view class="tn-tag-content tn-margin tn-text-justify">
        <view v-for="(item, index) in tags" :key="index" class="tn-tag-content__item tn-margin-right tn-round tn-text-sm tn-text-bold" :class="[item.select ? `tn-bg-${item.color}--light tn-color-${item.color}` : 'tn-bg-gray--light tn-color-gray--dark']"  @click="handleTagsClick(index)">
          <text :class="['tn-padding-right-xs tn-icon-' + item.icon]"></text> {{ item.title }}
        </view>
      </view>
      
      <!-- 悬浮按钮-->
      <view class="tn-flex tn-footerfixed">
        <view class="tn-flex-1 justify-content-item tn-margin-sm tn-text-center">
          <view class="tn-padding-xs tn-text-sm tn-margin-bottom-xs" style="opacity: 0.6;">
            <tn-icon class="tn-color-gray tn-padding-right-xs" name="tip-fill"></tn-icon> 
            <text class="tn-color-gray">文明公约</text>
          </view>
          <tn-button
            bg-color="#3668fc"
            :custom-style="{padding:'40rpx 0'}"
            width="70%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            @tap="upload"
          >
            <text class="">发 布 时 刻</text>
          </tn-button>
        </view>
      </view>
      
    </view>
    
    <view class='tn-tabbar-height'></view>
    
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { createMomentPost } from '@/api/moment'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'TemplateEdit'
})

// 标签数据
const postContent = ref('')
const tags = ref([
  {
    icon: "topic",
    title: "随性分享",
    color: 'red',
    select: false
  },
  {
    icon: "topic",
    title: "搬砖日常",
    color: 'cyan',
    select: false
  },
  {
    icon: "topic",
    title: "情绪吐槽",
    color: 'blue',
    select: false
  },
  {
    icon: "topic",
    title: "美食推荐",
    color: 'green',
    select: false
  },
  {
    icon: "topic",
    title: "忙碌出差",
    color: 'orange',
    select: false
  },
  {
    icon: "topic",
    title: "沉迷学习",
    color: 'purplered',
    select: false
  },
  {
    icon: "topic",
    title: "假期生活",
    color: 'purple',
    select: false
  },
  {
    icon: "topic",
    title: "立Flag",
    color: 'orangered',
    select: false
  },
  {
    icon: "topic",
    title: "知识分享",
    color: 'orangeyellow',
    select: false
  },
  {
    icon: "topic",
    title: "寻求帮助",
    color: 'brown',
    select: false
  },
  {
    icon: "topic",
    title: "未知",
    color: 'grey',
    select: false
  }
])

// 图片上传相关
const action = ref('https://www.hualigs.cn/api/upload')
const formData = ref({
  apiType: 'this,ali',
  token: 'dffc1e06e636cff0fdf7d877b6ae6a2e',
  image: null
})
const fileList = ref([])
const showUploadList = ref(true)
const customBtn = ref(false)
const autoUpload = ref(true)
const showProgress = ref(false)
const deleteable = ref(true)
const maxCount = ref(4)
const disabled = ref(false)

const imageUpload = ref(null)

// 处理标签点击事件
const handleTagsClick = (index) => {
  tags.value[index].select = !tags.value[index].select
}

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
}

// 发布动态
const upload = async () => {
  const labels = tags.value.filter((item) => item.select).map((item) => item.title)
  const images = fileList.value.map((item) => item.url).filter(Boolean)
  if (!postContent.value.trim() && !images.length) {
    uni.showToast({
      title: '请输入内容或选择图片',
      icon: 'none'
    })
    return
  }
  try {
    await createMomentPost({
      content: postContent.value.trim(),
      labels: labels.join(','),
      images: images.join(',')
    })
    uni.showToast({
      title: '发布成功',
      icon: 'success'
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 500)
  } catch (error) {
    console.log('发布动态失败', error)
  }
}

// 手动清空列表
const clear = () => {
  imageUpload.value.clear()
}

// 图片拖拽重新排序
const onSortList = (list) => {
  console.log(list);
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
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 180rpx;
  	height: calc(220rpx + env(safe-area-inset-bottom) / 2);
    height: calc(220rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    width: 100%;
    bottom: calc(30rpx + env(safe-area-inset-bottom));
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
    
  }
  /* 底部悬浮按钮 end*/
  
  /* 标签内容 start*/
  .tn-tag-content {
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
</style>
