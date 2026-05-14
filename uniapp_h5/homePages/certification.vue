<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#F8F7F8" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">企业认证</text>
      </view>
    </tn-navbar>

    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <view class="tn-padding" style="background-color: #FBF2EB;color: #FF6E16;">
        <tn-icon name="tip" class="tn-padding-right-xs"></tn-icon>
        认证通知：请于2024年06月08日前认证，否则认证标识将变为未认证状态，部分功能也将受限
      </view>
      
      <view class="tn-margin" style="background-color: #FFFFFF;border-radius: 24rpx;padding: 20rpx 30rpx 20rpx 30rpx;">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min" style="padding: 30rpx 30rpx 20rpx 0rpx;">
          <view class="justify-content-item">
            <text class="tn-text-bold tn-text-xl">企业信息</text>
          </view>
          <view class="justify-content-item tn-color-gray--dark">
            <!-- *为必填 -->
          </view>
        </view>
          
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              企业全称 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="广州图鸟科技有限公司" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              社会信用代码 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入社会信用代码" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top tn-padding-bottom-xs">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              营业执照 + 补充证明材料 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <!-- <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入营业执照" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view> -->
        </view>
        
        <view class="" style="padding: 20rpx 0rpx 0rpx 0rpx;">
          <tn-image-upload
            ref="imageUpload"
            :action="action"
            :width="210"
            :height="210"
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
        
      </view>
      
      <view class="tn-margin" style="background-color: #FFFFFF;border-radius: 24rpx;padding: 20rpx 30rpx 30rpx 30rpx;">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min" style="padding: 30rpx 30rpx 20rpx 0rpx;">
          <view class="justify-content-item">
            <text class="tn-text-bold tn-text-xl">运营者信息</text>
          </view>
          <view class="justify-content-item tn-color-gray--dark">
            <!-- *为必填 -->
          </view>
        </view>
          
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              姓名 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入姓名" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              手机号 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入手机号" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              短信验证码 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-flex tn-flex-row-right tn-flex-col-center tn-color-gray tn-color-black" style="max-width:50vw">
              <view class="tn-padding-right-xs">
                <input maxlength="6" placeholder="验证码" name="input" placeholder-style="color:#AAAAAA" value=""></input>
              </view>
              <view class="" @tap.stop="getCode">
                <tn-button
                  bg-color="#3668FC"
                  :custom-style="{padding:'5rpx 10rpx'}"
                  width="180rpx"
                  :fontSize="22"
                  text-color="#FFFFFF"
                  shape="round"
                  @click="tn('')"
                >
                  <text class="">{{ tips }}</text>
                </tn-button>
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top tn-padding-bottom-xs">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              身份证 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <!-- <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入身份证号" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view> -->
        </view>
        
        <view class="tn-padding-top">
          <view class="image-upload__wrap">
            <view class="image-upload__item" @tap.stop="uploadIdCardPeopleImage">
              <view v-if="idCardPeopleImage" class="image-upload__image">
                <image class="image" :src="idCardPeopleImage"></image>
              </view>
              <view v-else class="image-upload__empty">
                <image class="image" src="https://cdn.nlark.com/yuque/0/2023/png/280373/1691058667688-assets/web-upload/f6e87ca6-ee8b-4059-8aaf-68242a9fd351.png"></image>
              </view>
              <view class="image-upload__title tn-color-gray tn-text-sm">
                身份证人像面
              </view>
            </view>
            <view class="image-upload__item" @tap.stop="uploadIdCardEmblemImage">
              <view v-if="idCardEmblemImage" class="image-upload__image">
                <image class="image" :src="idCardEmblemImage"></image>
              </view>
              <view v-else class="image-upload__empty">
                <image class="image" src="https://cdn.nlark.com/yuque/0/2023/png/280373/1691058667723-assets/web-upload/653fbfc5-6ec5-48b7-9072-6e3212bea909.png"></image>
              </view>
              <view class="image-upload__title tn-color-gray tn-text-sm">
                身份证国徽面
              </view>
            </view>
          </view>
        </view>
        
      </view>
      
      
      
      <view class="tn-margin" style="background-color: #FFFFFF;border-radius: 24rpx;padding: 20rpx 30rpx 30rpx 30rpx;">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min" style="padding: 30rpx 30rpx 20rpx 0rpx;">
          <view class="justify-content-item">
            <text class="tn-text-bold tn-text-xl">其他信息</text>
          </view>
          <view class="justify-content-item tn-color-gray--dark">
            <!-- *为必填 -->
          </view>
        </view>
        
        <picker @change="bindPickerChange" :value="index" :range="array">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
            <view class="justify-content-item" style="min-width: 150rpx;">
              <view class="tn-text-bold">
                开票类型 <text class="tn-color-red tn-padding-left-xs">*</text>
              </view>
            </view>
            <view class="justify-content-item tn-color-grey tn-text-right">
              <view class="tn-color-gray tn-color-black" style="max-width:50vw">
                <view class="tn-color-gray" v-if="index===99">
                  请选择
                  <tn-icon name="right" class="tn-padding-left-xs"></tn-icon>
                </view>
                <view class="" v-else>
                  {{array[index]}}
                  <tn-icon name="right" class="tn-padding-left-xs"></tn-icon>
                </view>
              </view>
            </view>
          </view>
        </picker>
        
        <picker @change="bindDateChange" mode="date" :value="date" :start="startDate" :end="endDate" fields="year">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
            <view class="justify-content-item" style="min-width: 150rpx;">
              <view class="tn-text-bold">
                开票年份 <text class="tn-color-red tn-padding-left-xs">*</text>
              </view>
            </view>
            <view class="justify-content-item tn-color-grey tn-text-right">
              <view class="tn-color-gray tn-color-black" style="max-width:50vw">
                <view class="tn-color-gray" v-if="date===''">
                  请选择
                  <tn-icon name="right" class="tn-padding-left-xs"></tn-icon>
                </view>
                <view class="" v-else>
                  {{date}} 年
                  <tn-icon name="right" class="tn-padding-left-xs"></tn-icon>
                </view>
              </view>
            </view>
          </view>
        </picker>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              电子邮箱 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入电子邮箱" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min tn-padding-top tn-padding-bottom">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              企业简称 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入企业简称" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top tn-padding-bottom-xs">
          <view class="justify-content-item" style="min-width: 150rpx;">
            <view class="tn-text-bold">
              公司地址 <text class="tn-color-red tn-padding-left-xs">*</text>
            </view>
          </view>
          <view class="justify-content-item tn-color-grey tn-text-right">
            <view class="tn-color-gray tn-color-black" style="max-width:50vw">
              <input placeholder="请输入公司地址" name="input" placeholder-style="color:#AAAAAA" value=""></input>
            </view>
          </view>
        </view>
        
      </view>
      
      
      <view class="tn-margin tn-padding-top-sm tn-text-sm">
        如认证过程遇到未知问题，请在备注中说明即可
      </view>
      
      <view class="tn-margin" style="background-color: #FFFFFF;border-radius: 24rpx;padding: 20rpx 30rpx 20rpx 30rpx;">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-strip-bottom-min" style="padding: 30rpx 30rpx 20rpx 0rpx;">
          <view class="justify-content-item">
            <text class="tn-text-bold tn-text-xl">备注说明</text>
          </view>
          <view class="justify-content-item tn-color-gray--dark">
            <!-- *为必填 -->
          </view>
        </view>
      
        <view class="tn-color-gray--dark tn-text-justify" style="padding: 10rpx 0rpx 0rpx 0rpx;">
          <view class="tn-bg-gray--light" style="border-radius: 10rpx;margin: 20rpx 0rpx 20rpx 0rpx;">
            <textarea maxlength="300" placeholder="请填写关键信息" placeholder-style="color:#AAAAAA" style="height: 160rpx;padding:30rpx 0 20rpx 30rpx;"></textarea>
          </view>
        </view>
        
      </view>
      
      <!-- 验证码倒计时 TODO -->
      <!-- <tn-verification-code
        ref="code"
        uniqueKey="login-demo-4"
        :seconds="60"
        @change="codeChange">
      </tn-verification-code> -->
      
      <!-- 悬浮按钮-->
      <view class="tn-flex tn-footerfixed tn-padding">
        <view class="tn-flex-1 justify-content-item tn-text-center">
          <tn-button
            bg-color="#FFAC00"
            :custom-style="{padding:'40rpx 0'}"
            width="60%"
            :fontSize="28"
            text-color="#FFFFFF"
            shape="round"
            @click="tn('/workPages/prompt')"
          >
            <text class="">提交认证</text>
          </tn-button>
        </view>
      </view>
      
      <view class="tn-tabbar-height"></view>
      
    </view>
    
    
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const tips = ref('获取验证码')
const index = ref(99)
const date = ref('')
const array = ref(['电子普票', '专用发票'])
// 上传文件地址
const uploadAction = ref('https://www.hualigs.cn/api/upload')
// 身份证人像面
const idCardPeopleImage = ref('')
// 身份证国徽面
const idCardEmblemImage = ref('')

/* 营业执照*/
const action = ref('https://www.hualigs.cn/api/upload')
// action: '',
const formData = ref({
  apiType: 'this,ali',
  token: 'dffc1e06e636cff0fdf7d877b6ae6a2e',
  image: null
})
const fileList = ref([{url: 'https://resource.tuniaokj.com/images/album/xiong6.jpg'},{url: 'https://resource.tuniaokj.com/images/album/xiong5.jpg'}])
const showUploadList = ref(true)
const customBtn = ref(false)
const autoUpload = ref(true)
const showProgress = ref(false)
const deleteable = ref(true)
const customStyle = ref(false)
const maxCount = ref(3)
const disabled = ref(false)

// 计算属性
const startDate = computed(() => {
  return getDate('start')
})

const endDate = computed(() => {
  return getDate('end')
})

// 上传身份证人像面
const uploadIdCardPeopleImage = () => {
  chooseImageAndUpload().then((res) => {
    idCardPeopleImage.value = res
  })
}

// 上传身份证国徽面
const uploadIdCardEmblemImage = () => {
  chooseImageAndUpload().then((res) => {
    idCardEmblemImage.value = res
  })
}

// 点击上传图片
const chooseImageAndUpload = () => {
  // 只选择图片的时候使用 chooseImage 来实现
  const chooseFile = new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      success: resolve,
      fail: reject
    })
  })
  
  return new Promise((resolve, reject) => {
    chooseFile.then((res) => {
      const tempFile = res.tempFilePaths[0]
      uni.showLoading({
        mask:true
      })
      uni.uploadFile({
        url: uploadAction.value,
        success: (res) => {
          // 判断啊是否为json字符串，将其转换为json格式
          let data = $t.test.jsonString(res.data) ? JSON.parse(res.data) : res.data
          if (![200, 201, 204].includes(res.statusCode)) {
            
          } else {
            resolve(tempFile)
          }
        },
        fail: (err) => {
          uni.showModal({
            title: '提示',
            content: '上传文件失败',
            showCancel: false
          })
        },
        complete: (res) => {
          uni.hideLoading()
        }
      })
    }).catch((err) => {
      console.log(err);
      uni.hideLoading()
      uni.showModal({
        title: '提示',
        content: '选择图片失败',
        showCancel: false
      })
      reject(err)
    })
  })
}

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
}

// 获取验证码
const getCode = () => {
  if ($refs.code.canGetCode) {
    $t.message.loading('正在获取验证码')
    setTimeout(() => {
      $t.message.closeLoading()
      $t.message.toast('验证码已经发送')
      // 通知组件开始计时
      $refs.code.start()
    }, 2000)
  } else {
    $t.message.toast($refs.code.secNum + '秒后再重试')
  }
}

// 获取验证码倒计时被修改
const codeChange = (event) => {
  tips.value = event
}

// 手动上传文件
const upload = () => {
  $refs.imageUpload.upload()
}

// 图片拖拽重新排序
const onSortList = (list) => {
  console.log(list);
}

const bindPickerChange = (e) => {
  index.value = e.detail.value
}

const bindDateChange = (e) => {
  date.value = e.detail.value
}

const getDate = (type) => {
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();

  if (type === 'start') {
      year = year - 60;
  } else if (type === 'end') {
      year = year + 2;
  }
  month = month > 9 ? month : '0' + month;
  day = day > 9 ? day : '0' + day;
  return `${year}-${month}-${day}`;
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
    background-color: #E7EDF7;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }

    
    

    /* 间隔线 start*/
    .tn-strip-bottom-min {
      width: 100%;
      border-bottom: 1rpx solid #F8F9FB;
    }
    
    .tn-strip-bottom {
     width: 100%;
     border-bottom: 20rpx solid rgba(241, 241, 241, 0.8);
    }
     /* 间隔线 end*/


  /* 用户头像 start */
  .logo-image {
    width: 80rpx;
    height: 80rpx;
    position: relative;
  }

  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: top;
    border: 2rpx solid rgba(255,255,255,0.05);
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 50%;
    overflow: hidden;
    // background-color: #FFFFFF;
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
  
  /* 自定义上传事件 start */
  .image-upload {
    &__wrap {
      display: flex;
      align-items: center;
    }
    &__item {
      flex: 1;
      width: calc(100% - 60rpx);
      max-width: 50%;
      height: 260rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .image {
        width: 100%;
        height: 100%;
        border: 2rpx dashed #E6E6E6;
        border-radius: 15rpx;
        overflow: hidden;
      }
    }
    &__image, &__empty {
      width: 100%;
      height: 90%;
    }
    &__title {
      padding-top: 30rpx;
      line-height: 1;
    }
    &__item + &__item {
      margin-left: 20rpx;
    }
  }
  /* 自定义上传事件 end */
  
</style>
