package com.kadmin.hr.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrRewardPunish;
import com.kadmin.hr.mapper.HrRewardPunishMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class RewardPunishService extends ServiceImpl<HrRewardPunishMapper, HrRewardPunish> {

    public Page<HrRewardPunish> getPage(int pageNum, int pageSize, String employeeName, Integer type, Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, type, status);
    }

    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrRewardPunish entity = new HrRewardPunish();
        entity.setId(id);
        entity.setStatus(status);
        entity.setApproveRemark(remark);
        entity.setApproveBy(approveBy);
        entity.setApproveTime(LocalDateTime.now());
        return updateById(entity);
    }
}
