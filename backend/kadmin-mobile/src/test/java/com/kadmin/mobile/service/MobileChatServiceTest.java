package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 移动端聊天服务单元测试：群成员管理（加人/踢人/退群/改名/解散 权限与系统消息）、
 * 会话管理（置顶/隐藏）、消息撤回（本人/时限）与发送校验（对方状态/lastMessage 截断）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MobileChatServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final Long NEWCOMER_ID = 3L;
    private static final Long GROUP_ID = 10L;

    @Mock
    private MobileChatMessageMapper messageMapper;
    @Mock
    private MobileChatGroupMapper groupMapper;
    @Mock
    private MobileChatGroupMemberMapper groupMemberMapper;
    @Mock
    private MobileChatReadStateMapper readStateMapper;
    @Mock
    private EmployeeMapper employeeMapper;

    private MobileChatService service;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        java.util.stream.Stream.of(MobileChatGroup.class, MobileChatGroupMember.class, MobileChatMessage.class,
                MobileChatReadState.class, HrEmployee.class)
                .forEach(clazz -> TableInfoHelper.initTableInfo(assistant, clazz));
    }

    @BeforeEach
    void setUp() {
        service = new MobileChatService(messageMapper, groupMapper, groupMemberMapper, readStateMapper,
                employeeMapper);
    }

    private MobileChatGroup activeGroup() {
        MobileChatGroup group = new MobileChatGroup();
        group.setId(GROUP_ID);
        group.setGroupName("测试群");
        group.setOwnerId(OWNER_ID);
        group.setMemberCount(2);
        group.setStatus(1);
        return group;
    }

    private HrEmployee employee(Long id, String name) {
        HrEmployee employee = new HrEmployee();
        employee.setId(id);
        employee.setName(name);
        employee.setStatus(1);
        return employee;
    }

    private void stubGroupActive() {
        when(groupMapper.selectById(GROUP_ID)).thenReturn(activeGroup());
    }

    private void stubOwnerIsMember() {
        lenient().when(groupMemberMapper.selectCount(any())).thenReturn(1L);
    }

    // ==================== 群成员管理 ====================

    @Test
    @DisplayName("加人：非群主不允许")
    void addGroupMembers_byNonOwner_rejected() {
        stubGroupActive();
        assertThatThrownBy(() -> service.addGroupMembers(GROUP_ID, MEMBER_ID, List.of(NEWCOMER_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅群主");
    }

    @Test
    @DisplayName("加人：群主加人成功并发出系统消息（含邀请文案与姓名）")
    void addGroupMembers_byOwner_postsSystemMessage() {
        stubGroupActive();
        stubOwnerIsMember();
        when(employeeMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(employee(NEWCOMER_ID, "张三")));
        when(employeeMapper.selectById(OWNER_ID)).thenReturn(employee(OWNER_ID, "群主甲"));
        when(groupMemberMapper.selectList(any())).thenReturn(List.of());
        when(groupMemberMapper.selectCount(any())).thenReturn(3L);

        service.addGroupMembers(GROUP_ID, OWNER_ID, List.of(NEWCOMER_ID));

        ArgumentCaptor<MobileChatGroupMember> memberCaptor =
                ArgumentCaptor.forClass(MobileChatGroupMember.class);
        verify(groupMemberMapper).insert(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmployeeId()).isEqualTo(NEWCOMER_ID);
        assertThat(memberCaptor.getValue().getStatus()).isEqualTo(1);

        ArgumentCaptor<MobileChatMessage> messageCaptor = ArgumentCaptor.forClass(MobileChatMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).contains("群主甲").contains("张三").contains("邀请");
        assertThat(messageCaptor.getValue().getFromEmployeeId()).isEqualTo(OWNER_ID);
    }

    @Test
    @DisplayName("踢人：群主不能踢自己")
    void removeGroupMember_cannotKickSelf() {
        stubGroupActive();
        assertThatThrownBy(() -> service.removeGroupMember(GROUP_ID, OWNER_ID, OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能移出自己");
    }

    @Test
    @DisplayName("踢人：非群主不允许")
    void removeGroupMember_byNonOwner_rejected() {
        stubGroupActive();
        assertThatThrownBy(() -> service.removeGroupMember(GROUP_ID, MEMBER_ID, NEWCOMER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅群主");
    }

    @Test
    @DisplayName("退群：群主不允许退群（需先解散或转交）")
    void leaveGroup_ownerNotAllowed() {
        stubGroupActive();
        assertThatThrownBy(() -> service.leaveGroup(GROUP_ID, OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("群主不能退出");
    }

    @Test
    @DisplayName("退群：普通成员退群成功，成员关系置为移出并发系统消息")
    void leaveGroup_memberSuccess() {
        stubGroupActive();
        stubOwnerIsMember();
        MobileChatGroupMember membership = new MobileChatGroupMember();
        membership.setGroupId(GROUP_ID);
        membership.setEmployeeId(MEMBER_ID);
        membership.setStatus(1);
        when(groupMemberMapper.selectOne(any())).thenReturn(membership);
        when(employeeMapper.selectById(MEMBER_ID)).thenReturn(employee(MEMBER_ID, "成员乙"));
        when(groupMemberMapper.selectCount(any())).thenReturn(1L);

        service.leaveGroup(GROUP_ID, MEMBER_ID);

        ArgumentCaptor<MobileChatGroupMember> memberCaptor =
                ArgumentCaptor.forClass(MobileChatGroupMember.class);
        verify(groupMemberMapper).updateById(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getStatus()).isEqualTo(0);

        ArgumentCaptor<MobileChatMessage> messageCaptor = ArgumentCaptor.forClass(MobileChatMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).contains("成员乙").contains("退出了群聊");
    }

    @Test
    @DisplayName("改群名：非群主不允许")
    void renameGroup_byNonOwner_rejected() {
        stubGroupActive();
        assertThatThrownBy(() -> service.renameGroup(GROUP_ID, MEMBER_ID, "新名字"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅群主");
    }

    @Test
    @DisplayName("改群名：群主改名成功并发出系统消息")
    void renameGroup_byOwner_success() {
        stubGroupActive();
        stubOwnerIsMember();
        when(employeeMapper.selectById(OWNER_ID)).thenReturn(employee(OWNER_ID, "群主甲"));
        when(groupMemberMapper.selectCount(any())).thenReturn(2L);

        MobileChatGroup group = service.renameGroup(GROUP_ID, OWNER_ID, "新的群名");

        assertThat(group.getGroupName()).isEqualTo("新的群名");
        ArgumentCaptor<MobileChatMessage> messageCaptor = ArgumentCaptor.forClass(MobileChatMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).contains("修改群名为").contains("新的群名");
    }

    @Test
    @DisplayName("解散：非群主不允许")
    void dissolveGroup_byNonOwner_rejected() {
        stubGroupActive();
        assertThatThrownBy(() -> service.dissolveGroup(GROUP_ID, MEMBER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅群主");
    }

    @Test
    @DisplayName("解散：群主解散成功，群状态置0并留系统消息")
    void dissolveGroup_byOwner_success() {
        stubGroupActive();
        when(employeeMapper.selectById(OWNER_ID)).thenReturn(employee(OWNER_ID, "群主甲"));
        when(groupMemberMapper.selectCount(any())).thenReturn(2L);

        service.dissolveGroup(GROUP_ID, OWNER_ID);

        ArgumentCaptor<MobileChatGroup> groupCaptor = ArgumentCaptor.forClass(MobileChatGroup.class);
        // 解散包含系统消息刷新 lastMessage 与置状态两次更新
        verify(groupMapper, org.mockito.Mockito.atLeastOnce()).updateById(groupCaptor.capture());
        assertThat(groupCaptor.getValue().getStatus()).isEqualTo(0);
        ArgumentCaptor<MobileChatMessage> messageCaptor = ArgumentCaptor.forClass(MobileChatMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).contains("解散了群聊");
    }

    @Test
    @DisplayName("群成员列表：返回员工信息与群主标记")
    void listGroupMembers_marksOwner() {
        stubGroupActive();
        stubOwnerIsMember();
        MobileChatGroupMember ownerMember = new MobileChatGroupMember();
        ownerMember.setGroupId(GROUP_ID);
        ownerMember.setEmployeeId(OWNER_ID);
        ownerMember.setRoleType(1);
        ownerMember.setStatus(1);
        when(groupMemberMapper.selectList(any())).thenReturn(List.of(ownerMember));
        when(employeeMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(employee(OWNER_ID, "群主甲")));

        var members = service.listGroupMembers(GROUP_ID, OWNER_ID);
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getEmployeeId()).isEqualTo(OWNER_ID);
        assertThat(members.get(0).getName()).isEqualTo("群主甲");
        assertThat(members.get(0).getIsOwner()).isTrue();
    }

    // ==================== 会话管理 ====================

    @Test
    @DisplayName("会话置顶：非法 targetType 拒绝；合法值写入 sticky")
    void updateConversationSettings_sticky() {
        assertThatThrownBy(() -> service.updateConversationSettings(OWNER_ID, 3, GROUP_ID, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("会话类型不合法");

        MobileChatReadState state = new MobileChatReadState();
        state.setEmployeeId(OWNER_ID);
        state.setChatType(1);
        state.setTargetId(GROUP_ID);
        state.setLastReadMessageId(0L);
        state.setSticky(0);
        state.setHidden(0);
        when(readStateMapper.selectOne(any())).thenReturn(state);

        // targetType=2 为群聊（接口契约编码，内部换算为 chatType=1）
        service.updateConversationSettings(OWNER_ID, 2, GROUP_ID, 1);

        ArgumentCaptor<MobileChatReadState> captor = ArgumentCaptor.forClass(MobileChatReadState.class);
        verify(readStateMapper).updateById(captor.capture());
        assertThat(captor.getValue().getSticky()).isEqualTo(1);
        assertThat(captor.getValue().getHidden()).isEqualTo(0);
    }

    @Test
    @DisplayName("隐藏会话：隐藏仅作用于本人视角（hidden=1）")
    void hideConversation_setsHidden() {
        MobileChatReadState state = new MobileChatReadState();
        state.setEmployeeId(OWNER_ID);
        state.setChatType(2);
        state.setTargetId(MEMBER_ID);
        state.setLastReadMessageId(8L);
        state.setSticky(0);
        state.setHidden(0);
        when(readStateMapper.selectOne(any())).thenReturn(state);

        service.hideConversation(OWNER_ID, 1, MEMBER_ID);

        ArgumentCaptor<MobileChatReadState> captor = ArgumentCaptor.forClass(MobileChatReadState.class);
        verify(readStateMapper).updateById(captor.capture());
        assertThat(captor.getValue().getHidden()).isEqualTo(1);
    }

    @Test
    @DisplayName("会话列表：隐藏的会话不返回，置顶会话排在最前")
    void getConversations_hiddenFilteredAndStickyFirst() {
        MobileChatGroupMember membership = new MobileChatGroupMember();
        membership.setEmployeeId(OWNER_ID);
        membership.setGroupId(GROUP_ID);
        membership.setStatus(1);
        when(groupMemberMapper.selectList(any())).thenReturn(List.of(membership));

        MobileChatGroup group = activeGroup();
        group.setLastMessageTime(LocalDateTime.of(2026, 10, 1, 10, 0));
        when(groupMapper.selectBatchIds(anyCollection())).thenReturn(List.of(group));

        // 单聊会话（消息时间晚于群聊，但未置顶）
        MobileChatMessage single = new MobileChatMessage();
        single.setId(20L);
        single.setChatType(2);
        single.setFromEmployeeId(MEMBER_ID);
        single.setPeerEmployeeId(OWNER_ID);
        single.setContent("单聊消息");
        single.setFromEmployeeId(MEMBER_ID);
        single.setCreatedTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        when(messageMapper.selectList(any())).thenReturn(List.of(single));
        when(employeeMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(employee(MEMBER_ID, "同事乙")));

        // 群聊置顶；单聊隐藏
        MobileChatReadState stickyState = new MobileChatReadState();
        stickyState.setEmployeeId(OWNER_ID);
        stickyState.setChatType(1);
        stickyState.setTargetId(GROUP_ID);
        stickyState.setSticky(1);
        stickyState.setHidden(0);
        MobileChatReadState hiddenState = new MobileChatReadState();
        hiddenState.setEmployeeId(OWNER_ID);
        hiddenState.setChatType(2);
        hiddenState.setTargetId(MEMBER_ID);
        hiddenState.setSticky(0);
        hiddenState.setHidden(1);
        when(readStateMapper.selectList(any())).thenReturn(List.of(stickyState, hiddenState));
        when(messageMapper.countUnreadGroupByConversation(any(), any())).thenReturn(List.of());

        List<Map<String, Object>> conversations = service.getConversations(OWNER_ID);

        // 单聊已隐藏：只剩群聊，且带 sticky=1
        assertThat(conversations).hasSize(1);
        assertThat(conversations.get(0).get("targetId")).isEqualTo(GROUP_ID);
        assertThat(conversations.get(0).get("sticky")).isEqualTo(1);
        assertThat(conversations.get(0).get("unreadCount")).isEqualTo(0L);
    }

    // ==================== 消息撤回与发送校验 ====================

    @Test
    @DisplayName("撤回：非发送者本人不允许")
    void recallMessage_byNonSender_rejected() {
        MobileChatMessage message = new MobileChatMessage();
        message.setId(30L);
        message.setFromEmployeeId(OWNER_ID);
        message.setCreatedTime(LocalDateTime.now());
        when(messageMapper.selectById(30L)).thenReturn(message);
        assertThatThrownBy(() -> service.recallMessage(MEMBER_ID, 30L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只能撤回自己发送的消息");
    }

    @Test
    @DisplayName("撤回：超过2分钟不允许")
    void recallMessage_afterWindow_rejected() {
        MobileChatMessage message = new MobileChatMessage();
        message.setId(31L);
        message.setFromEmployeeId(OWNER_ID);
        message.setCreatedTime(LocalDateTime.now().minusMinutes(3));
        when(messageMapper.selectById(31L)).thenReturn(message);
        assertThatThrownBy(() -> service.recallMessage(OWNER_ID, 31L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2分钟");
    }

    @Test
    @DisplayName("撤回：本人2分钟内撤回成功，返回已撤回标记与替换后的内容")
    void recallMessage_success() {
        MobileChatMessage message = new MobileChatMessage();
        message.setId(32L);
        message.setChatType(2);
        message.setFromEmployeeId(OWNER_ID);
        message.setPeerEmployeeId(MEMBER_ID);
        message.setContent("原始内容");
        message.setMsgType(1);
        message.setStatus(0);
        message.setCreatedTime(LocalDateTime.now().minusSeconds(30));
        when(messageMapper.selectById(32L)).thenReturn(message);
        when(employeeMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(employee(OWNER_ID, "撤回人")));

        MobileChatMessage recalled = service.recallMessage(OWNER_ID, 32L);

        assertThat(recalled.getStatus()).isEqualTo(1);
        assertThat(recalled.getRecalled()).isTrue();
        assertThat(recalled.getContent()).isEqualTo("撤回人 撤回了一条消息");
        verify(messageMapper).updateById(any(com.kadmin.mobile.domain.MobileChatMessage.class));
    }

    @Test
    @DisplayName("单聊发送：对方已离职不允许发送")
    void send_singleChat_peerDeparted_rejected() {
        HrEmployee peer = employee(MEMBER_ID, "离职者");
        peer.setStatus(2);
        when(employeeMapper.selectById(MEMBER_ID)).thenReturn(peer);
        when(employeeMapper.selectSysUserStatusByEmployeeId(MEMBER_ID)).thenReturn(1);
        assertThatThrownBy(() -> service.send(OWNER_ID, 2, MEMBER_ID, "在吗", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("离职");
    }

    @Test
    @DisplayName("单聊发送：对方 PC 账号被禁用不允许发送")
    void send_singleChat_peerAccountDisabled_rejected() {
        when(employeeMapper.selectById(MEMBER_ID)).thenReturn(employee(MEMBER_ID, "禁用者"));
        when(employeeMapper.selectSysUserStatusByEmployeeId(MEMBER_ID)).thenReturn(0);
        assertThatThrownBy(() -> service.send(OWNER_ID, 2, MEMBER_ID, "在吗", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("禁用");
    }

    @Test
    @DisplayName("群聊发送：超长消息截断 lastMessage 至100字符")
    void send_groupMessage_truncatesLastMessage() {
        stubGroupActive();
        stubOwnerIsMember();
        String longContent = "长".repeat(150);

        service.send(OWNER_ID, 1, GROUP_ID, longContent, 1);

        ArgumentCaptor<MobileChatGroup> groupCaptor = ArgumentCaptor.forClass(MobileChatGroup.class);
        verify(groupMapper).updateById(groupCaptor.capture());
        assertThat(groupCaptor.getValue().getLastMessage()).hasSize(100);
        verify(messageMapper).insert(any(com.kadmin.mobile.domain.MobileChatMessage.class));
    }

    @Test
    @DisplayName("历史查询：已撤回消息内容替换为撤回提示并带 recalled 标记")
    void listMessages_masksRecalledContent() {
        stubOwnerIsMember();
        MobileChatMessage recalled = new MobileChatMessage();
        recalled.setId(40L);
        recalled.setChatType(1);
        recalled.setGroupId(GROUP_ID);
        recalled.setFromEmployeeId(OWNER_ID);
        recalled.setContent("原始内容");
        recalled.setStatus(1);
        MobileChatMessage normal = new MobileChatMessage();
        normal.setId(41L);
        normal.setChatType(1);
        normal.setGroupId(GROUP_ID);
        normal.setFromEmployeeId(OWNER_ID);
        normal.setContent("正常消息");
        normal.setStatus(0);
        when(messageMapper.selectList(any())).thenReturn(new java.util.ArrayList<>(List.of(normal, recalled)));
        when(employeeMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(employee(OWNER_ID, "张三")));

        List<MobileChatMessage> messages = service.listMessages(OWNER_ID, 1, GROUP_ID, null, null, null);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRecalled()).isTrue();
        assertThat(messages.get(0).getContent()).isEqualTo("张三 撤回了一条消息");
        assertThat(messages.get(1).getRecalled()).isFalse();
        assertThat(messages.get(1).getContent()).isEqualTo("正常消息");
    }
}
