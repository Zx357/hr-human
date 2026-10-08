<template>
  <view class="edit-page">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-title">
        <text>发布动态</text>
      </view>
    </tn-navbar>

    <view class="page-content" :style="{ paddingTop: vuex_custom_bar_height + 28 + 'px' }">
      <!-- 内容卡片 -->
      <view class="section-card">
        <view class="card-head">
          <view class="section-title">想说点什么</view>
          <view class="head-hint">500字内</view>
        </view>
        <textarea
          v-model="postContent"
          class="content-textarea"
          maxlength="500"
          placeholder="说点什么，万一火了呢"
          placeholder-style="color:#AEB8C8"
        ></textarea>
        <view class="textarea-count">{{ postContent.length }}/500</view>
      </view>

      <!-- 图片卡片 -->
      <view class="section-card">
        <view class="card-head">
          <view class="section-title">添加图片</view>
          <view class="head-action" @tap="clear">
            <text>清空上传</text>
            <tn-icon name="delete"></tn-icon>
          </view>
        </view>
        <view class="upload-wrap">
          <tn-image-upload
            ref="imageUpload"
            :custom-upload-handler="uploadImageHandler"
            :sizeType="['compressed']"
            :width="236"
            :height="236"
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
      </view>

      <!-- 话题标签卡片 -->
      <view class="section-card">
        <view class="card-head">
          <view class="section-title">话题标签</view>
          <view class="head-hint">多选</view>
        </view>
        <view class="tag-list">
          <view
            v-for="(item, index) in tags"
            :key="index"
            class="tag-chip"
            :class="{ 'tag-chip--active': item.select }"
            @click="handleTagsClick(index)"
          >
            {{ item.title }}
          </view>
        </view>
      </view>
    </view>

    <!-- 底部提交栏 -->
    <view class="footer-bar">
      <view class="footer-tip">
        <tn-icon name="tip-fill"></tn-icon>
        <text>文明公约</text>
      </view>
      <tn-button
        width="100%"
        height="88"
        shape="round"
        bg-color="#3668FC"
        text-color="#FFFFFF"
        :font-size="30"
        bold
        :loading="submitting"
        :disabled="submitting"
        @tap="upload"
      >
        <text>{{ submitting ? '发布中...' : '发布动态' }}</text>
      </tn-button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { createMomentPost } from '@/api/moment'
import { uploadImageToServer } from '@/utils/upload'
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

// 图片上传相关(上传走后端)
const uploadImageHandler = (file) => uploadImageToServer(file?.path || file)
const fileList = ref([])
const showUploadList = ref(true)
const customBtn = ref(false)
const autoUpload = ref(true)
const showProgress = ref(false)
const deleteable = ref(true)
const maxCount = ref(9)
const disabled = ref(false)
// 发布防重复提交
const submitting = ref(false)

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
  // 防重入:发布进行中直接忽略重复点击
  if (submitting.value) return
  const labels = tags.value.filter((item) => item.select).map((item) => item.title)
  const images = fileList.value.map((item) => item.url).filter(Boolean)
  if (!postContent.value.trim() && !images.length) {
    uni.showToast({
      title: '请输入内容或选择图片',
      icon: 'none'
    })
    return
  }
  submitting.value = true
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
  } finally {
    submitting.value = false
  }
}

// 手动清空列表
const clear = () => {
  imageUpload.value.clear()
}

// 图片拖拽重新排序
const onSortList = (list) => {
}
</script>

<style lang="scss" scoped>
.edit-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #f7f8fa;
  color: #1d2541;
}

.nav-back {
  width: 72rpx;
  height: 52rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.08);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
}

.nav-title {
  width: 100%;
  text-align: center;
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 800;
}

.page-content {
  padding: 0 24rpx 240rpx;
  box-sizing: border-box;
}

/* 表单卡片 */
.section-card {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  padding-left: 16rpx;
  border-left: 6rpx solid #3668fc;
  font-size: 30rpx;
  font-weight: 800;
  line-height: 1.2;
}

.head-hint {
  color: #9aa4b2;
  font-size: 24rpx;
}

.head-action {
  display: flex;
  align-items: center;
  color: #9aa4b2;
  font-size: 24rpx;
}

/* 内容输入 */
.content-textarea {
  box-sizing: border-box;
  width: 100%;
  min-height: 190rpx;
  margin-top: 20rpx;
  padding: 22rpx;
  border-radius: 16rpx;
  background: #f7f8fa;
  color: #1d2541;
  font-size: 28rpx;
  line-height: 1.55;
}

.textarea-count {
  margin-top: 12rpx;
  text-align: right;
  color: #9aa4b2;
  font-size: 22rpx;
}

/* 图片上传 */
.upload-wrap {
  margin-top: 20rpx;
}

/* 话题标签 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  margin-top: 20rpx;
}

.tag-chip {
  padding: 12rpx 30rpx;
  margin: 0 16rpx 16rpx 0;
  border-radius: 999rpx;
  background: #f3f5f9;
  color: #657189;
  font-size: 24rpx;
}

.tag-chip--active {
  background: rgba(54, 104, 252, 0.1);
  color: #3668fc;
  font-weight: 600;
}

/* 底部提交栏 */
.footer-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  max-width: 640px;
  margin: 0 auto;
  padding: 20rpx 28rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -12rpx 36rpx rgba(70, 84, 110, 0.08);
  box-sizing: border-box;
}

.footer-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

.footer-tip .tn-icon,
.footer-tip text {
  margin: 0 4rpx;
}
</style>
