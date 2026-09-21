package com.kadmin.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kadmin.common.event.NotificationCreatedEvent;
import com.kadmin.framework.websocket.ChatWebSocketSessionRegistry;
import com.kadmin.system.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 站内通知实时推送
 *
 * 监听 NotificationCreatedEvent，把新通知推给当前在线的接收员工
 * （与聊天推送共用 WS 通道；离线员工登录后由既有轮询拉取，互不影响）。
 *
 * 推送协议（JSON 文本帧）：
 * {"type":"notification","id":n,"notifType":"approval_result","title":"...","content":"...","unreadCount":n}
 */
@Slf4j
@Service
public class NotificationPushService {

    private static final String TYPE_NOTIFICATION = "notification";

    private final NotificationService notificationService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public NotificationPushService(NotificationService notificationService,
            ChatWebSocketSessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onNotificationCreated(NotificationCreatedEvent event) {
        try {
            if (event.getEmployeeId() == null || sessionRegistry.onlineSessionCount(event.getEmployeeId()) <= 0) {
                return;
            }
            Map<String, Object> frame = new LinkedHashMap<>();
            frame.put("type", TYPE_NOTIFICATION);
            frame.put("id", event.getNotificationId());
            frame.put("notifType", event.getType());
            frame.put("title", event.getTitle());
            frame.put("content", event.getContent());
            frame.put("unreadCount", notificationService.unreadCount(event.getEmployeeId()));
            String payload = objectMapper.writeValueAsString(frame);
            sessionRegistry.sendToEmployee(event.getEmployeeId(), payload);
        } catch (JsonProcessingException e) {
            log.warn("通知推送序列化失败: notificationId={}, {}", event.getNotificationId(), e.getMessage());
        } catch (Exception e) {
            // 推送失败静默：客户端轮询仍会兜底拉到该通知
            log.warn("通知推送失败(降级为轮询): notificationId={}, {}", event.getNotificationId(), e.getMessage());
        }
    }
}
