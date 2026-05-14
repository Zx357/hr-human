package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.MobileChatGroup;
import com.kadmin.entity.MobileContactRequest;
import com.kadmin.security.LoginUser;
import com.kadmin.service.MobileContactService;
import com.kadmin.utils.SecurityUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/requests")
    public Result<List<MobileContactRequest>> requests(@RequestParam(defaultValue = "received") String type) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(contactService.listRequests(employeeId, type));
    }

    @PostMapping("/requests")
    public Result<MobileContactRequest> sendRequest(@RequestBody ContactRequestCreateRequest request) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(contactService.sendRequest(
                    request.getTargetEmployeeId(),
                    request.getRemark(),
                    employeeId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/requests/{id}/handle")
    public Result<MobileContactRequest> handleRequest(
            @PathVariable Long id,
            @RequestBody ContactRequestHandleRequest request) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(contactService.handleRequest(id, request.getStatus(), employeeId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
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

    @Data
    public static class ContactRequestCreateRequest {
        private Long targetEmployeeId;
        private String remark;
    }

    @Data
    public static class ContactRequestHandleRequest {
        private Integer status;
    }
}
