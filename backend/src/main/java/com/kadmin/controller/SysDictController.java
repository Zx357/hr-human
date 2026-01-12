package com.kadmin.controller;

import com.kadmin.common.Result;
import com.kadmin.entity.SysDictData;
import com.kadmin.entity.SysDictType;
import com.kadmin.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
public class SysDictController {

    @Autowired
    private SysDictService dictService;

    // ==================== 字典类型 ====================

    /**
     * 获取所有字典类型
     */
    @Operation(summary = "获取所有字典类型")
    @GetMapping("/type/list")
    public Result<List<SysDictType>> listDictTypes() {
        List<SysDictType> list = dictService.getAllDictTypes();
        return Result.success(list);
    }

    /**
     * 获取字典类型详情
     */
    @Operation(summary = "获取字典类型详情")
    @GetMapping("/type/{id}")
    public Result<SysDictType> getDictType(@PathVariable Long id) {
        SysDictType dictType = dictService.getById(id);
        return Result.success(dictType);
    }

    /**
     * 新增字典类型
     */
    @Operation(summary = "新增字典类型")
    @PostMapping("/type")
    public Result<Boolean> addDictType(@RequestBody SysDictType dictType) {
        try {
            boolean success = dictService.addDictType(dictType);
            return success ? Result.success(true) : Result.error("新增失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新字典类型
     */
    @Operation(summary = "更新字典类型")
    @PutMapping("/type")
    public Result<Boolean> updateDictType(@RequestBody SysDictType dictType) {
        boolean success = dictService.updateDictType(dictType);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 删除字典类型
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/{id}")
    public Result<Boolean> deleteDictType(@PathVariable Long id) {
        boolean success = dictService.deleteDictType(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    // ==================== 字典数据 ====================

    /**
     * 获取字典数据列表
     */
    @Operation(summary = "获取字典数据列表")
    @GetMapping("/data/list")
    public Result<List<SysDictData>> listDictData(@RequestParam Long dictTypeId) {
        List<SysDictData> list = dictService.getDictDataList(dictTypeId);
        return Result.success(list);
    }

    /**
     * 根据字典编码获取字典数据
     */
    @Operation(summary = "根据字典编码获取字典数据")
    @GetMapping("/data/byCode")
    public Result<List<SysDictData>> getDictDataByCode(@RequestParam String dictCode) {
        List<SysDictData> list = dictService.getDictDataByCode(dictCode);
        return Result.success(list);
    }

    /**
     * 获取字典数据详情
     */
    @Operation(summary = "获取字典数据详情")
    @GetMapping("/data/{id}")
    public Result<SysDictData> getDictData(@PathVariable Long id) {
        SysDictData dictData = dictService.getDictDataById(id);
        return Result.success(dictData);
    }

    /**
     * 新增字典数据
     */
    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    public Result<Boolean> addDictData(@RequestBody SysDictData dictData) {
        boolean success = dictService.addDictData(dictData);
        return success ? Result.success(true) : Result.error("新增失败");
    }

    /**
     * 更新字典数据
     */
    @Operation(summary = "更新字典数据")
    @PutMapping("/data")
    public Result<Boolean> updateDictData(@RequestBody SysDictData dictData) {
        boolean success = dictService.updateDictData(dictData);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 删除字典数据
     */
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    public Result<Boolean> deleteDictData(@PathVariable Long id) {
        boolean success = dictService.deleteDictData(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}