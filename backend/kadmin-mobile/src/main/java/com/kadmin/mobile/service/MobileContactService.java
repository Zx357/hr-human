package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.mobile.domain.MobileChatGroup;
import com.kadmin.mobile.domain.MobileChatGroupMember;
import com.kadmin.mobile.domain.MobileContactRequest;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.mapper.MobileChatGroupMapper;
import com.kadmin.mobile.mapper.MobileChatGroupMemberMapper;
import com.kadmin.mobile.mapper.MobileContactRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MobileContactService extends ServiceImpl<MobileContactRequestMapper, MobileContactRequest> {

    private final EmployeeMapper employeeMapper;
    private final MobileChatGroupMapper groupMapper;
    private final MobileChatGroupMemberMapper groupMemberMapper;

    public Map<String, Object> getSummary(Long employeeId) {
        Long groupCount = groupMemberMapper.selectCount(new LambdaQueryWrapper<MobileChatGroupMember>()
                .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                .eq(MobileChatGroupMember::getStatus, 1));
        Long pendingRequestCount = count(new LambdaQueryWrapper<MobileContactRequest>()
                .eq(MobileContactRequest::getTargetId, employeeId)
                .eq(MobileContactRequest::getStatus, 0));

        Map<String, Object> summary = new HashMap<>();
        summary.put("groupCount", groupCount == null ? 0 : groupCount);
        summary.put("pendingRequestCount", pendingRequestCount == null ? 0 : pendingRequestCount);
        return summary;
    }

    public Page<HrEmployee> searchEmployees(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(HrEmployee::getName, keyword)
                    .or()
                    .like(HrEmployee::getEmployeeNo, keyword)
                    .or()
                    .like(HrEmployee::getPhone, keyword));
        }
        wrapper.orderByAsc(HrEmployee::getName);
        return employeeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<MobileChatGroup> listGroups(Long employeeId) {
        List<MobileChatGroupMember> members = groupMemberMapper.selectList(new LambdaQueryWrapper<MobileChatGroupMember>()
                .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                .eq(MobileChatGroupMember::getStatus, 1)
                .orderByDesc(MobileChatGroupMember::getCreatedTime));

        List<Long> groupIds = members.stream()
                .map(MobileChatGroupMember::getGroupId)
                .distinct()
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }

        List<MobileChatGroup> groups = groupMapper.selectBatchIds(groupIds);
        groups.forEach(this::fillOwnerName);
        groups.sort((left, right) -> {
            LocalDateTime l = left.getLastMessageTime() != null ? left.getLastMessageTime() : left.getCreatedTime();
            LocalDateTime r = right.getLastMessageTime() != null ? right.getLastMessageTime() : right.getCreatedTime();
            if (l == null && r == null) {
                return 0;
            }
            if (l == null) {
                return 1;
            }
            if (r == null) {
                return -1;
            }
            return r.compareTo(l);
        });
        return groups;
    }

    @Transactional
    public MobileChatGroup createGroup(String groupName, List<Long> memberIds, Long ownerId) {
        if (!StringUtils.hasText(groupName)) {
            throw new IllegalArgumentException("群名称不能为空");
        }

        Set<Long> ids = new LinkedHashSet<>();
        ids.add(ownerId);
        if (memberIds != null) {
            memberIds.stream()
                    .filter(id -> id != null && id > 0)
                    .forEach(ids::add);
        }

        MobileChatGroup group = new MobileChatGroup();
        group.setGroupName(groupName.trim());
        group.setOwnerId(ownerId);
        group.setMemberCount(ids.size());
        group.setLastMessage("群聊已创建");
        group.setLastMessageTime(LocalDateTime.now());
        group.setStatus(1);
        groupMapper.insert(group);

        for (Long id : ids) {
            MobileChatGroupMember member = new MobileChatGroupMember();
            member.setGroupId(group.getId());
            member.setEmployeeId(id);
            member.setRoleType(id.equals(ownerId) ? 1 : 2);
            member.setStatus(1);
            groupMemberMapper.insert(member);
        }

        fillOwnerName(group);
        return group;
    }

    public List<MobileContactRequest> listRequests(Long employeeId, String type) {
        LambdaQueryWrapper<MobileContactRequest> wrapper = new LambdaQueryWrapper<>();
        if ("sent".equals(type)) {
            wrapper.eq(MobileContactRequest::getRequesterId, employeeId);
        } else {
            wrapper.eq(MobileContactRequest::getTargetId, employeeId);
        }
        wrapper.orderByDesc(MobileContactRequest::getCreatedTime);

        List<MobileContactRequest> list = list(wrapper);
        list.forEach(this::fillRequestEmployeeNames);
        return list;
    }

    @Transactional
    public MobileContactRequest sendRequest(Long targetEmployeeId, String remark, Long requesterId) {
        if (targetEmployeeId == null || targetEmployeeId <= 0) {
            throw new IllegalArgumentException("请选择联系人");
        }
        if (targetEmployeeId.equals(requesterId)) {
            throw new IllegalArgumentException("不能添加自己");
        }
        HrEmployee target = employeeMapper.selectById(targetEmployeeId);
        if (target == null || (target.getStatus() != null && target.getStatus() != 1)) {
            throw new IllegalArgumentException("联系人不存在");
        }

        MobileContactRequest existing = getOne(new LambdaQueryWrapper<MobileContactRequest>()
                .eq(MobileContactRequest::getRequesterId, requesterId)
                .eq(MobileContactRequest::getTargetId, targetEmployeeId)
                .eq(MobileContactRequest::getStatus, 0)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        MobileContactRequest request = new MobileContactRequest();
        request.setRequesterId(requesterId);
        request.setTargetId(targetEmployeeId);
        request.setStatus(0);
        request.setRemark(remark);
        save(request);
        fillRequestEmployeeNames(request);
        return request;
    }

    @Transactional
    public MobileContactRequest handleRequest(Long requestId, Integer status, Long employeeId) {
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("处理状态不正确");
        }
        MobileContactRequest request = getById(requestId);
        if (request == null || !employeeId.equals(request.getTargetId())) {
            throw new IllegalArgumentException("申请不存在");
        }
        request.setStatus(status);
        request.setHandledTime(LocalDateTime.now());
        updateById(request);
        fillRequestEmployeeNames(request);
        return request;
    }

    public List<HrEmployee> listGroupMembers(Long groupId, Long employeeId) {
        Long membership = groupMemberMapper.selectCount(new LambdaQueryWrapper<MobileChatGroupMember>()
                .eq(MobileChatGroupMember::getGroupId, groupId)
                .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                .eq(MobileChatGroupMember::getStatus, 1));
        if (membership == null || membership <= 0) {
            return List.of();
        }

        List<Long> memberIds = groupMemberMapper.selectList(new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getGroupId, groupId)
                        .eq(MobileChatGroupMember::getStatus, 1))
                .stream()
                .map(MobileChatGroupMember::getEmployeeId)
                .toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(employeeMapper.selectBatchIds(memberIds));
    }

    private void fillOwnerName(MobileChatGroup group) {
        if (group.getOwnerId() == null) {
            return;
        }
        HrEmployee owner = employeeMapper.selectById(group.getOwnerId());
        if (owner != null) {
            group.setOwnerName(owner.getName());
        }
    }

    private void fillRequestEmployeeNames(MobileContactRequest request) {
        HrEmployee requester = employeeMapper.selectById(request.getRequesterId());
        if (requester != null) {
            request.setRequesterName(requester.getName());
            request.setRequesterAvatar(requester.getAvatar());
        }
        HrEmployee target = employeeMapper.selectById(request.getTargetId());
        if (target != null) {
            request.setTargetName(target.getName());
            request.setTargetAvatar(target.getAvatar());
        }
    }
}
