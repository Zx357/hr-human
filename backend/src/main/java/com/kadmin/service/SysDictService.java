package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.SysDictData;
import com.kadmin.entity.SysDictType;
import com.kadmin.mapper.SysDictDataMapper;
import com.kadmin.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典服务
 */
@Service
@RequiredArgsConstructor
public class SysDictService extends ServiceImpl<SysDictTypeMapper, SysDictType> {

    private final SysDictDataMapper dictDataMapper;

    /**
     * 获取所有字典类型
     */
    public List<SysDictType> getAllDictTypes() {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDictType::getId);
        return list(wrapper);
    }

    /**
     * 根据字典编码查询字典类型
     */
    public SysDictType getByDictCode(String dictCode) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictCode, dictCode);
        return getOne(wrapper);
    }

    /**
     * 新增字典类型
     */
    @Transactional
    public boolean addDictType(SysDictType dictType) {
        // 检查编码是否已存在
        SysDictType exist = getByDictCode(dictType.getDictCode());
        if (exist != null) {
            throw new RuntimeException("字典编码已存在");
        }
        return save(dictType);
    }

    /**
     * 更新字典类型
     */
    @Transactional
    public boolean updateDictType(SysDictType dictType) {
        return updateById(dictType);
    }

    /**
     * 删除字典类型（逻辑删除）
     */
    @Transactional
    public boolean deleteDictType(Long id) {
        // 先删除关联的字典数据
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictTypeId, id);
        // 使用delete方法，MyBatis Plus会自动处理逻辑删除
        dictDataMapper.delete(wrapper);

        // 删除字典类型，使用removeById，MyBatis Plus会自动处理逻辑删除
        return removeById(id);
    }

    /**
     * 获取字典数据列表
     */
    public List<SysDictData> getDictDataList(Long dictTypeId) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictTypeId, dictTypeId);
        wrapper.orderByAsc(SysDictData::getSortOrder);
        return dictDataMapper.selectList(wrapper);
    }

    /**
     * 根据字典编码获取字典数据
     */
    public List<SysDictData> getDictDataByCode(String dictCode) {
        return dictDataMapper.selectByDictCode(dictCode);
    }

    /**
     * 新增字典数据
     */
    @Transactional
    public boolean addDictData(SysDictData dictData) {
        return dictDataMapper.insert(dictData) > 0;
    }

    /**
     * 更新字典数据
     */
    @Transactional
    public boolean updateDictData(SysDictData dictData) {
        return dictDataMapper.updateById(dictData) > 0;
    }

    /**
     * 删除字典数据（逻辑删除）
     */
    @Transactional
    public boolean deleteDictData(Long id) {
        // 使用deleteById方法，MyBatis Plus会自动处理逻辑删除
        return dictDataMapper.deleteById(id) > 0;
    }

    /**
     * 获取字典数据详情
     */
    public SysDictData getDictDataById(Long id) {
        return dictDataMapper.selectById(id);
    }
}
