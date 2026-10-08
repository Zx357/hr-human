package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公开参数配置控制器（登录即可，不要求管理员）
 * 只暴露 is_public=1 且启用的配置，防敏感配置泄露
 */
@Tag(name = "公开参数配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class PublicConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 读取公开配置：keys 为空时返回全部公开配置；不存在的键直接忽略（不报错）
     *
     * @param keys 逗号分隔的配置键，如 map.amap.key,map.amap.security-code
     */
    @Operation(summary = "读取公开参数配置")
    @GetMapping("/public")
    public Result<Map<String, String>> publicConfigs(@RequestParam(required = false) String keys) {
        Map<String, String> configs = sysConfigService.getPublicConfigs(keys);
        return Result.success(configs);
    }
}
