package com.kadmin.framework.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 聊天 WebSocket 处理器
 *
 * 服务端 -> 客户端：{"type":"chat_message"|"unread_total"|"ping"} 文本帧
 * 客户端 -> 服务端：仅 {"type":"pong"}（心跳应答）；业务收发走 REST，本端不接收业务消息，
 * 收到其它内容一律忽略，防止滥用。单条消息大小由 WebSocketConfig 容器缓冲上限约束。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatWebSocketSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.register(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 只识别心跳帧：pong=客户端应答服务端心跳；ping=客户端主动保活，回 pong 供客户端做死链检测
        String payload = message.getPayload();
        if (payload.contains("\"pong\"")) {
            log.debug("聊天WS心跳应答: sessionId={}", session.getId());
        } else if (payload.contains("\"ping\"")) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                }
            } catch (Exception ignored) {
                // 发送失败由传输错误/关闭回调清理
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("聊天WS传输异常,关闭清理: sessionId={}, {}", session.getId(), exception.getMessage());
        registry.remove(session);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception ignored) {
            // 连接已坏，忽略关闭异常
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.remove(session);
    }
}
