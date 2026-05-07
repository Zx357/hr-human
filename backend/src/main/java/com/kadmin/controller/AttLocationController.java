package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.AttLocation;
import com.kadmin.service.AttLocationService;
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

@RestController
@RequestMapping("/attendance/location")
@RequiredArgsConstructor
public class AttLocationController {

    private final AttLocationService attLocationService;

    @GetMapping("/page")
    public Result<Page<AttLocation>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(attLocationService.getPage(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<AttLocation> detail(@PathVariable Long id) {
        return Result.success(attLocationService.getDetail(id));
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody AttLocation location) {
        return Result.success(attLocationService.saveLocation(location));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody AttLocation location) {
        location.setId(id);
        return Result.success(attLocationService.saveLocation(location));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(attLocationService.deleteLocation(id));
    }
}
