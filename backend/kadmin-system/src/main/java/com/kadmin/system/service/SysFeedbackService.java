package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.system.domain.SysFeedback;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.system.mapper.SysFeedbackMapper;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 意见反馈服务
 */
@Service
@RequiredArgsConstructor
public class SysFeedbackService extends ServiceImpl<SysFeedbackMapper, SysFeedback> {

    private final EmployeeMapper employeeMapper;

    public IPage<SysFeedback> pageFeedbacks(
            Page<SysFeedback> page,
            Integer feedbackType,
            Integer status,
            String keyword,
            String employeeNo) {
        LambdaQueryWrapper<SysFeedback> wrapper = new LambdaQueryWrapper<>();
        if (feedbackType != null) {
            wrapper.eq(SysFeedback::getFeedbackType, feedbackType);
        }
        if (status != null) {
            wrapper.eq(SysFeedback::getStatus, status);
        }
        if (StringUtils.hasText(employeeNo)) {
            wrapper.like(SysFeedback::getEmployeeNo, employeeNo);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SysFeedback::getFeedbackContent, keyword)
                    .or()
                    .like(SysFeedback::getContactName, keyword)
                    .or()
                    .like(SysFeedback::getContactPhone, keyword));
        }
        wrapper.orderByAsc(SysFeedback::getStatus).orderByDesc(SysFeedback::getCreatedTime);
        return page(page, wrapper);
    }

    public IPage<SysFeedback> pageMine(Page<SysFeedback> page) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        LambdaQueryWrapper<SysFeedback> wrapper = new LambdaQueryWrapper<>();
        if (loginUser != null && loginUser.getEmployeeId() != null) {
            wrapper.eq(SysFeedback::getEmployeeId, loginUser.getEmployeeId());
        } else if (loginUser != null) {
            wrapper.eq(SysFeedback::getCreatedBy, loginUser.getUserId());
        } else {
            wrapper.eq(SysFeedback::getId, -1L);
        }
        wrapper.orderByDesc(SysFeedback::getCreatedTime);
        return page(page, wrapper);
    }

    @Transactional
    public boolean submit(SysFeedback feedback) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser != null) {
            feedback.setEmployeeId(loginUser.getEmployeeId());
            if (!StringUtils.hasText(feedback.getContactName())) {
                feedback.setContactName(loginUser.getNickname() != null ? loginUser.getNickname() : loginUser.getUsername());
            }

            HrEmployee employee = loginUser.getEmployeeId() == null ? null : employeeMapper.selectById(loginUser.getEmployeeId());
            if (employee != null) {
                feedback.setEmployeeNo(employee.getEmployeeNo());
                if (!StringUtils.hasText(feedback.getContactName())) {
                    feedback.setContactName(employee.getName());
                }
                if (!StringUtils.hasText(feedback.getContactPhone())) {
                    feedback.setContactPhone(employee.getPhone());
                }
            } else if (StringUtils.hasText(loginUser.getUsername())) {
                feedback.setEmployeeNo(loginUser.getUsername());
            }
        }

        if (feedback.getFeedbackType() == null) {
            feedback.setFeedbackType(1);
        }
        feedback.setStatus(0);
        return save(feedback);
    }

    @Transactional
    public boolean reply(Long id, String replyContent) {
        SysFeedback feedback = getById(id);
        if (feedback == null) {
            return false;
        }
        feedback.setReplyContent(replyContent);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus(2);
        return updateById(feedback);
    }

    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        SysFeedback feedback = getById(id);
        if (feedback == null) {
            return false;
        }
        feedback.setStatus(status);
        if (status != null && status == 2 && feedback.getReplyTime() == null) {
            feedback.setReplyTime(LocalDateTime.now());
        }
        return updateById(feedback);
    }
}
