package com.kadmin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.system.domain.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典数据Mapper
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 根据字典编码查询字典数据
     */
    @Select("SELECT d.* FROM sys_dict_data d " +
            "INNER JOIN sys_dict_type t ON d.dict_type_id = t.id " +
            "WHERE t.dict_code = #{dictCode} AND d.status = 1 " +
            "ORDER BY d.sort_order")
    List<SysDictData> selectByDictCode(@Param("dictCode") String dictCode);
}