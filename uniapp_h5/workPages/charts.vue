<template>
  <view class="oa-content tn-safe-area-inset-bottom">
    <!-- 顶部自定义导航 -->
    <tn-navbar :placeholder="false" fixed bg-color="#ffffff00" :bottom-shadow="false"> </tn-navbar>
    <view
      class="top-info"
      :style="{ paddingTop: 90 + 'px' }"
    >
      <view
        class="tn-margin tn-padding-left-sm tn-padding-bottom-xl tn-padding-top tn-color-white tn-text-center"
      >
        <view class="">
          <text>参考总市值 (元)</text>
          <tn-icon
            name="eye"
            class="tn-icon-eye"
            style="opacity: 0.8; margin-left: 10rpx"
          ></tn-icon>
        </view>
        <view class="tn-text-bold tn-padding-top-xs" style="font-size: 70rpx"
          >129,086.82</view
        >
      </view>
      <view class="tn-color-white">
        <view class="tn-flex tn-flex-col-center tn-text-center">
          <view class="justify-content-item tn-flex-1 tn-flex-column">
            <view class="tn-text-sm" style="opacity: 0.6">持仓盈亏 (元)</view>
            <view class="tn-text-lg tn-text-bold tn-padding-top-xs"
              >+ 608.62</view
            >
          </view>
          <view class="justify-content-item tn-flex-1 tn-flex-column">
            <view class="tn-text-sm" style="opacity: 0.6">持仓份额 (份)</view>
            <view class="tn-text-lg tn-text-bold tn-padding-top-xs"
              >+ 26.09%</view
            >
          </view>
          <view class="justify-content-item tn-flex-1 tn-flex-column">
            <view class="tn-text-sm" style="opacity: 0.6">累计盈亏 (元)</view>
            <view class="tn-text-lg tn-text-bold tn-padding-top-xs"
              >1,506.22</view
            >
          </view>
        </view>
      </view>
    </view>

    <view class="">
      <view class="tn-padding-top">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            业务数据
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon name="down" class="tn-padding-right-xs"></tn-icon>
          </view>
        </view>

        <view
          class="tn-flex tn-flex-wrap tn-padding-bottom"
          style="margin: 15rpx"
        >
          <view
            v-for="(item, index) in tuniaoData"
            :key="index"
            style="width: 50%"
          >
            <view
              class="tn-flex tn-flex-row-between tn-flex-col-center tn-bg-gray--light"
              style="margin: 15rpx; padding: 30rpx 20rpx; border-radius: 15rpx"
            >
              <view class="justify-content-item">
                <view class="tn-flex tn-flex-col-center tn-flex-row-left">
                  <view
                    class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white"
                    :style="'background-color:' + item.color + ';'"
                  >
                    <tn-icon :name="item.icon"></tn-icon>
                  </view>
                  <view class="">
                    <view class="tn-padding-right tn-padding-left-sm">
                      <text style="font-size: 42rpx">{{ item.title }}</text>
                      <text
                        class="tn-text-sm tn-padding-left-xs"
                        style="opacity: 0.7"
                        >万</text
                      >
                    </view>
                    <view
                      class="tn-padding-right tn-padding-top-xs tn-text-sm tn-padding-left-sm tn-text-ellipsis tn-color-gray"
                    >
                      <text class="tn-padding-right-xs">较上年</text>
                      <text :style="'color:' + item.updown + ';'">{{
                        item.number
                      }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            营收占比
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs"
              >23年7月 - 24年6月</text
            >
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="ring"
            :opts="makeupOpts"
            :chartData="makeupChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 员工 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            集团成员
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="pie"
            :opts="memberOpts"
            :chartData="memberChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 营收 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            公司营收
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs"
              >23年7月 - 24年6月</text
            >
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="area"
            :opts="incomeOpts"
            :chartData="incomeChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 能力 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            员工能力
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="radar"
            :opts="abilityOpts"
            :chartData="abilityChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 年龄 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            员工年龄
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="scatter"
            :opts="ageOpts"
            :chartData="ageChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 分布 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            员工分布
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="map"
            :opts="placeOpts"
            :chartData="placeChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 业绩 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            区域业绩
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="bar"
            :opts="firmOpts"
            :chartData="firmChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 转化 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            用户转化
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="funnel"
            :opts="convertOpts"
            :chartData="convertChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 终端 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            用户终端
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="rose"
            :opts="terminalOpts"
            :chartData="terminalChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 活跃 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            活跃用户
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="column"
            :opts="activeOpts"
            :chartData="activeChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 流量 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            流量来源
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="bubble"
            :opts="flowOpts"
            :chartData="flowChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 系统 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            系统性能
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="gauge"
            :opts="systemOpts"
            :chartData="systemChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 全勤 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            出勤请假
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="arcbar"
            :opts="workOpts"
            :chartData="workChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 贸易 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            贸易数量
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="mount"
            :opts="saleOpts"
            :chartData="saleChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 股价 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            公司股价
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs"
              >23年1月 - 23年6月</text
            >
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="candle"
            :opts="stockOpts"
            :chartData="stockChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 收支 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            收入支出
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs"
              >23年7月 - 24年3月</text
            >
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="column"
            :opts="monthOpts"
            :chartData="monthChartData"
          />
        </view>
      </view>

      <view class="tn-strip-bottom"></view>

      <!-- 词云 -->
      <view class="tn-padding-top-xl tn-padding-bottom-xl">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-margin tn-text-bold tn-text-lg">
            年度词云
          </view>
          <view
            class="justify-content-item tn-margin-right tn-padding-xs tn-text-sm tn-bg-gray--light tn-radius tn-color-gray--dark"
          >
            <text class="tn-padding-left-xs tn-padding-right-xs">2023年</text>
            <tn-icon
              name="down"
              class="tn-icon-down tn-padding-right-xs"
            ></tn-icon>
          </view>
        </view>
        <view class="" style="min-height: 500rpx">
          <qiun-data-charts
            canvas2d
            type="word"
            :opts="wordOpts"
            :chartData="wordChartData"
          />
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
import mapdata from "@/mockdata/mapdata.json";
import { ref } from "vue";
import { onReady } from "@dcloudio/uni-app";
import { useCustomBarHeight, useGoBack } from "@/libs/composables";
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight();
const { goBack } = useGoBack();

const tuniaoData = ref([
  {
    title: "1,209",
    icon: "cube-fill",
    color: "#FB6A67",
    number: "+21%",
    updown: "#00D05E",
  },
  {
    title: "609",
    icon: "cube-fill",
    color: "#FFAC00",
    number: "+18%",
    updown: "#00D05E",
  },
  {
    title: "9,613",
    icon: "cube-fill",
    color: "#957BFE",
    number: "-12%",
    updown: "#FB6A67",
  },
  {
    title: "3,822",
    icon: "cube-fill",
    color: "#4B98FE",
    number: "+16%",
    updown: "#00D05E",
  },
  {
    title: "609",
    icon: "cube-fill",
    color: "#00C8B0",
    number: "+22%",
    updown: "#00D05E",
  },
  {
    title: "9,613",
    icon: "cube-fill",
    color: "#00B9FE",
    number: "-16%",
    updown: "#FB6A67",
  },
  {
    title: "3,822",
    icon: "cube-fill",
    color: "#F674D6",
    number: "+21%",
    updown: "#00D05E",
  },
  {
    title: "3,822",
    icon: "cube-fill",
    color: "#FE871B",
    number: "+18%",
    updown: "#00D05E",
  },
]);

const memberChartData = ref({});
const memberOpts = ref({
  rotate: false,
  rotateLock: false,
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 10, 15, 10],
  dataLabel: true,
  enableScroll: false,
  legend: { show: true, position: "left", lineHeight: 32 },
  title: { name: "", fontSize: 15, color: "#666666" },
  subtitle: { name: "", fontSize: 25, color: "#4B98FE" },
  extra: {
    pie: {
      activeOpacity: 0.5,
      activeRadius: 10,
      offsetAngle: 0,
      labelWidth: 15,
      border: true,
      borderWidth: 2,
      borderColor: "#FFFFFF",
    },
  },
});
const systemChartData = ref({});
const systemOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: true,
  title: { name: "686", fontSize: 32, color: "#4B98FE", offsetY: 0 },
  subtitle: { name: "综合评分", fontSize: 12, color: "#4B98FE", offsetY: 6 },
  extra: {
    gauge: {
      type: "progress",
      width: 26,
      labelColor: "#E6E6E6",
      labelOffset: 50,
      startAngle: 0.75,
      endAngle: 0.25,
      startNumber: 0,
      endNumber: 100,
      labelFormat: "",
      splitLine: {
        fixRadius: -30,
        splitNumber: 10,
        width: 40,
        color: "#FFFFFF",
        childNumber: 5,
        childWidth: 12,
      },
      pointer: { width: 16, color: "auto" },
    },
  },
});
const flowChartData = ref({});
const flowOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  xAxis: { disableGrid: true, splitNumber: 4, min: 0, max: 260 },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { show: true, position: "top", float: "right" },
  extra: { bubble: { border: 0.4, opacity: 0.5 } },
});
const wordChartData = ref({});
const wordOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: undefined,
  enableScroll: false,
  extra: { word: { type: "normal", autoColors: false } },
});
const saleChartData = ref({});
const saleOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  xAxis: { disableGrid: true },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { show: true, position: "top", float: "right" },
  extra: { mount: { type: "sharp", widthRatio: 1.2 } },
});
const abilityChartData = ref({});
const abilityOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 10, 15, 10],
  dataLabel: false,
  enableScroll: false,
  legend: { show: true, position: "left", lineHeight: 32 },
  extra: {
    radar: {
      gridType: "circle",
      gridColor: "#E6E6E6",
      gridCount: 2,
      opacity: 0.2,
      max: 100,
      labelShow: true,
    },
  },
});
const firmChartData = ref({});
const firmOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  xAxis: { disableGrid: true },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
  },
  legend: { show: false, position: "top", float: "right" },
  extra: {
    bar: {
      type: "stack",
      width: 16,
      meterBorde: 1,
      meterFillColor: "#FFFFFF",
      activeBgColor: "#000000",
      activeBgOpacity: 0.04,
      categoryGap: 2,
    },
  },
});
const ageChartData = ref({});
const ageOpts = ref({
  color: [
    "#4B98FE",
    "#FB6A67",
    "#FFAC00",
    "#00D05E",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  enableScroll: false,
  xAxis: { disableGrid: true },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { lineHeight: 25 },
  extra: { scatter: {} },
});
const activeChartData = ref({});
const activeOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  xAxis: { disableGrid: true },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { show: true, position: "top", float: "right" },
  extra: {
    column: {
      type: "stack",
      width: 16,
      activeBgColor: "#000000",
      activeBgOpacity: 0.04,
    },
  },
});
const terminalChartData = ref({});
const terminalOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 10, 15, 10],
  enableScroll: false,
  legend: { show: true, position: "left", lineHeight: 32 },
  extra: {
    rose: {
      type: "radius",
      minRadius: 50,
      activeOpacity: 0.5,
      activeRadius: 10,
      offsetAngle: 0,
      labelWidth: 15,
      border: true,
      borderWidth: 2,
      borderColor: "#FFFFFF",
    },
  },
});
const placeChartData = ref({});
const placeOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [0, 15, 0, 0],
  dataLabel: false,
  enableScroll: false,
  extra: {
    map: {
      mercator: true,
      border: true,
      borderWidth: 0.4,
      borderColor: "#AAAAAA",
      fillOpacity: 0.5,
      activeBorderColor: "#FFFFFF",
      activeFillColor: "#4B98FE",
      activeFillOpacity: 1,
    },
  },
});
const workChartData = ref({});
const workOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: undefined,
  title: { name: "82.46%", fontSize: 20, color: "#4B98FE" },
  subtitle: { name: "全勤率", fontSize: 12, color: "#4B98FE", offsetY: 6 },
  extra: {
    arcbar: {
      type: "circle",
      width: 8,
      backgroundColor: "#F8F7F8",
      startAngle: 1.5,
      endAngle: 0.25,
      gap: 2,
    },
  },
});
const stockChartData = ref({});
const stockOpts = ref({
  rotate: false,
  rotateLock: false,
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  touchMoveLimit: 24,
  enableScroll: true,
  enableMarkLine: true,
  xAxis: {
    labelCount: 6,
    itemCount: 36,
    disableGrid: true,
    scrollShow: true,
    scrollAlign: "left",
    scrollColor: "#E5E5E5",
    scrollBackgroundColor: "#F8F7F8",
  },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { show: true, position: "top", float: "right" },
  extra: {
    candle: {
      color: {
        upLine: "#FB6A67",
        upFill: "#FB6A67",
        downLine: "#00D05E",
        downFill: "#00D05E",
      },
      average: {
        show: true,
        name: ["TN01", "TN02", "TN09"],
        day: [5, 10, 20],
        color: ["#4B98FE", "#00C8B0", "#FFAC00"],
      },
    },
    markLine: {
      type: "dash",
      dashLength: 5,
      data: [
        { value: 2150, lineColor: "rgba(254,135,27,0)", showLabel: false },
        { value: 2350, lineColor: "rgba(254,135,27,0)", showLabel: false },
      ],
    },
    tooltip: { showCategory: true },
  },
});
const incomeChartData = ref({});
const incomeOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  xAxis: { disableGrid: true },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { show: false, position: "top", float: "right" },
  extra: {
    area: {
      type: "curve",
      opacity: 0.9,
      addLine: true,
      width: 2,
      gradient: true,
    },
  },
});
const monthChartData = ref({});
const monthOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  dataLabel: false,
  xAxis: { disableGrid: true },
  yAxis: {
    gridColor: "rgba(230,230,230,0.6)",
    disabled: false,
    disableGrid: false,
    gridType: "dash",
    dashLength: "4",
    data: [{ axisLineColor: "#FFFFFF" }],
  },
  legend: { show: true, position: "top", float: "right" },
  extra: {
    column: {
      type: "group",
      width: 16,
      activeBgColor: "#000000",
      activeBgOpacity: 0.04,
    },
  },
});
const convertChartData = ref({});
const convertOpts = ref({
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 15, 0, 15],
  enableScroll: false,
  extra: {
    funnel: {
      activeOpacity: 0.5,
      activeWidth: 10,
      border: true,
      borderWidth: 2,
      borderColor: "#FFFFFF",
      fillOpacity: 1,
      labelAlign: "right",
    },
  },
});
const makeupChartData = ref({});
const makeupOpts = ref({
  rotate: false,
  rotateLock: false,
  color: [
    "#4B98FE",
    "#00D05E",
    "#FFAC00",
    "#FB6A67",
    "#957BFE",
    "#00B9FE",
    "#FE871B",
    "#00C8B0",
    "#F674D6",
  ],
  padding: [15, 10, 15, 10],
  dataLabel: true,
  enableScroll: false,
  legend: { show: true, position: "left", lineHeight: 32 },
  title: { name: "", fontSize: 15, color: "#666666" },
  subtitle: { name: "", fontSize: 25, color: "#4B98FE" },
  extra: {
    ring: {
      ringWidth: 30,
      activeOpacity: 0.5,
      activeRadius: 10,
      offsetAngle: 0,
      labelWidth: 15,
      border: true,
      borderWidth: 2,
      borderColor: "#FFFFFF",
    },
  },
});

function drawMemberCharts() {
  memberChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        {
          data: [
            { name: "生产", value: 3000 },
            { name: "运维", value: 2600 },
            { name: "销售", value: 2200 },
            { name: "研发", value: 900 },
            { name: "管理", value: 600 },
          ],
        },
      ],
    }),
  );
}
function drawSystemCharts() {
  systemChartData.value = JSON.parse(
    JSON.stringify({
      categories: [
        { value: 0.2, color: "#4B98FE" },
        { value: 0.8, color: "#00D05E" },
        { value: 1, color: "#FB6A67" },
      ],
      series: [{ name: "综合评分", data: 0.686 }],
    }),
  );
}
function drawFlowCharts() {
  flowChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        { name: "抖音", data: [[210, 100, 50, "抖音"]] },
        { name: "微信", data: [[180, 40, 45, "微信"]] },
        { name: "快手", data: [[110, 40, 40, "快手"]] },
        { name: "小红书", data: [[60, 80, 30, "小红书"]] },
        { name: "其他", data: [[30, 20, 20, "其他"]] },
      ],
    }),
  );
}
function drawWordCharts() {
  wordChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        { name: "图鸟UI", textSize: 25, data: undefined },
        { name: "uniapp", textSize: 20, data: undefined },
        { name: "微信小程序", textSize: 20, data: undefined },
        { name: "H5", textSize: 20, data: undefined },
        { name: "APP", textSize: 20, data: undefined },
        { name: "vue2", textSize: 20, data: undefined },
        { name: "vue3", textSize: 20, data: undefined },
        { name: "组件库", textSize: 20, data: undefined },
        { name: "UI框架", textSize: 10, data: undefined },
        { name: "简约", textSize: 12, data: undefined },
        { name: "酷炫", textSize: 10, data: undefined },
        { name: "开源", textSize: 12, data: undefined },
        { name: "生态", textSize: 10, data: undefined },
        { name: "前端", textSize: 12, data: undefined },
        { name: "共赢", textSize: 12, data: undefined },
        { name: "合作", textSize: 12, data: undefined },
      ],
    }),
  );
}
function drawSaleCharts() {
  saleChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        {
          data: [
            { name: "广东", value: 82, legendShape: "circle" },
            { name: "湖北", value: 63, legendShape: "circle" },
            { name: "河南", value: 86, legendShape: "circle" },
            { name: "其他", value: 79, legendShape: "circle" },
          ],
        },
      ],
    }),
  );
}
function drawAbilityCharts() {
  abilityChartData.value = JSON.parse(
    JSON.stringify({
      categories: ["处事", "学习", "工作", "沟通", "领导", "决策"],
      series: [
        { name: "技术总监", data: [90, 86, 99, 66, 95, 93] },
        { name: "财务总监", data: [70, 56, 99, 89, 57, 69] },
      ],
    }),
  );
}
function drawFirmCharts() {
  firmChartData.value = JSON.parse(
    JSON.stringify({
      categories: ["广东", "上海", "福建", "江苏", "江西", "其他"],
      series: [
        {
          name: "区域业绩",
          data: [36, 24, 18, 12, 9, 6],
          legendShape: "circle",
        },
      ],
    }),
  );
}
function drawAgeCharts() {
  ageChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        {
          name: "男员工年龄工龄",
          data: [
            [1.5, 24],
            [3.9, 36],
            [6.3, 52],
            [3.3, 36],
            [1.7, 26],
            [7.2, 42],
            [5.0, 38],
            [8.3, 51],
            [1.2, 39],
            [0.6, 28],
            [9.1, 48],
          ],
        },
        {
          name: "女员工年龄工龄",
          data: [
            [4.3, 26],
            [2.4, 26],
            [5.2, 43],
            [0.5, 24],
            [6.6, 54],
            [3.2, 31],
            [8.0, 43],
            [5.4, 37],
            [2.9, 42],
            [4.6, 32],
            [5.1, 46],
            [7.2, 38],
          ],
        },
      ],
    }),
  );
}
function drawActiveCharts() {
  activeChartData.value = JSON.parse(
    JSON.stringify({
      categories: ["Android", "iPhone", "Win PC", "Mac PC", "其他"],
      series: [
        { name: "老用户", data: [66, 46, 31, 33, 13], legendShape: "circle" },
        { name: "新用户", data: [36, 27, 21, 24, 6], legendShape: "circle" },
        { name: "未知", data: [12, 6, 5, 6, 8], legendShape: "circle" },
      ],
    }),
  );
}
function drawTerminalCharts() {
  terminalChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        {
          data: [
            { name: "Android", value: 652 },
            { name: "iPhone", value: 508 },
            { name: "Win PC", value: 422 },
            { name: "Mac PC", value: 346 },
            { name: "其他", value: 262 },
            { name: "未知", value: 129 },
          ],
        },
      ],
    }),
  );
}
function drawConvertCharts() {
  convertChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        {
          data: [
            { name: "访问", value: 56 },
            { name: "登录", value: 31 },
            { name: "下单", value: 18 },
            { name: "付款", value: 12 },
            { name: "完成交易", value: 8 },
          ],
        },
      ],
    }),
  );
}
function drawPlaceCharts() {
  placeChartData.value = { series: mapdata.features };
}
function drawWorkCharts() {
  workChartData.value = JSON.parse(
    JSON.stringify({
      series: [{ name: "全勤率", color: "#4B98FE", data: 0.8246 }],
    }),
  );
}
function drawStockCharts() {
  const categories = [
    "1/24",
    "1/25",
    "1/28",
    "1/29",
    "1/30",
    "1/31",
    "2/1",
    "2/4",
    "2/5",
    "2/6",
    "2/7",
    "2/8",
    "2/18",
    "2/19",
    "2/20",
    "2/21",
    "2/22",
    "2/25",
    "2/26",
    "2/27",
    "2/28",
    "3/1",
    "3/4",
    "3/5",
    "3/6",
    "3/7",
    "3/8",
    "3/11",
    "3/12",
    "3/13",
    "3/14",
    "3/15",
    "3/18",
    "3/19",
    "3/20",
    "3/21",
    "3/22",
    "3/25",
    "3/26",
    "3/27",
    "3/28",
    "3/29",
    "4/1",
    "4/2",
    "4/3",
    "4/8",
    "4/9",
    "4/10",
    "4/11",
    "4/12",
    "4/15",
    "4/16",
    "4/17",
    "4/18",
    "4/19",
    "4/22",
    "4/23",
    "4/24",
    "4/25",
    "4/26",
    "5/2",
    "5/3",
    "5/6",
    "5/7",
    "5/8",
    "5/9",
    "5/10",
    "5/13",
    "5/14",
    "5/15",
    "5/16",
    "5/17",
    "5/20",
    "5/21",
    "5/22",
    "5/23",
    "5/24",
    "5/27",
    "5/28",
    "5/29",
    "5/30",
    "5/31",
    "6/3",
    "6/4",
    "6/5",
    "6/6",
    "6/7",
    "6/13",
  ];
  const data = [
    [2320.26, 2302.6, 2287.3, 2362.94],
    [2300, 2291.3, 2288.26, 2308.38],
    [2295.35, 2346.5, 2295.35, 2346.92],
    [2347.22, 2358.98, 2337.35, 2363.8],
    [2360.75, 2382.48, 2347.89, 2383.76],
    [2383.43, 2385.42, 2371.23, 2391.82],
    [2377.41, 2419.02, 2369.57, 2421.15],
    [2425.92, 2428.15, 2417.58, 2440.38],
    [2411, 2433.13, 2403.3, 2437.42],
    [2432.68, 2434.48, 2427.7, 2441.73],
    [2430.69, 2418.53, 2394.22, 2433.89],
    [2416.62, 2432.4, 2414.4, 2443.03],
    [2441.91, 2421.56, 2415.43, 2444.8],
    [2420.26, 2382.91, 2373.53, 2427.07],
    [2383.49, 2397.18, 2370.61, 2397.94],
    [2378.82, 2325.95, 2309.17, 2378.82],
    [2322.94, 2314.16, 2308.76, 2330.88],
    [2320.62, 2325.82, 2315.01, 2338.78],
    [2313.74, 2293.34, 2289.89, 2340.71],
    [2297.77, 2313.22, 2292.03, 2324.63],
    [2322.32, 2365.59, 2308.92, 2366.16],
    [2364.54, 2359.51, 2330.86, 2369.65],
    [2332.08, 2273.4, 2259.25, 2333.54],
    [2274.81, 2326.31, 2270.1, 2328.14],
    [2333.61, 2347.18, 2321.6, 2351.44],
    [2340.44, 2324.29, 2304.27, 2352.02],
    [2326.42, 2318.61, 2314.59, 2333.67],
    [2314.68, 2310.59, 2296.58, 2320.96],
    [2309.16, 2286.6, 2264.83, 2333.29],
    [2282.17, 2263.97, 2253.25, 2286.33],
    [2255.77, 2270.28, 2253.31, 2276.22],
    [2269.31, 2278.4, 2250, 2312.08],
    [2267.29, 2240.02, 2239.21, 2276.05],
    [2244.26, 2257.43, 2232.02, 2261.31],
    [2257.74, 2317.37, 2257.42, 2317.86],
    [2318.21, 2324.24, 2311.6, 2330.81],
    [2321.4, 2328.28, 2314.97, 2332],
    [2334.74, 2326.72, 2319.91, 2344.89],
    [2318.58, 2297.67, 2281.12, 2319.99],
    [2299.38, 2301.26, 2289, 2323.48],
    [2273.55, 2236.3, 2232.91, 2273.55],
    [2238.49, 2236.62, 2228.81, 2246.87],
    [2229.46, 2234.4, 2227.31, 2243.95],
    [2234.9, 2227.74, 2220.44, 2253.42],
    [2232.69, 2225.29, 2217.25, 2241.34],
    [2196.24, 2211.59, 2180.67, 2212.59],
    [2215.47, 2225.77, 2215.47, 2234.73],
    [2224.93, 2226.13, 2212.56, 2233.04],
    [2236.98, 2219.55, 2217.26, 2242.48],
    [2218.09, 2206.78, 2204.44, 2226.26],
    [2199.91, 2181.94, 2177.39, 2204.99],
    [2169.63, 2194.85, 2165.78, 2196.43],
    [2195.03, 2193.8, 2178.47, 2197.51],
    [2181.82, 2197.6, 2175.44, 2206.03],
    [2201.12, 2244.64, 2200.58, 2250.11],
    [2236.4, 2242.17, 2232.26, 2245.12],
    [2242.62, 2184.54, 2182.81, 2242.62],
    [2187.35, 2218.32, 2184.11, 2226.12],
    [2213.19, 2199.31, 2191.85, 2224.63],
    [2202.47, 2205.95, 2195.55, 2219.91],
    [2209.94, 2212.06, 2202.17, 2231.43],
    [2216.56, 2209.2, 2203.03, 2222.51],
    [2202.66, 2208.24, 2204.13, 2219.51],
    [2210.66, 2213.33, 2203.72, 2222.42],
    [2218.35, 2211.4, 2202.2, 2222.2],
    [2209.22, 2223.38, 2205.02, 2231.54],
    [2216.88, 2219.07, 2213.78, 2227.86],
    [2223.61, 2224.93, 2218.54, 2230.44],
    [2228.09, 2220.04, 2217.49, 2231.88],
    [2215.18, 2216.18, 2209.78, 2225.64],
    [2208.74, 2205.16, 2197.49, 2213.03],
    [2200.31, 2195.88, 2179.29, 2206.31],
    [2185.74, 2190.08, 2175.17, 2199.5],
    [2193.64, 2195.68, 2185.26, 2201.67],
    [2199.1, 2195.12, 2181.88, 2204.49],
    [2199.23, 2198.48, 2187.33, 2200.78],
    [2204.31, 2195.6, 2188.73, 2211.99],
    [2195.12, 2191.84, 2185.6, 2196.93],
    [2184.4, 2190.35, 2175.49, 2196.4],
    [2193.05, 2199.43, 2183.37, 2201.24],
    [2201.13, 2196.24, 2180.65, 2202.8],
    [2179.11, 2180.26, 2165.26, 2186.58],
    [2175.68, 2178.86, 2162.96, 2179.71],
    [2168.68, 2179.32, 2166.09, 2183.23],
    [2176.82, 2189.18, 2170.09, 2194.16],
    [2194.91, 2194.27, 2184.28, 2205.64],
    [2193.31, 2188.26, 2177.94, 2195.59],
    [2186.58, 2178.68, 2173.6, 2187.68],
    [2177.05, 2184.35, 2170.87, 2189.88],
    [2186.09, 2192.37, 2179.37, 2197.93],
    [2192.41, 2195.58, 2186.29, 2199.68],
    [2186.8, 2188.11, 2179.92, 2193.2],
    [2193.07, 2191.78, 2183.47, 2197.84],
    [2186.95, 2187.68, 2175.81, 2190.74],
    [2178.23, 2183.27, 2174.75, 2186.46],
  ];
  stockChartData.value = JSON.parse(
    JSON.stringify({ categories, series: [{ name: "上证指数", data }] }),
  );
}
function drawMakeupCharts() {
  makeupChartData.value = JSON.parse(
    JSON.stringify({
      series: [
        {
          data: [
            { name: "前端", value: 5000 },
            { name: "设计", value: 3000 },
            { name: "赞赏", value: 2000 },
            { name: "原型", value: 1800 },
            { name: "运维", value: 900 },
          ],
        },
      ],
    }),
  );
}
function drawIncomeCharts() {
  const categories = [
    "7月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "8月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "9月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "10月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "11月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "12月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "1月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "2月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "3月",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
  ];
  const data = [
    3000, 800, 2000, 4000, 5000, 8000, 9000, 10000, 9000, 8000, 6000, 2000,
    1000, 1800, 7600, 20000, 20555, 25455, 24555, 25454, 27678, 27453, 27432,
    27678, 32880, 32560, 36560, 47560, 62345, 66522, 64560, 66560, 53400, 43244,
    43355, 54440, 65550, 76660, 67770, 68880, 89990, 65500, 51110, 52220, 53330,
    54440, 55550, 56660, 57770, 48880, 49990, 41000, 41330, 41110, 42220, 43330,
    44440, 45550, 56660, 47770, 48880, 55990, 51110, 52220, 53555, 53330, 54440,
    55550, 56660, 57770, 65680, 68880,
  ];
  incomeChartData.value = JSON.parse(
    JSON.stringify({
      categories,
      series: [
        {
          name: "收益",
          legendShape: "circle",
          color: "#4B98FE",
          pointShape: "none",
          data,
        },
      ],
    }),
  );
}
function drawMonthCharts() {
  monthChartData.value = JSON.parse(
    JSON.stringify({
      categories: [
        "7月",
        "8月",
        "9月",
        "10月",
        "11月",
        "12月",
        "1月",
        "2月",
        "3月",
      ],
      series: [
        {
          name: "收入",
          data: [300, 1340, 2578, 3700, 6688, 8980, 5800, 4677, 3700],
          legendShape: "circle",
          color: "#4B98FE",
        },
        {
          name: "支出",
          data: [332, 1193, 1162, 1157, 1189, 2432, 1643, 1321, 1238],
          legendShape: "circle",
          color: "#FFAC00",
        },
      ],
    }),
  );
}

onReady(() => {
  drawMemberCharts();
  drawSystemCharts();
  drawFlowCharts();
  drawWordCharts();
  drawSaleCharts();
  drawAbilityCharts();
  drawFirmCharts();
  drawAgeCharts();
  drawActiveCharts();
  drawTerminalCharts();
  drawConvertCharts();
  drawPlaceCharts();
  drawWorkCharts();
  drawStockCharts();
  drawIncomeCharts();
  drawMonthCharts();
  drawMakeupCharts();
});

defineOptions({});
</script>
<style lang="scss" scoped>
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
  color: #ffffff;
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
    background-color: #ffffff;
  }
}
.oa-content {
  max-width: 640px;
  margin: 0 auto;
  min-height: 100vh;
  padding-bottom: 60rpx;
  padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
  padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
}

.top-info {
  position: relative;
  background: linear-gradient(90deg, #3668fc, #395acd);
  top: 0;
  width: 100%;
  height: 580rpx;
  transition: all 0.25s ease-out;
  z-index: -1;
}
.top-info:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  z-index: -1;
  mask-image: linear-gradient(to bottom, transparent, black);
  background: linear-gradient(90deg, #395acd, #3668fc);
}

.tn-strip-bottom {
  width: 100%;
  border-bottom: 20rpx solid #f8f7f8;
}
.tn-strip-bottom-min {
  width: 100%;
  border-bottom: 1rpx solid #f8f7f8;
}

.icon12__item {
  transform: scale(1);
  transition: transform 0.3s linear;
  transform-origin: center center;
  &--icon {
    width: 90rpx;
    height: 90rpx;
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
</style>
