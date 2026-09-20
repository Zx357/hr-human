<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">发起群聊</text>
      </view>
    </tn-navbar>

    <!-- 页面内容 -->
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 10 +'px'}">
      
      
     <view class="tn-padding tn-flex tn-flex-col-between tn-strip-bottom-min" @click="showFaceToFaceTip">
        <view class="icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" style="background-color: #00C8B0;">
          <tn-icon name="menu-match"></tn-icon>
        </view>
        <view class="tn-flex tn-flex-row-left tn-flex-col-center">
          <view class="justify-content-item tn-margin-sm tn-text-bold tn-text-lg">
            <text>面对面建群</text>
          </view>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loadingEmployees" class="tn-text-center tn-color-gray tn-padding-xl">加载中...</view>

      <!-- 空态:无可邀请的同事 -->
      <view v-else-if="!listData || !Object.keys(listData).length" class="tn-text-center tn-padding-xl">
        <view class="tn-text-center" style="font-size: 160rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-lg">暂无可邀请的同事</view>
      </view>

      <tn-index-list v-else :data="listData">
        <template #default="{ data }">
          <view class="list-data" @click="toggleSelect(data)">
            <!-- 未选择 -->
            <view v-if="!isSelected(data.id)" class="tn-padding-right">
              <tn-icon name="circle" class="tn-text-xl tn-color-gray"></tn-icon>
            </view>
            <!-- 选择 -->
            <view v-else class="tn-padding-right">
              <tn-icon name="circle-fill" class="tn-text-xl" style="color: #00C8B0;"></tn-icon>
            </view>
            <image class="image" :src="formatAvatar(data.avatar)" mode="aspectFill" />
            <view class="info">
              <view class="username">{{ data.username }}</view>
              <view class="office tn-gray_text">{{ data.office }}</view>
            </view>
          </view>
        </template>
      </tn-index-list>

    </view>
    
    <!-- 悬浮按钮-->
    <view class="tn-flex tn-footerfixed tn-padding">
      <view class="tn-flex-1 justify-content-item tn-text-center">
        <tn-button
          bg-color="#00C8B0"
          :custom-style="{padding:'40rpx 0'}"
          width="60%"
          :fontSize="28"
          text-color="#FFFFFF"
          shape="round"
          @click="submitGroup"
        >
          <text class="">创建群聊</text>
        </tn-button>
      </view>
    </view>
    
    <view class="tn-tabbar-height"></view>

  </view>

</template>

<script setup>
  import { computed, onMounted, ref } from 'vue'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  import config from '@/config'
  import { getEmployeeList } from '@/api/employee'
  import { createContactGroup } from '@/api/contact'
  import { groupContacts } from '@/utils/surname'

  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()

  // 员工列表数据(加载失败或为空时展示空态,不再使用假数据兜底)
  const employees = ref([])
  const loadingEmployees = ref(true)
  const selectedIds = ref([])

  const listData = computed(() => groupContacts(employees.value.map((item) => ({
    id: item.id,
    avatar: item.avatar,
    username: item.name || item.employeeName || item.employeeNo || '未命名员工',
    office: item.deptName || item.companyName || item.position || item.post || '未设置部门'
  }))))

  const formatAvatar = (avatar) => {
    if (!avatar) return '/static/author.jpg'
    if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
    return config.baseUrl + avatar
  }

  const isSelected = (id) => selectedIds.value.includes(id)

  const toggleSelect = (data) => {
    if (!data?.id) return
    if (isSelected(data.id)) {
      selectedIds.value = selectedIds.value.filter((id) => id !== data.id)
      return
    }
    selectedIds.value = [...selectedIds.value, data.id]
  }

  const loadEmployees = async () => {
    try {
      const res = await getEmployeeList({ status: 1 })
      employees.value = Array.isArray(res.data) ? res.data : []
    } catch (error) {
      uni.showToast({ icon: 'none', title: '加载同事列表失败' })
    } finally {
      loadingEmployees.value = false
    }
  }

  const submitGroup = async () => {
    if (!selectedIds.value.length) {
      uni.showToast({
        title: '请选择群成员',
        icon: 'none'
      })
      return
    }
    try {
      await createContactGroup({
        groupName: `工作群(${selectedIds.value.length + 1}人)`,
        memberIds: selectedIds.value
      })
      uni.showToast({
        title: '创建成功',
        icon: 'success'
      })
      setTimeout(() => {
        uni.redirectTo({ url: '/partnerPages/group' })
      }, 500)
    } catch (error) {
    }
  }
  
  // 面对面建群:功能尚未实现,给出提示而不是死链接
  const showFaceToFaceTip = () => {
    uni.showToast({
      title: '功能开发中，敬请期待',
      icon: 'none'
    })
  }

  // 跳转
  const tn = (e) => {
    if (!e) return
    uni.navigateTo({
      url: e,
    })
  }

  onMounted(() => {
    loadEmployees()
  })
  

</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    z-index: 9999;
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
    font-size: 32rpx;
    
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
  .logo-image {
    width: 90rpx;
    height: 90rpx;
    position: relative;
  }
  
  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: center;
    // border: 1rpx solid rgba(255,255,255,0.05);
    // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 100rpx;
    overflow: hidden;
    // background-color: #FFFFFF;
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
        width: 80rpx;
        height: 80rpx;
        font-size: 50rpx;
        border-radius: 50%;
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
  
  /* 列表数据样式 start */
  .list-data {
    display: flex;
    padding: 30rpx;
    align-items: center;
    
    .image {
      width: 90rpx;
      height: 90rpx;
      border-radius: 15rpx;
    }
    .info {
      margin-left: 20rpx;
      line-height: 1;
      flex: 1;
      .username {
        font-size: 34rpx;
        font-weight: bold;
      }
      .office {
        margin-top: 18rpx;
        font-size: 26rpx;
      }
    }
  }
  /* 列表数据样式 end */
  
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
