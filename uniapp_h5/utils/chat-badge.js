import store from '@/store'
import { getUnreadTotal } from '@/api/chat'

/**
 * 刷新全局聊天未读角标(tabbar 首页角标 + 首页"消息"快捷入口)
 *
 * 取值链路:tabbar 角标来自 store.state.unreadBadge.chatUnread,
 * 该值只在首页 loadHomeData 里通过 getUnreadTotal 同步;
 * 聊天页离开时调用本方法,避免角标一直停留到下次首页刷新才清零。
 * @returns {Promise} 失败时静默(角标维持旧值,不打断页面)
 */
export function refreshChatUnreadBadge() {
  return getUnreadTotal()
    .then((res) => {
      store.commit('SET_UNREAD_BADGE', { chatUnread: Number(res.data || 0) })
    })
    .catch(() => {})
}
