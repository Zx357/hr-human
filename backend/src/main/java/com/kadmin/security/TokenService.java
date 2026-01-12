package com.kadmin.security;

import cn.hutool.core.util.IdUtil;
import com.kadmin.entity.SysUser;
import com.kadmin.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token服务
 */
@Slf4j
@Service
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;

    public TokenService(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils,
            @Lazy SysUserService userService) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private static final String TOKEN_PREFIX = "login_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final Long MILLIS_MINUTE = 60 * 1000L;
    private static final Long MILLIS_MINUTE_TEN = 20 * MILLIS_MINUTE;

    /**
     * 创建Token
     */
    public String createToken(LoginUser loginUser) {
        String uuid = IdUtil.fastUUID();
        loginUser.setToken(uuid);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expiration);

        // 生成JWT Token
        String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername());

        // 缓存用户信息
        String tokenKey = getTokenKey(uuid);
        redisTemplate.opsForValue().set(tokenKey, loginUser, expiration, TimeUnit.MILLISECONDS);

        return token;
    }

    /**
     * 创建RefreshToken
     */
    public String createRefreshToken(LoginUser loginUser) {
        String refreshUuid = IdUtil.fastUUID();

        // 生成Refresh JWT Token
        String refreshToken = jwtUtils.generateRefreshToken(loginUser.getUserId(), loginUser.getUsername());

        // 缓存RefreshToken关联的用户信息
        String refreshTokenKey = getRefreshTokenKey(refreshUuid);
        redisTemplate.opsForValue().set(refreshTokenKey, loginUser, refreshExpiration, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    /**
     * 验证RefreshToken并返回用户信息
     */
    public LoginUser validateRefreshToken(String refreshToken) {
        try {
            if (!jwtUtils.validateToken(refreshToken)) {
                return null;
            }

            Long userId = jwtUtils.getUserIdFromToken(refreshToken);
            String username = jwtUtils.getUsernameFromToken(refreshToken);

            // 从Redis中查找用户信息
            String pattern = REFRESH_TOKEN_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null) {
                for (String key : keys) {
                    Object obj = redisTemplate.opsForValue().get(key);
                    if (obj instanceof LoginUser) {
                        LoginUser loginUser = (LoginUser) obj;
                        if (loginUser.getUserId().equals(userId) && loginUser.getUsername().equals(username)) {
                            // 删除旧的RefreshToken
                            redisTemplate.delete(key);
                            return loginUser;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("验证RefreshToken失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取登录用户
     */
    public LoginUser getLoginUser(String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);

            // 首先从Redis中查找用户信息
            String pattern = TOKEN_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null) {
                for (String key : keys) {
                    Object obj = redisTemplate.opsForValue().get(key);
                    if (obj instanceof LoginUser) {
                        LoginUser loginUser = (LoginUser) obj;
                        if (loginUser.getUserId().equals(userId) && loginUser.getUsername().equals(username)) {
                            return loginUser;
                        }
                    }
                }
            }

            // 如果Redis中没有，从数据库重新加载用户信息
            log.info("Redis中未找到用户信息，从数据库重新加载: userId={}, username={}", userId, username);
            SysUser user = userService.getById(userId);
            if (user != null && user.getUsername().equals(username) && user.getStatus() == 1
                    && user.getDeleted() == 0) {
                // 获取用户角色和权限
                Set<String> roles = userService.getUserRoleCodes(userId);
                Set<String> permissions = userService.getUserPermissions(userId);

                // 创建LoginUser
                LoginUser loginUser = new LoginUser();
                loginUser.setUserId(user.getId());
                loginUser.setUsername(user.getUsername());
                loginUser.setNickname(user.getNickname());
                loginUser.setRoles(roles);
                loginUser.setPermissions(permissions);
                loginUser.setToken(IdUtil.fastUUID());
                loginUser.setLoginTime(System.currentTimeMillis());
                loginUser.setExpireTime(loginUser.getLoginTime() + expiration);

                // 缓存到Redis
                String tokenKey = getTokenKey(loginUser.getToken());
                redisTemplate.opsForValue().set(tokenKey, loginUser, expiration, TimeUnit.MILLISECONDS);

                return loginUser;
            }
        } catch (Exception e) {
            log.error("获取登录用户失败: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 验证Token，如果快过期则刷新
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();

        // 如果剩余时间小于20分钟，则刷新Token
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新Token
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expiration);

        String tokenKey = getTokenKey(loginUser.getToken());
        redisTemplate.opsForValue().set(tokenKey, loginUser, expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * 删除Token
     */
    public void deleteToken(String uuid) {
        if (uuid != null) {
            String tokenKey = getTokenKey(uuid);
            redisTemplate.delete(tokenKey);
        }
    }

    /**
     * 删除用户Token
     */
    public void deleteUserToken(Long userId) {
        // 删除普通Token
        String pattern = TOKEN_PREFIX + "*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null) {
            for (String key : keys) {
                Object obj = redisTemplate.opsForValue().get(key);
                if (obj instanceof LoginUser) {
                    LoginUser loginUser = (LoginUser) obj;
                    if (loginUser.getUserId().equals(userId)) {
                        redisTemplate.delete(key);
                    }
                }
            }
        }

        // 删除RefreshToken
        String refreshPattern = REFRESH_TOKEN_PREFIX + "*";
        var refreshKeys = redisTemplate.keys(refreshPattern);
        if (refreshKeys != null) {
            for (String key : refreshKeys) {
                Object obj = redisTemplate.opsForValue().get(key);
                if (obj instanceof LoginUser) {
                    LoginUser loginUser = (LoginUser) obj;
                    if (loginUser.getUserId().equals(userId)) {
                        redisTemplate.delete(key);
                    }
                }
            }
        }
    }

    /**
     * 获取Token Key
     */
    private String getTokenKey(String uuid) {
        return TOKEN_PREFIX + uuid;
    }

    /**
     * 获取RefreshToken Key
     */
    private String getRefreshTokenKey(String uuid) {
        return REFRESH_TOKEN_PREFIX + uuid;
    }
}