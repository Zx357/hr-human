<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class='icon'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">问卷详情</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
      
      <view class="">
        
        <view class="tn-margin" style="background-color: #FFFFFF;padding: 20rpx 30rpx 20rpx 30rpx;position: relative;border-radius:20rpx">
          <view class="tn-main-gradient-blue" style="width: 82rpx;height: 68rpx;position: absolute;top: 0;left:0;border-radius: 20rpx 0 100rpx 0;z-index: 0;opacity: 0.6;">
            <!-- <view class="tn-margin-xs tn-color-white">序号</view> -->
          </view>
          <view class="tn-flex tn-flex-row-between tn-flex-col-center" style="padding: 0rpx 0rpx 20rpx 0rpx;position: relative;">
            <view class="justify-content-item tn-text-justify">
              <text class="tn-text-xl tn-padding-right tn-color-white" style="margin-left: -20rpx;">01</text>
              <text class="tn-text-bold tn-text-lg">你在团队中主要扮演什么角色 ~【单选】</text>
            </view>
            <view class="justify-content-item tn-color-gray--dark">
              <!-- *为必填 -->
            </view>
          </view>
          
          <view class="">
            <tn-radio-group width="100%" wrap>
              <tn-radio name="单选1" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  祭天产品经理
                </view>
              </tn-radio>
              <tn-radio name="单选2" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  花里胡哨UI设计
                </view>
              </tn-radio>
              <tn-radio name="单选3" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  菜鸡前端开发
                </view>
              </tn-radio>
              <tn-radio name="单选4" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  挖坑后端开发
                </view>
              </tn-radio>
            </tn-radio-group>
          </view>
          
        </view>
        
        <view class="tn-margin" style="background-color: #FFFFFF;padding: 20rpx 30rpx 20rpx 30rpx;position: relative;border-radius:20rpx">
          <view class="tn-main-gradient-blue" style="width: 82rpx;height: 68rpx;position: absolute;top: 0;left:0;border-radius: 20rpx 0 100rpx 0;z-index: 0;opacity: 0.6;">
            <!-- <view class="tn-margin-xs tn-color-white">序号</view> -->
          </view>
          <view class="tn-flex tn-flex-row-between tn-flex-col-center" style="padding: 0rpx 0rpx 20rpx 0rpx;position: relative;">
            <view class="justify-content-item tn-text-justify">
              <text class="tn-text-xl tn-padding-right tn-color-white" style="margin-left: -20rpx;">02</text>
              <text class="tn-text-bold tn-text-lg">你对哪些岗位感兴趣 ~【多选】</text>
            </view>
            <view class="justify-content-item tn-color-gray--dark">
              <!-- *为必填 -->
            </view>
          </view>
          
          <view class="">
            <tn-checkbox-group width="100%" wrap>
              <tn-checkbox name="多选1" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  祭天产品经理
                </view>
              </tn-checkbox>
              <tn-checkbox name="多选2" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  花里胡哨UI设计
                </view>
              </tn-checkbox>
              <tn-checkbox name="多选3" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  菜鸡前端开发
                </view>
              </tn-checkbox>
              <tn-checkbox name="多选4" activeColor="#4B98FE">
                <view class="tn-margin-sm tn-text-sm">
                  挖坑后端开发
                </view>
              </tn-checkbox>
            </tn-checkbox-group>
          </view>
          
        </view>
        
        <view class="tn-margin" style="background-color: #FFFFFF;padding: 20rpx 30rpx 20rpx 30rpx;position: relative;border-radius:20rpx">
          <view class="tn-main-gradient-blue" style="width: 82rpx;height: 68rpx;position: absolute;top: 0;left:0;border-radius: 20rpx 0 100rpx 0;z-index: 0;opacity: 0.6;">
            <!-- <view class="tn-margin-xs tn-color-white">序号</view> -->
          </view>
          <view class="tn-flex tn-flex-row-between tn-flex-col-center" style="padding: 0rpx 0rpx 20rpx 0rpx;position: relative;">
            <view class="justify-content-item tn-text-justify">
              <text class="tn-text-xl tn-padding-right tn-color-white" style="margin-left: -20rpx;">14</text>
              <text class="tn-text-bold tn-text-lg">你觉得目前的图鸟生态，能打几分 ~</text>
            </view>
            <view class="justify-content-item tn-color-gray--dark">
              <!-- *为必填 -->
            </view>
          </view>
          
          <view class="tn-flex tn-flex-row-left tn-flex-col-center tn-padding-top-sm tn-padding-bottom-sm">
            <view class="justify-content-item tn-padding-right">
              气氛热度 <!-- <text class="tn-color-red tn-padding-left-xs">*</text> -->
            </view>
            <view class="justify-content-item">
              <tn-rate  activeColor="#4B98FE" inactiveColor="#E6E6E6" size="36"></tn-rate>
            </view>
          </view>
          <view class="tn-flex tn-flex-row-left tn-flex-col-center tn-padding-top-sm tn-padding-bottom-sm">
            <view class="justify-content-item tn-padding-right">
              技术能力 <!-- <text class="tn-color-red tn-padding-left-xs">*</text> -->
            </view>
            <view class="justify-content-item">
              <tn-rate  activeColor="#4B98FE" inactiveColor="#E6E6E6" size="36"></tn-rate>
            </view>
          </view>
          <view class="tn-flex tn-flex-row-left tn-flex-col-center tn-padding-top-sm tn-padding-bottom-sm">
            <view class="justify-content-item tn-padding-right">
              响应速度 <!-- <text class="tn-color-red tn-padding-left-xs">*</text> -->
            </view>
            <view class="justify-content-item">
              <tn-rate  activeColor="#4B98FE" inactiveColor="#E6E6E6" size="36"></tn-rate>
            </view>
          </view>
          
        </view>
        
        <view class="tn-margin" style="background-color: #FFFFFF;padding: 20rpx 30rpx 20rpx 30rpx;position: relative;border-radius:20rpx">
          <view class="tn-main-gradient-blue" style="width: 82rpx;height: 68rpx;position: absolute;top: 0;left:0;border-radius: 20rpx 0 100rpx 0;z-index: 0;opacity: 0.6;">
            <!-- <view class="tn-margin-xs tn-color-white">序号</view> -->
          </view>
          <view class="tn-flex tn-flex-row-between tn-flex-col-center" style="padding: 0rpx 0rpx 20rpx 0rpx;position: relative;">
            <view class="justify-content-item tn-text-justify">
              <text class="tn-text-xl tn-padding-right tn-color-white" style="margin-left: -20rpx;">15</text>
              <text class="tn-text-bold tn-text-lg">对图鸟生态有想法和建议，请畅所欲言 ~</text>
            </view>
            <view class="justify-content-item tn-color-gray--dark">
              <!-- *为必填 -->
            </view>
          </view>
          <view class="tn-color-gray--dark tn-text-justify">
            <view class="tn-bg-gray--light" style="border-radius: 10rpx;margin: 20rpx 0rpx 20rpx 0rpx;">
              <textarea maxlength="300" placeholder="请填写关键信息" placeholder-style="color:#AAAAAA;" style="height: 160rpx;padding:30rpx 0 20rpx 30rpx;"></textarea>
            </view>
          </view>
        </view>
        
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
            @click="tn('/workPages/prompt')"
          >
            <text class="">提交问卷</text>
          </tn-button>
        </view>
      </view>
      
      <view class="tn-tabbar-height"></view>
      
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const list = ref([{
    name: '祭天产品经理',
    disabled: false
  },
  {
    name: '花里胡哨UI设计',
    disabled: false
  },
  {
    name: '菜鸡前端开发',
    disabled: false
  },
  {
    name: '挖坑后端开发',
    disabled: false
  }
])
// tn-radio-group的v-model绑定的值如果设置为某个radio的name，就会被默认选中
const value = ref('')

// 选中某个单选框时，由radio时触发
function radioChange(e) {
  // console.log(e);
}

// 选中任一radio时，由radio-group触发
function radioGroupChange(e) {
  // console.log(e);
}

// 跳转
function tn(e) {
  uni.navigateTo({
    url: e,
  });
}
</script>

<script>
export default {
  name: 'TemplateContent',
  
}
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
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
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
  
</style>
