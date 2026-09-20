package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.system.domain.SysNotice;
import com.kadmin.system.domain.SysNoticeRead;
import com.kadmin.system.domain.SysNotification;
import com.kadmin.system.mapper.SysNoticeMapper;
import com.kadmin.system.mapper.SysNoticeReadMapper;
import com.kadmin.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 移动端站内通知与公告已读接口
 */
@RestController
@RequestMapping("/mobile")
@RequiredArgsConstructor
public class MobileNotificationController {

    private final NotificationService notificationService;
    private final SysNoticeMapper noticeMapper;
    private final SysNoticeReadMapper noticeReadMapper;

    /**
     * 我的通知分页（含未读标记）
     */
    @GetMapping("/notifications")
    public Result<Page<SysNotification>> notifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(notificationService.pageForEmployee(pageNum, pageSize, employeeId));
    }

    /**
     * 最近通知（消息中心顶部展示）
     */
    @GetMapping("/notifications/top")
    public Result<List<SysNotification>> topNotifications(
            @RequestParam(defaultValue = "10") Integer limit) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(notificationService.topForEmployee(employeeId, Math.min(limit, 50)));
    }

    @GetMapping("/notifications/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.unreadCount(employeeId));
        return Result.success(data);
    }

    @PostMapping("/notifications/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        notificationService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysNotification>()
                .eq(SysNotification::getId, id)
                .eq(SysNotification::getEmployeeId, employeeId)
                .eq(SysNotification::getReadFlag, 0)
                .set(SysNotification::getReadFlag, 1)
                .set(SysNotification::getReadTime, LocalDateTime.now()));
        return Result.success();
    }

    @PostMapping("/notifications/read-all")
    public Result<Void> markAllRead() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        notificationService.markAllRead(employeeId);
        return Result.success();
    }

    /**
     * 公告分页（已发布，附带当前员工已读标记）
     */
    @GetMapping("/notices")
    public Result<Map<String, Object>> notices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer noticeType) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }

        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1)
                .eq(noticeType != null, SysNotice::getNoticeType, noticeType)
                .like(keyword != null && !keyword.isBlank(), SysNotice::getNoticeTitle, keyword)
                .orderByDesc(SysNotice::getPublishTime)
                .orderByDesc(SysNotice::getId);
        Page<SysNotice> page = noticeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Set<Long> readIds = loadReadNoticeIds(employeeId);
        List<Map<String, Object>> records = new ArrayList<>();
        for (SysNotice notice : page.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", notice.getId());
            item.put("noticeTitle", notice.getNoticeTitle());
            item.put("noticeType", notice.getNoticeType());
            item.put("noticeContent", notice.getNoticeContent());
            item.put("publishTime", notice.getPublishTime());
            item.put("readFlag", readIds.contains(notice.getId()) ? 1 : 0);
            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @GetMapping("/notices/unread-count")
    public Result<Map<String, Object>> noticeUnreadCount() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        Set<Long> readIds = loadReadNoticeIds(employeeId);
        Long total = noticeMapper.selectCount(new LambdaQueryWrapper<SysNotice>().eq(SysNotice::getStatus, 1));
        long unread = (total != null ? total : 0) - readIds.size();
        Map<String, Object> data = new HashMap<>();
        data.put("count", Math.max(unread, 0));
        return Result.success(data);
    }

    /**
     * 标记公告已读（幂等）
     */
    @PostMapping("/notices/{id}/read")
    public Result<Void> markNoticeRead(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        Long exists = noticeReadMapper.selectCount(new LambdaQueryWrapper<SysNoticeRead>()
                .eq(SysNoticeRead::getNoticeId, id)
                .eq(SysNoticeRead::getEmployeeId, employeeId));
        if (exists == null || exists == 0) {
            SysNoticeRead read = new SysNoticeRead();
            read.setNoticeId(id);
            read.setEmployeeId(employeeId);
            read.setReadTime(LocalDateTime.now());
            try {
                noticeReadMapper.insert(read);
            } catch (org.springframework.dao.DuplicateKeyException ignored) {
                // 并发下已存在则忽略
            }
        }
        return Result.success();
    }

    private Set<Long> loadReadNoticeIds(Long employeeId) {
        List<SysNoticeRead> reads = noticeReadMapper.selectList(new LambdaQueryWrapper<SysNoticeRead>()
                .eq(SysNoticeRead::getEmployeeId, employeeId)
                .select(SysNoticeRead::getNoticeId));
        Set<Long> ids = new HashSet<>();
        for (SysNoticeRead read : reads) {
            ids.add(read.getNoticeId());
        }
        return ids;
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }
}
