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

      <block v-for="(item, index) in navList" :key="index">
        <view class="nav_title--wrap tn-margin-bottom-sm">
          <view class="nav_title">{{ item.title }}</view>
        </view>

        <view class='nav-list'>
          <navigator
            open-type="navigate"
            hover-class='none'
            :url="content_item.url"
            v-for="(content_item, content_index) in item.list"
            :key="content_index"
            class="nav-list-item"
          >
            <view class="nav-link">
              <view class='title'>{{ content_item.title }}</view>
              <view class='desc'>{{ content_item.desc }}</view>
            </view>
            <tn-icon name="right" class="nav-arrow"></tn-icon>
          </navigator>
        </view>
      </block>

    </view>

    <!-- 回到首页悬浮按钮-->
    <nav-index-button></nav-index-button>

  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()
  import NavIndexButton from '@/libs/components/nav-index-button.vue'

  // 功能列表(仅保留实际存在的功能页)
  const navList = ref([
    {
      title: '常用功能',
      list: [
        { title: '待办事项', desc: '需要我审批的申请', url: '/homePages/pending' },
        { title: '沟通交流', desc: '聊天会话与未读消息', url: '/homePages/chat' },
        { title: '系统公告', desc: '查看公司公告通知', url: '/homePages/notice' },
        { title: '全局搜索', desc: '搜索同事、公告、功能', url: '/homePages/search' }
      ]
    },
    {
      title: '个人设置',
      list: [
        { title: '个人信息', desc: '查看我的员工档案', url: '/minePages/set' },
        { title: '修改密码', desc: '定期更换登录密码', url: '/minePages/password' },
        { title: '意见反馈', desc: '提交问题或建议', url: '/minePages/feedback' },
        { title: '帮助中心', desc: '常见问题解答', url: '/minePages/help' }
      ]
    }
  ])
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

  /* 标题start */
  .nav_title--wrap {
    display: flex;
    height: 100rpx;
    align-items: center;
    justify-content: center;
  }

  .nav_title {
    color: #1d2541;
    font-size: 36rpx;
    font-weight: bold;
  }
  /* 标题end */

  /* 功能导航列表 start*/
  .nav-list {
    margin: 0 24rpx;
    background: #ffffff;
    border-radius: 18rpx;
    overflow: hidden;
  }

  .nav-list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f3f2f7;
  }

  .nav-list-item:last-child {
    border-bottom: none;
  }

  .nav-link {
    flex: 1;
    min-width: 0;

    .title {
      color: #1d2541;
      font-size: 30rpx;
      font-weight: bold;
    }

    .desc {
      margin-top: 8rpx;
      color: #9aa4b2;
      font-size: 24rpx;
    }
  }

  .nav-arrow {
    flex-shrink: 0;
    margin-left: 20rpx;
    color: #8ca0b3;
    font-size: 32rpx;
  }
  /* 功能导航列表 end*/
</style>
