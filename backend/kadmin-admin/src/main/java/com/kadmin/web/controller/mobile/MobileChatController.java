package com.kadmin.web.controller.mobile;

import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.mobile.domain.MobileChatMessage;
import com.kadmin.mobile.domain.vo.MobileChatGroupMemberVO;
import com.kadmin.mobile.service.MobileChatService;
import com.kadmin.web.service.ChatMessagePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mobile/chat")
@RequiredArgsConstructor
public class MobileChatController {

    private final MobileChatService chatService;
    private final ChatMessagePushService chatMessagePushService;

    /**
     * 拉取会话消息
     *
     * @param afterMessageId  增量参数：仅返回 id 大于该值的消息（轮询使用）
     * @param beforeMessageId 历史分页参数：仅返回 id 小于该值的一页消息
     * @param limit           分页大小（1-200，默认200）
     */
    @GetMapping("/messages")
    public Result<List<MobileChatMessage>> messages(
            @RequestParam(defaultValue = "1") Integer chatType,
            @RequestParam Long targetId,
            @RequestParam(required = false) Long afterMessageId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(required = false) Integer limit) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatService.listMessages(employeeId, chatType, targetId, afterMessageId,
                beforeMessageId, limit));
    }

    /**
     * 最近会话列表（群聊+单聊，含未读数与置顶标记，隐藏会话不返回）
     */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatService.getConversations(employeeId));
    }

    /**
     * 未读消息总数（角标）
     */
    @GetMapping("/unread-total")
    public Result<Long> unreadTotal() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatService.getTotalUnread(employeeId));
    }

    /**
     * 标记会话已读
     */
    @PostMapping("/read")
    public Result<Void> markRead(@RequestBody Map<String, Object> body) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        Integer chatType = body.get("chatType") == null ? 1 : Integer.valueOf(String.valueOf(body.get("chatType")));
        Long targetId = body.get("targetId") == null ? null : Long.valueOf(String.valueOf(body.get("targetId")));
        chatService.markRead(employeeId, chatType, targetId);
        return Result.success();
    }

    @PostMapping("/send")
    public Result<MobileChatMessage> send(@RequestBody Map<String, Object> body) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        Integer chatType = body.get("chatType") == null ? 1 : Integer.valueOf(String.valueOf(body.get("chatType")));
        Long targetId = body.get("targetId") == null ? null : Long.valueOf(String.valueOf(body.get("targetId")));
        String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
        Integer msgType = body.get("msgType") == null ? 1 : Integer.valueOf(String.valueOf(body.get("msgType")));
        // chatService.send 返回即事务已提交，此后异步推送不影响发送结果（失败由客户端轮询兜底）
        MobileChatMessage saved = chatService.send(employeeId, chatType, targetId, content, msgType);
        chatMessagePushService.pushNewMessage(saved);
        return Result.success(saved);
    }

    // ==================== 群成员管理 ====================

    /**
     * 群成员列表（仅群成员可查看）
     * 返回 VO：employeeId、name、avatar、isOwner（是否群主）
     */
    @GetMapping("/group/{groupId}/members")
    public Result<List<MobileChatGroupMemberVO>> groupMembers(@PathVariable Long groupId) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatService.listGroupMembers(groupId, employeeId));
    }

    /**
     * 群主加人，body = {memberIds:[Long]}
     */
    @PostMapping("/group/{groupId}/members")
    public Result<Void> addGroupMembers(@PathVariable Long groupId,
            @RequestBody Map<String, Object> body) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        List<Long> memberIds = parseLongList(body.get("memberIds"));
        chatService.addGroupMembers(groupId, employeeId, memberIds);
        return Result.success();
    }

    /**
     * 群主踢人（不能踢自己）
     */
    @DeleteMapping("/group/{groupId}/members/{employeeId}")
    public Result<Void> removeGroupMember(@PathVariable Long groupId, @PathVariable Long employeeId) {
        Long operatorId = currentEmployeeId();
        if (operatorId == null) {
            return Result.error("请先登录");
        }
        chatService.removeGroupMember(groupId, operatorId, employeeId);
        return Result.success();
    }

    /**
     * 退群（群主不允许退群，需先解散或转交）
     */
    @PostMapping("/group/{groupId}/leave")
    public Result<Void> leaveGroup(@PathVariable Long groupId) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        chatService.leaveGroup(groupId, employeeId);
        return Result.success();
    }

    /**
     * 修改群名（仅群主），body = {name}
     */
    @PutMapping("/group/{groupId}/name")
    public Result<Void> renameGroup(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        String name = body.get("name") == null ? null : String.valueOf(body.get("name"));
        chatService.renameGroup(groupId, employeeId, name);
        return Result.success();
    }

    /**
     * 解散群聊（仅群主）
     */
    @DeleteMapping("/group/{groupId}")
    public Result<Void> dissolveGroup(@PathVariable Long groupId) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        chatService.dissolveGroup(groupId, employeeId);
        return Result.success();
    }

    // ==================== 会话管理 ====================

    /**
     * 会话置顶/取消置顶，body = {targetType: 1单聊 2群聊, targetId, sticky: 0取消 1置顶}
     * 注意：targetType 与消息接口的 chatType（1群聊 2单聊）取值相反，此处为前端约定的会话类型编码
     */
    @PutMapping("/conversation/settings")
    public Result<Void> conversationSettings(@RequestBody Map<String, Object> body) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        Integer targetType = body.get("targetType") == null ? null
                : Integer.valueOf(String.valueOf(body.get("targetType")));
        Long targetId = body.get("targetId") == null ? null : Long.valueOf(String.valueOf(body.get("targetId")));
        Integer sticky = body.get("sticky") == null ? null : Integer.valueOf(String.valueOf(body.get("sticky")));
        chatService.updateConversationSettings(employeeId, targetType, targetId, sticky);
        return Result.success();
    }

    /**
     * 隐藏会话（仅从会话列表隐藏，不删除消息记录；发新消息自动恢复）
     */
    @DeleteMapping("/conversation")
    public Result<Void> hideConversation(
            @RequestParam Integer targetType,
            @RequestParam Long targetId) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        chatService.hideConversation(employeeId, targetType, targetId);
        return Result.success();
    }

    // ==================== 消息撤回 ====================

    /**
     * 撤回消息：仅发送者本人、发出后2分钟内
     */
    @PutMapping("/message/{messageId}/recall")
    public Result<MobileChatMessage> recallMessage(@PathVariable Long messageId) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        MobileChatMessage recalled = chatService.recallMessage(employeeId, messageId);
        // 事务提交后异步推送撤回事件（失败由客户端拉取历史兜底）
        chatMessagePushService.pushRecallMessage(recalled);
        return Result.success(recalled);
    }

    private List<Long> parseLongList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(item -> item == null ? null : Long.valueOf(String.valueOf(item)))
                .toList();
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }
}
