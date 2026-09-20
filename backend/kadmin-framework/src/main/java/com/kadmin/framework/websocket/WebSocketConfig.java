package com.kadmin.framework.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 原生 WebSocket 配置（移动端聊天实时推送）
 * 端点：/ws/chat（应用有 /api context-path，完整路径为 /api/ws/chat）
 * 握手鉴权在 {@link ChatHandshakeInterceptor} 中完成（token 校验），
 * Spring Security 对 /ws/chat 放行（见 SecurityConfig 白名单）。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(chatHandshakeInterceptor)
                // H5 跨域（dev 前端 localhost:5173）与微信小程序（无 Origin 头）均可握手
                .setAllowedOriginPatterns("*");
    }

    /**
     * 容器级缓冲限制：限制单条 WS 文本消息大小（聊天连接只收心跳/控制帧，业务走 REST）
     */
    @Bean
    public ServletServerContainerFactoryBean servletServerContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(64 * 1024);
        container.setMaxBinaryMessageBufferSize(64 * 1024);
        return container;
    }
}
