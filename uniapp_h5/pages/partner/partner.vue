<template>
  <view class="template-partner tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottomShadow="false" bg-color="#FFFFFF00" backText="" backIcon="" homeIcon="">
      <view class="custom-nav tn-flex tn-flex-col-center tn-flex-row-left">
        <view class="custom-nav__back" @tap.stop="openPopupModal">
          <tn-icon name="add-circle"></tn-icon>
        </view>
        <view class="" style="width: 52vw; overflow: hidden;">
          <tn-tabs v-model="current" activeColor="#000" bold fontSize="36">
            <tn-tabs-item
              v-for="(item, index) in scrollList"
              :key="index"
              :title="item.name"
            />
          </tn-tabs>
        </view>
      </view>
    </tn-navbar>

    <view
      class="partner-fixed"
      :style="{ paddingTop: vuex_custom_bar_height + 10 + 'px' }"
    >
      <!-- 数据信息 -->
      <view
        class="tn-info__container tn-flex tn-flex-wrap tn-flex-col-center tn-flex-row-between tn-margin-left tn-margin-right tn-margin-top tn-margin-bottom-sm"
      >
        <view
          class="tn-info__item group-card tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between"
          @click="tn('/partnerPages/group')"
        >
          <view
            class="tn-info__item__left tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-left"
          >
            <view class="tn-info__item__left__content">
              <view
                class="tn-info__item__left__content--title tn-text-bold tn-text-lg"
                >我的群聊</view
              >
              <view
                class="tn-info__item__left__content--data tn-padding-top-xs"
                style="color: #8a94a6"
                >{{ groupCount }} 个群聊,点击进入</view
              >
            </view>
          </view>
          <view class="tn-info__item__right">
            <view class="tn-info__item__right--icon tn-bg-blue--disabled">
              <view class="tn-icon-team tn-color-white"></view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view
      class="tn-margin-top-sm"
      :style="{ paddingTop: vuex_custom_bar_height + 40 + 'px' }"
    >
      <view class="" v-if="current == 0">
        <view id="list-anchor"></view>
        <tn-index-list :data="listData" :height="height">
          <template #default="{ data }">
            <view class="list-data" @click="openUser(data)">
              <image class="image" :src="data.avatar || defaultAvatar" mode="aspectFill" />
              <view class="info">
                <view class="username">{{ data.username }}</view>
                <view class="office tn-gray_text">{{ data.office }}</view>
              </view>
            </view>
          </template>
        </tn-index-list>
      </view>

      <view class="" v-if="current == 1">
        <view
          class="tn-flex tn-flex-col-center"
          style="margin: 50rpx 0rpx 50rpx 30rpx"
          v-for="item in orgList"
          :key="item.id"
          @click="openOrg(item)"
        >
          <view class="">
            <view
              class="icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-bg-blue--light tn-color-blue"
            >
              <view class="tn-icon-organizatio-fill"></view>
            </view>
          </view>
          <view class="tn-padding-left-sm" style="width: 67vw">
            <view class="tn-flex tn-flex-row-between tn-flex-col-between">
              <view class="justify-content-item">
                <text class="oa-black tn-text-xl">{{ item.name }}</text>
              </view>
            </view>
            <!-- <view class="tn-padding-top-xs tn-text-ellipsis">
              <text class="tn-color-gray tn-text-sm">6人</text>
            </view> -->
          </view>
          <view class="" style="width: 15vw">
            <text class="tn-color-gray tn-padding-right-xs">{{ item.count }}人</text>
            <text class="tn-color-gray tn-text-lg tn-icon-right"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 气泡弹框 -->
    <!-- popup会有延迟，采用这种方式来优化弹窗的优化体验-->
    <popup-modal v-model="showPopupModal"></popup-modal>

    <view class="tn-tabbar-height"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import PopupModal from "@/components/popup/popup.vue";
import config from "@/config";
import { getEmployeeList } from "@/api/employee";
import { getOrgTreeWithCount } from "@/api/org";
import { getContactSummary } from "@/api/contact";

import { useStore } from "vuex";

const store = useStore();
// 使用 computed 保持响应式
const vuex_custom_bar_height = computed(
  () => store.state.vuex_custom_bar_height,
);

const showPopupModal = ref(false);
const current = ref(0);
const scrollList = ref([{ name: "联系人" }, { name: "组织架构" }]);


const height = ref(500);
const defaultAvatar = "/static/author.jpg";
const employees = ref([]);
const organizations = ref([]);
const groupSummaryCount = ref(0);

const groupCount = computed(() => groupSummaryCount.value || orgList.value.length);

const contactList = computed(() => {
  return employees.value.map((item) => ({
    id: item.id,
    avatar: formatAvatar(item.avatar),
    username: item.name || item.employeeName || item.employeeNo || "未命名员工",
    office: item.deptName || item.companyName || item.position || item.post || "未设置部门",
    raw: item,
  }));
});

const listData = computed(() => groupContacts(contactList.value));

const orgList = computed(() => flattenOrganizations(organizations.value));

// 跳转
const tn = (e) => {
  if (!e) return;
  uni.navigateTo({
    url: e,
  });
};

const openUser = (data) => {
  if (data?.id) {
    uni.navigateTo({
      url: `/partnerPages/user?id=${data.id}`,
    });
    return;
  }
};

// 查看部门成员
const openOrg = (item) => {
  if (!item?.id) return;
  uni.navigateTo({
    url: `/partnerPages/dept-members?id=${item.id}&name=${encodeURIComponent((item.name || '').trim())}`,
  });
};

// tab选项卡切换
const tabChange = (index) => {
  current.value = index;
};

// 打开信息弹框
const openPopupModal = () => {
  showPopupModal.value = true;
};

const getSystemHeight = () => {
  const sysInfo = uni.getSystemInfoSync();
  height.value = sysInfo.windowHeight - 330;
};

const formatAvatar = (avatar) => {
  if (!avatar) return defaultAvatar;
  if (/^https?:\/\//.test(avatar) || avatar.startsWith("/static")) return avatar;
  return config.baseUrl + avatar;
};

const getFirstLetter = (name = "") => {
  const first = String(name).trim().charAt(0);
  if (!first) return "#";
  if (/[a-zA-Z]/.test(first)) return first.toUpperCase();

  const map = {
    陈: "C",
    蔡: "C",
    丁: "D",
    邓: "D",
    付: "F",
    冯: "F",
    高: "G",
    郭: "G",
    何: "H",
    黄: "H",
    胡: "H",
    李: "L",
    林: "L",
    刘: "L",
    罗: "L",
    马: "M",
    彭: "P",
    钱: "Q",
    沈: "S",
    宋: "S",
    孙: "S",
    唐: "T",
    王: "W",
    吴: "W",
    徐: "X",
    许: "X",
    杨: "Y",
    叶: "Y",
    张: "Z",
    赵: "Z",
    郑: "Z",
    周: "Z",
  };
  return map[first] || "#";
};

const groupContacts = (list) => {
  const groups = {};
  list.forEach((item) => {
    const key = getFirstLetter(item.username);
    if (!groups[key]) {
      groups[key] = {
        title: key,
        data: [],
      };
    }
    groups[key].data.push(item);
  });

  return Object.keys(groups)
    .sort((a, b) => {
      if (a === "#") return 1;
      if (b === "#") return -1;
      return a.localeCompare(b);
    })
    .reduce((result, key) => {
      result[key.toLowerCase()] = groups[key];
      return result;
    }, {});
};

const flattenOrganizations = (list = [], level = 0) => {
  return list.reduce((result, item) => {
    result.push({
      id: item.id,
      name: `${"　".repeat(level)}${item.unitName || item.shortName || "未命名组织"}`,
      count: item.employeeCount || 0,
    });
    if (Array.isArray(item.children) && item.children.length) {
      result.push(...flattenOrganizations(item.children, level + 1));
    }
    return result;
  }, []);
};

const normalizeList = (data) => {
  if (Array.isArray(data)) return data;
  return data?.records || data?.rows || data?.list || [];
};

const loadContacts = async () => {
  try {
    const res = await getEmployeeList({ status: 1 });
    employees.value = normalizeList(res.data);
  } catch (error) {
    console.log("加载通讯录失败", error);
  }
};

const loadOrganizations = async () => {
  try {
    const res = await getOrgTreeWithCount();
    organizations.value = normalizeList(res.data);
  } catch (error) {
    console.log("加载组织架构失败", error);
  }
};

const loadContactSummary = async () => {
  try {
    const res = await getContactSummary();
    groupSummaryCount.value = Number(res.data?.groupCount || 0);
  } catch (error) {
    console.log("加载通讯录摘要失败", error);
  }
};

onMounted(() => {
  getSystemHeight();
});

// tab 页常驻内存,每次切到通讯录都刷新,保证群数/成员/组织架构是最新的
onShow(() => {
  loadContactSummary();
  loadContacts();
  loadOrganizations();
});
</script>

<style lang="scss" scoped>
.template-partner {
  max-height: 100vh;
  max-width: 640px;
  margin: 0 auto;
}

/* 自定义导航栏内容 start */
.custom-nav {
  height: 100%;
  max-width: 640px;

  &__back {
    margin: auto 5rpx;
    font-size: 45rpx;
    margin-right: 10rpx;
    margin-left: 30rpx;
    flex-basis: 5%;
  }
}
/* 自定义导航栏内容 end */

/* 新增OA色系，自行调用，或者拿色值去用，多种方式*/
.oa-black {
  color: #1d2541;
}
.oa-blue {
  color: #4b98fe;
}
.oa-orangeyellow {
  color: #ffac00;
}
.oa-green {
  color: #00d05e;
}
.oa-orange {
  color: #fe871b;
}
.oa-cyan {
  color: #00c8b0;
}
.oa-indigo {
  color: #00b9fe;
}
.oa-orangered {
  color: #fb6a67;
}
.oa-purple {
  color: #957bfe;
}

/* 底部安全边距 start*/
.tn-tabbar-height {
  min-height: 120rpx;
  height: calc(140rpx + env(safe-area-inset-bottom));
  height: calc(140rpx + constant(safe-area-inset-bottom));
}

.partner-fixed {
  max-width: 640px;
  position: fixed;
  background-color: rgba(255, 255, 255, 1);
  top: 0;
  width: 100%;
  transition: all 0.25s ease-out;
  z-index: 10037;
}

.index-list-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  color: $tn-font-color;
  font-size: 28rpx;

  &__image {
    width: 100rpx;
    height: 100rpx;
    margin: 8rpx 8rpx;
    margin-right: 10rpx;
  }
}

/* OA色系*/
.oa-black {
  color: #1d2541;
}

/* 信息展示 start */
.tn-info {
  z-index: 9999;

  &__container {
    margin-top: 0rpx;
  }

  &__item {
    width: 48%;
    margin: 15rpx 0rpx;
    padding: 36rpx 30rpx;
    border-radius: 15rpx;

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
      background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/6.png);
    }

    &__left {
      &--icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        font-size: 40rpx;
        margin-right: 20rpx;
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
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg5.png);
        }
      }

      &__content {
        // font-size: 32rpx;

        &--data {
          margin-top: 5rpx;
        }
      }
    }

    &__right {
      &--icon {
        position: absolute;
        right: 0rpx;
        bottom: 0rpx;
        font-size: 60rpx;
        width: 98rpx;
        height: 78rpx;
        text-align: center;
        line-height: 80rpx;
        border-radius: 20rpx 0 20rpx 6rpx;
        // opacity: 0.15;

        &::after {
          content: " ";
          position: absolute;
          z-index: -1;
          right: 16rpx;
          bottom: 0rpx;
          width: 108rpx;
          height: 56rpx;
          background-color: inherit;
          border-radius: inherit;
          opacity: 1;
          border-radius: 20rpx 0 20rpx 0;
          opacity: 0.55;
        }
      }
    }
  }
}
/* 群聊卡片 */
.group-card {
  width: 100%;
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid #EEF0F4;
}

.group-card .tn-info__item__left__content--title {
  color: #1d2541;
}

.group-card .tn-info__item__left__content--data {
  color: #8a94a6;
  opacity: 1;
}

.group-card::after {
  display: none;
}

.group-card .tn-info__item__right--icon {
  color: #ffffff !important;
}

/* 信息展示 end */

/* 图标容器15 start */
.icon15 {
  &__item {
    width: 30%;
    background-color: #ffffff;
    padding: 30rpx;
    margin: 20rpx 10rpx;
    transform: scale(1);
    transition: transform 0.3s linear;
    transform-origin: center center;

    &--icon {
      width: 90rpx;
      height: 90rpx;
      font-size: 60rpx;
      border-radius: 20rpx;
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
  border-radius: 20rpx;
  overflow: hidden;
  // background-color: #FFFFFF;
}

/* 列表数据样式 start */
.list-data {
  display: flex;
  padding: 30rpx;

  .image {
    width: 90rpx;
    height: 90rpx;
    border-radius: 15rpx;
  }
  .info {
    margin-left: 20rpx;
    line-height: 1;
    .username {
      font-size: 34rpx;
    }
    .office {
      margin-top: 18rpx;
      font-size: 26rpx;
    }
  }
}
/* 列表数据样式 end */
</style>
