package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.common.event.NotificationCreatedEvent;
import com.kadmin.system.domain.SysNotification;
import com.kadmin.system.mapper.SysNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内通知服务：写入、分页查询、未读数、全部已读
 * 写入成功后发布 NotificationCreatedEvent，由 WebSocket 监听器实时推给在线员工
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService extends ServiceImpl<SysNotificationMapper, SysNotification> {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建通知（通知写入失败不影响主业务）
     */
    public void notify(Long employeeId, String type, String title, String content, Long refId, String url) {
        if (employeeId == null) {
            return;
        }
        try {
            SysNotification notification = new SysNotification();
            notification.setEmployeeId(employeeId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRefId(refId);
            notification.setUrl(url);
            notification.setReadFlag(0);
            save(notification);
            // 推送失败不影响通知写入（监听器内部兜底）
            eventPublisher.publishEvent(new NotificationCreatedEvent(employeeId, notification.getId(), type, title,
                    content));
        } catch (Exception e) {
            log.warn("写入站内通知失败: employeeId={}, type={}, refId={}", employeeId, type, refId, e);
        }
    }

    /**
     * 同一业务事件去重：已存在同类型同业务的未读通知则不再写入
     */
    public void notifyOnce(Long employeeId, String type, String title, String content, Long refId, String url) {
        if (employeeId == null || refId == null) {
            return;
        }
        Long exists = count(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getEmployeeId, employeeId)
                .eq(SysNotification::getType, type)
                .eq(SysNotification::getRefId, refId));
        if (exists != null && exists > 0) {
            return;
        }
        notify(employeeId, type, title, content, refId, url);
    }

    public Page<SysNotification> pageForEmployee(int pageNum, int pageSize, Long employeeId) {
        return page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getEmployeeId, employeeId)
                .orderByDesc(SysNotification::getId));
    }

    public List<SysNotification> topForEmployee(Long employeeId, int limit) {
        return list(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getEmployeeId, employeeId)
                .orderByDesc(SysNotification::getId)
                .last("LIMIT " + limit));
    }

    public long unreadCount(Long employeeId) {
        Long count = count(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getEmployeeId, employeeId)
                .eq(SysNotification::getReadFlag, 0));
        return count != null ? count : 0;
    }

    /**
     * 全部已读
     */
    public void markAllRead(Long employeeId) {
        SysNotification entity = new SysNotification();
        entity.setReadFlag(1);
        entity.setReadTime(LocalDateTime.now());
        update(entity, new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getEmployeeId, employeeId)
                .eq(SysNotification::getReadFlag, 0));
    }
}
