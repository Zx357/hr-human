package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrRewardPunish;
import com.kadmin.hr.mapper.HrRewardPunishMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class RewardPunishService extends ServiceImpl<HrRewardPunishMapper, HrRewardPunish> {

    public Page<HrRewardPunish> getPage(int pageNum, int pageSize, String employeeName, Integer type, Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, type, status);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrRewardPunish record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("奖惩记录不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("无效的审批操作");
        }

        // 条件更新（仅待审批状态可流转），防止并发重复审批
        LambdaUpdateWrapper<HrRewardPunish> wrapper = new LambdaUpdateWrapper<HrRewardPunish>()
                .eq(HrRewardPunish::getId, id)
                .set(HrRewardPunish::getStatus, status)
                .set(HrRewardPunish::getApproveRemark, remark)
                .set(HrRewardPunish::getApproveBy, approveBy)
                .set(HrRewardPunish::getApproveTime, LocalDateTime.now());
        if (record.getStatus() != null) {
            wrapper.eq(HrRewardPunish::getStatus, 0);
        }
        boolean result = update(wrapper);
        if (!result) {
            throw new IllegalArgumentException("该记录已被处理，请刷新后查看");
        }
        return true;
    }
}
