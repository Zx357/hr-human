import config from '@/config'
import { getToken } from '@/utils/auth'

/**
 * 聊天 WebSocket 连接管理（uni.connectSocket 统一 API，H5 与微信小程序双端兼容）
 *
 * 服务端端点：ws(s)://<host>/api/ws/chat?token=<JWT>
 * 服务端协议（JSON 文本帧）：
 *   - {"type":"chat_message","conversationId":"S123"|"G45","message":{...}} 新消息
 *   - {"type":"unread_total","total":n}                                     未读总数变更
 *   - {"type":"ping"}                                                       服务端心跳
 * 客户端应答：{"type":"pong"}（其余消息类型通过 onChatMessage(type, handler) 分发给页面）
 *
 * 行为约定：
 *   - token 为空（未登录/登录失效）时不发起连接；
 *   - 连接失败/断开后按指数退避自动重连（1s 起，封顶 30s，累计 MAX_RECONNECT_ATTEMPTS 次后放弃），
 *     放弃后页面侧 isChatSocketOpen() 返回 false，自动回退到 4 秒轮询兜底；
 *   - closeChatSocket() 为页面主动关闭（离开页面/切后台），不触发重连。
 */

const STATUS_CLOSED = 'closed'
const STATUS_CONNECTING = 'connecting'
const STATUS_OPEN = 'open'

let socketTask = null
let status = STATUS_CLOSED
let manuallyClosed = false
let reconnectAttempts = 0
let reconnectTimer = null

// 指数退避重连上限：放弃后由页面轮询兜底
const MAX_RECONNECT_ATTEMPTS = 6
const MAX_RECONNECT_DELAY = 30000

// 消息分发注册表：type -> Set<handler>
const messageHandlers = {}

function buildWsUrl() {
  const token = getToken()
  if (!token) return ''
  let base = String(config.baseUrl || '')
  // 生产 H5 配置的 baseUrl 为相对路径(/api)：用当前站点 origin 补全；
  // 小程序端无法解析相对地址，返回空串即不连接（走轮询兜底）
  if (base.startsWith('/')) {
    // #ifdef H5
    base = (typeof window !== 'undefined' && window.location ? window.location.origin : '') + base
    // #endif
    // #ifndef H5
    return ''
    // #endif
  }
  let url = base.replace(/^http/i, 'ws').replace(/\/+$/, '') + '/ws/chat'
  url += (url.indexOf('?') > -1 ? '&' : '?') + 'token=' + encodeURIComponent(token)
  return url
}

function setStatus(next) {
  status = next
}

function dispatch(data) {
  const handlers = messageHandlers[data.type]
  if (!handlers) return
  handlers.forEach((handler) => {
    try {
      handler(data)
    } catch (error) {
      console.error('[websocket] handler error:', error)
    }
  })
}

function onMessage(res) {
  if (typeof res.data !== 'string') return
  let data
  try {
    data = JSON.parse(res.data)
  } catch (error) {
    return
  }
  if (!data || !data.type) return
  // 服务端心跳：立即应答 pong（协议层保活），不向页面分发
  if (data.type === 'ping') {
    sendRaw({ type: 'pong' })
    return
  }
  if (data.type === 'pong') return
  dispatch(data)
}

function sendRaw(payload) {
  if (status !== STATUS_OPEN || !socketTask) return false
  try {
    socketTask.send({ data: JSON.stringify(payload), fail: () => {} })
    return true
  } catch (error) {
    return false
  }
}

function scheduleReconnect() {
  if (manuallyClosed) return
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    status = STATUS_CLOSED
    // 重连放弃，交由页面轮询兜底
    return
  }
  const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), MAX_RECONNECT_DELAY)
  reconnectAttempts += 1
  if (reconnectTimer) clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectChatSocket()
  }, delay)
}

function cleanupTask() {
  if (!socketTask) return
  socketTask = null
}

function onOpen() {
  setStatus(STATUS_OPEN)
  // 连接成功后重置退避计数，下次断开从 1s 重新开始
  reconnectAttempts = 0
}

function onErrorOrClose() {
  // error 与 close 可能先后触发（小程序端尤其如此），保证一次断开只处理一次
  if (!socketTask) return
  cleanupTask()
  if (manuallyClosed) {
    setStatus(STATUS_CLOSED)
    return
  }
  setStatus(STATUS_CONNECTING)
  scheduleReconnect()
}

/**
 * 建立聊天 WS 连接（幂等：已连接/连接中/重连排队中时直接返回）
 * @returns {boolean} 是否已发起（或已有）连接；token 为空/URL 无法构造时返回 false
 */
export function connectChatSocket() {
  if (!getToken()) return false
  if (reconnectTimer) return true // 已有重连排队，避免叠加重连定时器
  if (socketTask && (status === STATUS_OPEN || status === STATUS_CONNECTING)) return true

  const url = buildWsUrl()
  if (!url) return false

  manuallyClosed = false
  setStatus(STATUS_CONNECTING)
  // 不传 success/fail/complete 回调，保证 H5 与小程序均返回 SocketTask
  socketTask = uni.connectSocket({ url })
  if (!socketTask) {
    cleanupTask()
    scheduleReconnect()
    return false
  }
  socketTask.onOpen(onOpen)
  socketTask.onMessage(onMessage)
  socketTask.onError(onErrorOrClose)
  socketTask.onClose(onErrorOrClose)
  return true
}

/**
 * 主动关闭连接（离开聊天页/切后台时调用），不触发自动重连
 */
export function closeChatSocket() {
  manuallyClosed = true
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  reconnectAttempts = 0
  if (socketTask) {
    const task = socketTask
    cleanupTask()
    try {
      task.close({ code: 1000 })
    } catch (error) {
      // 已断开时 close 可能报错，忽略
    }
  }
  setStatus(STATUS_CLOSED)
}

/**
 * 连接状态查询：仅 STATUS_OPEN 视为在线
 */
export function isChatSocketOpen() {
  return status === STATUS_OPEN
}

/**
 * 注册消息处理器
 * @param {string} type 消息类型，如 'chat_message' / 'unread_total'（ping/pong 不分发）
 * @param {Function} handler 接收完整帧对象
 * @returns {Function} 取消注册函数
 */
export function onChatMessage(type, handler) {
  if (!messageHandlers[type]) {
    messageHandlers[type] = new Set()
  }
  messageHandlers[type].add(handler)
  return () => {
    const handlers = messageHandlers[type]
    if (handlers) handlers.delete(handler)
  }
}
