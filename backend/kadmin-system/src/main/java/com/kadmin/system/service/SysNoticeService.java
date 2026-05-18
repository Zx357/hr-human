package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.system.domain.SysNotice;
import com.kadmin.system.mapper.SysNoticeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 公告通知服务
 */
@Service
public class SysNoticeService extends ServiceImpl<SysNoticeMapper, SysNotice> {

    /**
     * 分页查询公告
     */
    public IPage<SysNotice> pageNotices(Page<SysNotice> page, String noticeTitle, Integer noticeType, Integer status) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(noticeTitle)) {
            wrapper.like(SysNotice::getNoticeTitle, noticeTitle);
        }
        if (noticeType != null) {
            wrapper.eq(SysNotice::getNoticeType, noticeType);
        }
        if (status != null) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getCreatedTime);
        return page(page, wrapper);
    }

    /**
     * 查询公告列表（不分页，供小程序使用）
     */
    public List<SysNotice> listNotices(Integer noticeType, Integer status) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (noticeType != null) {
            wrapper.eq(SysNotice::getNoticeType, noticeType);
        }
        if (status != null) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getCreatedTime);
        return list(wrapper);
    }

    /**
     * 新增公告
     */
    @Transactional
    public boolean addNotice(SysNotice notice) {
        return save(notice);
    }

    /**
     * 更新公告
     */
    @Transactional
    public boolean updateNotice(SysNotice notice) {
        return updateById(notice);
    }

    /**
     * 删除公告
     */
    @Transactional
    public boolean deleteNotice(Long id) {
        return removeById(id);
    }
}
