package com.kadmin.common.event;

/**
 * 站内通知创建事件
 * NotificationService 写入通知后发布，由 WebSocket 推送监听器实时通知在线员工
 */
public class NotificationCreatedEvent {

    /**
     * 接收员工ID
     */
    private final Long employeeId;

    /**
     * 通知ID
     */
    private final Long notificationId;

    /**
     * 通知类型（approval_result/approval_todo/contract/probation/cert/birthday/anniversary...）
     */
    private final String type;

    /**
     * 标题
     */
    private final String title;

    /**
     * 内容
     */
    private final String content;

    public NotificationCreatedEvent(Long employeeId, Long notificationId, String type, String title,
            String content) {
        this.employeeId = employeeId;
        this.notificationId = notificationId;
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
