package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.domain.MobileChatGroup;
import com.kadmin.mobile.domain.MobileChatGroupMember;
import com.kadmin.mobile.domain.MobileChatMessage;
import com.kadmin.mobile.mapper.MobileChatGroupMapper;
import com.kadmin.mobile.mapper.MobileChatGroupMemberMapper;
import com.kadmin.mobile.mapper.MobileChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mobile chat service: group chat and single (1:1) chat.
 */
@Service
@RequiredArgsConstructor
public class MobileChatService {

    private static final int CHAT_TYPE_GROUP = 1;
    private static final int CHAT_TYPE_SINGLE = 2;
    private static final int MAX_MESSAGES = 200;

    private final MobileChatMessageMapper messageMapper;
    private final MobileChatGroupMapper groupMapper;
    private final MobileChatGroupMemberMapper groupMemberMapper;
    private final EmployeeMapper employeeMapper;

    public List<MobileChatMessage> listMessages(Long employeeId, Integer chatType, Long targetId) {
        LambdaQueryWrapper<MobileChatMessage> wrapper = new LambdaQueryWrapper<>();
        if (chatType != null && chatType == CHAT_TYPE_SINGLE) {
            Long peerId = targetId;
            wrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_SINGLE)
                    .and(w -> w
                            .and(q -> q.eq(MobileChatMessage::getFromEmployeeId, employeeId)
                                    .eq(MobileChatMessage::getPeerEmployeeId, peerId))
                            .or(q -> q.eq(MobileChatMessage::getFromEmployeeId, peerId)
                                    .eq(MobileChatMessage::getPeerEmployeeId, employeeId)));
        } else {
            wrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_GROUP)
                    .eq(MobileChatMessage::getGroupId, targetId);
        }
        // 取最近 N 条后按时间正序返回
        wrapper.orderByDesc(MobileChatMessage::getCreatedTime).last("LIMIT " + MAX_MESSAGES);
        List<MobileChatMessage> list = messageMapper.selectList(wrapper);
        Collections.reverse(list);
        fillSenderInfo(list);
        return list;
    }

    @Transactional
    public MobileChatMessage send(Long employeeId, Integer chatType, Long targetId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("会话目标不能为空");
        }

        MobileChatMessage message = new MobileChatMessage();
        message.setFromEmployeeId(employeeId);
        message.setContent(content.trim());

        if (chatType != null && chatType == CHAT_TYPE_SINGLE) {
            if (targetId.equals(employeeId)) {
                throw new IllegalArgumentException("不能给自己发消息");
            }
            HrEmployee peer = employeeMapper.selectById(targetId);
            if (peer == null) {
                throw new IllegalArgumentException("对方不存在");
            }
            message.setChatType(CHAT_TYPE_SINGLE);
            message.setPeerEmployeeId(targetId);
        } else {
            MobileChatGroup group = groupMapper.selectById(targetId);
            if (group == null) {
                throw new IllegalArgumentException("群聊不存在");
            }
            Long memberCount = groupMemberMapper.selectCount(new LambdaQueryWrapper<MobileChatGroupMember>()
                    .eq(MobileChatGroupMember::getGroupId, targetId)
                    .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                    .eq(MobileChatGroupMember::getStatus, 1));
            if (memberCount == null || memberCount == 0) {
                throw new IllegalArgumentException("你不在该群聊中");
            }
            message.setChatType(CHAT_TYPE_GROUP);
            message.setGroupId(targetId);
            group.setLastMessage(message.getContent());
            group.setLastMessageTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }

        messageMapper.insert(message);
        fillSenderInfo(Collections.singletonList(message));
        return message;
    }

    private void fillSenderInfo(List<MobileChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<Long> senderIds = messages.stream()
                .map(MobileChatMessage::getFromEmployeeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (senderIds.isEmpty()) {
            return;
        }
        Map<Long, HrEmployee> employees = employeeMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(HrEmployee::getId, Function.identity(), (a, b) -> a));
        for (MobileChatMessage message : messages) {
            HrEmployee employee = employees.get(message.getFromEmployeeId());
            if (employee != null) {
                message.setFromName(employee.getName());
                message.setFromAvatar(employee.getAvatar());
            }
        }
    }
}
