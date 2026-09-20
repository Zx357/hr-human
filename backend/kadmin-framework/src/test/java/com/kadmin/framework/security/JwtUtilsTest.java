package com.kadmin.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JWT 工具单元测试：生成/解析往返、过期与篡改拒绝、prod 环境弱密钥 fail-fast。
 * 不起 Spring 上下文，直接反射注入 @Value 字段。
 */
class JwtUtilsTest {

    private static final String DEFAULT_SECRET = "change-me-in-production-with-a-long-random-string";
    private static final String STRONG_SECRET = "prod-strong-secret-0123456789-abcdefghijklmnop-xyz";
    private static final String ANOTHER_SECRET = "another-secret-for-tamper-testing-0123456789abcdef";
    private static final Long EXPIRATION = 3_600_000L; // 1 小时
    private static final Long REFRESH_EXPIRATION = 86_400_000L; // 1 天

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", DEFAULT_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expiration", EXPIRATION);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpiration", REFRESH_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtils, "activeProfile", "dev");
    }

    @Test
    @DisplayName("生成并解析 Token：claims 往返一致（userId/username/jti）")
    void generateThenParse_roundTrip() {
        String token = jwtUtils.generateToken(42L, "admin", "session-uuid-1");

        Claims claims = jwtUtils.parseToken(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("username", String.class)).isEqualTo("admin");
        assertThat(claims.get("jti", String.class)).isEqualTo("session-uuid-1");

        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("admin");
        assertThat(jwtUtils.getUuidFromToken(token)).isEqualTo("session-uuid-1");
    }

    @Test
    @DisplayName("有效 Token 校验通过且未过期")
    void validateToken_validToken() {
        String token = jwtUtils.generateToken(1L, "admin", "u1");
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("过期 Token：解析抛 ExpiredJwtException、校验失败、判定已过期")
    void expiredToken_rejected() {
        ReflectionTestUtils.setField(jwtUtils, "expiration", -1000L);
        String token = jwtUtils.generateToken(1L, "admin", "u1");

        assertThatThrownBy(() -> jwtUtils.parseToken(token)).isInstanceOf(ExpiredJwtException.class);
        assertThat(jwtUtils.validateToken(token)).isFalse();
        assertThat(jwtUtils.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("篡改签名的 Token（用其他密钥签发）解析被拒绝")
    void tamperedToken_rejected() {
        // 用另一个密钥签发的 token 相当于对当前密钥的伪造/篡改
        ReflectionTestUtils.setField(jwtUtils, "secret", ANOTHER_SECRET);
        String forged = jwtUtils.generateToken(1L, "admin", "u1");
        ReflectionTestUtils.setField(jwtUtils, "secret", DEFAULT_SECRET);

        assertThatThrownBy(() -> jwtUtils.parseToken(forged)).isInstanceOf(JwtException.class);
        assertThat(jwtUtils.validateToken(forged)).isFalse();
    }

    @Test
    @DisplayName("垃圾字符串 Token 校验失败且不抛出")
    void garbageToken_validateFalse() {
        assertThat(jwtUtils.validateToken("not-a-jwt")).isFalse();
        assertThat(jwtUtils.isRefreshToken("not-a-jwt")).isFalse();
    }

    @Test
    @DisplayName("刷新 Token 携带 refresh=true 标记，访问 Token 无该标记")
    void refreshToken_flag() {
        String refresh = jwtUtils.generateRefreshToken(1L, "admin", "u1");
        String access = jwtUtils.generateToken(1L, "admin", "u1");
        assertThat(jwtUtils.isRefreshToken(refresh)).isTrue();
        assertThat(jwtUtils.isRefreshToken(access)).isFalse();
    }

    @Test
    @DisplayName("刷新 Token 使用独立的过期时长")
    void refreshToken_usesRefreshExpiration() {
        String refresh = jwtUtils.generateRefreshToken(1L, "admin", "u1");
        String access = jwtUtils.generateToken(1L, "admin", "u1");
        Date refreshExp = jwtUtils.getExpirationFromToken(refresh);
        Date accessExp = jwtUtils.getExpirationFromToken(access);
        // 刷新 Token 有效期约为访问 Token 的 24 倍（容差 10 秒）
        long diff = refreshExp.getTime() - accessExp.getTime();
        assertThat(diff).isBetween(REFRESH_EXPIRATION - EXPIRATION - 10_000L,
                REFRESH_EXPIRATION - EXPIRATION + 10_000L);
    }

    // ==================== prod 弱密钥 fail-fast ====================

    @Test
    @DisplayName("prod 环境使用默认弱密钥时启动快速失败")
    void rejectWeakSecretInProd_throwsWithDefaultSecret() {
        ReflectionTestUtils.setField(jwtUtils, "activeProfile", "prod");
        assertThatThrownBy(() -> jwtUtils.rejectWeakSecretInProd())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("生产环境");
    }

    @Test
    @DisplayName("多 profile（dev,prod）中包含 prod 且密钥为默认值时同样快速失败")
    void rejectWeakSecretInProd_checksCommaSeparatedProfiles() {
        ReflectionTestUtils.setField(jwtUtils, "activeProfile", "dev,prod");
        assertThatThrownBy(() -> jwtUtils.rejectWeakSecretInProd())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("prod 环境配置了强密钥时不拦截")
    void rejectWeakSecretInProd_allowsStrongSecret() {
        ReflectionTestUtils.setField(jwtUtils, "activeProfile", "prod");
        ReflectionTestUtils.setField(jwtUtils, "secret", STRONG_SECRET);
        assertThatCode(() -> jwtUtils.rejectWeakSecretInProd()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("dev 环境保留默认密钥时不拦截（仅 prod fail-fast）")
    void rejectWeakSecretInProd_ignoresDevProfile() {
        ReflectionTestUtils.setField(jwtUtils, "activeProfile", "dev");
        assertThatCode(() -> jwtUtils.rejectWeakSecretInProd()).doesNotThrowAnyException();
    }
}
