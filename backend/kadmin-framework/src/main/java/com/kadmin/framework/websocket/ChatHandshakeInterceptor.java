package com.kadmin.framework.websocket;

import com.kadmin.common.security.LoginUser;
import com.kadmin.framework.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 聊天 WebSocket 握手鉴权拦截器
 *
 * 从 query 参数 token 取 JWT，经 TokenService 校验（JWT 签名 + Redis 会话存在性，
 * 登出/被踢后 Redis 会话删除即握手失败），解析出用户并按 MobileChatController.currentEmployeeId
 * 同款惯例映射为聊天身份 employeeId（LoginUser.employeeId 优先，管理员无员工档案时回退 userId）。
 * 校验失败返回 false 拒绝握手，并显式置 401（HandshakeInterceptor 约定：状态码由拦截器自己设置）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    /** 会话属性 key：握手通过后写入聊天身份 employeeId，Handler 中读取 */
    public static final String ATTR_EMPLOYEE_ID = "chatEmployeeId";

    private final TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = extractToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("聊天WS握手拒绝: 缺少token, uri={}", request.getURI());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            // JWT 签名校验 + Redis 会话校验二合一；非法/过期/已登出均返回 null
            LoginUser loginUser = tokenService.getLoginUser(token);
            if (loginUser == null) {
                log.warn("聊天WS握手拒绝: token无效或会话不存在");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            Long employeeId = loginUser.getEmployeeId() != null
                    ? loginUser.getEmployeeId()
                    : loginUser.getUserId();
            if (employeeId == null) {
                log.warn("聊天WS握手拒绝: 无法解析聊天身份, userId={}", loginUser.getUserId());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            attributes.put(ATTR_EMPLOYEE_ID, employeeId);
            log.debug("聊天WS握手通过: employeeId={}", employeeId);
            return true;
        } catch (Exception e) {
            log.warn("聊天WS握手异常拒绝: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }

    /**
     * 从握手 URL query 中提取 token（JWT 字符本身无需百分号编码，
     * 但仍做一次容错解码，解码失败按原值使用）
     */
    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getRawQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0 || !"token".equals(pair.substring(0, eq))) {
                continue;
            }
            String value = pair.substring(eq + 1);
            try {
                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                return value;
            }
        }
        return null;
    }
}
