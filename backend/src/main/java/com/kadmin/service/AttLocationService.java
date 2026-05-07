package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.AttLocation;
import com.kadmin.entity.AttLocationEmployee;
import com.kadmin.entity.HrEmployee;
import com.kadmin.mapper.AttLocationEmployeeMapper;
import com.kadmin.mapper.AttLocationMapper;
import com.kadmin.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttLocationService extends ServiceImpl<AttLocationMapper, AttLocation> {

    private final AttLocationEmployeeMapper locationEmployeeMapper;
    private final EmployeeMapper employeeMapper;

    public Page<AttLocation> getPage(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<AttLocation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AttLocation::getLocationName, keyword)
                    .or()
                    .like(AttLocation::getAddress, keyword));
        }
        wrapper.orderByDesc(AttLocation::getUpdatedTime).orderByDesc(AttLocation::getId);

        Page<AttLocation> page = page(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(this::fillAssignments);
        return page;
    }

    public AttLocation getDetail(Long id) {
        AttLocation location = getById(id);
        if (location != null) {
            fillAssignments(location);
        }
        return location;
    }

    @Transactional
    public boolean saveLocation(AttLocation location) {
        if (location.getStatus() == null) {
            location.setStatus(1);
        }
        if (location.getClockRange() == null) {
            location.setClockRange(300);
        }

        boolean saved = location.getId() == null ? save(location) : updateById(location);
        if (saved) {
            saveAssignments(location.getId(), location.getEmployeeIds());
        }
        return saved;
    }

    @Transactional
    public boolean deleteLocation(Long id) {
        locationEmployeeMapper.delete(new LambdaQueryWrapper<AttLocationEmployee>()
                .eq(AttLocationEmployee::getLocationId, id));
        return removeById(id);
    }

    public List<AttLocation> getAssignedActiveLocations(Long employeeId) {
        return locationEmployeeMapper.selectActiveLocationsByEmployeeId(employeeId);
    }

    private void saveAssignments(Long locationId, List<Long> employeeIds) {
        locationEmployeeMapper.delete(new LambdaQueryWrapper<AttLocationEmployee>()
                .eq(AttLocationEmployee::getLocationId, locationId));

        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }

        List<Long> normalizedIds = employeeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (normalizedIds.isEmpty()) {
            return;
        }

        for (Long employeeId : normalizedIds) {
            AttLocationEmployee assignment = new AttLocationEmployee();
            assignment.setLocationId(locationId);
            assignment.setEmployeeId(employeeId);
            locationEmployeeMapper.insert(assignment);
        }
    }

    private void fillAssignments(AttLocation location) {
        List<AttLocationEmployee> assignments = locationEmployeeMapper.selectList(
                new LambdaQueryWrapper<AttLocationEmployee>()
                        .eq(AttLocationEmployee::getLocationId, location.getId()));
        List<Long> employeeIds = assignments.stream().map(AttLocationEmployee::getEmployeeId).toList();

        location.setEmployeeIds(employeeIds);
        location.setAssignedCount(employeeIds.size());
        if (employeeIds.isEmpty()) {
            location.setEmployees(Collections.emptyList());
            return;
        }

        location.setEmployees(employeeMapper.selectBatchIds(employeeIds));
    }
}
