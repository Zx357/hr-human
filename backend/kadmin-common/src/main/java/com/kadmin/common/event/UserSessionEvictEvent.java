package com.kadmin.common.event;

/**
 * 用户会话吊销事件
 * 用户被禁用/删除/重置密码/角色变更/离职时发布，由框架层监听并删除其全部登录会话
 * PC系统用户与移动端员工分属两套ID序列，需区分命名空间避免误踢
 */
public class UserSessionEvictEvent {

    /**
     * 用户ID（PC系统用户ID 或 员工ID，由 employee 标志区分）
     */
    private final Long userId;

    /**
     * true-移动端员工会话，false-PC系统用户会话
     */
    private final boolean employee;

    public UserSessionEvictEvent(Long userId) {
        this(userId, false);
    }

    public UserSessionEvictEvent(Long userId, boolean employee) {
        this.userId = userId;
        this.employee = employee;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isEmployee() {
        return employee;
    }
}
