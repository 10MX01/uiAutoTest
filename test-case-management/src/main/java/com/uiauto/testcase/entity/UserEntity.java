package com.uiauto.testcase.entity;

import com.uiauto.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 用户实体类
 * 管理系统用户，支持登录认证和权限管理
 */
@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity {

    /**
     * 用户名（登录名）
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", nullable = false, length = 100)
    private String realName;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 角色：ADMIN-管理员, USER-普通用户
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    /**
     * 状态：ACTIVE-启用, DISABLED-禁用
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private java.time.LocalDateTime lastLoginTime;
}
