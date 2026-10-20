package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysPost;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 岗位 Mapper
 */
public interface SysPostMapper extends BaseMapper<SysPost> {

    @Delete("delete from sys_user_post where user_id = #{userId}")
    int deleteUserPostByUserId(@Param("userId") Long userId);
}
