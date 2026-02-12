package com.uiauto.testcase.service.impl;

import com.uiauto.testcase.dto.LoginRequest;
import com.uiauto.testcase.dto.UserCreateRequest;
import com.uiauto.testcase.dto.UserUpdateRequest;
import com.uiauto.testcase.entity.UserEntity;
import com.uiauto.testcase.repository.UserRepository;
import com.uiauto.testcase.service.UserService;
import com.uiauto.testcase.util.JwtUtil;
import com.uiauto.testcase.util.PasswordUtil;
import com.uiauto.testcase.vo.LoginResponse;
import com.uiauto.testcase.vo.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求, username: {}", request.getUsername());

        // 查找用户
        UserEntity user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", request.getUsername());
                    return new RuntimeException("用户名或密码错误");
                });

        log.info("找到用户: {}, status: {}", user.getUsername(), user.getStatus());

        // 验证密码
        boolean passwordMatch = PasswordUtil.matches(request.getPassword(), user.getPassword());
        log.info("密码验证结果: {}", passwordMatch);

        if (!passwordMatch) {
            log.error("密码验证失败! 输入密码: {}, 数据库hash: {}", request.getPassword(), user.getPassword());
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("用户已被禁用，请联系管理员");
        }

        // 生成Token
        String token = JwtUtil.generateToken(user.getUniqueId(), user.getUsername(), user.getRole());
        log.info("Token生成成功");

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        repository.save(user);

        // 构建响应
        return new LoginResponse(
                token,
                user.getUniqueId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getLastLoginTime()
        );
    }

    @Override
    public void logout(Long userId) {
        // 无状态JWT，登出只需客户端删除Token
        // 可以在这里添加登出日志等操作
    }

    @Override
    public List<UserResponse> getAllUsers(String role, String status, String search) {
        List<UserEntity> users;

        // 根据条件查询
        if (search != null && !search.trim().isEmpty()) {
            // 搜索模式
            users = repository.searchActiveUsers(search.trim());
        } else if (role != null && !role.trim().isEmpty() && status != null && !status.trim().isEmpty()) {
            users = repository.findByRoleAndStatus(role, status);
        } else if (role != null && !role.trim().isEmpty()) {
            users = repository.findByRole(role);
        } else if (status != null && !status.trim().isEmpty()) {
            users = repository.findByStatus(status);
        } else {
            users = repository.findAll();
        }

        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getExecutors() {
        List<UserEntity> users = repository.findActiveUsers();
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long userId) {
        UserEntity user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toResponse(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request, Long creatorId) {
        // 检查用户名是否已存在
        if (repository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus("ACTIVE");
        user.setCreatedBy(creatorId);

        UserEntity saved = repository.save(user);
        return toResponse(saved);
    }

    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request, Long updaterId) {
        UserEntity user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户信息
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        user.setUpdatedBy(updaterId);

        UserEntity saved = repository.save(user);
        return toResponse(saved);
    }

    @Override
    public void enableUser(Long userId, Long updaterId) {
        UserEntity user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setStatus("ACTIVE");
        user.setUpdatedBy(updaterId);
        repository.save(user);
    }

    @Override
    public void disableUser(Long userId, Long updaterId) {
        UserEntity user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setStatus("DISABLED");
        user.setUpdatedBy(updaterId);
        repository.save(user);
    }

    @Override
    public void deleteUser(Long userId, Long updaterId) {
        UserEntity user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 软删除，改为禁用状态
        user.setStatus("DISABLED");
        user.setUpdatedBy(updaterId);
        repository.save(user);
    }

    /**
     * 实体转VO
     */
    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getUniqueId(),
                entity.getUsername(),
                entity.getRealName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getRole(),
                entity.getStatus(),
                entity.getLastLoginTime(),
                entity.getCreatedTime()
        );
    }
}
