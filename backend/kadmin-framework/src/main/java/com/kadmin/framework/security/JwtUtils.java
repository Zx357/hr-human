package com.kadmin.framework.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * application.yml 中内置的默认弱密钥，生产环境禁止使用
     */
    private static final String DEFAULT_SECRET = "change-me-in-production-with-a-long-random-string";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 启动时校验：prod 环境下若仍使用默认弱密钥则快速失败，防止弱密钥上线
     */
    @jakarta.annotation.PostConstruct
    void rejectWeakSecretInProd() {
        boolean prod = activeProfile != null && java.util.Arrays.stream(activeProfile.split(","))
                .map(String::trim)
                .anyMatch("prod"::equalsIgnoreCase);
        if (prod && DEFAULT_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "生产环境(prod)检测到 JWT 密钥仍为默认值，请通过环境变量 JWT_SECRET 配置足够强度的密钥后再启动");
        }
    }

    /**
     * 获取密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username, String uuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("jti", uuid);
        return createToken(claims, expiration);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username, String uuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("jti", uuid);
        claims.put("refresh", true);
        return createToken(claims, refreshExpiration);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从Token中获取会话标识（用于定位Redis中的登录会话）
     */
    public String getUuidFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("jti", String.class);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 判断Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 判断是否是刷新Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            Boolean refresh = claims.get("refresh", Boolean.class);
            return refresh != null && refresh;
        } catch (JwtException e) {
            return false;
        }
    }
}