package com.sudutech.system.service;

import com.sudutech.system.domain.SysPost;

import java.util.List;

/**
 * 岗位服务
 */
public interface SysPostService {

    List<SysPost> selectAll();

    SysPost selectById(Long postId);

    boolean insert(SysPost post);

    boolean update(SysPost post);

    int deleteByIds(List<Long> postIds);
}
