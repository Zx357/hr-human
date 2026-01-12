package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.AttShift;
import com.kadmin.entity.AttShiftPeriod;
import com.kadmin.mapper.AttShiftMapper;
import com.kadmin.mapper.AttShiftPeriodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftService extends ServiceImpl<AttShiftMapper, AttShift> {
    private final AttShiftPeriodMapper periodMapper;

    public List<AttShift> listAll() {
        List<AttShift> shifts = list(new LambdaQueryWrapper<AttShift>()
                .eq(AttShift::getDeleted, 0)
                .orderByAsc(AttShift::getId));
        shifts.forEach(shift -> shift.setPeriods(
                periodMapper.selectList(new LambdaQueryWrapper<AttShiftPeriod>()
                        .eq(AttShiftPeriod::getShiftId, shift.getId())
                        .orderByAsc(AttShiftPeriod::getSortOrder))));
        return shifts;
    }

    public AttShift getDetailById(Long id) {
        AttShift shift = getById(id);
        if (shift != null) {
            shift.setPeriods(periodMapper.selectList(new LambdaQueryWrapper<AttShiftPeriod>()
                    .eq(AttShiftPeriod::getShiftId, id)
                    .orderByAsc(AttShiftPeriod::getSortOrder)));
        }
        return shift;
    }

    @Transactional
    public boolean saveShift(AttShift shift) {
        shift.setDeleted(0);
        if (shift.getStatus() == null) shift.setStatus(1);
        boolean result = save(shift);
        if (result && shift.getPeriods() != null) {
            for (int i = 0; i < shift.getPeriods().size(); i++) {
                AttShiftPeriod period = shift.getPeriods().get(i);
                period.setShiftId(shift.getId());
                period.setSortOrder(i + 1);
                periodMapper.insert(period);
            }
        }
        return result;
    }

    @Transactional
    public boolean updateShift(AttShift shift) {
        boolean result = updateById(shift);
        if (result && shift.getPeriods() != null) {
            periodMapper.delete(new LambdaQueryWrapper<AttShiftPeriod>()
                    .eq(AttShiftPeriod::getShiftId, shift.getId()));
            for (int i = 0; i < shift.getPeriods().size(); i++) {
                AttShiftPeriod period = shift.getPeriods().get(i);
                period.setId(null);
                period.setShiftId(shift.getId());
                period.setSortOrder(i + 1);
                periodMapper.insert(period);
            }
        }
        return result;
    }
}
