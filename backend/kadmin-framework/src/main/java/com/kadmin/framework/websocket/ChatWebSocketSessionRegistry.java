package com.kadmin.framework.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天 WebSocket 会话注册表（单机内存实现，见 README 单机部署约定）
 *
 * employeeId -> 多个 WebSocketSession：同一员工多端（H5/小程序）同时在线时全部推送。
 * 线程安全：Map 与 Set 均为并发容器；对同一 session 的发送用 synchronized(session) 串行化，
 * 满足标准 WebSocket API "同一时刻仅允许一个线程写" 的约束。
 */
@Slf4j
@Component
public class ChatWebSocketSessionRegistry {

    private final Map<Long, Set<WebSocketSession>> sessionsByEmployee = new ConcurrentHashMap<>();

    /**
     * 注册会话（握手已写入 employeeId 属性）
     */
    public void register(WebSocketSession session) {
        Long employeeId = employeeIdOf(session);
        if (employeeId == null) {
            return;
        }
        sessionsByEmployee.computeIfAbsent(employeeId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.debug("聊天WS注册: employeeId={}, sessionId={}, 在线连接数={}",
                employeeId, session.getId(), sessionsByEmployee.get(employeeId).size());
    }

    /**
     * 注销会话（连接关闭/IO 异常时调用），空集合顺手清掉防泄漏
     */
    public void remove(WebSocketSession session) {
        Long employeeId = employeeIdOf(session);
        if (employeeId == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByEmployee.get(employeeId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByEmployee.remove(employeeId, sessions);
        }
        log.debug("聊天WS注销: employeeId={}, sessionId={}", employeeId, session.getId());
    }

    /**
     * 按 employeeId 推送 JSON 文本帧到该员工当前全部在线连接。
     * payload 为已序列化的 JSON 字符串（序列化由调用方用统一的 ObjectMapper 完成，避免二次编码）。
     *
     * @return 成功送达的连接数（无在线连接或全部失败返回 0）
     */
    public int sendToEmployee(Long employeeId, String payload) {
        Set<WebSocketSession> sessions = sessionsByEmployee.get(employeeId);
        if (sessions == null || sessions.isEmpty() || payload == null) {
            return 0;
        }
        int delivered = 0;
        for (WebSocketSession session : sessions) {
            if (sendSafely(session, payload)) {
                delivered++;
            }
        }
        return delivered;
    }

    /**
     * 服务端心跳：每 30 秒向全部在线连接发 {"type":"ping"}，
     * 客户端回 {"type":"pong"}；发送失败的死连接在此顺带清理，
     * 避免半开连接（小程序切后台/网络切换）长期占用注册表。
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void pingAll() {
        for (Set<WebSocketSession> sessions : sessionsByEmployee.values()) {
            for (WebSocketSession session : new ArrayList<>(sessions)) {
                sendSafely(session, "{\"type\":\"ping\"}");
            }
        }
    }

    /**
     * 单连接发送：检查存活 + synchronized(session) 串行发送，
     * IO 异常时关闭并清理该 session（推送失败静默，不影响调用方）
     */
    private boolean sendSafely(WebSocketSession session, String text) {
        if (session == null || !session.isOpen()) {
            remove(session);
            return false;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
            return true;
        } catch (IOException | IllegalStateException e) {
            log.debug("聊天WS推送失败,关闭连接: sessionId={}, {}", session.getId(), e.getMessage());
            closeQuietly(session);
            remove(session);
            return false;
        }
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ignored) {
            // 已是坏连接，忽略关闭异常
        }
    }

    private Long employeeIdOf(WebSocketSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttributes().get(ChatHandshakeInterceptor.ATTR_EMPLOYEE_ID);
        return value instanceof Long id ? id : null;
    }

    /**
     * 当前在线员工数（运维观测用）
     */
    public int onlineEmployeeCount() {
        return sessionsByEmployee.size();
    }

    /**
     * 指定员工的在线连接数
     */
    public int onlineSessionCount(Long employeeId) {
        Set<WebSocketSession> sessions = sessionsByEmployee.get(employeeId);
        return sessions == null ? 0 : sessions.size();
    }

    /**
     * 列出指定员工全部会话（预留：群发等场景）
     */
    public List<WebSocketSession> sessionsOf(Long employeeId) {
        Set<WebSocketSession> sessions = sessionsByEmployee.get(employeeId);
        return sessions == null ? List.of() : new ArrayList<>(sessions);
    }
}
