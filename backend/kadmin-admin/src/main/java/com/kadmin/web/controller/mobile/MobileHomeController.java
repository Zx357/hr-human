package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.mobile.domain.dto.MobileHomeMessageDTO;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.system.domain.SysNotice;
import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.system.mapper.SysNoticeMapper;
import com.kadmin.system.service.NotificationService;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端首页接口
 */
@RestController
@RequestMapping("/mobile/home")
@RequiredArgsConstructor
public class MobileHomeController {

    private final AttClockRecordMapper clockRecordMapper;
    private final HrApplicationMapper applicationMapper;
    private final SysNoticeMapper noticeMapper;
    private final NotificationService notificationService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            employeeId = loginUser.getUserId();
        }

        Map<String, Object> stats = new HashMap<>();

        // 本月出勤天数：统计本月有上班打卡(clockType=1)的不同日期数（按日期去重，防一日多卡虚增）
        YearMonth ym = YearMonth.now();
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = ym.atEndOfMonth().atTime(LocalTime.MAX);
        Long attendDays = clockRecordMapper.countDistinctAttendDays(employeeId, monthStart, monthEnd);
        stats.put("monthAttendDays", attendDays != null ? attendDays : 0);

        // 待处理（我的待审批申请）
        Long pendingCount = applicationMapper.selectCount(
                new LambdaQueryWrapper<HrApplication>()
                        .eq(HrApplication::getEmployeeId, employeeId)
                        .eq(HrApplication::getStatus, 0));
        stats.put("pendingCount", pendingCount != null ? pendingCount : 0);

        // 已通过
        Long approvedCount = applicationMapper.selectCount(
                new LambdaQueryWrapper<HrApplication>()
                        .eq(HrApplication::getEmployeeId, employeeId)
                        .eq(HrApplication::getStatus, 1));
        stats.put("approvedCount", approvedCount != null ? approvedCount : 0);

        // 待我审批（作为审批人需要处理的）
        Long approvalCount = applicationMapper.selectMobilePendingCount(employeeId);
        stats.put("approvalCount", approvalCount != null ? approvalCount : 0);

        Long noticeCount = noticeMapper.selectCount(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1));
        stats.put("noticeCount", noticeCount != null ? noticeCount : 0);

        // 站内通知未读数（审批结果/待审批/到期提醒）
        stats.put("unreadNotifications", notificationService.unreadCount(employeeId));

        return Result.success(stats);
    }

    @GetMapping("/messages")
    public Result<List<MobileHomeMessageDTO>> getMessages() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }

        List<MobileHomeMessageDTO> messages = new ArrayList<>();

        Long approvalCount = applicationMapper.selectMobilePendingCount(employeeId);
        if (approvalCount != null && approvalCount > 0) {
            messages.add(MobileHomeMessageDTO.builder()
                    .id("approval")
                    .title("审批通知")
                    .desc("你有 " + approvalCount + " 条待审批事项需要处理")
                    .time("现在")
                    .color("#4B98FE")
                    .icon("menu-fill")
                    .badge(formatBadge(approvalCount))
                    .url("/homePages/approval")
                    .type("approval")
                    .build());
        }

        Long pendingCount = applicationMapper.selectCount(new LambdaQueryWrapper<HrApplication>()
                .eq(HrApplication::getEmployeeId, employeeId)
                .eq(HrApplication::getStatus, 0));
        if (pendingCount != null && pendingCount > 0) {
            messages.add(MobileHomeMessageDTO.builder()
                    .id("pending")
                    .title("待办提醒")
                    .desc("你有 " + pendingCount + " 条申请正在处理中")
                    .time("现在")
                    .color("#00C8B0")
                    .icon("flag-fill")
                    .badge(formatBadge(pendingCount))
                    .url("/homePages/pending")
                    .type("pending")
                    .build());
        }

        List<SysNotice> notices = noticeMapper.selectList(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1)
                .orderByDesc(SysNotice::getPublishTime)
                .orderByDesc(SysNotice::getCreatedTime)
                .last("LIMIT 5"));
        for (SysNotice notice : notices) {
            boolean isNotify = notice.getNoticeType() != null && notice.getNoticeType() == 2;
            messages.add(MobileHomeMessageDTO.builder()
                    .id("notice-" + notice.getId())
                    .title(notice.getNoticeTitle())
                    .desc(stripHtml(notice.getNoticeContent()))
                    .time(formatDate(notice.getPublishTime()))
                    .color(isNotify ? "#4B98FE" : "#00C8B0")
                    .icon(isNotify ? "notice-fill" : "image-text-fill")
                    .url("/homePages/notice")
                    .type("notice")
                    .build());
        }

        List<HrApplication> applications = applicationMapper.selectList(new LambdaQueryWrapper<HrApplication>()
                .eq(HrApplication::getEmployeeId, employeeId)
                .in(HrApplication::getStatus, 1, 2)
                .orderByDesc(HrApplication::getApproveTime)
                .orderByDesc(HrApplication::getCreatedTime)
                .last("LIMIT 3"));
        for (HrApplication application : applications) {
            messages.add(MobileHomeMessageDTO.builder()
                    .id("application-" + application.getId())
                    .title("申请结果")
                    .desc((application.getTitle() != null ? application.getTitle() : "你的申请")
                            + statusText(application.getStatus()))
                    .time(formatTime(application.getApproveTime() != null
                            ? application.getApproveTime()
                            : application.getCreatedTime()))
                    .color(application.getStatus() != null && application.getStatus() == 1 ? "#00C8B0" : "#FB6A67")
                    .icon("ticket-fill")
                    .url("/homePages/application")
                    .type("application")
                    .build());
        }

        return Result.success(messages);
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }

    private String stripHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("<[^>]+>", "").replace("&nbsp;", " ").trim();
    }

    private String formatBadge(Long value) {
        if (value == null || value <= 0) {
            return "";
        }
        return value > 99 ? "99+" : String.valueOf(value);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("MM-dd"));
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }

    private String statusText(Integer status) {
        if (status != null && status == 1) {
            return "已通过";
        }
        if (status != null && status == 2) {
            return "已驳回";
        }
        return "状态已更新";
    }
}
