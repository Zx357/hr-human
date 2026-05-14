<template>
  <view class="index-list-container">
    <!-- 左侧列表内容 -->
    <scroll-view
      class="index-list-scroll"
      scroll-y
      :scroll-top="scrollViewTop"
      :scroll-with-animation="true"
      @scroll="onScroll"
    >
      <view class="index-list-content">
        <view
          v-for="(group, groupIndex) in indexedData"
          :key="groupIndex"
          class="index-group"
          :id="`group-${group.letter}`"
        >
          <!-- 分组标题 -->
          <view class="index-group-title tn-bg-gray--light">
            {{ group.letter }}
          </view>
          
          <!-- 分组内容 -->
          <view class="index-group-content">
            <view
              v-for="(item, itemIndex) in group.data"
              :key="itemIndex"
              class="index-item tn-flex tn-flex-col-center"
              @click="onItemClick(item, group.letter, itemIndex)"
            >
              <!-- 头像 -->
              <view v-if="item.avatar || showAvatar" class="index-item-avatar">
                <image :src="item.avatar || defaultAvatar" mode="aspectFill" class="avatar-image" />
              </view>
              
              <!-- 信息 -->
              <view class="index-item-info">
                <view class="index-item-name tn-text-lg">{{ item.name }}</view>
                <view v-if="item.department" class="index-item-department tn-color-gray tn-text-sm">
                  {{ item.department }}
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 右侧索引栏 -->
    <view class="index-sidebar">
      <view
        v-for="(letter, index) in indexLetters"
        :key="index"
        class="index-letter"
        :class="{ 'index-letter-active': currentIndex === index }"
        @touchstart.prevent="onTouchStart(index)"
        @touchmove.prevent="onTouchMove"
        @touchend="onTouchEnd"
        @click="onLetterClick(index)"
      >
        {{ letter }}
      </view>
    </view>
    
    <!-- 索引提示 -->
    <view v-if="showIndexTip" class="index-tip">
      {{ indexLetters[currentIndex] }}
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, getCurrentInstance as getVueInstance } from 'vue'

const props = defineProps({
  // 列表数据，格式：[{ name: '姓名', department: '部门', avatar: '头像' }]
  list: {
    type: Array,
    default: () => []
  },
  // 是否显示头像
  showAvatar: {
    type: Boolean,
    default: false
  },
  // 默认头像
  defaultAvatar: {
    type: String,
    default: 'https://resource.tuniaokj.com/images/simple/image2.jpg'
  },
  // 自定义索引列表
  indexList: {
    type: Array,
    default: () => ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#']
  }
})

const emit = defineEmits(['select', 'indexSelect', 'itemClick'])

// 当前索引
const currentIndex = ref(0)
// 是否显示索引提示
const showIndexTip = ref(false)
// 滚动位置
const scrollViewTop = ref(0)
// 分组位置信息
const groupPositions = ref([])

// 获取姓名首字母
const getFirstLetter = (name) => {
  if (!name) return '#'
  const firstChar = name.charAt(0)
  // 判断是否为中文
  if (/[\u4e00-\u9fa5]/.test(firstChar)) {
    const pinyin = getPinyinFirstLetter(firstChar)
    return pinyin || '#'
  }
  // 判断是否为英文
  if (/[a-zA-Z]/.test(firstChar)) {
    return firstChar.toUpperCase()
  }
  return '#'
}

// 简单的中文转拼音首字母映射
const getPinyinFirstLetter = (char) => {
  const map = {
    '阿': 'A', '安': 'A', '艾': 'A',
    '巴': 'B', '白': 'B', '包': 'B', '班': 'B', '北': 'B', '贝': 'B', '毕': 'B', '卞': 'B',
    '蔡': 'C', '曹': 'C', '岑': 'C', '柴': 'C', '常': 'C', '陈': 'C', '程': 'C', '褚': 'C', '楚': 'C', '崔': 'C',
    '达': 'D', '代': 'D', '戴': 'D', '邓': 'D', '狄': 'D', '丁': 'D', '董': 'D', '杜': 'D', '段': 'D',
    '鄂': 'E', '恩': 'E',
    '范': 'F', '方': 'F', '房': 'F', '费': 'F', '冯': 'F', '符': 'F', '福': 'F', '付': 'F', '傅': 'F',
    '甘': 'G', '高': 'G', '葛': 'G', '耿': 'G', '宫': 'G', '龚': 'G', '顾': 'G', '关': 'G', '郭': 'G',
    '哈': 'H', '海': 'H', '韩': 'H', '杭': 'H', '郝': 'H', '何': 'H', '贺': 'H', '侯': 'H', '洪': 'H', '胡': 'H', '华': 'H', '黄': 'H', '霍': 'H',
    '姬': 'J', '吉': 'J', '纪': 'J', '季': 'J', '贾': 'J', '简': 'J', '江': 'J', '姜': 'J', '蒋': 'J', '焦': 'J', '金': 'J', '靳': 'J', '景': 'J',
    '卡': 'K', '康': 'K', '柯': 'K', '孔': 'K', '寇': 'K',
    '来': 'L', '兰': 'L', '郎': 'L', '雷': 'L', '黎': 'L', '李': 'L', '连': 'L', '廉': 'L', '梁': 'L', '廖': 'L', '林': 'L', '凌': 'L', '刘': 'L', '龙': 'L', '娄': 'L', '卢': 'L', '鲁': 'L', '陆': 'L', '路': 'L', '罗': 'L', '吕': 'L',
    '马': 'M', '麦': 'M', '茅': 'M', '毛': 'M', '梅': 'M', '孟': 'M', '米': 'M', '苗': 'M', '明': 'M', '莫': 'M', '缪': 'M', '穆': 'M',
    '那': 'N', '南': 'N', '倪': 'N', '聂': 'N', '宁': 'N', '牛': 'N',
    '欧': 'O', '区': 'O',
    '潘': 'P', '庞': 'P', '裴': 'P', '彭': 'P', '皮': 'P', '朴': 'P', '平': 'P',
    '戚': 'Q', '齐': 'Q', '祁': 'Q', '钱': 'Q', '乔': 'Q', '秦': 'Q', '邱': 'Q', '裘': 'Q', '曲': 'Q',
    '冉': 'R', '饶': 'R', '任': 'R',
    '沙': 'S', '山': 'S', '商': 'S', '尚': 'S', '邵': 'S', '申': 'S', '沈': 'S', '盛': 'S', '施': 'S', '石': 'S', '史': 'S', '舒': 'S', '司': 'S', '宋': 'S', '苏': 'S', '孙': 'S', '索': 'S',
    '塔': 'T', '台': 'T', '太': 'T', '谈': 'T', '汤': 'T', '唐': 'T', '陶': 'T', '滕': 'T', '田': 'T', '童': 'T', '屠': 'T',
    '万': 'W', '汪': 'W', '王': 'W', '韦': 'W', '魏': 'W', '卫': 'W', '尉': 'W', '温': 'W', '文': 'W', '翁': 'W', '巫': 'W', '乌': 'W', '吴': 'W', '伍': 'W', '武': 'W',
    '西': 'X', '希': 'X', '席': 'X', '夏': 'X', '项': 'X', '萧': 'X', '谢': 'X', '辛': 'X', '邢': 'X', '熊': 'X', '徐': 'X', '许': 'X', '薛': 'X',
    '严': 'Y', '闫': 'Y', '颜': 'Y', '杨': 'Y', '羊': 'Y', '姚': 'Y', '叶': 'Y', '伊': 'Y', '易': 'Y', '殷': 'Y', '尹': 'Y', '应': 'Y', '尤': 'Y', '于': 'Y', '余': 'Y', '俞': 'Y', '虞': 'Y', '宇': 'Y', '羽': 'Y', '郁': 'Y', '元': 'Y', '袁': 'Y', '岳': 'Y', '云': 'Y',
    '臧': 'Z', '曾': 'Z', '翟': 'Z', '詹': 'Z', '张': 'Z', '章': 'Z', '赵': 'Z', '甄': 'Z', '郑': 'Z', '钟': 'Z', '仲': 'Z', '周': 'Z', '朱': 'Z', '庄': 'Z', '卓': 'Z', '宗': 'Z', '邹': 'Z', '祖': 'Z'
  }
  return map[char] || '#'
}

// 计算属性：按字母分组的数据
const indexedData = computed(() => {
  const groups = {}
  
  // 初始化分组
  props.indexList.forEach(letter => {
    groups[letter] = []
  })
  
  // 将数据分配到对应分组
  props.list.forEach(item => {
    const letter = getFirstLetter(item.name)
    if (groups[letter]) {
      groups[letter].push(item)
    } else {
      groups['#'].push(item)
    }
  })
  
  // 转换为数组并过滤空分组
  return props.indexList
    .filter(letter => groups[letter].length > 0)
    .map(letter => ({
      letter,
      data: groups[letter]
    }))
})

// 计算属性：实际显示的索引字母
const indexLetters = computed(() => {
  return indexedData.value.map(group => group.letter)
})

// 点击索引
const onLetterClick = (index) => {
  currentIndex.value = index
  scrollToGroup(index)
  emit('indexSelect', indexLetters.value[index])
}

// 触摸开始
const onTouchStart = (index) => {
  currentIndex.value = index
  showIndexTip.value = true
  scrollToGroup(index)
  emit('indexSelect', indexLetters.value[index])
}

// 触摸移动
const onTouchMove = (e) => {
  const touch = e.touches[0]
  const query = uni.createSelectorQuery().in(instance)
  query.select('.index-sidebar').boundingClientRect()
  query.exec((res) => {
    if (res && res[0]) {
      const sidebarRect = res[0]
      const itemHeight = sidebarRect.height / indexLetters.value.length
      const offsetY = touch.clientY - sidebarRect.top
      let index = Math.floor(offsetY / itemHeight)
      index = Math.max(0, Math.min(index, indexLetters.value.length - 1))
      
      if (index !== currentIndex.value) {
        currentIndex.value = index
        scrollToGroup(index)
        emit('indexSelect', indexLetters.value[index])
      }
    }
  })
}

// 触摸结束
const onTouchEnd = () => {
  showIndexTip.value = false
}

// 滚动到指定分组
const scrollToGroup = (index) => {
  const letter = indexLetters.value[index]
  const groupId = `group-${letter}`
  
  const query = uni.createSelectorQuery().in(instance)
  query.select(`#${groupId}`).boundingClientRect()
  query.select('.index-list-scroll').boundingClientRect()
  query.exec((res) => {
    if (res && res[0] && res[1]) {
      const groupRect = res[0]
      const scrollRect = res[1]
      const scrollTop = groupRect.top - scrollRect.top + scrollViewTop.value
      scrollViewTop.value = scrollTop
    }
  })
}

// 滚动监听
const onScroll = (e) => {
  const scrollTop = e.detail.scrollTop
  
  // 计算当前所在分组
  for (let i = groupPositions.value.length - 1; i >= 0; i--) {
    if (scrollTop >= groupPositions.value[i].top) {
      if (currentIndex.value !== i) {
        currentIndex.value = i
      }
      break
    }
  }
}

// 点击列表项
const onItemClick = (item, letter, index) => {
  emit('itemClick', item, letter, index)
  emit('select', item)
}

// 组件实例
const instance = getVueInstance()

// 计算分组位置
const calcGroupPositions = () => {
  nextTick(() => {
    const query = uni.createSelectorQuery().in(instance)
    query.selectAll('.index-group').boundingClientRect()
    query.select('.index-list-content').boundingClientRect()
    query.exec((res) => {
      if (res && res[0] && res[1]) {
        const groups = res[0]
        const contentRect = res[1]
        groupPositions.value = groups.map((group, index) => ({
          index,
          top: group.top - contentRect.top,
          letter: indexedData.value[index]?.letter
        }))
      }
    })
  })
}

onMounted(() => {
  calcGroupPositions()
})

// 暴露方法
defineExpose({
  scrollToLetter: (letter) => {
    const index = indexLetters.value.indexOf(letter)
    if (index !== -1) {
      onLetterClick(index)
    }
  },
  getIndexedData: () => indexedData.value
})
</script>

<style lang="scss" scoped>
.index-list-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.index-list-scroll {
  width: 100%;
  height: 100%;
}

.index-list-content {
  padding-right: 60rpx;
}

.index-group {
  width: 100%;
}

.index-group-title {
  height: 60rpx;
  line-height: 60rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  color: #666;
  background-color: #F5F5F5;
}

.index-group-content {
  background-color: #fff;
}

.index-item {
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #F0F0F0;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background-color: #F5F5F5;
  }
}

.index-item-avatar {
  margin-right: 24rpx;
  
  .avatar-image {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
    background-color: #F0F0F0;
  }
}

.index-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.index-item-name {
  color: #333;
  font-weight: 500;
  margin-bottom: 6rpx;
}

.index-item-department {
  color: #999;
}

// 右侧索引栏
.index-sidebar {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  padding: 20rpx 0;
  z-index: 100;
}

.index-letter {
  width: 40rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  font-size: 22rpx;
  color: #666;
  font-weight: 500;
  
  &-active {
    color: #1D2541;
    font-weight: bold;
    background-color: rgba(29, 37, 65, 0.1);
    border-radius: 50%;
  }
}

// 索引提示
.index-tip {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  text-align: center;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 16rpx;
  color: #fff;
  font-size: 48rpx;
  font-weight: bold;
  z-index: 200;
}
</style>
