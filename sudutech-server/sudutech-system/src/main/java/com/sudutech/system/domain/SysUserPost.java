package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户岗位关联 sys_user_post
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("sys_user_post")
public class SysUserPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long userId;

    @Id
    private Long postId;
}
