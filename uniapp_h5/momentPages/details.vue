<template>
  <view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#ffffff00" :placeholder="false" customBack>
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left"></tn-icon>
        <tn-icon class='icon' name="home-capsule-fill"></tn-icon>
      </view></template>
    </tn-navbar>

    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <!-- 加载中 -->
      <view v-if="loading" class="tn-padding-xl">
        <view class="tn-text-center tn-color-gray tn-padding-top-xl">加载中...</view>
      </view>

      <!-- 加载失败 -->
      <view v-else-if="!post" class="tn-padding-xl">
        <view class="tn-text-center" style="font-size: 160rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-center tn-text-lg">动态不存在或已删除</view>
      </view>

      <!-- 帖子内容 -->
      <block v-else>
        <view class="blogger__item">
          <view class="blogger__author tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify__author__info">
              <view class="tn-flex tn-flex-row-center">
                <view class="tn-flex tn-flex-row-center tn-flex-col-center">
                  <view class="">
                    <tn-avatar
                      class=""
                      shape="circle"
                      :src="post.userAvatar"
                      size="lg">
                    </tn-avatar>
                  </view>
                  <view class="tn-padding-right tn-text-ellipsis">
                    <view class="tn-padding-right tn-padding-left-sm tn-text-bold tn-text-lg">{{ post.userName }}</view>
                    <view class="tn-padding-right tn-padding-left-sm tn-padding-top-xs tn-color-gray">{{ post.date }}</view>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="blogger__desc tn-margin-top-sm tn-margin-bottom-sm tn-text-justify tn-flex-col-center tn-flex-row-left">
            <view v-for="(label_item,label_index) in post.label" :key="label_index" class="blogger__desc__label tn-float-left tn-margin-right">
              <tn-icon class="blogger__desc__label--prefix" name="topics-fill"></tn-icon>
              <text class="tn-text-df">{{ label_item }}</text>
            </view>
          </view>

          <text class="blogger__content-text tn-text-justify">{{ post.content }}</text>

          <block v-if="post.mainImage && post.mainImage.length">
            <view v-if="[1,2,4].indexOf(post.mainImage.length) != -1" class="tn-padding-top-xs">
              <image v-for="(image_item,image_index) in post.mainImage" :key="image_index"
                class="blogger__main-image"
                :class="{
                  'blogger__main-image--1 tn-margin-bottom-sm': post.mainImage.length === 1,
                  'blogger__main-image--2 tn-margin-right-sm tn-margin-bottom-sm': post.mainImage.length === 2 || post.mainImage.length === 4
                }"
                :src="image_item"
                mode="aspectFill"
                @click="previewImage(image_index)"
              ></image>
            </view>
            <view v-else class="tn-padding-top-xs">
              <view class="blogger__grid-image">
                <block v-for="(image_item,image_index) in post.mainImage" :key="image_index">
                  <image
                    class="blogger__main-image blogger__main-image--3"
                    :src="image_item"
                    mode="aspectFill"
                    @click="previewImage(image_index)"
                  ></image>
                </block>
              </view>
            </view>
          </block>

          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-top">
            <view class="justify-content-item tn-flex tn-flex-col-center">
              <text class="tn-color-gray">{{ post.date }}</text>
            </view>

            <view class="justify-content-item tn-color-gray tn-text-center" style="padding-top: 5rpx;">
              <tn-icon class="tn-text-lg" name="message" style="padding-right: 5rpx;"></tn-icon>
              <text class="tn-padding-right tn-text-df">{{ post.commentCount }}</text>
              <tn-icon
                :class="post.liked ? 'tn-icon-like-fill' : 'tn-icon-like-lack'"
                class="tn-text-lg"
                style="padding-right: 5rpx;"
                @click="toggleLike"
              ></tn-icon>
              <text class="tn-text-df">{{ post.likeCount }}</text>
            </view>
          </view>
        </view>

        <!-- 评论 -->
        <view class="tn-margin" style="padding-bottom: 200rpx;">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin-bottom">
            <view class="tn-text-bold tn-text-lg">评论</view>
            <text class="tn-color-gray tn-text-sm">{{ post.commentCount }} 条</text>
          </view>

          <!-- 评论列表(增量渲染:每次多显示 10 条,避免大列表一次渲染) -->
          <view v-if="comments.length">
            <view
              v-for="item in visibleComments"
              :key="item.id"
              class="comment-item tn-flex tn-flex-col-top"
            >
              <tn-avatar class="comment-item__avatar" shape="circle" :src="item.avatar" size="sm"></tn-avatar>
              <view class="tn-flex-1 tn-padding-left-sm" style="min-width: 0;">
                <view class="tn-flex tn-flex-row-between tn-flex-col-center">
                  <text class="tn-text-bold tn-text-df tn-text-ellipsis">{{ item.name }}</text>
                  <text
                    v-if="isMyComment(item)"
                    class="comment-item__delete tn-color-gray tn-text-sm"
                    @click="removeComment(item)"
                  >删除</text>
                </view>
                <view class="tn-text-df tn-text-justify tn-padding-top-xs">{{ item.content }}</view>
                <view class="tn-color-gray--disabled tn-text-xs tn-padding-top-xs">{{ item.time }}</view>
              </view>
            </view>

            <!-- 加载更多(评论接口为全量返回,前端分批渲染) -->
            <view v-if="comments.length > visibleCount" class="comment-load-more tn-text-center tn-color-gray tn-text-sm tn-padding" @click="showMoreComments">
              加载更多评论（{{ comments.length - visibleCount }}）
            </view>
          </view>

          <!-- 空评论 -->
          <view v-else class="tn-text-center tn-color-gray--disabled tn-padding-xl">
            <text class="tn-icon-clip" style="font-size: 120rpx;"></text>
            <view class="tn-padding-top">还没有评论，快来抢沙发</view>
          </view>
        </view>
      </block>
    </view>

    <!-- 底部点赞栏 + 评论输入 -->
    <view v-if="post" class="tabbar footerfixed tn-bg-white">
      <!-- 评论输入框 -->
      <view class="comment-input tn-flex tn-flex-col-center">
        <input
          v-model="commentDraft"
          class="comment-input__field"
          placeholder="写下你的评论..."
          placeholder-style="color:#AAAAAA"
          confirm-type="send"
          :adjust-position="true"
          :disabled="commentSubmitting"
          @confirm="submitComment"
        />
        <tn-button
          shape="round"
          bg-color="#3668FC"
          text-color="#FFFFFF"
          :custom-style="{ padding: '12rpx 34rpx' }"
          :disabled="commentSubmitting"
          @click="submitComment"
        >
          发送
        </tn-button>
      </view>

      <view class="tn-flex tn-flex-row-between tn-flex-col-center">
        <view class="tn-flex-1 justify-content-item tn-text-center tn-margin-sm">
          <tn-button
            :bg-color="post.liked ? '#FB6A67' : '#FBBD12'"
            :custom-style="{padding:'30rpx 0'}"
            width="90%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            @click="toggleLike"
          >
            <text class="">{{ post.liked ? '已点赞' : '点赞支持' }}</text>
          </tn-button>
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <view class="tn-flex-1 justify-content-item tn-text-center tn-margin-sm">
          <tn-button
            bg-color="#05C160"
            :custom-style="{padding:'30rpx 0'}"
            width="90%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            open-type="share"
          >
            <text class="">分享好友</text>
          </tn-button>
        </view>
        <!-- #endif -->
      </view>
    </view>

  </view>
</template>

<script setup>
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import config from '@/config'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMomentPostDetail, toggleMomentLike, getMomentComments, addMomentComment, deleteMomentComment } from '@/api/moment'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

defineOptions({
  name: 'TemplateDetails'
})

const postId = ref(null)
const loading = ref(true)
const post = ref(null)

// 评论
const comments = ref([])
const commentDraft = ref('')
const commentSubmitting = ref(false)
// 评论接口为全量返回(不支持分页),前端增量渲染避免一次渲染全部
const COMMENT_PAGE_SIZE = 10
const visibleCount = ref(COMMENT_PAGE_SIZE)
const visibleComments = computed(() => comments.value.slice(0, visibleCount.value))

const showMoreComments = () => {
  visibleCount.value += COMMENT_PAGE_SIZE
}

const myEmployeeId = () => {
  const info = store.getters.employeeInfo || store.state.user?.employeeInfo || uni.getStorageSync('userInfo') || {}
  return store.getters.id || info.id || null
}

const isMyComment = (item) => {
  return item.employeeId != null && String(item.employeeId) === String(myEmployeeId())
}

const normalizeComment = (item) => {
  return {
    id: item.id,
    employeeId: item.employeeId,
    avatar: formatAvatar(item.authorAvatar),
    name: item.authorName || '未命名员工',
    content: item.content || '',
    time: String(item.createdTime || '').replace('T', ' ').slice(0, 16)
  }
}

const loadComments = async () => {
  if (!postId.value) return
  try {
    const res = await getMomentComments(postId.value)
    comments.value = Array.isArray(res.data) ? res.data.map(normalizeComment) : []
    visibleCount.value = COMMENT_PAGE_SIZE
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const submitComment = async () => {
  const content = commentDraft.value.trim()
  if (!content || commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    const res = await addMomentComment(postId.value, content)
    if (res.data) {
      comments.value = comments.value.concat(normalizeComment(res.data))
      // 新评论必须立即可见(可能超出当前增量渲染条数)
      visibleCount.value = Math.max(visibleCount.value, comments.value.length)
      if (post.value) {
        post.value.commentCount = Number(post.value.commentCount || 0) + 1
      }
      commentDraft.value = ''
    }
  } catch (error) {
    uni.showToast({ title: '发送失败，请重试', icon: 'none' })
  } finally {
    commentSubmitting.value = false
  }
}

const removeComment = (item) => {
  uni.showModal({
    title: '删除评论',
    content: '确定删除这条评论吗？',
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteMomentComment(item.id)
        comments.value = comments.value.filter((c) => c.id !== item.id)
        if (post.value) {
          post.value.commentCount = Math.max(0, Number(post.value.commentCount || 0) - 1)
        }
        uni.showToast({ title: '已删除', icon: 'none' })
      } catch (error) {
        uni.showToast({ title: '删除失败，请重试', icon: 'none' })
      }
    }
  })
}

const splitField = (value) => {
  if (!value) return []
  if (Array.isArray(value)) return value
  return String(value).split(',').map((item) => item.trim()).filter(Boolean)
}

const formatAvatar = (avatar) => {
  if (!avatar) return '/static/author.jpg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

const normalizePost = (item) => {
  return {
    id: item.id,
    userAvatar: formatAvatar(item.authorAvatar),
    userName: item.authorName || '未命名员工',
    date: String(item.createdTime || '').replace('T', ' ').slice(0, 16),
    label: splitField(item.labels),
    content: item.content || '',
    mainImage: splitField(item.images).map((img) => (/^https?:\/\//.test(img) || img.startsWith('/static') ? img : config.baseUrl + img)),
    commentCount: Number(item.commentCount || 0),
    likeCount: Number(item.likeCount || 0),
    liked: !!item.liked
  }
}

const loadPost = async () => {
  if (!postId.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await getMomentPostDetail(postId.value)
    post.value = res.data ? normalizePost(res.data) : null
  } catch (error) {
    post.value = null
  } finally {
    loading.value = false
  }
}

// 下拉刷新：重拉动态详情与评论
onPullDownRefresh(async () => {
  try {
    await Promise.all([loadPost(), loadComments()])
  } finally {
    uni.stopPullDownRefresh()
  }
})

let likePending = false
const toggleLike = async () => {
  if (!post.value || likePending) return
  likePending = true
  const target = post.value
  const oldLiked = !!target.liked
  const oldLikeCount = Number(target.likeCount || 0)
  target.liked = !oldLiked
  target.likeCount = Math.max(0, oldLikeCount + (target.liked ? 1 : -1))

  try {
    const res = await toggleMomentLike(target.id)
    if (res.data) {
      target.liked = !!res.data.liked
      target.likeCount = Number(res.data.likeCount || 0)
    }
  } catch (error) {
    target.liked = oldLiked
    target.likeCount = oldLikeCount
  } finally {
    likePending = false
  }
}

const previewImage = (index) => {
  if (!post.value?.mainImage?.length) return
  uni.previewImage({
    current: post.value.mainImage[index],
    urls: post.value.mainImage
  })
}

onLoad((options) => {
  postId.value = options?.id || null
  loadPost()
  loadComments()
})

// 小程序分享:标题取动态内容前20字,路径直达该动态详情页(带动态 id)
import { onShareAppMessage } from '@dcloudio/uni-app'
onShareAppMessage(() => {
  const content = post.value?.content || ''
  const title = content.length > 20 ? content.slice(0, 20) + '…' : (content || '时光动态')
  return {
    title,
    path: postId.value ? `/momentPages/details?id=${postId.value}` : '/pages/index?index=1'
  }
})
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
      background-color: #F8F7F8;
      min-height: 100vh;
      padding-bottom: 60rpx;
      padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
      padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
    }

    .blogger__content-text {
      display: block;
      padding: 0 30rpx;
      line-height: 46rpx;
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
      }
    /* 文章内容 end*/

    .tabbar {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      max-width: 640px;
      margin: 0 auto;
      box-shadow: 0rpx -6rpx 20rpx 0rpx rgba(0, 0, 0, 0.06);
      z-index: 1000;
      padding-bottom: env(safe-area-inset-bottom);
    }

    /* 评论输入 start */
    .comment-input {
      padding: 16rpx 30rpx 4rpx;

      &__field {
        flex: 1;
        height: 68rpx;
        padding: 0 24rpx;
        margin-right: 20rpx;
        background-color: #F4F5F9;
        border-radius: 100rpx;
        font-size: 28rpx;
      }
    }

    /* 评论列表 start */
    .comment-load-more {
      border-top: 1rpx solid #F3F2F7;
    }

    .comment-item {
      padding: 24rpx 0;
      border-bottom: 1rpx solid #F3F2F7;

      &:last-child {
        border-bottom: none;
      }

      &__avatar {
        flex-shrink: 0;
      }

      &__delete {
        flex-shrink: 0;
        padding-left: 16rpx;
      }
    }
</style>
