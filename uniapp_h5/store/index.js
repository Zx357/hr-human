import { createStore } from 'vuex'
import user from './modules/user'
import getters from './getters'

// 尝试获取本地是否存在lifeData变量，第一次启动时不存在
let lifeData = {}
try {
  lifeData = uni.getStorageSync('lifeData')
} catch(e) {}

// 标记需要永久存储的变量，在每次启动时取出，在state中的变量名
const saveStateKeys = ['vuex_user']

// 保存变量到本地存储
const saveLifeData = (key, value) => {
  // 判断变量是否在存储数组中
  if (saveStateKeys.indexOf(key) !== -1) {
    // 获取本地存储的lifeData对象，将变量添加到对象中
    let tmpLifeData = uni.getStorageSync('lifeData')
    // 第一次启动时不存在，则放一个空对象
    tmpLifeData = tmpLifeData ? tmpLifeData : {}
    tmpLifeData[key] = value
    // 将变量再次放回本地存储中
    uni.setStorageSync('lifeData', tmpLifeData)
  }
}

const store = createStore({
  state: {
    // 如果上面从本地获取的lifeData对象下有对应的属性，就赋值给state中对应的变量
    // 加上vuex_前缀，是防止变量名冲突，也让人一目了然
    vuex_user: lifeData.vuex_user ? lifeData.vuex_user : { name: '图鸟' },
    
    // 如果vuex_version无需保存到本地永久存储，无需lifeData.vuex_version方式
    // app版本
    vuex_version: '1.0.0',
    // 是否使用自定义导航栏
    vuex_custom_nav_bar: true,
    // 状态栏高度
    vuex_status_bar_height: 0,
    // 自定义导航栏的高度
    vuex_custom_bar_height: 0,
    // 底部 tabbar 角标(聊天未读数 / 时光未读数 / 工作台待办数,0 不显示)
    unreadBadge: {
      chatUnread: 0,
      momentUnread: 0,
      workTodo: 0
    }
  },
  mutations: {
    // 更新 tabbar 角标
    SET_UNREAD_BADGE(state, payload) {
      state.unreadBadge = {
        ...state.unreadBadge,
        ...(payload || {})
      }
    },
    $tStore(state, payload) {
      // 判断是否多层调用，state中为对象存在的情况，例如user.info.score = 1
      const nameArr = payload.name.split('.')
      let saveKey = ''
      const len = nameArr.length
      
      if (len >= 2) {
        let obj = state[nameArr[0]]
        for (let i = 1; i < len - 1; i++) {
          obj = obj[nameArr[i]]
        }
        obj[nameArr[len - 1]] = payload.value
        saveKey = nameArr[0]
      } else {
        // 单层级变量
        state[payload.name] = payload.value
        saveKey = payload.name
      }
      
      // 保存变量到本地中
      saveLifeData(saveKey, state[saveKey])
    }
  },
  modules: {
    user
  },
  getters
})

export default store
