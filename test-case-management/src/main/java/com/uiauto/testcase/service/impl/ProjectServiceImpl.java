package com.uiauto.testcase.service.impl;

import com.uiauto.testcase.dto.ProjectCreateRequest;
import com.uiauto.testcase.dto.ProjectUpdateRequest;
import com.uiauto.testcase.entity.ProjectEntity;
import com.uiauto.testcase.repository.ProjectRepository;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.testcase.service.ProjectService;
import com.uiauto.testcase.vo.ProjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        log.info("创建项目: {}", request.getName());

        // 1. 验证项目代码唯一性
        if (projectRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("项目代码已存在: " + request.getCode());
        }

        // 2. 创建项目实体
        ProjectEntity entity = ProjectEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .code(request.getCode())
                .targetUrl(request.getTargetUrl())
                .baseUrl(request.getBaseUrl())
                .build();

        // 设置审计字段
        entity.setCreatedBy(1L); // TODO: 从当前登录用户获取
        entity.setUpdatedBy(1L);

        // 3. 保存项目
        ProjectEntity saved = projectRepository.save(entity);

        log.info("项目创建成功, ID: {}", saved.getUniqueId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse update(Long uniqueId, ProjectUpdateRequest request) {
        log.info("更新项目: {}", uniqueId);

        // 1. 查询原实体
        ProjectEntity entity = projectRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + uniqueId));

        // 2. 验证项目代码唯一性（排除当前项目）
        projectRepository.findByCode(request.getCode()).ifPresent(existing -> {
            if (!existing.getUniqueId().equals(uniqueId)) {
                throw new RuntimeException("项目代码已被其他项目使用: " + request.getCode());
            }
        });

        // 3. 更新字段
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCode(request.getCode());
        entity.setTargetUrl(request.getTargetUrl());
        entity.setBaseUrl(request.getBaseUrl());
        entity.setUpdatedBy(1L); // TODO: 从当前登录用户获取

        // 4. 保存更新
        ProjectEntity saved = projectRepository.save(entity);

        log.info("项目更新成功: {}", uniqueId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long uniqueId) {
        log.info("删除项目: {}", uniqueId);

        // 1. 查询项目
        ProjectEntity entity = projectRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + uniqueId));

        // 2. 检查是否有关联的测试用例
        long testCaseCount = testCaseRepository.countByProjectId(uniqueId);
        if (testCaseCount > 0) {
            throw new RuntimeException("该项目下还有 " + testCaseCount + " 个测试用例，无法删除");
        }

        // 3. 物理删除
        projectRepository.delete(entity);

        log.info("项目删除成功: {}", uniqueId);
    }

    @Override
    public List<ProjectResponse> listAll() {
        log.info("查询所有项目");

        List<ProjectEntity> projects = projectRepository.findAll();

        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getById(Long uniqueId) {
        log.info("查询项目详情: {}", uniqueId);

        ProjectEntity entity = projectRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + uniqueId));

        return toResponse(entity);
    }

    @Override
    public boolean existsById(Long uniqueId) {
        return projectRepository.existsById(uniqueId);
    }

    /**
     * 转换为响应VO
     */
    private ProjectResponse toResponse(ProjectEntity entity) {
        long testCaseCount = testCaseRepository.countByProjectId(entity.getUniqueId());

        return ProjectResponse.builder()
                .uniqueId(entity.getUniqueId())
                .name(entity.getName())
                .description(entity.getDescription())
                .code(entity.getCode())
                .targetUrl(entity.getTargetUrl())
                .baseUrl(entity.getBaseUrl())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdTime(entity.getCreatedTime())
                .updatedTime(entity.getUpdatedTime())
                .testCaseCount(testCaseCount)
                .build();
    }
}
