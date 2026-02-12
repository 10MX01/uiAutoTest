package com.uiauto.testcase.service;

import com.uiauto.testcase.dto.LoginRequest;
import com.uiauto.testcase.dto.UserCreateRequest;
import com.uiauto.testcase.dto.UserUpdateRequest;
import com.uiauto.testcase.vo.LoginResponse;
import com.uiauto.testcase.vo.UserResponse;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含Token）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 获取所有用户列表
     *
     * @param role   角色筛选（可选）
     * @param status 状态筛选（可选）
     * @param search 搜索关键词（可选）
     * @return 用户列表
     */
    List<UserResponse> getAllUsers(String role, String status, String search);

    /**
     * 获取执行人列表（用于下拉框）
     *
     * @return 执行人列表
     */
    List<UserResponse> getExecutors();

    /**
     * 根据ID获取用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserResponse getUserById(Long userId);

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @param creatorId 创建人ID
     * @return 创建的用户信息
     */
    UserResponse createUser(UserCreateRequest request, Long creatorId);

    /**
     * 更新用户
     *
     * @param userId      用户ID
     * @param request     更新请求
     * @param updaterId   更新人ID
     * @return 更新后的用户信息
     */
    UserResponse updateUser(Long userId, UserUpdateRequest request, Long updaterId);

    /**
     * 启用用户
     *
     * @param userId    用户ID
     * @param updaterId 更新人ID
     */
    void enableUser(Long userId, Long updaterId);

    /**
     * 禁用用户
     *
     * @param userId    用户ID
     * @param updaterId 更新人ID
     */
    void disableUser(Long userId, Long updaterId);

    /**
     * 删除用户（软删除，改为禁用状态）
     *
     * @param userId    用户ID
     * @param updaterId 更新人ID
     */
    void deleteUser(Long userId, Long updaterId);
}
