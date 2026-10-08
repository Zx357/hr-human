package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.domain.MobileChatGroup;
import com.kadmin.mobile.domain.MobileChatGroupMember;
import com.kadmin.mobile.domain.MobileChatMessage;
import com.kadmin.mobile.domain.MobileChatReadState;
import com.kadmin.mobile.domain.vo.MobileChatGroupMemberVO;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    /** 会话管理接口契约的 targetType：1-单聊 2-群聊（与内部 chatType 取值相反，注意换算） */
    public static final int TARGET_TYPE_SINGLE = 1;
    public static final int TARGET_TYPE_GROUP = 2;

    /** 消息状态：0-正常 1-已撤回 */
    private static final int MSG_STATUS_NORMAL = 0;
    private static final int MSG_STATUS_RECALLED = 1;
    /** 消息撤回时限（分钟） */
    private static final int RECALL_WINDOW_MINUTES = 2;
    /** 会话 lastMessage 入库前最大长度（mobile_chat_group.last_message 列宽防护） */
    private static final int LAST_MESSAGE_MAX_LENGTH = 100;

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
            maskRecalled(list);
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
            maskRecalled(list);
            return list;
        }

        // 首屏拉取：取最近一页后按时间正序返回
        wrapper.orderByDesc(MobileChatMessage::getId).last("LIMIT " + pageSize);
        List<MobileChatMessage> list = messageMapper.selectList(wrapper);
        Collections.reverse(list);
        fillSenderInfo(list);
        maskRecalled(list);
        return list;
    }

    /**
     * 已撤回消息出参脱敏：内容替换为"xxx 撤回了一条消息"并带 recalled 标记（原内容保留在库中不删除）
     */
    private void maskRecalled(List<MobileChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (MobileChatMessage message : messages) {
            if (message.getStatus() != null && message.getStatus() == MSG_STATUS_RECALLED) {
                message.setRecalled(true);
                String sender = StringUtils.hasText(message.getFromName()) ? message.getFromName() : "对方";
                message.setContent(sender + " 撤回了一条消息");
            } else {
                message.setRecalled(false);
            }
        }
    }

    /**
     * 最近会话列表（群聊 + 单聊），含最后一条消息、未读数与置顶标记；
     * 已隐藏会话不出现在列表中（hidden 仅隐藏入口，消息记录保留）
     */
    public List<Map<String, Object>> getConversations(Long employeeId) {
        Map<String, Map<String, Object>> conversations = new LinkedHashMap<>();

        // 0. 会话个人设置（置顶/隐藏）：一次查出，用于过滤与排序
        Map<String, MobileChatReadState> settingsMap = loadConversationSettings(employeeId);

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

        // 4. 回填置顶标记并过滤隐藏会话（隐藏仅从列表移除，不删除消息）
        List<Map<String, Object>> visible = new ArrayList<>();
        for (Map<String, Object> item : conversations.values()) {
            Integer chatType = (Integer) item.get("chatType");
            Long targetId = (Long) item.get("targetId");
            String key = chatType + ":" + targetId;
            MobileChatReadState settings = settingsMap.get(key);
            if (settings != null && settings.getHidden() != null && settings.getHidden() == 1) {
                continue;
            }
            item.put("unreadCount", unreadMap.getOrDefault(key, 0L));
            item.put("sticky", settings != null && settings.getSticky() != null && settings.getSticky() == 1 ? 1 : 0);
            visible.add(item);
        }

        // 5. 排序：置顶优先，其次按最后消息时间倒序（LocalDateTime 类型比较，无消息排最后）
        return visible.stream()
                .sorted(Comparator
                        .comparingInt((Map<String, Object> item) -> (Integer) item.get("sticky")).reversed()
                        .thenComparing((a, b) -> {
                            LocalDateTime ta = toDateTime(a.get("lastMessageTime"));
                            LocalDateTime tb = toDateTime(b.get("lastMessageTime"));
                            LocalDateTime min = LocalDateTime.MIN;
                            LocalDateTime la = ta != null ? ta : min;
                            LocalDateTime lb = tb != null ? tb : min;
                            return lb.compareTo(la);
                        }))
                .collect(Collectors.toList());
    }

    /**
     * 当前员工的会话个人设置（sticky/hidden），key = chatType:targetId
     */
    private Map<String, MobileChatReadState> loadConversationSettings(Long employeeId) {
        Map<String, MobileChatReadState> map = new HashMap<>();
        for (MobileChatReadState state : readStateMapper.selectList(
                new LambdaQueryWrapper<MobileChatReadState>()
                        .eq(MobileChatReadState::getEmployeeId, employeeId))) {
            if (state.getChatType() == null || state.getTargetId() == null) {
                continue;
            }
            map.put(state.getChatType() + ":" + state.getTargetId(), state);
        }
        return map;
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
            // 群会话仅群成员可推进已读位置（与读/发消息的成员校验一致）
            requireGroupMember(employeeId, targetId);
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
        message.setStatus(MSG_STATUS_NORMAL);

        if (chatType != null && chatType == CHAT_TYPE_SINGLE) {
            if (targetId.equals(employeeId)) {
                throw new IllegalArgumentException("不能给自己发消息");
            }
            HrEmployee peer = employeeMapper.selectById(targetId);
            if (peer == null) {
                throw new IllegalArgumentException("对方不存在");
            }
            // 离职或 PC 账号被禁用的员工不可发送/接收单聊
            if (peer.getStatus() == null || peer.getStatus() != 1) {
                throw new IllegalArgumentException("对方已离职，无法发送消息");
            }
            Integer accountStatus = employeeMapper.selectSysUserStatusByEmployeeId(targetId);
            if (accountStatus != null && accountStatus != 1) {
                throw new IllegalArgumentException("对方账号已被禁用，无法发送消息");
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
            group.setLastMessage(truncateLastMessage(
                    "2".equals(String.valueOf(msgType)) ? "[图片]" : message.getContent()));
            group.setLastMessageTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        message.setMsgType(msgType != null && msgType == 2 ? 2 : 1);

        messageMapper.insert(message);
        // 发送新消息自动取消该会话的隐藏（仅发送者自己的列表视角）
        unhideConversation(employeeId, message.getChatType(), targetId);
        fillSenderInfo(Collections.singletonList(message));
        message.setRecalled(false);
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

    // ==================== 群成员管理 ====================

    /**
     * 群成员列表（仅群成员可查看），VO 含 employeeId/name/avatar/isOwner
     */
    public List<MobileChatGroupMemberVO> listGroupMembers(Long groupId, Long operatorId) {
        MobileChatGroup group = requireActiveGroup(groupId);
        requireGroupMember(operatorId, groupId);

        List<Long> memberIds = groupMemberMapper.selectList(new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getGroupId, groupId)
                        .eq(MobileChatGroupMember::getStatus, 1)
                        .orderByAsc(MobileChatGroupMember::getRoleType)
                        .orderByAsc(MobileChatGroupMember::getId))
                .stream()
                .map(MobileChatGroupMember::getEmployeeId)
                .filter(Objects::nonNull)
                .toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        Map<Long, HrEmployee> employees = employeeMapper.selectBatchIds(memberIds).stream()
                .collect(Collectors.toMap(HrEmployee::getId, Function.identity(), (a, b) -> a));
        List<MobileChatGroupMemberVO> result = new ArrayList<>();
        for (Long memberId : memberIds) {
            HrEmployee employee = employees.get(memberId);
            if (employee == null) {
                continue;
            }
            MobileChatGroupMemberVO vo = new MobileChatGroupMemberVO();
            vo.setEmployeeId(employee.getId());
            vo.setName(employee.getName());
            vo.setAvatar(employee.getAvatar());
            vo.setIsOwner(employee.getId().equals(group.getOwnerId()));
            result.add(vo);
        }
        return result;
    }

    /**
     * 群主加人（仅群主可操作）：已在群（含被移出记录）的复用原行复活，其余新增普通成员；
     * 成功后发系统消息"xxx 邀请 xx、yy 加入群聊"
     */
    @Transactional
    public void addGroupMembers(Long groupId, Long operatorId, List<Long> memberIds) {
        MobileChatGroup group = requireGroupOwner(operatorId, groupId);
        if (memberIds == null || memberIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要邀请的成员");
        }

        Set<Long> distinctIds = new LinkedHashSet<>(memberIds.stream()
                .filter(id -> id != null && id > 0)
                .toList());
        if (distinctIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要邀请的成员");
        }

        // 校验被邀请人均为在职员工
        Map<Long, HrEmployee> employees = distinctIds.isEmpty() ? Map.of()
                : employeeMapper.selectBatchIds(distinctIds).stream()
                        .collect(Collectors.toMap(HrEmployee::getId, Function.identity(), (a, b) -> a));
        for (Long memberId : distinctIds) {
            HrEmployee employee = employees.get(memberId);
            if (employee == null || employee.getStatus() == null || employee.getStatus() != 1) {
                throw new IllegalArgumentException("员工不存在或已离职，无法邀请: " + memberId);
            }
        }

        // 现有成员关系（含已退群/被移出的 status=0 记录，复用唯一键行）
        Map<Long, MobileChatGroupMember> existing = groupMemberMapper.selectList(
                        new LambdaQueryWrapper<MobileChatGroupMember>()
                                .eq(MobileChatGroupMember::getGroupId, groupId))
                .stream()
                .collect(Collectors.toMap(MobileChatGroupMember::getEmployeeId, Function.identity(), (a, b) -> a));

        List<String> addedNames = new ArrayList<>();
        for (Long memberId : distinctIds) {
            MobileChatGroupMember member = existing.get(memberId);
            if (member != null && member.getStatus() != null && member.getStatus() == 1) {
                continue; // 已在群中，跳过
            }
            if (member == null) {
                member = new MobileChatGroupMember();
                member.setGroupId(groupId);
                member.setEmployeeId(memberId);
                member.setRoleType(2);
                member.setStatus(1);
                groupMemberMapper.insert(member);
            } else {
                member.setStatus(1);
                member.setRoleType(2);
                groupMemberMapper.updateById(member);
            }
            HrEmployee employee = employees.get(memberId);
            addedNames.add(employee != null ? employee.getName() : String.valueOf(memberId));
        }

        if (addedNames.isEmpty()) {
            throw new IllegalArgumentException("所选成员已在群中");
        }

        refreshGroupMemberCount(group);
        String operatorName = resolveEmployeeName(operatorId);
        postGroupSystemMessage(group, operatorId,
                (operatorName != null ? operatorName : "群主") + " 邀请 " + String.join("、", addedNames)
                        + " 加入群聊");
    }

    /**
     * 群主踢人（仅群主可操作，不能踢自己）：成员关系置为移出状态，并发系统消息
     */
    @Transactional
    public void removeGroupMember(Long groupId, Long operatorId, Long employeeId) {
        MobileChatGroup group = requireGroupOwner(operatorId, groupId);
        if (employeeId == null) {
            throw new IllegalArgumentException("请选择要移出的成员");
        }
        if (employeeId.equals(operatorId)) {
            throw new IllegalArgumentException("不能移出自己，如需解散群聊请使用解散功能");
        }
        MobileChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getGroupId, groupId)
                        .eq(MobileChatGroupMember::getEmployeeId, employeeId)
                        .last("LIMIT 1"));
        if (member == null || member.getStatus() == null || member.getStatus() != 1) {
            throw new IllegalArgumentException("该成员不在群中");
        }
        member.setStatus(0);
        groupMemberMapper.updateById(member);

        refreshGroupMemberCount(group);
        String operatorName = resolveEmployeeName(operatorId);
        String memberName = resolveEmployeeName(employeeId);
        postGroupSystemMessage(group, operatorId,
                (operatorName != null ? operatorName : "群主") + " 将 "
                        + (memberName != null ? memberName : "成员") + " 移出了群聊");
    }

    /**
     * 退群（群主不允许退群，需先解散或转交）；退出后发系统消息
     */
    @Transactional
    public void leaveGroup(Long groupId, Long operatorId) {
        MobileChatGroup group = requireActiveGroup(groupId);
        if (operatorId.equals(group.getOwnerId())) {
            throw new IllegalArgumentException("群主不能退出群聊，请先解散群聊或转交群主后退出");
        }
        requireGroupMember(operatorId, groupId);

        MobileChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<MobileChatGroupMember>()
                        .eq(MobileChatGroupMember::getGroupId, groupId)
                        .eq(MobileChatGroupMember::getEmployeeId, operatorId)
                        .last("LIMIT 1"));
        member.setStatus(0);
        groupMemberMapper.updateById(member);

        refreshGroupMemberCount(group);
        String name = resolveEmployeeName(operatorId);
        postGroupSystemMessage(group, operatorId, (name != null ? name : "成员") + " 退出了群聊");
    }

    /**
     * 修改群名（仅群主），成功后发系统消息
     */
    @Transactional
    public MobileChatGroup renameGroup(Long groupId, Long operatorId, String name) {
        MobileChatGroup group = requireGroupOwner(operatorId, groupId);
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("群名称不能为空");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("群名称不能超过100个字符");
        }
        group.setGroupName(name.trim());
        groupMapper.updateById(group);

        String operatorName = resolveEmployeeName(operatorId);
        postGroupSystemMessage(group, operatorId,
                (operatorName != null ? operatorName : "群主") + " 修改群名为 " + group.getGroupName());
        return group;
    }

    /**
     * 解散群聊（仅群主）：群置为解散状态并发系统消息留痕，消息记录保留
     */
    @Transactional
    public void dissolveGroup(Long groupId, Long operatorId) {
        MobileChatGroup group = requireGroupOwner(operatorId, groupId);
        String operatorName = resolveEmployeeName(operatorId);
        postGroupSystemMessage(group, operatorId,
                (operatorName != null ? operatorName : "群主") + " 解散了群聊");
        group.setStatus(0);
        groupMapper.updateById(group);
    }

    /**
     * 校验群聊存在且未解散
     */
    private MobileChatGroup requireActiveGroup(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("群聊不能为空");
        }
        MobileChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("群聊不存在");
        }
        if (group.getStatus() != null && group.getStatus() != 1) {
            throw new IllegalArgumentException("群聊已解散");
        }
        return group;
    }

    /**
     * 校验操作人为群主（群成员管理/改名/解散仅群主可操作）
     */
    private MobileChatGroup requireGroupOwner(Long operatorId, Long groupId) {
        MobileChatGroup group = requireActiveGroup(groupId);
        if (!operatorId.equals(group.getOwnerId())) {
            throw new IllegalArgumentException("仅群主可以执行该操作");
        }
        return group;
    }

    /**
     * 重算群成员数（在职成员口径）
     */
    private void refreshGroupMemberCount(MobileChatGroup group) {
        Long count = groupMemberMapper.selectCount(new LambdaQueryWrapper<MobileChatGroupMember>()
                .eq(MobileChatGroupMember::getGroupId, group.getId())
                .eq(MobileChatGroupMember::getStatus, 1));
        group.setMemberCount(count != null ? count.intValue() : 0);
        groupMapper.updateById(group);
    }

    /**
     * 群系统消息：复用消息表，sender 为操作者，同时刷新群 lastMessage（截断防列超长）
     */
    private void postGroupSystemMessage(MobileChatGroup group, Long senderId, String content) {
        MobileChatMessage message = new MobileChatMessage();
        message.setChatType(CHAT_TYPE_GROUP);
        message.setGroupId(group.getId());
        message.setFromEmployeeId(senderId);
        message.setContent(content);
        message.setMsgType(1);
        message.setStatus(MSG_STATUS_NORMAL);
        messageMapper.insert(message);

        group.setLastMessage(truncateLastMessage(content));
        group.setLastMessageTime(LocalDateTime.now());
        groupMapper.updateById(group);
    }

    /**
     * lastMessage 截断至100字符，防止超出列宽报错
     */
    private String truncateLastMessage(String lastMessage) {
        if (lastMessage == null || lastMessage.length() <= LAST_MESSAGE_MAX_LENGTH) {
            return lastMessage;
        }
        return lastMessage.substring(0, LAST_MESSAGE_MAX_LENGTH);
    }

    private String resolveEmployeeName(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        HrEmployee employee = employeeMapper.selectById(employeeId);
        return employee != null ? employee.getName() : null;
    }

    // ==================== 会话管理（置顶/隐藏） ====================

    /**
     * 会话置顶/取消置顶
     *
     * @param targetType 接口契约的会话类型：1-单聊 2-群聊（与内部 chatType 相反）
     */
    @Transactional
    public void updateConversationSettings(Long employeeId, Integer targetType, Long targetId, Integer sticky) {
        if (targetType == null || (targetType != TARGET_TYPE_SINGLE && targetType != TARGET_TYPE_GROUP)) {
            throw new IllegalArgumentException("会话类型不合法：1-单聊 2-群聊");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("会话目标不能为空");
        }
        if (sticky == null || (sticky != 0 && sticky != 1)) {
            throw new IllegalArgumentException("sticky 取值不合法：0-取消置顶 1-置顶");
        }
        int chatType = targetTypeToChatType(targetType);
        MobileChatReadState state = getOrCreateReadState(employeeId, chatType, targetId);
        state.setSticky(sticky);
        readStateMapper.updateById(state);
    }

    /**
     * 隐藏会话（仅从会话列表隐藏，不删除消息记录）
     *
     * @param targetType 接口契约的会话类型：1-单聊 2-群聊（与内部 chatType 相反）
     */
    @Transactional
    public void hideConversation(Long employeeId, Integer targetType, Long targetId) {
        if (targetType == null || (targetType != TARGET_TYPE_SINGLE && targetType != TARGET_TYPE_GROUP)) {
            throw new IllegalArgumentException("会话类型不合法：1-单聊 2-群聊");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("会话目标不能为空");
        }
        int chatType = targetTypeToChatType(targetType);
        MobileChatReadState state = getOrCreateReadState(employeeId, chatType, targetId);
        state.setHidden(1);
        readStateMapper.updateById(state);
    }

    /**
     * 发送新消息时自动取消隐藏（仅发送者自己的列表视角）
     */
    private void unhideConversation(Long employeeId, Integer chatType, Long targetId) {
        try {
            MobileChatReadState state = getOrCreateReadState(employeeId, chatType, targetId);
            if (state.getHidden() != null && state.getHidden() == 1) {
                state.setHidden(0);
                readStateMapper.updateById(state);
            }
        } catch (Exception ignored) {
            // 取消隐藏失败不影响发消息主流程
        }
    }

    /**
     * 接口契约 targetType（1-单聊 2-群聊）→ 内部 chatType（1-群聊 2-单聊）
     */
    private int targetTypeToChatType(Integer targetType) {
        return targetType != null && targetType == TARGET_TYPE_GROUP ? CHAT_TYPE_GROUP : CHAT_TYPE_SINGLE;
    }

    /**
     * 查询或初始化会话个人设置行（无则插入一条水位为0的记录）
     */
    private MobileChatReadState getOrCreateReadState(Long employeeId, Integer chatType, Long targetId) {
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
            state.setLastReadMessageId(0L);
            state.setSticky(0);
            state.setHidden(0);
            readStateMapper.insert(state);
        }
        return state;
    }

    // ==================== 消息撤回 ====================

    /**
     * 撤回消息：仅发送者本人、发出后2分钟内可撤回；
     * 撤回后消息内容保留在库中，查询返回时替换为"xxx 撤回了一条消息"
     *
     * @return 撤回后的消息（含替换后的内容与 recalled 标记，供 WS 推送使用）
     */
    @Transactional
    public MobileChatMessage recallMessage(Long employeeId, Long messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        MobileChatMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        if (!employeeId.equals(message.getFromEmployeeId())) {
            throw new IllegalArgumentException("只能撤回自己发送的消息");
        }
        if (message.getStatus() != null && message.getStatus() == MSG_STATUS_RECALLED) {
            throw new IllegalArgumentException("消息已撤回");
        }
        LocalDateTime createdTime = message.getCreatedTime();
        if (createdTime == null || createdTime.isBefore(LocalDateTime.now().minusMinutes(RECALL_WINDOW_MINUTES))) {
            throw new IllegalArgumentException("仅发送后" + RECALL_WINDOW_MINUTES + "分钟内的消息可以撤回");
        }
        message.setStatus(MSG_STATUS_RECALLED);
        messageMapper.updateById(message);

        fillSenderInfo(Collections.singletonList(message));
        maskRecalled(Collections.singletonList(message));
        return message;
    }
}
