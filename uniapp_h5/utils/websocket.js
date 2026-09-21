import config from '@/config'
import { getToken } from '@/utils/auth'

/**
 * 聊天 WebSocket 全局单例管理器（uni.connectSocket 统一 API，H5 与微信小程序双端兼容）
 *
 * 服务端端点：ws(s)://<host>/api/ws/chat?token=<JWT>（token 走查询串为后端既有约定，见 buildWsUrl）
 * 服务端协议（JSON 文本帧）：
 *   - {"type":"chat_message","conversationId":"S123"|"G45","message":{...}} 新消息
 *   - {"type":"unread_total","total":n}                                     未读总数变更
 *   - {"type":"ping"}                                                       服务端心跳
 *   - {"type":"pong"}                                                       服务端对客户端 ping 的应答
 * 客户端协议：
 *   - 收到 {"type":"ping"} 应答 {"type":"pong"}
 *   - 每 30s 主动发 {"type":"ping"} 保活；超过 75s 未收到任何帧视为死链，主动断开重连
 *
 * App 级单例行为约定：
 *   - 登录成功（store Login）与启动时本地已有 token（App onLaunch）自动 connect()；
 *   - 页面 onShow 只需 connect()（幂等）+ on(type, cb) 订阅；onUnload/onHide 仅注销订阅，
 *     不断开连接（单例常驻，离开聊天页其他页仍能实时收到推送）；
 *   - 仅退出登录/会话过期（store 清理本地会话）时 disconnect()；
 *   - 断线按指数退避自动重连（1s 起，封顶 30s，累计 MAX_RECONNECT_ATTEMPTS 次后放弃），
 *     放弃后 isOpen() 返回 false，页面侧可回退轮询兜底；网络恢复（uni.onNetworkStatusChange）
 *     时重置计数自动再次尝试；
 *   - 多订阅者：on(type, handler) 按帧 type 分发（如 'chat_message' / 'unread_total'），
 *     type='*' 可订阅全部业务帧；ping/pong 协议帧不分发；
 *   - 非聊天业务帧（后端将来推 type='notification' 等）在按 type 分发之外，
 *     额外通过 uni.$emit('ws-notification', data) 全局广播，供任意页面监听。
 */

const STATUS_CLOSED = 'closed'
const STATUS_CONNECTING = 'connecting'
const STATUS_OPEN = 'open'

// 聊天域已知帧类型：其余业务类型（通知等）额外广播 ws-notification
const CHAT_FRAME_TYPES = ['chat_message', 'unread_total']

let socketTask = null
let status = STATUS_CLOSED
let manuallyClosed = false
let reconnectAttempts = 0
let reconnectTimer = null

// 客户端保活与死链检测
let heartbeatTimer = null
let watchdogTimer = null
let lastFrameAt = 0
const HEARTBEAT_INTERVAL = 30000   // 客户端 ping 间隔
const DEAD_LINK_THRESHOLD = 75000  // 超过该时长未收到任何帧视为死链（覆盖两次服务端 ping）

// 指数退避重连上限：放弃后由页面轮询兜底
const MAX_RECONNECT_ATTEMPTS = 6
const MAX_RECONNECT_DELAY = 30000

// 消息分发注册表：type -> Set<handler>（'*' 为全量帧订阅）
const messageHandlers = {}
const WILDCARD = '*'

// 网络恢复监听只注册一次
let networkListenerRegistered = false

/**
 * WS 连接 URL 构造（唯一收敛点）：token 走查询串（后端握手约定）
 */
function buildWsUrl() {
  const token = getToken()
  if (!token) return ''
  let base = String(config.baseUrl || '')
  // 兜底：相对地址仅 H5 可用（同源）；小程序端 config.js 已解析为绝对地址，仍相对则放弃连接
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
  if (handlers) {
    handlers.forEach((handler) => {
      try {
        handler(data)
      } catch (error) {
        console.error('[websocket] handler error:', error)
      }
    })
  }
  const wildcardHandlers = messageHandlers[WILDCARD]
  if (wildcardHandlers) {
    wildcardHandlers.forEach((handler) => {
      try {
        handler(data)
      } catch (error) {
        console.error('[websocket] wildcard handler error:', error)
      }
    })
  }
  // 非聊天业务帧（如将来的 type=notification）：全局广播，供任意页面监听
  if (!CHAT_FRAME_TYPES.includes(data.type)) {
    uni.$emit('ws-notification', data)
  }
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
  lastFrameAt = Date.now()
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

function startHeartbeat() {
  stopHeartbeat()
  lastFrameAt = Date.now()
  heartbeatTimer = setInterval(() => {
    if (status !== STATUS_OPEN) return
    sendRaw({ type: 'ping' })
  }, HEARTBEAT_INTERVAL)
  // 死链检测：连接看似在线但长时间收不到任何帧（半开连接）时主动断开触发重连
  watchdogTimer = setInterval(() => {
    if (status !== STATUS_OPEN) return
    if (Date.now() - lastFrameAt > DEAD_LINK_THRESHOLD) {
      forceReconnect('dead link')
    }
  }, 15000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
  if (watchdogTimer) {
    clearInterval(watchdogTimer)
    watchdogTimer = null
  }
}

function forceReconnect(reason) {
  if (manuallyClosed) return
  const task = socketTask
  socketTask = null
  setStatus(STATUS_CONNECTING)
  if (task) {
    try {
      task.close({ code: 4000, reason })
    } catch (error) {
      // 已断开时 close 可能报错，忽略
    }
  }
  scheduleReconnect()
}

function scheduleReconnect() {
  if (manuallyClosed) return
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    status = STATUS_CLOSED
    // 重连放弃，交由页面轮询兜底；网络恢复/重新 connect() 时会重置计数再试
    return
  }
  const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), MAX_RECONNECT_DELAY)
  reconnectAttempts += 1
  if (reconnectTimer) clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connect()
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
  startHeartbeat()
}

function onErrorOrClose() {
  // error 与 close 可能先后触发（小程序端尤其如此），保证一次断开只处理一次
  if (!socketTask) return
  cleanupTask()
  stopHeartbeat()
  if (manuallyClosed) {
    setStatus(STATUS_CLOSED)
    return
  }
  setStatus(STATUS_CONNECTING)
  scheduleReconnect()
}

function registerNetworkListener() {
  if (networkListenerRegistered) return
  networkListenerRegistered = true
  uni.onNetworkStatusChange((res) => {
    if (!res.isConnected) return
    if (manuallyClosed || !getToken()) return
    // 网络恢复：重置退避计数，立即尝试重连（幂等）
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      reconnectAttempts = 0
    }
    connect()
  })
}

/**
 * 建立 WS 连接（幂等：已连接/连接中/重连排队中时直接返回）
 * 登录成功与 App 启动（本地有 token）时自动调用；页面 onShow 亦可调用兜底重试。
 * @returns {boolean} 是否已发起（或已有）连接；token 为空/URL 无法构造时返回 false
 */
export function connect() {
  registerNetworkListener()
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
 * 主动关闭单例连接（仅退出登录/会话过期时调用），不触发自动重连。
 * 页面 onUnload/onHide 请勿调用，只应注销 on() 返回的取消订阅函数。
 */
export function disconnect() {
  manuallyClosed = true
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  reconnectAttempts = 0
  stopHeartbeat()
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
export function isOpen() {
  return status === STATUS_OPEN
}

/**
 * 注册消息处理器（多订阅者，同一 type 可注册多个）
 * @param {string} type 消息类型，如 'chat_message' / 'unread_total'，'*' 订阅全部业务帧（ping/pong 不分发）
 * @param {Function} handler 接收完整帧对象
 * @returns {Function} 取消注册函数
 */
export function on(type, handler) {
  if (!type || typeof handler !== 'function') return () => {}
  if (!messageHandlers[type]) {
    messageHandlers[type] = new Set()
  }
  messageHandlers[type].add(handler)
  return () => {
    const handlers = messageHandlers[type]
    if (handlers) handlers.delete(handler)
  }
}

/**
 * 通过当前连接发送 JSON 帧（如 {"type":"pong"} 等自定义控制帧）
 * @returns {boolean} 是否已发出；未在线时返回 false（调用方自行兜底）
 */
export function send(payload) {
  if (!payload || typeof payload !== 'object') return false
  return sendRaw(payload)
}
