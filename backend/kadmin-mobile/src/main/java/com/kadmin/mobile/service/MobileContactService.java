package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.domain.MobileChatGroup;
import com.kadmin.mobile.domain.MobileChatGroupMember;
import com.kadmin.mobile.mapper.MobileChatGroupMapper;
import com.kadmin.mobile.mapper.MobileChatGroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MobileContactService {

    private final EmployeeMapper employeeMapper;
    private final MobileChatGroupMapper groupMapper;
    private final MobileChatGroupMemberMapper groupMemberMapper;

    public Map<String, Object> getSummary(Long employeeId) {
        Long groupCount = groupMemberMapper.selectCount(new LambdaQueryWrapper<MobileChatGroupMember>()
                .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                .eq(MobileChatGroupMember::getStatus, 1));

        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("groupCount", groupCount == null ? 0 : groupCount);
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
}
