package com.kadmin.framework.config;

import com.kadmin.framework.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 白名单路径（无需登录）
     * 员工头像（/employee_photo/**，UUID 文件名防枚举）与动态图片（/uploads/**）需直接在 <img> 标签中
     * 展示，保持匿名可读；身份证/合同/毕业证/证书等证件类图片属敏感 PII，不在白名单中，
     * 由管理端经 /file/** 接口鉴权后访问。
     */
    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/mobile/login",
            "/auth/refreshToken",
            "/system/mobile-menu/mobile/list",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/error",
            "/uploads/**",
            "/employee_photo/**",
            // 聊天 WebSocket 握手（/api context-path 下完整路径 /api/ws/chat）：
            // 鉴权在 ChatHandshakeInterceptor 内用 token query 参数完成，故对安全链放行
            "/ws/chat"
    };

    @Value("${security.cors.allowed-origins:http://localhost:9527,http://127.0.0.1:9527,http://localhost:5173,http://127.0.0.1:5173}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 启用CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 禁用Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权：移动端员工与管理员接口隔离
                // 注意顺序：精确规则在前，/xx/** 的 ADMIN 兜底规则在后
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        // ===== 敏感证件图片（身份证/合同/毕业证/证书）：仅管理员可读，管理端经带token的请求加载 =====
                        .requestMatchers("/id_card_front/**", "/id_card_back/**", "/contract_photo/**",
                                "/diploma_photo/**", "/cert_photo/**").hasRole("ADMIN")
                        // ===== 移动端员工需要共用的接口（登录即可） =====
                        .requestMatchers(HttpMethod.GET, "/employee/list", "/employee/current").authenticated()
                        // 仅匹配数字ID，避免单段通配遮蔽 /employee/page、/employee/export 等管理接口
                        .requestMatchers(HttpMethod.GET, "/employee/{id:[0-9]+}").authenticated()
                        .requestMatchers("/mobile/**").authenticated()
                        .requestMatchers("/hr/application/**").authenticated()
                        // 组织架构移动端只读，写操作归管理员
                        .requestMatchers(HttpMethod.GET, "/org-unit/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/file/upload/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/system/notice/list", "/system/notice/{id:[0-9]+}",
                                "/system/feedback/my")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/system/feedback").authenticated()
                        // ===== PC管理端接口（仅管理员） =====
                        .requestMatchers("/system/**", "/organization/**", "/calendar/**",
                                "/attendance/**", "/employee/**", "/hr/**", "/org-unit/**", "/file/**",
                                "/reminder/**", "/report/**", "/salary/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                // 认证失败处理：返回JSON而非默认403页面
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":\"8888\",\"msg\":\"请先登录\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"code\":\"403\",\"msg\":\"没有操作权限\",\"data\":null}");
                        }))
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS配置（来源可通过 security.cors.allowed-origins 配置，生产环境应收敛为实际域名）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
