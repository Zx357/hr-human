package com.kadmin.web.controller.attendance;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.attendance.domain.AttShift;
import com.kadmin.attendance.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/attendance/shift")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService service;

    @GetMapping("/list")
    public Result<List<AttShift>> list() {
        return Result.success(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<AttShift> getById(@PathVariable Long id) {
        return Result.success(service.getDetailById(id));
    }

    @RequiresPermission("attendance:shift:add")
    @PostMapping
    public Result<Void> add(@RequestBody AttShift shift) {
        service.saveShift(shift);
        return Result.success();
    }

    @RequiresPermission("attendance:shift:edit")
    @PutMapping
    public Result<Void> update(@RequestBody AttShift shift) {
        service.updateShift(shift);
        return Result.success();
    }

    @RequiresPermission("attendance:shift:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.deleteShift(id);
        return Result.success();
    }
}
