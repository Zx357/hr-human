package com.kadmin.web.controller.mobile;

import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.mobile.domain.MobileChatMessage;
import com.kadmin.mobile.service.MobileChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 最近会话列表（群聊+单聊，含未读数）
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
        return Result.success(chatService.send(employeeId, chatType, targetId, content, msgType));
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }
}
