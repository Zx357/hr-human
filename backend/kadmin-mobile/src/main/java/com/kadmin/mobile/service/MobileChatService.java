package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.domain.MobileChatGroup;
import com.kadmin.mobile.domain.MobileChatGroupMember;
import com.kadmin.mobile.domain.MobileChatMessage;
import com.kadmin.mobile.domain.MobileChatReadState;
import com.kadmin.mobile.mapper.MobileChatGroupMapper;
import com.kadmin.mobile.mapper.MobileChatGroupMemberMapper;
import com.kadmin.mobile.mapper.MobileChatMessageMapper;
import com.kadmin.mobile.mapper.MobileChatReadStateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final MobileChatReadStateMapper readStateMapper;
    private final EmployeeMapper employeeMapper;

    /**
     * 拉取会话消息
     *
     * @param afterMessageId 增量参数：仅返回 id 大于该值的消息；为空时返回最近 N 条
     */
    public List<MobileChatMessage> listMessages(Long employeeId, Integer chatType, Long targetId,
            Long afterMessageId) {
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
            // 群消息仅群成员可读
            requireGroupMember(employeeId, targetId);
            wrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_GROUP)
                    .eq(MobileChatMessage::getGroupId, targetId);
        }

        if (afterMessageId != null && afterMessageId > 0) {
            // 增量拉取：只取新增消息，按时间正序
            wrapper.gt(MobileChatMessage::getId, afterMessageId)
                    .orderByAsc(MobileChatMessage::getId)
                    .last("LIMIT " + MAX_MESSAGES);
            List<MobileChatMessage> list = messageMapper.selectList(wrapper);
            fillSenderInfo(list);
            return list;
        }

        // 全量拉取：取最近 N 条后按时间正序返回
        wrapper.orderByDesc(MobileChatMessage::getCreatedTime).last("LIMIT " + MAX_MESSAGES);
        List<MobileChatMessage> list = messageMapper.selectList(wrapper);
        Collections.reverse(list);
        fillSenderInfo(list);
        return list;
    }

    /**
     * 最近会话列表（群聊 + 单聊），含最后一条消息与未读数
     */
    public List<Map<String, Object>> getConversations(Long employeeId) {
        Map<String, Map<String, Object>> conversations = new LinkedHashMap<>();

        // 1. 群聊：我的群成员关系
        List<MobileChatGroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                        .eq(MobileChatGroupMember::getStatus, 1));
        if (!memberships.isEmpty()) {
            List<Long> groupIds = memberships.stream()
                    .map(MobileChatGroupMember::getGroupId)
                    .distinct()
                    .toList();
            java.util.Map<Long, MobileChatGroup> groupMap = groupMapper.selectBatchIds(groupIds).stream()
                    .collect(Collectors.toMap(MobileChatGroup::getId, g -> g, (a, b) -> a));
            for (MobileChatGroupMember membership : memberships) {
                MobileChatGroup group = groupMap.get(membership.getGroupId());
                if (group == null || (group.getStatus() != null && group.getStatus() != 1)) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("chatType", CHAT_TYPE_GROUP);
                item.put("targetId", group.getId());
                item.put("name", group.getGroupName());
                item.put("avatar", group.getAvatar());
                item.put("lastMessage", group.getLastMessage());
                item.put("lastMessageTime", group.getLastMessageTime());
                conversations.put("G" + group.getId(), item);
            }
        }

        // 2. 单聊：最近涉及我的单聊消息按对方聚合
        List<MobileChatMessage> recentSingles = messageMapper.selectList(
                new LambdaQueryWrapper<MobileChatMessage>()
                        .eq(MobileChatMessage::getChatType, CHAT_TYPE_SINGLE)
                        .and(w -> w.eq(MobileChatMessage::getFromEmployeeId, employeeId)
                                .or().eq(MobileChatMessage::getPeerEmployeeId, employeeId))
                        .orderByDesc(MobileChatMessage::getId)
                        .last("LIMIT 500"));
        Map<Long, MobileChatMessage> latestByPeer = new LinkedHashMap<>();
        for (MobileChatMessage message : recentSingles) {
            Long peerId = message.getFromEmployeeId().equals(employeeId)
                    ? message.getPeerEmployeeId()
                    : message.getFromEmployeeId();
            if (peerId == null || latestByPeer.containsKey(peerId)) {
                continue;
            }
            latestByPeer.put(peerId, message);
        }
        List<Long> peerIds = new ArrayList<>(latestByPeer.keySet());
        Map<Long, HrEmployee> peers = peerIds.isEmpty() ? Map.of()
                : employeeMapper.selectBatchIds(peerIds).stream()
                        .collect(Collectors.toMap(HrEmployee::getId, Function.identity(), (a, b) -> a));
        for (Map.Entry<Long, MobileChatMessage> entry : latestByPeer.entrySet()) {
            HrEmployee peer = peers.get(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("chatType", CHAT_TYPE_SINGLE);
            item.put("targetId", entry.getKey());
            item.put("name", peer != null ? peer.getName() : "未知同事");
            item.put("avatar", peer != null ? peer.getAvatar() : null);
            item.put("lastMessage", entry.getValue().getContent());
            item.put("lastMessageTime", entry.getValue().getCreatedTime());
            conversations.put("S" + entry.getKey(), item);
        }

        // 3. 计算未读数
        List<MobileChatReadState> readStates = readStateMapper.selectList(
                new LambdaQueryWrapper<MobileChatReadState>()
                        .eq(MobileChatReadState::getEmployeeId, employeeId));
        Map<String, Long> readMap = readStates.stream()
                .collect(Collectors.toMap(r -> r.getChatType() + ":" + r.getTargetId(),
                        MobileChatReadState::getLastReadMessageId, (a, b) -> Math.max(a, b)));

        for (Map<String, Object> item : conversations.values()) {
            Integer chatType = (Integer) item.get("chatType");
            Long targetId = (Long) item.get("targetId");
            Long lastReadId = readMap.getOrDefault(chatType + ":" + targetId, 0L);
            LambdaQueryWrapper<MobileChatMessage> unreadWrapper = new LambdaQueryWrapper<MobileChatMessage>()
                    .gt(MobileChatMessage::getId, lastReadId)
                    .ne(MobileChatMessage::getFromEmployeeId, employeeId);
            if (chatType == CHAT_TYPE_SINGLE) {
                unreadWrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_SINGLE)
                        .eq(MobileChatMessage::getFromEmployeeId, targetId)
                        .eq(MobileChatMessage::getPeerEmployeeId, employeeId);
            } else {
                unreadWrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_GROUP)
                        .eq(MobileChatMessage::getGroupId, targetId);
            }
            Long unread = messageMapper.selectCount(unreadWrapper);
            item.put("unreadCount", unread != null ? unread : 0);
        }

        // 4. 按最后消息时间倒序（LocalDateTime 类型比较，无消息排最后）
        return conversations.values().stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = toDateTime(a.get("lastMessageTime"));
                    LocalDateTime tb = toDateTime(b.get("lastMessageTime"));
                    LocalDateTime min = LocalDateTime.MIN;
                    LocalDateTime la = ta != null ? ta : min;
                    LocalDateTime lb = tb != null ? tb : min;
                    return lb.compareTo(la);
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime toDateTime(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof java.time.OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        return null;
    }

    /**
     * 标记会话已读（已读位置推进到会话最新一条消息）
     */
    @Transactional
    public void markRead(Long employeeId, Integer chatType, Long targetId) {
        LambdaQueryWrapper<MobileChatMessage> wrapper = new LambdaQueryWrapper<MobileChatMessage>()
                .orderByDesc(MobileChatMessage::getId)
                .last("LIMIT 1");
        if (chatType != null && chatType == CHAT_TYPE_SINGLE) {
            wrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_SINGLE)
                    .and(w -> w
                            .and(q -> q.eq(MobileChatMessage::getFromEmployeeId, employeeId)
                                    .eq(MobileChatMessage::getPeerEmployeeId, targetId))
                            .or(q -> q.eq(MobileChatMessage::getFromEmployeeId, targetId)
                                    .eq(MobileChatMessage::getPeerEmployeeId, employeeId)));
        } else {
            wrapper.eq(MobileChatMessage::getChatType, CHAT_TYPE_GROUP)
                    .eq(MobileChatMessage::getGroupId, targetId);
        }
        MobileChatMessage latest = messageMapper.selectOne(wrapper);
        if (latest == null) {
            return;
        }

        MobileChatReadState state = readStateMapper.selectOne(new LambdaQueryWrapper<MobileChatReadState>()
                .eq(MobileChatReadState::getEmployeeId, employeeId)
                .eq(MobileChatReadState::getChatType, chatType)
                .eq(MobileChatReadState::getTargetId, targetId)
                .last("LIMIT 1"));
        if (state == null) {
            state = new MobileChatReadState();
            state.setEmployeeId(employeeId);
            state.setChatType(chatType);
            state.setTargetId(targetId);
            state.setLastReadMessageId(latest.getId());
            readStateMapper.insert(state);
        } else {
            state.setLastReadMessageId(latest.getId());
            readStateMapper.updateById(state);
        }
    }

    /**
     * 未读消息总数（用于首页角标）
     */
    public long getTotalUnread(Long employeeId) {
        long total = 0;
        for (Map<String, Object> conversation : getConversations(employeeId)) {
            Object unread = conversation.get("unreadCount");
            if (unread instanceof Number number) {
                total += number.longValue();
            }
        }
        return total;
    }

    @Transactional
    public MobileChatMessage send(Long employeeId, Integer chatType, Long targetId, String content) {
        return send(employeeId, chatType, targetId, content, 1);
    }

    @Transactional
    public MobileChatMessage send(Long employeeId, Integer chatType, Long targetId, String content, Integer msgType) {
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
            requireGroupMember(employeeId, targetId);
            message.setChatType(CHAT_TYPE_GROUP);
            message.setGroupId(targetId);
            group.setLastMessage("2".equals(String.valueOf(msgType)) ? "[图片]" : message.getContent());
            group.setLastMessageTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        message.setMsgType(msgType != null && msgType == 2 ? 2 : 1);

        messageMapper.insert(message);
        fillSenderInfo(Collections.singletonList(message));
        return message;
    }

    /**
     * 群成员校验：非群成员禁止读/发群消息
     */
    private void requireGroupMember(Long employeeId, Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("群聊不能为空");
        }
        Long memberCount = groupMemberMapper.selectCount(new LambdaQueryWrapper<MobileChatGroupMember>()
                .eq(MobileChatGroupMember::getGroupId, groupId)
                .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                .eq(MobileChatGroupMember::getStatus, 1));
        if (memberCount == null || memberCount == 0) {
            throw new IllegalArgumentException("你不在该群聊中");
        }
    }

    private void fillSenderInfo(List<MobileChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<Long> senderIds = messages.stream()
                .map(MobileChatMessage::getFromEmployeeId)
                .filter(Objects::nonNull)
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
