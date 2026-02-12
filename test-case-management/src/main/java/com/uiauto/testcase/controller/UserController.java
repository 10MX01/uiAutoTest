package com.uiauto.testcase.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.dto.LoginRequest;
import com.uiauto.testcase.dto.UserCreateRequest;
import com.uiauto.testcase.dto.UserUpdateRequest;
import com.uiauto.testcase.service.UserService;
import com.uiauto.testcase.vo.LoginResponse;
import com.uiauto.testcase.vo.UserResponse;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userService.logout(userId);
            return ApiResponse.success("登出成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            List<UserResponse> users = userService.getAllUsers(role, status, search);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取执行人列表（用于下拉框）
     */
    @GetMapping("/executors")
    public ApiResponse<List<UserResponse>> getExecutors() {
        try {
            List<UserResponse> executors = userService.getExecutors();
            return ApiResponse.success(executors);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long creatorId = (Long) httpRequest.getAttribute("userId");
            UserResponse user = userService.createUser(request, creatorId);
            return ApiResponse.success("用户创建成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long updaterId = (Long) httpRequest.getAttribute("userId");
            UserResponse user = userService.updateUser(id, request, updaterId);
            return ApiResponse.success("用户更新成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enableUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long updaterId = (Long) httpRequest.getAttribute("userId");
            userService.enableUser(id, updaterId);
            return ApiResponse.success("用户已启用", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disableUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long updaterId = (Long) httpRequest.getAttribute("userId");
            userService.disableUser(id, updaterId);
            return ApiResponse.success("用户已禁用", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long updaterId = (Long) httpRequest.getAttribute("userId");
            userService.deleteUser(id, updaterId);
            return ApiResponse.success("用户已删除", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 生成密码hash（临时调试用）
     */
    @GetMapping("/generate-password")
    public ApiResponse<String> generatePassword(@RequestParam String password) {
        String hash = com.uiauto.testcase.util.PasswordUtil.encode(password);
        return ApiResponse.success("密码hash生成成功", hash);
    }
}
