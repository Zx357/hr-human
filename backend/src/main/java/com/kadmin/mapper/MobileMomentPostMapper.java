package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.entity.MobileMomentPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MobileMomentPostMapper extends BaseMapper<MobileMomentPost> {
    Page<MobileMomentPost> selectPostPage(Page<MobileMomentPost> page,
            @Param("tab") String tab,
            @Param("viewerEmployeeId") Long viewerEmployeeId);

    MobileMomentPost selectPostDetail(@Param("id") Long id,
            @Param("viewerEmployeeId") Long viewerEmployeeId);
}
