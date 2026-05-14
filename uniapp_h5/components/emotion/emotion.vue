<template>
  <view
    v-if="openModal"
    class="wx-modal"
  >
    <view
      class="wam__mask"
      @touchmove.prevent=""
      @tap.stop="closeModal"
    ></view>
    
    <!-- 内容区域 -->
    <view class="wam__wrapper">
      <!-- 关闭按钮 -->
      <!-- <view class="wam__close-btn" @tap.stop="closeModal">
        <text class="tn-icon-close"></text>
      </view> -->
      
      <view class="tn-flex tn-flex-wrap">
        <block>
         <view class="" style="width: 18%;" @tap.stop="modeSwitch(0)">
           <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
             <!-- 当然，如果你有图片，可以换成图片模式 -->
             <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
             </view> -->
             <view class="icon28__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-black" :class="currentModeIndex==0?'tn-bg-gray--light':''">
               <view class="tn-icon-like" style="text-shadow: 5rpx 8rpx 10rpx rgba(0,0,0,0.16);">
               </view>
             </view>
             <!-- <view class="tn-color-black tn-text-center tn-text-sm">
               <text class="tn-text-ellipsis">{{ item.title }}</text>
             </view> -->
           </view>
         </view>
        </block>
        
        <block v-for="(item, index) in list" :key="index">
         <view class="" style="width: 18%;"  @tap.stop="modeSwitch(index+1)">
           <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
             <!-- 当然，如果你有图片，可以换成图片模式 -->
             <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
             </view> -->
             <view class="icon28__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-black" :class="currentModeIndex==index+1?'tn-bg-gray--light':''">
               <view :class="[`tn-${item.icon}`]">
               </view>
             </view>
             <!-- <view class="tn-color-black tn-text-center tn-text-sm">
               <text class="tn-text-ellipsis">{{ item.title }}</text>
             </view> -->
           </view>
         </view>
        </block>
      </view>
      
      <view class="tn-padding-top-sm">
        
        <scroll-view scroll-y="true" style="max-height: 30vh;">
          
          <!-- 喜欢-->
          <view class="" :class="currentModeIndex==0?'content-show':'content-hide'">
            <!-- 方式5 start-->
            <view class="tn-flex tn-flex-wrap tn-padding-bottom">
             <block v-for="(item, index) in like" :key="index">
              <view class="tn-margin-bottom tn-margin-top-sm" style="width: 25%;" @click="tn(item.url)">
                <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                  <!-- 当然，如果你有图片，可以换成图片模式 -->
                  <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                  </view> -->
                  <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center">
                    <view :class="[`tn-${item.icon}`]">
                      <!-- <tn-badge backgroundColor="#EA5E55" fontColor="#FFFFFF" :absolute="true" :fontSize="16" :translateCenter="false" top="-30rpx" right="-26rpx">
                        <text>16</text>
                      </tn-badge> -->
                    </view>
                  </view>
                  <view class="tn-color-gray--dark tn-text-center tn-text-xs">
                    <text class="tn-text-ellipsis">{{ item.title }}</text>
                  </view>
                </view>
              </view>
             </block>
            </view>
            <!-- 方式5 end-->
            
          </view>
          
          <!-- 小猫咪-->
          <view class="" :class="currentModeIndex==1?'content-show':'content-hide'">
            <!-- 方式5 start-->
            <view class="tn-flex tn-flex-wrap tn-padding-bottom">
             <block v-for="(item, index) in emocat" :key="index">
              <view class="tn-margin-bottom tn-margin-top-sm" style="width: 25%;" @click="tn(item.url)">
                <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                  <!-- 当然，如果你有图片，可以换成图片模式 -->
                  <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                  </view> -->
                  <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center">
                    <view :class="[`tn-${item.icon}`]">
                      <!-- <tn-badge backgroundColor="#EA5E55" fontColor="#FFFFFF" :absolute="true" :fontSize="16" :translateCenter="false" top="-30rpx" right="-26rpx">
                        <text>16</text>
                      </tn-badge> -->
                    </view>
                  </view>
                  <view class="tn-color-gray--dark tn-text-center tn-text-xs">
                    <text class="tn-text-ellipsis">{{ item.title }}</text>
                  </view>
                </view>
              </view>
             </block>
            </view>
            <!-- 方式5 end-->
            
          </view>
        
          <!-- 小虫子-->
          <view class="" :class="currentModeIndex==2?'content-show':'content-hide'">
            <!-- 方式5 start-->
            <view class="tn-flex tn-flex-wrap tn-padding-bottom">
             <block v-for="(item, index) in emoworm" :key="index">
              <view class="tn-margin-bottom tn-margin-top-sm" style="width: 25%;" @click="tn(item.url)">
                <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                  <!-- 当然，如果你有图片，可以换成图片模式 -->
                  <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                  </view> -->
                  <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center">
                    <view :class="[`tn-${item.icon}`]">
                      <!-- <tn-badge backgroundColor="#EA5E55" fontColor="#FFFFFF" :absolute="true" :fontSize="16" :translateCenter="false" top="-30rpx" right="-26rpx">
                        <text>16</text>
                      </tn-badge> -->
                    </view>
                  </view>
                  <view class="tn-color-gray--dark tn-text-center tn-text-xs">
                    <text class="tn-text-ellipsis">{{ item.title }}</text>
                  </view>
                </view>
              </view>
             </block>
            </view>
            <!-- 方式5 end-->
            
          </view>
          
          
          <!--  -->
          <view class="" :class="currentModeIndex==3?'content-show':'content-hide'">
            <!-- 方式5 start-->
            <view class="tn-flex tn-flex-wrap tn-padding-bottom">
             <block v-for="(item, index) in emoface" :key="index">
              <view class="tn-margin-bottom tn-margin-top-sm" style="width: 25%;" @click="tn(item.url)">
                <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                  <!-- 当然，如果你有图片，可以换成图片模式 -->
                  <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                  </view> -->
                  <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center">
                    <view :class="[`tn-${item.icon}`]">
                      <!-- <tn-badge backgroundColor="#EA5E55" fontColor="#FFFFFF" :absolute="true" :fontSize="16" :translateCenter="false" top="-30rpx" right="-26rpx">
                        <text>16</text>
                      </tn-badge> -->
                    </view>
                  </view>
                  <view class="tn-color-gray--dark tn-text-center tn-text-xs">
                    <text class="tn-text-ellipsis">{{ item.title }}</text>
                  </view>
                </view>
              </view>
             </block>
            </view>
            <!-- 方式5 end-->
            
          </view>
          
        </scroll-view>
        
      </view>
      
      
      <!-- 悬浮按钮-->
      <!-- <view class="tn-flex tn-footerfixed" style="padding: 40rpx;margin-bottom: 100rpx;">
        <view class="tn-flex-1 justify-content-item tn-margin-left-xs tn-text-center">
          <tn-button backgroundColor="#07C160 " padding="40rpx 0" width="100%" :fontSize="28" fontColor="#FFFFFF" shape="round" @tap.stop="closeModal">
            <text class="">下一步</text>
          </tn-button>
        </view>
      </view> -->
      
      
    </view>
  </view>
</template>

<script>
  
  export default {
    options: {
      // 在微信小程序中将组件节点渲染为虚拟节点，更加接近Vue组件的表现(不会出现shadow节点下再去创建元素)
      virtualHost: true
    },
    props: {
      value: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        // 当前选中的模式
        currentModeIndex: 0,
        
        openModal: false,
        
        cardCur: 0,
        /* 表情分类区*/
        list: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "小猫咪",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211148-assets/web-upload/bc9ff0e7-36a5-4d99-8698-cd589b00dc99.png",
            title: "小虫子",
            icon: 'worm-eat',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765049011-assets/web-upload/e49243fa-5182-4fbb-a850-33e927316a90.png",
            title: "小黄脸",
            icon: 'emoji-face1',
          }
        ],
        
        /* 喜欢*/
        like: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "我火龙果呢",
            icon: 'worm-eat',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211148-assets/web-upload/bc9ff0e7-36a5-4d99-8698-cd589b00dc99.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765049011-assets/web-upload/e49243fa-5182-4fbb-a850-33e927316a90.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "恶魔来袭",
            icon: 'emoji-face9',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          }
        ],
        
        /* 小猫咪*/
        emocat: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "吃瓜群众",
            icon: 'cat-eat',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "认真摸鱼",
            icon: 'cat-fish',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "无奈叹气",
            icon: 'cat-sigh',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "出去玩",
            icon: 'cat-play',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "嘤嘤嘤",
            icon: 'cat-sad',
          }
        ],
        
        /* 小虫子*/
        emoworm: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "我火龙果呢",
            icon: 'worm-eat',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211148-assets/web-upload/bc9ff0e7-36a5-4d99-8698-cd589b00dc99.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "内心窃喜",
            icon: 'worm-like',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "内心窃喜",
            icon: 'worm-like',
          }
        ],
        
        /* 小黄脸*/
        emoface: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211148-assets/web-upload/bc9ff0e7-36a5-4d99-8698-cd589b00dc99.png",
            title: "一脸懵逼",
            icon: 'emoji-face2',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765049011-assets/web-upload/e49243fa-5182-4fbb-a850-33e927316a90.png",
            title: "爱你凶姐",
            icon: 'emoji-face3',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "惊恐万分",
            icon: 'emoji-face4',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "假装很酷",
            icon: 'emoji-face5',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "调皮一下",
            icon: 'emoji-face6',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "很萌很萌",
            icon: 'emoji-face7',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "开怀大笑",
            icon: 'emoji-face8',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "恶魔来袭",
            icon: 'emoji-face9',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "有点蓝瘦",
            icon: 'emoji-face10',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "越想越气",
            icon: 'emoji-face11',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "委屈巴巴",
            icon: 'emoji-face12',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "飞行模式",
            icon: 'emoji-face13',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "笑死老子",
            icon: 'emoji-face1',
          }
        ],
        
      }
    },
    watch: {
      value: {
        handler(val) {
          this.openModal = val
        },
        immediate: true
      }
    },
    methods: {
      // 切换模式
      modeSwitch(index) {
        this.currentModeIndex = index
      },
      
      // 关闭弹框
      closeModal() {
        this.$emit('input', false)
      },
      
      // cardSwiper
      cardSwiper(e) {
        this.cardCur = e.detail.current
      },
      
    }
  }
</script>

<style lang="scss" scoped>
  
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
  
  .content-show{
    display: block;
  }
  
  .content-hide{
    display: none;
  }
  
  
  .wx-modal {
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
    z-index: 99998 !important;
    
    view {
      box-sizing: border-box;
    }
    
    .image {
      width: 100%;
      height: 100%;
      border-radius: inherit;
    }
    
    .wam {
      z-index: 9999 !important;
      /* mask */
      &__mask {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        opacity: 0;
        animation: showMask 0.25s ease 0.1s forwards;
      }
      
      /* close-btn */
      &__close-btn {
        position: absolute;
        top: 30rpx;
        right: 30rpx;
        z-index: 99999;
        font-size: 40rpx;
      }
      
      /* wrapper */
      &__wrapper {
        position: absolute;
        left: 0;
        bottom: 0;
        width: 100%;
        background-color: #FFFFFF;
        border-radius: 40rpx 40rpx 0rpx 0rpx;
        padding-top: 40rpx;
        // padding-bottom: 40rpx;
        // padding-bottom: calc(constant(safe-area-inset-bottom) + 40rpx);
        // padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
        transform-origin: center bottom;
        transform: scaleY(0);
        animation: showWrapper 0.25s ease 0.1s forwards;
        z-index: 99999;
      }
      
    }
  }
  
  @keyframes showMask {
    0% {
      opacity: 0;
    }
    100% {
      opacity: 1;
    }
  }
  @keyframes showWrapper {
    0% {
      transform: scaleY(0);
      opacity: 0;
    }
    100% {
      transform: scaleY(1);
      opacity: 1;
    }
  }
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
  	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    width: 100%;
    margin-top: 60rpx;
    margin-bottom: calc(env(safe-area-inset-bottom) - 20rpx );
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
  
  }
  /* 底部悬浮按钮 end*/
  
  
  /* 金刚区轮播 start */
  .card-swiper {
    height: 470rpx !important;
  }
    
  .card-swiper swiper-item {
    width: 750rpx !important;
    left: 0rpx;
    box-sizing: border-box;
    // padding: 0rpx 30rpx 90rpx 30rpx;
    overflow: initial;
  }
    
  .card-swiper swiper-item .swiper-item {
    width: 100%;
    display: block;
    height: 100%;
    transform: scale(1);
    transition: all 0.2s ease-in 0s;
    will-change: transform;
    overflow: hidden;
  }
    
  .card-swiper swiper-item.cur .swiper-item {
    transform: none;
    transition: all 0.2s ease-in 0s;
    will-change: transform;
  }
    
  .card-swiper swiper-item .swiper-item-text {
    margin-top: -300rpx;
    text-align: center;
    width: 100%;
    display: block;
    height: 50%;
    border-radius: 10rpx;
    transform: translate(100rpx, 0rpx) scale(0.9, 0.9);
    transition: all 0.6s ease 0s;
    will-change: transform;
    overflow: hidden;
  }
    
  .card-swiper swiper-item.cur .swiper-item-text {
    margin-top: -300rpx;
    width: 100%;
    transform: translate(0rpx, 0rpx) scale(0.9, 0.9);
    transition: all 0.6s ease 0s;
    will-change: transform;
  }
  
  .image-banner{
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .image-banner image{
    width: 100%;
    height: 100%;
  }
  
  /* 轮播指示点 start*/
  .indication{
    z-index: 9999;
    width: 100%;
    height: 36rpx;
    position: absolute;
    display:flex;
    flex-direction:row;
    align-items:center;
    justify-content:center;
  }
  
  .spot{
    background-color: #000000;
    opacity: 0.1;
    width: 10rpx;
    height: 10rpx;
    border-radius: 20rpx;
    top: -70rpx;
    margin: 0 8rpx !important;
    position: relative;
  }
  
  .spot.active{
    opacity: 0.15;
    width: 30rpx;
    background-color: #000000;
  }
  
  .icon12 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 112rpx;
        margin-bottom: 18rpx;
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
          background-size: 100% 100%;
        }
      }
      
    }
  }
  
  .icon28 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 80rpx;
        height: 80rpx;
        font-size: 46rpx;
        margin-bottom: 18rpx;
        position: relative;
        border-radius: 20rpx;
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
          background-size: 100% 100%;
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg4.png);
        }
      }
      
    }
  }
  
  .icon13 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 56rpx;
        margin-bottom: 18rpx;
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
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg4.png);
            
        }
       
      }
      
    }
  }
  
</style>