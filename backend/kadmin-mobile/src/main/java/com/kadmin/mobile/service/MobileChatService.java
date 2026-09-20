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
     * @param afterMessageId 增量参数：仅返回 id 大于该值的消息（轮询使用）
     * @param beforeMessageId 历史分页参数：仅返回 id 小于该值的消息，按时间倒序取 limit 条再反转为正序
     * @param limit 分页大小，默认 {@link #MAX_MESSAGES}
     */
    public List<MobileChatMessage> listMessages(Long employeeId, Integer chatType, Long targetId,
            Long afterMessageId, Long beforeMessageId, Integer limit) {
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

        int pageSize = (limit != null && limit > 0 && limit <= MAX_MESSAGES) ? limit : MAX_MESSAGES;

        if (afterMessageId != null && afterMessageId > 0) {
            // 增量拉取：只取新增消息，按时间正序
            wrapper.gt(MobileChatMessage::getId, afterMessageId)
                    .orderByAsc(MobileChatMessage::getId)
                    .last("LIMIT " + pageSize);
            List<MobileChatMessage> list = messageMapper.selectList(wrapper);
            fillSenderInfo(list);
            return list;
        }

        if (beforeMessageId != null && beforeMessageId > 0) {
            // 历史分页：取该消息之前的一页，倒序取后再反转
            wrapper.lt(MobileChatMessage::getId, beforeMessageId)
                    .orderByDesc(MobileChatMessage::getId)
                    .last("LIMIT " + pageSize);
            List<MobileChatMessage> list = messageMapper.selectList(wrapper);
            Collections.reverse(list);
            fillSenderInfo(list);
            return list;
        }

        // 首屏拉取：取最近一页后按时间正序返回
        wrapper.orderByDesc(MobileChatMessage::getId).last("LIMIT " + pageSize);
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

        // 3. 计算未读数（单条 GROUP BY 聚合查询，替代逐会话 count 的 N+1）
        Map<String, Long> unreadMap = countUnreadByConversation(employeeId,
                memberships.stream().map(MobileChatGroupMember::getGroupId).distinct().toList());

        for (Map<String, Object> item : conversations.values()) {
            Integer chatType = (Integer) item.get("chatType");
            Long targetId = (Long) item.get("targetId");
            item.put("unreadCount", unreadMap.getOrDefault(chatType + ":" + targetId, 0L));
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
     * 按会话聚合未读数：一条 GROUP BY 查询返回 {chatType:targetId -> 未读数}
     */
    private Map<String, Long> countUnreadByConversation(Long employeeId, List<Long> groupIds) {
        Map<String, Long> unreadMap = new HashMap<>();
        List<Map<String, Object>> rows = messageMapper.countUnreadGroupByConversation(employeeId, groupIds);
        for (Map<String, Object> row : rows) {
            Object chatType = row.get("chatType");
            Object targetId = row.get("targetId");
            Object unread = row.get("unread");
            if (chatType == null || targetId == null) {
                continue;
            }
            unreadMap.put(chatType + ":" + targetId, unread instanceof Number number ? number.longValue() : 0L);
        }
        return unreadMap;
    }

    /**
     * 未读消息总数（用于首页角标）：复用按会话聚合的结果，不再遍历会话列表
     */
    public long getTotalUnread(Long employeeId) {
        List<Long> groupIds = groupMemberMapper.selectList(
                new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                        .eq(MobileChatGroupMember::getStatus, 1))
                .stream()
                .map(MobileChatGroupMember::getGroupId)
                .distinct()
                .toList();
        long total = 0;
        for (Long unread : countUnreadByConversation(employeeId, groupIds).values()) {
            total += unread;
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

    /**
     * 群在职成员的员工ID列表（WS 实时推送用：群消息需要逐个推给除发送者外的成员）
     */
    public List<Long> getGroupMemberEmployeeIds(Long groupId) {
        if (groupId == null) {
            return List.of();
        }
        return groupMemberMapper.selectList(new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getGroupId, groupId)
                        .eq(MobileChatGroupMember::getStatus, 1))
                .stream()
                .map(MobileChatGroupMember::getEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
