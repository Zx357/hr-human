package com.kadmin.framework.aspectj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kadmin.common.annotation.OperLog;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.system.domain.SysOperLog;
import com.kadmin.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

/**
 * 操作日志切面：记录标注 @OperLog 的接口调用
 * 日志入库失败不影响业务请求
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private static final int PARAMS_MAX_LENGTH = 2000;
    private static final int ERROR_MAX_LENGTH = 500;
    private static final Set<String> SENSITIVE_KEYS = Set.of("password", "oldPassword", "newPassword",
            "miniAppPassword", "idCard", "confirmPassword");

    private final SysOperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            try {
                saveLog(joinPoint, operLog, cost, error);
            } catch (Exception e) {
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperLog operLog, long cost, Throwable error) {
        SysOperLog operLogEntity = new SysOperLog();
        operLogEntity.setModule(operLog.module());
        operLogEntity.setAction(operLog.action());
        operLogEntity.setCostMs(cost);
        operLogEntity.setStatus(error == null ? 1 : 0);
        if (error != null) {
            String message = error.getMessage();
            operLogEntity.setErrorMsg(message != null && message.length() > ERROR_MAX_LENGTH
                    ? message.substring(0, ERROR_MAX_LENGTH)
                    : message);
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operLogEntity.setRequestUri(request.getMethod() + " " + request.getRequestURI());
            operLogEntity.setIp(resolveClientIp(request));
            operLogEntity.setRequestParams(truncate(buildParams(joinPoint)));
        }

        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser != null) {
            operLogEntity.setUserId(loginUser.getUserId());
            operLogEntity.setUsername(loginUser.getUsername());
        }

        operLogEntity.setCreatedTime(LocalDateTime.now());
        operLogMapper.insert(operLogEntity);
    }

    private String buildParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            Object[] filtered = Arrays.stream(args)
                    .map(arg -> {
                        if (arg instanceof MultipartFile) {
                            return arg.getClass().getSimpleName();
                        }
                        if (arg instanceof jakarta.servlet.ServletRequest
                                || arg instanceof jakarta.servlet.ServletResponse) {
                            return arg.getClass().getSimpleName();
                        }
                        return arg;
                    })
                    .toArray();
            String json = objectMapper.writeValueAsString(filtered);
            return maskSensitiveFields(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 简单脱敏："password":"xxx" → "password":"***"
     */
    private String maskSensitiveFields(String json) {
        for (String key : SENSITIVE_KEYS) {
            json = json.replaceAll("\"" + key + "\"\\s*:\\s*\"[^\"]*\"", "\"" + key + "\":\"***\"");
        }
        return json;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > PARAMS_MAX_LENGTH ? value.substring(0, PARAMS_MAX_LENGTH) : value;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index) : ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
