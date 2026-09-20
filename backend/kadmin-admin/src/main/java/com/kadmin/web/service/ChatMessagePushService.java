package com.kadmin.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kadmin.framework.websocket.ChatWebSocketSessionRegistry;
import com.kadmin.mobile.domain.MobileChatMessage;
import com.kadmin.mobile.service.MobileChatService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 聊天消息实时推送服务
 *
 * 放在 kadmin-admin 模块：需要同时看到 kadmin-framework 的 WS 会话注册表
 * 与 kadmin-mobile 的聊天服务（群成员/未读数计算）。
 *
 * 链路：MobileChatController.send 落库成功（服务事务已提交）后调用 {@link #pushNewMessage}，
 * 在独立线程池异步完成接收人计算与推送；任何失败仅记日志，绝不影响发送结果。
 *
 * 推送协议（JSON 文本帧）：
 * {"type":"chat_message","conversationId":"S123"|"G45","message":{...与 REST 返回结构一致}}
 * {"type":"unread_total","total":n}
 * conversationId 与会话列表接口的 key 惯例一致：单聊 "S"+对方员工ID（接收人视角下对方即发送人），
 * 群聊 "G"+群ID。
 */
@Slf4j
@Service
public class ChatMessagePushService {

    private static final String TYPE_CHAT_MESSAGE = "chat_message";
    private static final String TYPE_UNREAD_TOTAL = "unread_total";
    private static final int CHAT_TYPE_GROUP = 1;

    private final MobileChatService chatService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;
    private final ExecutorService pushExecutor;

    public ChatMessagePushService(MobileChatService chatService,
            ChatWebSocketSessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
        // 注入 Spring 统一配置的 ObjectMapper，保证 message 字段序列化与 REST 返回一致（日期格式/非空忽略）
        this.objectMapper = objectMapper;
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "chat-ws-push-" + index.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
        this.pushExecutor = Executors.newFixedThreadPool(2, threadFactory);
    }

    /**
     * 发送成功后触发（非阻塞）：任务进队列，主流程立即返回
     */
    public void pushNewMessage(MobileChatMessage message) {
        if (message == null || message.getId() == null) {
            return;
        }
        try {
            pushExecutor.execute(() -> doPush(message));
        } catch (Exception e) {
            // 线程池已关闭或拒绝等极端情况：推送降级为客户端轮询兜底
            log.warn("聊天推送任务提交失败(降级为轮询): {}", e.getMessage());
        }
    }

    private void doPush(MobileChatMessage message) {
        try {
            List<Long> recipients = resolveRecipients(message);
            if (recipients.isEmpty()) {
                return;
            }
            Map<String, Object> frame = new LinkedHashMap<>();
            frame.put("type", TYPE_CHAT_MESSAGE);
            frame.put("conversationId", conversationIdOf(message));
            frame.put("message", message);
            String payload = toJson(frame);
            if (payload == null) {
                return;
            }
            for (Long recipientId : recipients) {
                // 无在线连接则跳过该接收人（省掉未读数的 DB 聚合查询）
                if (sessionRegistry.onlineSessionCount(recipientId) <= 0) {
                    continue;
                }
                sessionRegistry.sendToEmployee(recipientId, payload);
                pushUnreadTotal(recipientId);
            }
        } catch (Exception e) {
            // 推送失败静默：客户端 4 秒轮询仍会兜底拉到该消息
            log.warn("聊天推送失败(降级为轮询): messageId={}, {}", message.getId(), e.getMessage());
        }
    }

    /**
     * 接收人未读总数推送给其全部在线端
     */
    private void pushUnreadTotal(Long recipientId) {
        long total = chatService.getTotalUnread(recipientId);
        Map<String, Object> frame = new LinkedHashMap<>();
        frame.put("type", TYPE_UNREAD_TOTAL);
        frame.put("total", total);
        String payload = toJson(frame);
        if (payload == null) {
            return;
        }
        sessionRegistry.sendToEmployee(recipientId, payload);
    }

    /**
     * 计算接收人：单聊为对方员工；群聊为除发送者外的全部在职成员
     * （发送方自己的多端同步不做，发送者其余端由其自身轮询兜底）
     */
    private List<Long> resolveRecipients(MobileChatMessage message) {
        if (message.getChatType() != null && message.getChatType() == CHAT_TYPE_GROUP) {
            List<Long> members = chatService.getGroupMemberEmployeeIds(message.getGroupId());
            return members.stream()
                    .filter(id -> !id.equals(message.getFromEmployeeId()))
                    .toList();
        }
        return message.getPeerEmployeeId() == null ? List.of() : List.of(message.getPeerEmployeeId());
    }

    private String conversationIdOf(MobileChatMessage message) {
        if (message.getChatType() != null && message.getChatType() == CHAT_TYPE_GROUP) {
            return "G" + message.getGroupId();
        }
        // 接收人视角：单聊会话以"对方"（即发送人）为标识
        return "S" + message.getFromEmployeeId();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("聊天推送消息序列化失败: {}", e.getMessage());
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        pushExecutor.shutdown();
    }
}
