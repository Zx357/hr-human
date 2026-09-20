package com.kadmin.framework.security;

import cn.hutool.core.util.IdUtil;
import com.kadmin.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token服务
 * JWT 中携带会话标识(jti)，登录会话存储于Redis：
 * 登出/禁用/改密时删除对应会话即可使Token立即失效
 */
@Slf4j
@Service
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;

    public TokenService(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private static final String TOKEN_PREFIX = "login_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String REFRESH_GRACE_PREFIX = "refresh_grace:";
    /** 访问会话->配对刷新会话的映射，供登出时一并吊销 */
    private static final String REFRESH_PAIR_PREFIX = "refresh_pair:";
    private static final String USER_TOKEN_INDEX_PREFIX = "user_tokens:";
    /** 会话索引命名空间：PC系统用户与移动端员工ID分属两套序列，必须隔离避免互踢 */
    private static final String NAMESPACE_USER = "U";
    private static final String NAMESPACE_EMPLOYEE = "E";
    private static final long REFRESH_GRACE_MILLIS = 60_000L;
    private static final Long MILLIS_MINUTE = 60 * 1000L;
    private static final Long MILLIS_MINUTE_TEN = 20 * MILLIS_MINUTE;

    /**
     * 判断是否为纯移动端员工登录（仅含 ROLE_EMPLOYEE 角色）
     */
    private boolean isEmployeeOnly(LoginUser loginUser) {
        return loginUser.getRoles() != null && !loginUser.getRoles().isEmpty()
                && loginUser.getRoles().stream().allMatch("ROLE_EMPLOYEE"::equals);
    }

    /**
     * 创建Token
     */
    public String createToken(LoginUser loginUser) {
        String uuid = IdUtil.fastUUID();
        loginUser.setToken(uuid);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expiration);

        // 生成JWT Token（jti与会话绑定）
        String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername(), uuid);

        // 缓存用户信息
        String tokenKey = getTokenKey(uuid);
        redisTemplate.opsForValue().set(tokenKey, loginUser, expiration, TimeUnit.MILLISECONDS);
        addToUserIndex(loginUser, TOKEN_PREFIX, uuid);

        return token;
    }

    /**
     * 创建RefreshToken
     */
    public String createRefreshToken(LoginUser loginUser) {
        String refreshUuid = IdUtil.fastUUID();

        // 生成Refresh JWT Token
        String refreshToken = jwtUtils.generateRefreshToken(loginUser.getUserId(), loginUser.getUsername(),
                refreshUuid);

        // 缓存RefreshToken关联的用户信息
        String refreshTokenKey = getRefreshTokenKey(refreshUuid);
        redisTemplate.opsForValue().set(refreshTokenKey, loginUser, refreshExpiration, TimeUnit.MILLISECONDS);
        addToUserIndex(loginUser, REFRESH_TOKEN_PREFIX, refreshUuid);
        // 记录与当前访问会话的配对关系，供登出时一并吊销
        if (loginUser.getToken() != null && !loginUser.getToken().isEmpty()) {
            redisTemplate.opsForValue().set(REFRESH_PAIR_PREFIX + loginUser.getToken(), refreshUuid,
                    refreshExpiration, TimeUnit.MILLISECONDS);
        }

        return refreshToken;
    }

    /**
     * 验证RefreshToken并返回用户信息（一次性旋转 + 60秒宽限期）
     * 首个调用者旋转成功；并发的重复刷新请求在宽限期内返回同一用户，避免被误登出
     */
    public LoginUser validateRefreshToken(String refreshToken) {
        try {
            if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
                return null;
            }

            String uuid = jwtUtils.getUuidFromToken(refreshToken);
            if (uuid == null || uuid.isEmpty()) {
                return null;
            }

            Object obj = redisTemplate.opsForValue().get(getRefreshTokenKey(uuid));
            if (obj instanceof LoginUser loginUser) {
                // 原子删除，仅首个调用者旋转成功
                Boolean deleted = redisTemplate.delete(getRefreshTokenKey(uuid));
                if (Boolean.TRUE.equals(deleted)) {
                    deleteFromUserIndex(loginUser, REFRESH_TOKEN_PREFIX, uuid);
                    // 写入宽限期记录，供并发刷新请求兜底
                    redisTemplate.opsForValue().set(getRefreshGraceKey(uuid), loginUser,
                            REFRESH_GRACE_MILLIS, TimeUnit.MILLISECONDS);
                    return loginUser;
                }
            }
            // 宽限期内允许并发刷新重复使用（返回同一用户，由调用方签发新token对）
            Object grace = redisTemplate.opsForValue().get(getRefreshGraceKey(uuid));
            if (grace instanceof LoginUser loginUser) {
                return loginUser;
            }
        } catch (Exception e) {
            log.error("验证RefreshToken失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取登录用户（从Redis会话读取，会话被删除即视为未登录）
     */
    public LoginUser getLoginUser(String token) {
        try {
            String uuid = jwtUtils.getUuidFromToken(token);
            if (uuid == null || uuid.isEmpty()) {
                return null;
            }

            Object obj = redisTemplate.opsForValue().get(getTokenKey(uuid));
            if (obj instanceof LoginUser loginUser) {
                return loginUser;
            }
            return null;
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
            Object obj = redisTemplate.opsForValue().get(tokenKey);
            redisTemplate.delete(tokenKey);
            if (obj instanceof LoginUser loginUser) {
                deleteFromUserIndex(loginUser, TOKEN_PREFIX, uuid);
            }
        }
    }

    /**
     * 登出：删除访问Token会话，并同时吊销其配对的RefreshToken会话，
     * 防止登出后仍可用RefreshToken换取新Token
     */
    public void deleteTokenWithRefreshToken(String accessUuid) {
        deleteToken(accessUuid);
        if (accessUuid == null) {
            return;
        }
        Object refreshUuid = redisTemplate.opsForValue().get(REFRESH_PAIR_PREFIX + accessUuid);
        if (refreshUuid instanceof String uuid) {
            String refreshTokenKey = getRefreshTokenKey(uuid);
            Object obj = redisTemplate.opsForValue().get(refreshTokenKey);
            redisTemplate.delete(refreshTokenKey);
            redisTemplate.delete(REFRESH_GRACE_PREFIX + uuid);
            if (obj instanceof LoginUser loginUser) {
                deleteFromUserIndex(loginUser, REFRESH_TOKEN_PREFIX, uuid);
            }
        }
        redisTemplate.delete(REFRESH_PAIR_PREFIX + accessUuid);
    }

    /**
     * 删除PC系统用户的全部Token（禁用/重置密码/修改角色时调用）
     */
    public void deleteUserToken(Long userId) {
        deleteUserToken(NAMESPACE_USER, userId);
    }

    /**
     * 删除移动端员工的全部Token（改密/离职时调用）
     */
    public void deleteUserTokenByEmployee(Long employeeId) {
        deleteUserToken(NAMESPACE_EMPLOYEE, employeeId);
    }

    private void deleteUserToken(String namespace, Long userId) {
        if (userId == null) {
            return;
        }
        String indexKey = USER_TOKEN_INDEX_PREFIX + namespace + ":" + userId;
        Set<Object> tokens = redisTemplate.opsForSet().members(indexKey);
        if (tokens != null) {
            for (Object token : tokens) {
                if (token instanceof String key) {
                    redisTemplate.delete(key);
                }
            }
        }
        redisTemplate.delete(indexKey);
    }

    /**
     * 记录用户->会话的反向索引（按命名空间隔离），便于按用户批量吊销
     */
    private void addToUserIndex(LoginUser loginUser, String prefix, String uuid) {
        if (loginUser.getUserId() == null) {
            return;
        }
        String indexKey = getUserIndexKey(loginUser);
        redisTemplate.opsForSet().add(indexKey, prefix + uuid);
        redisTemplate.expire(indexKey, Math.max(expiration, refreshExpiration), TimeUnit.MILLISECONDS);
    }

    private void deleteFromUserIndex(LoginUser loginUser, String prefix, String uuid) {
        if (loginUser.getUserId() == null) {
            return;
        }
        redisTemplate.opsForSet().remove(getUserIndexKey(loginUser), prefix + uuid);
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

    private String getRefreshGraceKey(String uuid) {
        return REFRESH_GRACE_PREFIX + uuid;
    }

    private String getUserIndexKey(LoginUser loginUser) {
        String namespace = isEmployeeOnly(loginUser) ? NAMESPACE_EMPLOYEE : NAMESPACE_USER;
        return USER_TOKEN_INDEX_PREFIX + namespace + ":" + loginUser.getUserId();
    }
}
