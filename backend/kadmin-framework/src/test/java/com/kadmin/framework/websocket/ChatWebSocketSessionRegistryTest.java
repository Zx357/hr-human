package com.kadmin.framework.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 聊天 WebSocket 会话注册表单元测试：注册/注销、多端推送、死连接清理、发送异常隔离。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatWebSocketSessionRegistryTest {

    private final ChatWebSocketSessionRegistry registry = new ChatWebSocketSessionRegistry();

    @Mock
    private WebSocketSession session1;
    @Mock
    private WebSocketSession session2;
    @Mock
    private WebSocketSession otherEmployeeSession;

    @BeforeEach
    void setUp() {
        stubSession(session1, 1L, "s1");
        stubSession(session2, 1L, "s2");
        stubSession(otherEmployeeSession, 2L, "s3");
    }

    private void stubSession(WebSocketSession session, Long employeeId, String sessionId) {
        Map<String, Object> attributes = new HashMap<>();
        if (employeeId != null) {
            attributes.put(ChatHandshakeInterceptor.ATTR_EMPLOYEE_ID, employeeId);
        }
        when(session.getAttributes()).thenReturn(attributes);
        when(session.getId()).thenReturn(sessionId);
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    @DisplayName("注册：同一员工多端在线全部登记，按员工计数正确")
    void register_tracksMultipleSessionsPerEmployee() {
        registry.register(session1);
        registry.register(session2);
        registry.register(otherEmployeeSession);

        assertThat(registry.onlineSessionCount(1L)).isEqualTo(2);
        assertThat(registry.onlineSessionCount(2L)).isEqualTo(1);
        assertThat(registry.onlineEmployeeCount()).isEqualTo(2);
        assertThat(registry.sessionsOf(1L)).hasSize(2);
    }

    @Test
    @DisplayName("注册：会话缺少 employeeId 属性时忽略，不影响注册表")
    void register_ignoresSessionWithoutEmployeeId() {
        WebSocketSession anonymous = org.mockito.Mockito.mock(WebSocketSession.class);
        stubSession(anonymous, null, "anon");
        registry.register(anonymous);
        registry.register(null);

        assertThat(registry.onlineEmployeeCount()).isZero();
    }

    @Test
    @DisplayName("注销：移除会话并顺手清理空集合防泄漏")
    void remove_deletesSessionAndCleansUpEmptyEmployee() {
        registry.register(session1);
        registry.register(session2);

        registry.remove(session1);
        assertThat(registry.onlineSessionCount(1L)).isEqualTo(1);
        assertThat(registry.onlineEmployeeCount()).isEqualTo(1);

        registry.remove(session2);
        assertThat(registry.onlineSessionCount(1L)).isZero();
        assertThat(registry.onlineEmployeeCount()).isZero();
    }

    @Test
    @DisplayName("推送：多 session 全部送达并返回成功数")
    void sendToEmployee_deliversToAllSessions() throws Exception {
        registry.register(session1);
        registry.register(session2);

        int delivered = registry.sendToEmployee(1L, "{\"type\":\"msg\"}");

        assertThat(delivered).isEqualTo(2);
        verify(session1).sendMessage(any(TextMessage.class));
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("推送：无在线连接或 payload 为空时不发送且返回0")
    void sendToEmployee_returnsZeroWhenNoSessionOrPayload() throws Exception {
        assertThat(registry.sendToEmployee(999L, "json")).isZero();

        registry.register(session1);
        assertThat(registry.sendToEmployee(1L, null)).isZero();
        verify(session1, never()).sendMessage(any());
    }

    @Test
    @DisplayName("推送：已关闭的死连接被顺带清理，其余连接正常送达")
    void sendToEmployee_skipsAndRemovesClosedSession() throws Exception {
        registry.register(session1);
        registry.register(session2);
        when(session2.isOpen()).thenReturn(false);

        int delivered = registry.sendToEmployee(1L, "json");

        assertThat(delivered).isEqualTo(1);
        verify(session1).sendMessage(any(TextMessage.class));
        verify(session2, never()).sendMessage(any(TextMessage.class));
        assertThat(registry.onlineSessionCount(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("推送：单 session 发送 IO 异常时关闭并清理，不影响其他 session 送达")
    void sendToEmployee_ioErrorOnOneSessionDoesNotAffectOthers() throws Exception {
        registry.register(session1);
        registry.register(session2);
        doThrow(new IOException("broken pipe")).when(session1).sendMessage(any(TextMessage.class));

        int delivered = registry.sendToEmployee(1L, "json");

        assertThat(delivered).isEqualTo(1);
        verify(session1).close();
        verify(session2).sendMessage(any(TextMessage.class));
        assertThat(registry.onlineSessionCount(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("心跳：向全部在线连接发送 ping，死连接被清理且不抛异常")
    void pingAll_sendsPingAndCleansDeadSessions() throws Exception {
        registry.register(session1);
        registry.register(otherEmployeeSession);
        doAnswer(invocation -> {
            throw new IllegalStateException("session closed mid-flight");
        }).when(otherEmployeeSession).sendMessage(any(TextMessage.class));

        assertThatCode(() -> registry.pingAll()).doesNotThrowAnyException();

        org.mockito.ArgumentCaptor<TextMessage> captor = org.mockito.ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor.capture());
        assertThat(captor.getValue().getPayload()).isEqualTo("{\"type\":\"ping\"}");
        verify(otherEmployeeSession).close();
        assertThat(registry.onlineSessionCount(2L)).isZero();
        assertThat(registry.onlineSessionCount(1L)).isEqualTo(1);
    }
}
