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
          <view class="tn-padding-left-sm org-item__name">
            <view class="tn-flex tn-flex-row-between tn-flex-col-between">
              <view class="justify-content-item org-item__name-text">
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
  // 小程序端无拼音库/GB2312 编码接口,采用常用姓氏表覆盖(约300姓),其余归入 #
  const map = {
    陈: "C", 蔡: "C", 曹: "C", 崔: "C", 程: "C", 柴: "C", 岑: "C", 常: "C", 昌: "C", 车: "C",
    成: "C", 迟: "C", 仇: "C", 褚: "C", 丛: "C", 从: "C", 晁: "C", 苍: "C", 岑: "C",
    邓: "D", 丁: "D", 董: "D", 杜: "D", 戴: "D", 窦: "D", 段: "D", 党: "D", 东: "D", 都: "D",
    单: "D", 刁: "D", 独: "D", 达: "D", 代: "D", 丹: "D", 德: "D", 狄: "D", 顿: "D",
    鄂: "E", 尔: "E", 而: "E",
    冯: "F", 付: "F", 范: "F", 方: "F", 费: "F", 丰: "F", 封: "F", 符: "F", 傅: "F", 伏: "F",
    扶: "F", 福: "F", 樊: "F", 繁: "F", 房: "F", 方: "F",
    高: "G", 郭: "G", 葛: "G", 顾: "G", 关: "G", 龚: "G", 古: "G", 谷: "G", 甘: "G", 耿: "G",
    桂: "G", 戈: "G", 盖: "G", 国: "G", 公: "G", 官: "G", 广: "G", 过: "G", 光: "G", 归: "G", 淦: "G",
    何: "H", 黄: "H", 胡: "H", 韩: "H", 郝: "H", 侯: "H", 洪: "H", 华: "H", 霍: "H", 花: "H",
    惠: "H", 贺: "H", 呼: "H", 哈: "H", 海: "H", 汉: "H", 杭: "H", 合: "H", 和: "H", 衡: "H",
    恒: "H", 弘: "H", 扈: "H", 滑: "H", 化: "H", 皇: "H", 煌: "H", 回: "H", 会: "H", 邰: "T",
    蒋: "J", 金: "J", 贾: "J", 姜: "J", 江: "J", 纪: "J", 季: "J", 简: "J", 焦: "J", 解: "J",
    靳: "J", 经: "J", 景: "J", 巨: "J", 吉: "J", 籍: "J", 姬: "J", 嵇: "J", 及: "J", 郏: "J",
    荆: "J", 计: "J", 居: "J", 敬: "J", 竞: "J",
    孔: "K", 柯: "K", 康: "K", 况: "K", 寇: "K", 库: "K", 开: "K", 凯: "K", 亢: "K", 可: "K", 空: "K",
    李: "L", 林: "L", 刘: "L", 罗: "L", 梁: "L", 卢: "L", 雷: "L", 黎: "L", 骆: "L", 郎: "L",
    吕: "L", 蓝: "L", 练: "L", 廖: "L", 凌: "L", 柳: "L", 龙: "L", 楼: "L", 陆: "L", 路: "L",
    伦: "L", 芦: "L", 鲁: "L", 逯: "L", 赖: "L", 兰: "L", 岚: "L", 澜: "L", 乐: "L", 蕾: "L",
    磊: "L", 冷: "L", 里: "L", 丽: "L", 励: "L", 莉: "L", 联: "L", 廉: "L", 良: "L", 辽: "L",
    临: "L", 琳: "L", 玲: "L", 灵: "L", 令: "L", 琉: "L", 榴: "L", 隆: "L", 娄: "L", 泸: "L",
    露: "L", 栾: "L", 滦: "L", 仑: "L", 轮: "L", 沦: "L", 纶: "L", 萝: "L", 逻: "L", 洛: "L",
    泺: "L", 漯: "L", 雒: "L",
    马: "M", 毛: "M", 莫: "M", 孟: "M", 缪: "M", 麦: "M", 梅: "M", 蒙: "M", 米: "M", 苗: "M",
    明: "M", 牟: "M", 木: "M", 慕: "M", 满: "M", 迈: "M", 曼: "M", 茂: "M", 眉: "M", 枚: "M",
    湄: "M", 美: "M", 萌: "M", 梦: "M", 弥: "M", 谧: "M", 密: "M", 棉: "M", 淼: "M", 敏: "M",
    鸣: "M", 茗: "M", 铭: "M", 谟: "M", 墨: "M", 母: "M", 沐: "M", 穆: "M", 暮: "M",
    倪: "N", 聂: "N", 牛: "N", 宁: "N", 南: "N", 那: "N", 纳: "N", 耐: "N", 男: "N", 楠: "N",
    妮: "N", 泥: "N", 尼: "N", 霓: "N", 年: "N", 念: "N", 娘: "N", 农: "N", 诺: "N", 暖: "N",
    欧: "O", 区: "O", 讴: "O",
    潘: "P", 彭: "P", 皮: "P", 平: "P", 庞: "P", 裴: "P", 沛: "P", 朋: "P", 批: "P", 品: "P",
    聘: "P", 丕: "P", 篇: "P", 飘: "P", 苹: "P", 凭: "P", 萍: "P", 坡: "P", 泼: "P", 破: "P",
    魄: "P", 粕: "P", 蒲: "P", 浦: "P", 圃: "P", 普: "P", 谱: "P",
    钱: "Q", 秦: "Q", 齐: "Q", 邱: "Q", 乔: "Q", 祁: "Q", 强: "Q", 亓: "Q", 洽: "Q", 千: "Q",
    迁: "Q", 佥: "Q", 乾: "Q", 潜: "Q", 谦: "Q", 羌: "Q", 桥: "Q", 侨: "Q", 樵: "Q", 巧: "Q",
    俏: "Q", 切: "Q", 且: "Q", 妾: "Q", 怯: "Q", 窃: "Q", 亲: "Q", 侵: "Q", 钦: "Q", 芹: "Q",
    琴: "Q", 勤: "Q", 青: "Q", 卿: "Q", 轻: "Q", 倾: "Q", 清: "Q", 晴: "Q", 顷: "Q", 庆: "Q",
    穹: "Q", 丘: "Q", 秋: "Q", 求: "Q", 球: "Q", 曲: "Q", 屈: "Q", 蛆: "Q", 趋: "Q", 渠: "Q",
    取: "Q", 娶: "Q", 去: "Q", 趣: "Q", 圈: "Q", 全: "Q", 权: "Q", 泉: "Q", 拳: "Q", 犬: "Q",
    券: "Q", 劝: "Q", 缺: "Q", 却: "Q", 确: "Q", 雀: "Q", 群: "Q", 逡: "Q",
    任: "R", 阮: "R", 冉: "R", 荣: "R", 茹: "R", 芮: "R", 让: "R", 饶: "R", 惹: "R", 人: "R",
    仁: "R", 壬: "R", 刃: "R", 仞: "R", 认: "R", 日: "R", 戎: "R", 茸: "R", 容: "R", 柔: "R",
    如: "R", 儒: "R", 汝: "R", 入: "R", 软: "R", 锐: "R", 瑞: "R", 润: "R", 若: "R", 弱: "R",
    孙: "S", 宋: "S", 沈: "S", 施: "S", 苏: "S", 石: "S", 史: "S", 司: "S", 邵: "S", 沙: "S",
    申: "S", 尚: "S", 盛: "S", 师: "S", 时: "S", 佘: "S", 舒: "S", 束: "S", 水: "S", 税: "S",
    顺: "S", 说: "S", 硕: "S", 四: "S", 松: "S", 嵩: "S", 送: "S", 诵: "S", 搜: "S", 粟: "S",
    隋: "S", 岁: "S", 隼: "S", 唆: "S", 索: "S", 琐: "S", 锁: "S", 桑: "S", 商: "S", 赏: "S",
    上: "S", 少: "S", 舍: "S", 深: "S", 神: "S", 沈: "S", 胜: "S", 圣: "S", 诗: "S", 施: "S",
    十: "S", 什: "S", 世: "S", 仕: "S", 市: "S", 式: "S", 事: "S", 侍: "S", 势: "S", 视: "S",
    试: "S", 收: "S", 手: "S", 守: "S", 首: "S", 寿: "S", 受: "S", 书: "S", 殊: "S", 抒: "S",
    输: "S", 蔬: "S", 暑: "S", 黍: "S", 属: "S", 术: "S", 树: "S", 竖: "S", 帅: "S", 双: "S",
    谁: "S", 顺: "S", 舜: "S", 说: "S", 思: "S", 斯: "S", 私: "S", 松: "S", 宋: "S", 颂: "S",
    搜: "S", 苏: "S", 速: "S", 宿: "S", 塑: "S", 算: "S", 虽: "S", 随: "S", 岁: "S", 孙: "S",
    损: "S", 缩: "S", 所: "S", 琐: "S", 锁: "S",
    唐: "T", 汤: "T", 陶: "T", 谭: "T", 田: "T", 童: "T", 泰: "T", 坛: "T", 昙: "T", 檀: "T",
    谈: "T", 坦: "T", 叹: "T", 棠: "T", 塘: "T", 膛: "T", 螳: "T", 倘: "T", 淌: "T", 躺: "T",
    烫: "T", 趟: "T", 掏: "T", 涛: "T", 滔: "T", 绦: "T", 萄: "T", 逃: "T", 洮: "T", 桃: "T",
    讨: "T", 套: "T", 特: "T", 腾: "T", 誊: "T", 梯: "T", 剔: "T", 踢: "T", 锑: "T", 提: "T",
    题: "T", 蹄: "T", 啼: "T", 体: "T", 替: "T", 嚏: "T", 悌: "T", 逖: "T", 天: "T", 添: "T",
    甜: "T", 填: "T", 恬: "T", 挑: "T", 条: "T", 跳: "T", 铁: "T", 帖: "T", 厅: "T", 汀: "T",
    佟: "T", 彤: "T", 同: "T", 桐: "T", 铜: "T", 捅: "T", 统: "T", 痛: "T", 偷: "T", 头: "T",
    透: "T", 突: "T", 图: "T", 徒: "T", 途: "T", 涂: "T", 屠: "T", 土: "T", 吐: "T", 兔: "T",
    团: "T", 推: "T", 腿: "T", 退: "T", 吞: "T", 屯: "T", 臀: "T", 托: "T", 拖: "T", 脱: "T",
    驮: "T", 鸵: "T", 拓: "T", 沱: "T",
    王: "W", 吴: "W", 魏: "W", 万: "W", 汪: "W", 韦: "W", 温: "W", 伍: "W", 卫: "W", 文: "W",
    闻: "W", 乌: "W", 武: "W", 午: "W", 舞: "W", 务: "W", 雾: "W", 完: "W", 顽: "W", 挽: "W",
    晚: "W", 皖: "W", 惋: "W", 婉: "W", 万: "W", 亡: "W", 网: "W", 往: "W", 忘: "W", 旺: "W",
    望: "W", 危: "W", 威: "W", 微: "W", 为: "W", 违: "W", 围: "W", 帷: "W", 惟: "W", 维: "W",
    伟: "W", 伪: "W", 尾: "W", 纬: "W", 委: "W", 伟: "W", 未: "W", 位: "W", 味: "W", 畏: "W",
    胃: "W", 尉: "W", 慰: "W", 温: "W", 瘟: "W", 文: "W", 纹: "W", 蚊: "W", 稳: "W", 问: "W",
    翁: "W", 涡: "W", 窝: "W", 我: "W", 卧: "W", 握: "W", 沃: "W", 无: "W", 毋: "W", 五: "W",
    午: "W", 伍: "W", 侮: "W", 勿: "W", 物: "W", 务: "W", 误: "W", 悟: "W",
    徐: "X", 许: "X", 谢: "X", 萧: "X", 肖: "X", 夏: "X", 熊: "X", 席: "X", 向: "X", 项: "X",
    辛: "X", 邢: "X", 幸: "X", 匈: "X", 兄: "X", 雄: "X", 修: "X", 秀: "X", 绣: "X", 袖: "X",
    序: "X", 叙: "X", 绪: "X", 续: "X", 宣: "X", 玄: "X", 选: "X", 炫: "X", 学: "X", 薛: "X",
    雪: "X", 血: "X", 寻: "X", 巡: "X", 旬: "X", 讯: "X", 迅: "X", 西: "X", 吸: "X", 希: "X",
    析: "X", 息: "X", 悉: "X", 惜: "X", 稀: "X", 熙: "X", 析: "X", 喜: "X", 戏: "X", 系: "X",
    细: "X", 霞: "X", 下: "X", 先: "X", 纤: "X", 掀: "X", 鲜: "X", 闲: "X", 贤: "X", 弦: "X",
    咸: "X", 显: "X", 险: "X", 现: "X", 献: "X", 县: "X", 线: "X", 限: "X", 相: "X", 香: "X",
    箱: "X", 详: "X", 祥: "X", 翔: "X", 享: "X", 响: "X", 想: "X", 象: "X", 像: "X", 橡: "X",
    消: "X", 宵: "X", 涵: "X", 小: "X", 晓: "X", 孝: "X", 效: "X", 校: "X", 些: "X", 协: "X",
    邪: "X", 胁: "X", 斜: "X", 谐: "X", 写: "X", 泄: "X", 卸: "X", 屑: "X", 芯: "X", 锌: "X",
    新: "X", 心: "X", 信: "X", 芯: "X", 兴: "X", 星: "X", 刑: "X", 行: "X", 形: "X", 型: "X",
    醒: "X", 杏: "X", 姓: "X", 兴: "X", 凶: "X", 胸: "X", 休: "X", 朽: "X", 秀: "X", 墟: "X",
    戌: "X", 需: "X", 虚: "X", 须: "X", 徐: "X", 许: "X", 序: "X", 絮: "X", 蓄: "X", 悬: "X",
    旋: "X", 玄: "X", 雪: "X", 逊: "X", 压: "Y", 押: "Y", 鸦: "Y",
    杨: "Y", 叶: "Y", 严: "Y", 姚: "Y", 余: "Y", 阎: "Y", 燕: "Y", 尹: "Y", 袁: "Y", 岳: "Y",
    于: "Y", 应: "Y", 尤: "Y", 阳: "Y", 央: "Y", 秧: "Y", 羊: "Y", 扬: "Y", 洋: "Y", 仰: "Y",
    养: "Y", 样: "Y", 要: "Y", 耶: "Y", 也: "Y", 冶: "Y", 野: "Y", 业: "Y", 页: "Y", 一: "Y",
    医: "Y", 衣: "Y", 依: "Y", 仪: "Y", 宜: "Y", 姨: "Y", 移: "Y", 遗: "Y", 疑: "Y", 乙: "Y",
    已: "Y", 以: "Y", 蚁: "Y", 倚: "Y", 义: "Y", 亿: "Y", 忆: "Y", 艺: "Y", 议: "Y", 亦: "Y",
    异: "Y", 阴: "Y", 音: "Y", 银: "Y", 引: "Y", 隐: "Y", 印: "Y", 英: "Y", 樱: "Y", 鹰: "Y",
    迎: "Y", 赢: "Y", 影: "Y", 映: "Y", 硬: "Y", 拥: "Y", 佣: "Y", 永: "Y", 泳: "Y", 咏: "Y",
    勇: "Y", 用: "Y", 优: "Y", 忧: "Y", 由: "Y", 邮: "Y", 犹: "Y", 游: "Y", 有: "Y", 友: "Y",
    右: "Y", 幼: "Y", 淤: "Y", 鱼: "Y", 娱: "Y", 渔: "Y", 愉: "Y", 榆: "Y", 虞: "Y", 愚: "Y",
    与: "Y", 宇: "Y", 羽: "Y", 雨: "Y", 语: "Y", 玉: "Y", 育: "Y", 郁: "Y", 预: "Y", 驭: "Y",
    鸳: "Y", 渊: "Y", 元: "Y", 员: "Y", 园: "Y", 原: "Y", 圆: "Y", 援: "Y", 缘: "Y", 源: "Y",
    远: "Y", 怨: "Y", 院: "Y", 愿: "Y", 约: "Y", 月: "Y", 悦: "Y", 阅: "Y", 跃: "Y", 越: "Y",
    云: "Y", 匀: "Y", 允: "Y", 运: "Y", 韵: "Y", 蕴: "Y", 颜: "Y", 闫: "Y", 严: "Y", 燕: "Y",
    张: "Z", 赵: "Z", 周: "Z", 郑: "Z", 朱: "Z", 曾: "Z", 翟: "Z", 詹: "Z", 章: "Z", 长: "Z",
    昭: "Z", 折: "Z", 哲: "Z", 者: "Z", 锗: "Z", 真: "Z", 甄: "Z", 震: "Z", 镇: "Z", 正: "Z",
    之: "Z", 支: "Z", 只: "Z", 旨: "Z", 志: "Z", 制: "Z", 治: "Z", 中: "Z", 忠: "Z", 钟: "Z",
    仲: "Z", 重: "Z", 洲: "Z", 珠: "Z", 诸: "Z", 竹: "Z", 烛: "Z", 主: "Z", 祝: "Z", 著: "Z",
    卓: "Z", 子: "Z", 自: "Z", 宗: "Z", 邹: "Z", 祖: "Z", 左: "Z", 佐: "Z", 战: "Z", 展: "Z",
    占: "Z", 湛: "Z", 涨: "Z", 掌: "Z", 丈: "Z", 招: "Z", 兆: "Z", 照: "Z", 遮: "Z", 浙: "Z",
    珍: "Z", 斟: "Z", 砧: "Z", 针: "Z", 枕: "Z", 诊: "Z", 振: "Z", 直: "Z", 值: "Z", 职: "Z",
    执: "Z", 纸: "Z", 挚: "Z", 掷: "Z", 致: "Z", 智: "Z", 秩: "Z", 置: "Z", 终: "Z", 冢: "Z",
    众: "Z", 昼: "Z", 皱: "Z", 骤: "Z", 住: "Z", 助: "Z", 注: "Z", 贮: "Z", 驻: "Z", 撰: "Z",
    壮: "Z", 状: "Z", 撞: "Z", 追: "Z", 准: "Z", 捉: "Z", 浊: "Z", 兹: "Z", 资: "Z", 滋: "Z",
    紫: "Z", 字: "Z", 综: "Z", 总: "Z", 纵: "Z", 走: "Z", 奏: "Z", 租: "Z", 足: "Z", 族: "Z",
    阻: "Z", 组: "Z", 钻: "Z", 醉: "Z", 尊: "Z", 遵: "Z", 昨: "Z", 臧: "Z", 泽: "Z", 曾: "Z",
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
    uni.showToast({ icon: "none", title: "加载通讯录失败" });
  }
};

const loadOrganizations = async () => {
  try {
    const res = await getOrgTreeWithCount();
    organizations.value = normalizeList(res.data);
  } catch (error) {
    uni.showToast({ icon: "none", title: "加载组织架构失败" });
  }
};

const loadContactSummary = async () => {
  try {
    const res = await getContactSummary();
    groupSummaryCount.value = Number(res.data?.groupCount || 0);
  } catch (error) {
  }
};

onMounted(() => {
  getSystemHeight();
  // 首次挂载加载数据,后续由父页面切换/下拉时刷新
  loadContactSummary();
  loadContacts();
  loadOrganizations();
});

// 供 pages/index.vue 调用:刷新
defineExpose({
  refresh: async () => {
    await Promise.allSettled([
      loadContactSummary(),
      loadContacts(),
      loadOrganizations(),
    ]);
  },
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

/* 组织架构行名称截断 start */
.org-item__name {
  width: 67vw;
  min-width: 0;
}

.org-item__name-text {
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
}

.org-item__name-text text {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 组织架构行名称截断 end */

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
