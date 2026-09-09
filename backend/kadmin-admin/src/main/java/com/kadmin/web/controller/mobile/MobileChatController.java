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

    @GetMapping("/messages")
    public Result<List<MobileChatMessage>> messages(
            @RequestParam(defaultValue = "1") Integer chatType,
            @RequestParam Long targetId) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatService.listMessages(employeeId, chatType, targetId));
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
        return Result.success(chatService.send(employeeId, chatType, targetId, content));
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }
}
