package com.kadmin.common.event;

/**
 * 员工删除事件
 * 由框架层监听：清理关联系统用户并吊销其全部会话（PC + 移动端）
 */
public class EmployeeDeletedEvent {

    /**
     * 被删除的员工ID
     */
    private final Long employeeId;

    public EmployeeDeletedEvent(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }
}
