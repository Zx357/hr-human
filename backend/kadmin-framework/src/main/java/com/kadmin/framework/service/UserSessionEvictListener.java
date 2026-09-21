package com.kadmin.framework.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.event.EmployeeDeletedEvent;
import com.kadmin.common.event.UserSessionEvictEvent;
import com.kadmin.framework.security.TokenService;
import com.kadmin.framework.websocket.ChatWebSocketSessionRegistry;
import com.kadmin.system.domain.SysUser;
import com.kadmin.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户会话吊销监听：删除该用户的全部登录Token，强制重新登录
 * AFTER_COMMIT：仅在实际事务提交后执行，避免事务回滚时会话被误删
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSessionEvictListener {

    private final TokenService tokenService;
    private final SysUserMapper sysUserMapper;
    private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserSessionEvict(UserSessionEvictEvent event) {
        if (event.getUserId() == null) {
            return;
        }
        if (event.isEmployee()) {
            tokenService.deleteUserTokenByEmployee(event.getUserId());
            // 同步关闭其聊天 WS 长连接，避免会话已吊销但推送仍可达
            chatWebSocketSessionRegistry.closeAllForEmployee(event.getUserId());
            log.info("已吊销员工 {} 的全部移动端登录会话", event.getUserId());
        } else {
            tokenService.deleteUserToken(event.getUserId());
            log.info("已吊销系统用户 {} 的全部登录会话", event.getUserId());
        }
    }

    /**
     * 员工删除：清理关联系统用户（保护admin），吊销其PC与移动端全部会话
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEmployeeDeleted(EmployeeDeletedEvent event) {
        Long employeeId = event.getEmployeeId();
        if (employeeId == null) {
            return;
        }
        for (SysUser user : sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeId, employeeId))) {
            if ("admin".equals(user.getUsername())) {
                continue;
            }
            tokenService.deleteUserToken(user.getId());
            sysUserMapper.deleteById(user.getId());
            log.info("员工 {} 删除：已清理关联系统用户 {}", employeeId, user.getUsername());
        }
        tokenService.deleteUserTokenByEmployee(employeeId);
        chatWebSocketSessionRegistry.closeAllForEmployee(employeeId);
    }
}
