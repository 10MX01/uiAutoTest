package com.uiauto.testcase.repository;

import com.uiauto.testcase.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据角色查询用户列表
     *
     * @param role 角色
     * @return 用户列表
     */
    List<UserEntity> findByRole(String role);

    /**
     * 根据状态查询用户列表
     *
     * @param status 状态
     * @return 用户列表
     */
    List<UserEntity> findByStatus(String status);

    /**
     * 根据角色和状态查询用户列表
     *
     * @param role   角色
     * @param status 状态
     * @return 用户列表
     */
    List<UserEntity> findByRoleAndStatus(String role, String status);

    /**
     * 搜索用户（按用户名或真实姓名模糊查询）
     *
     * @param search 搜索关键词
     * @return 用户列表
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' AND " +
           "(u.username LIKE %:search% OR u.realName LIKE %:search%) " +
           "ORDER BY u.realName")
    List<UserEntity> searchActiveUsers(@Param("search") String search);

    /**
     * 获取所有启用的用户（用于执行人下拉列表）
     *
     * @return 启用的用户列表
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' ORDER BY u.realName")
    List<UserEntity> findActiveUsers();
}
