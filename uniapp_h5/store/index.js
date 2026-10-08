import { createStore } from 'vuex'
import user from './modules/user'
import getters from './getters'

const store = createStore({
  state: {
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
    // 更新状态栏/自定义导航栏高度(App 启动时落地,所有自定义导航页面依赖)
    SET_STATUS_BAR_HEIGHT(state, height) {
      state.vuex_status_bar_height = height
    },
    SET_CUSTOM_BAR_HEIGHT(state, height) {
      state.vuex_custom_bar_height = height
    }
  },
  modules: {
    user
  },
  getters
})

export default store
