package com.kadmin.framework.aspectj;

import com.kadmin.common.Result;
import com.kadmin.common.ResultCode;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.common.security.LoginUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 权限校验切面单元测试：PermissionAspect.checkPermission 的放行/拒绝/豁免分支。
 * 不起 Spring 上下文：直接以 mock 的 ProceedingJoinPoint + 真实 Method 注解调用切面方法。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionAspectTest {

    /** 供切面反射读取注解的测试桩方法宿主（类本身不标注注解） */
    static class DummyController {
        @RequiresPermission("hr:employee:add")
        public String add() {
            return "add";
        }

        @RequiresPermission(value = {"hr:employee:add", "hr:employee:edit"}, logical = RequiresPermission.Logical.AND)
        public String andBoth() {
            return "and";
        }

        @RequiresPermission(value = {"hr:employee:add", "hr:employee:edit"}, logical = RequiresPermission.Logical.OR)
        public String orAny() {
            return "or";
        }

        public String free() {
            return "free";
        }
    }

    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature signature;

    private final PermissionAspect aspect = new PermissionAspect();
    private final DummyController target = new DummyController();

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, Set<String> roles, Set<String> permissions) {
        LoginUser loginUser = new LoginUser(userId, "user" + userId, null, null, null, null, roles, permissions);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, "N/A"));
    }

    private Object run(String methodName) throws Throwable {
        Method method = DummyController.class.getMethod(methodName);
        when(signature.getMethod()).thenReturn(method);
        return aspect.checkPermission(joinPoint);
    }

    @Test
    @DisplayName("无 @RequiresPermission 注解的连接点不拦截，直接放行（未登录也放行）")
    void aspect_skipsMethodWithoutAnnotation() throws Throwable {
        when(joinPoint.proceed()).thenReturn("proceeded");
        Object result = run("free");
        assertThat(result).isEqualTo("proceeded");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("未登录（会话无用户）返回 UNAUTHORIZED 且不执行业务方法")
    void aspect_rejectsAnonymousUser() throws Throwable {
        Object result = run("add");
        assertThat(result).isInstanceOf(Result.class);
        assertThat(((Result<?>) result).getCode()).isEqualTo(ResultCode.UNAUTHORIZED.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("ROLE_ADMIN 超级管理员一律放行")
    void aspect_allowsAdmin() throws Throwable {
        login(1L, Set.of("ROLE_ADMIN"), Set.of());
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("add")).isEqualTo("proceeded");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("纯移动端员工（仅 ROLE_EMPLOYEE）豁免按钮级权限校验")
    void aspect_allowsPureMobileEmployee() throws Throwable {
        login(2L, Set.of("ROLE_EMPLOYEE"), null);
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("add")).isEqualTo("proceeded");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("拥有注解要求的精确权限时放行")
    void aspect_allowsExactPermission() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:employee:add"));
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("add")).isEqualTo("proceeded");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("无所需权限时返回 FORBIDDEN 且不执行业务方法")
    void aspect_forbidsWithoutPermission() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:employee:delete"));
        Object result = run("add");
        assertThat(((Result<?>) result).getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("权限集合为空时拒绝")
    void aspect_forbidsEmptyPermissions() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of());
        Object result = run("add");
        assertThat(((Result<?>) result).getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("功能级通配符 hr:employee:* 命中具体权限")
    void aspect_allowsFunctionWildcard() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:employee:*"));
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("add")).isEqualTo("proceeded");
    }

    @Test
    @DisplayName("模块级通配符 hr:*:* 命中具体权限")
    void aspect_allowsModuleWildcard() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:*:*"));
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("add")).isEqualTo("proceeded");
    }

    @Test
    @DisplayName("全局通配符 *:*:* 命中一切权限")
    void aspect_allowsGlobalWildcard() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("*:*:*"));
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("add")).isEqualTo("proceeded");
    }

    @Test
    @DisplayName("AND 模式：两个权限都具备才放行")
    void aspect_andLogicalRequiresAllPermissions() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:employee:add", "hr:employee:edit"));
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("andBoth")).isEqualTo("proceeded");
    }

    @Test
    @DisplayName("AND 模式：缺任一权限即拒绝")
    void aspect_andLogicalRejectsPartialPermissions() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:employee:add"));
        Object result = run("andBoth");
        assertThat(((Result<?>) result).getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("OR 模式：具备任一权限即放行")
    void aspect_orLogicalAllowsAnyPermission() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("hr:employee:edit"));
        when(joinPoint.proceed()).thenReturn("proceeded");
        assertThat(run("orAny")).isEqualTo("proceeded");
    }

    @Test
    @DisplayName("OR 模式：一个权限都没有则拒绝")
    void aspect_orLogicalRejectsNoPermission() throws Throwable {
        login(3L, Set.of("ROLE_USER"), Set.of("sys:user:list"));
        Object result = run("orAny");
        assertThat(((Result<?>) result).getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        verify(joinPoint, never()).proceed();
    }
}
