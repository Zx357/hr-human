package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.mobile.domain.MobileChatGroup;
import com.kadmin.common.security.LoginUser;
import com.kadmin.mobile.service.MobileContactService;
import com.kadmin.common.utils.SecurityUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mobile/contacts")
@RequiredArgsConstructor
public class MobileContactController {

    private final MobileContactService contactService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(contactService.getSummary(employeeId));
    }

    @GetMapping("/search")
    public Result<Page<HrEmployee>> search(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(contactService.searchEmployees(pageNum, pageSize, keyword));
    }

    @GetMapping("/groups")
    public Result<List<MobileChatGroup>> groups() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(contactService.listGroups(employeeId));
    }

    @PostMapping("/groups")
    public Result<MobileChatGroup> createGroup(@RequestBody GroupCreateRequest request) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(contactService.createGroup(request.getGroupName(), request.getMemberIds(), employeeId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/groups/{id}/members")
    public Result<List<HrEmployee>> groupMembers(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(contactService.listGroupMembers(id, employeeId));
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }

    @Data
    public static class GroupCreateRequest {
        private String groupName;
        private List<Long> memberIds;
    }

}
