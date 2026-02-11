package com.uiauto.testcase.service.impl;

import com.uiauto.testcase.dto.ProjectCreateRequest;
import com.uiauto.testcase.dto.ProjectUpdateRequest;
import com.uiauto.testcase.entity.ProjectEntity;
import com.uiauto.testcase.repository.ProjectRepository;
import com.uiauto.testcase.service.ProjectService;
import com.uiauto.testcase.vo.ProjectResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProjectService集成测试（使用H2内存数据库）
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class ProjectServiceImplTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    private ProjectCreateRequest createRequest;
    private ProjectUpdateRequest updateRequest;
    private static Long createdId;

    @BeforeEach
    void setUp() {
        // 清理数据库
        projectRepository.deleteAll();

        // 准备测试数据
        createRequest = ProjectCreateRequest.builder()
                .name("测试项目")
                .description("这是一个测试项目")
                .code("TEST-001")
                .build();

        updateRequest = ProjectUpdateRequest.builder()
                .name("更新后的项目")
                .description("更新后的描述")
                .code("TEST-002")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("应该成功创建项目")
    void shouldCreateProject() {
        // When
        ProjectResponse response = projectService.create(createRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getUniqueId());
        assertEquals("测试项目", response.getName());
        assertEquals("TEST-001", response.getCode());
        assertEquals("这是一个测试项目", response.getDescription());
        assertEquals(0L, response.getTestCaseCount()); // 新项目应该没有测试用例

        // 验证数据库中存在该记录
        ProjectEntity entity = projectRepository.findById(response.getUniqueId()).orElse(null);
        assertNotNull(entity);
        assertEquals("TEST-001", entity.getCode());

        createdId = response.getUniqueId();
    }

    @Test
    @Order(2)
    @DisplayName("应该成功更新项目")
    void shouldUpdateProject() {
        // Given - 先创建一个项目
        ProjectResponse created = projectService.create(createRequest);
        Long uniqueId = created.getUniqueId();

        // When
        ProjectResponse updated = projectService.update(uniqueId, updateRequest);

        // Then
        assertNotNull(updated);
        assertEquals("更新后的项目", updated.getName());
        assertEquals("TEST-002", updated.getCode());
        assertEquals("更新后的描述", updated.getDescription());

        // 验证数据库中已更新
        ProjectEntity entity = projectRepository.findById(uniqueId).orElse(null);
        assertNotNull(entity);
        assertEquals("更新后的项目", entity.getName());
        assertEquals("TEST-002", entity.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("应该成功查询所有项目")
    void shouldListAllProjects() {
        // Given - 创建多个项目
        projectService.create(createRequest);

        ProjectCreateRequest request2 = ProjectCreateRequest.builder()
                .name("项目2")
                .code("TEST-002")
                .build();
        projectService.create(request2);

        // When
        List<ProjectResponse> projects = projectService.listAll();

        // Then
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("测试项目")));
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("项目2")));
    }

    @Test
    @Order(4)
    @DisplayName("应该成功根据ID查询项目")
    void shouldGetProjectById() {
        // Given
        ProjectResponse created = projectService.create(createRequest);
        Long uniqueId = created.getUniqueId();

        // When
        ProjectResponse found = projectService.getById(uniqueId);

        // Then
        assertNotNull(found);
        assertEquals(uniqueId, found.getUniqueId());
        assertEquals("测试项目", found.getName());
        assertEquals("TEST-001", found.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("应该成功删除项目")
    void shouldDeleteProject() {
        // Given
        ProjectResponse created = projectService.create(createRequest);
        Long uniqueId = created.getUniqueId();

        // When
        projectService.delete(uniqueId);

        // Then
        assertFalse(projectRepository.existsById(uniqueId));
    }

    @Test
    @Order(6)
    @DisplayName("删除有测试用例的项目应该抛出异常")
    void shouldThrowExceptionWhenDeleteProjectWithTestCases() {
        // TODO: 这个测试需要关联测试用例，需要先实现测试用例的创建逻辑
        // Given - 创建一个项目并关联测试用例
        // When - 尝试删除
        // Then - 应该抛出异常
    }

    @Test
    @DisplayName("创建项目时代码重复应该抛出异常")
    void shouldThrowExceptionWhenCreateProjectWithDuplicateCode() {
        // Given
        projectService.create(createRequest);

        ProjectCreateRequest duplicateRequest = ProjectCreateRequest.builder()
                .name("另一个项目")
                .code("TEST-001") // 重复的代码
                .build();

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.create(duplicateRequest);
        });

        assertTrue(exception.getMessage().contains("项目代码已存在"));
    }

    @Test
    @DisplayName("更新项目时将代码改为已存在的代码应该抛出异常")
    void shouldThrowExceptionWhenUpdateProjectWithExistingCode() {
        // Given - 创建两个项目
        projectService.create(createRequest);

        ProjectCreateRequest request2 = ProjectCreateRequest.builder()
                .name("项目2")
                .code("TEST-002")
                .build();
        ProjectResponse project2 = projectService.create(request2);

        ProjectUpdateRequest updateRequest = ProjectUpdateRequest.builder()
                .name("更新后的项目2")
                .code("TEST-001") // 使用项目1的代码
                .build();

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.update(project2.getUniqueId(), updateRequest);
        });

        assertTrue(exception.getMessage().contains("项目代码已被其他项目使用"));
    }

    @Test
    @DisplayName("查询不存在的项目应该抛出异常")
    void shouldThrowExceptionWhenGetNonExistentProject() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.getById(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("项目不存在"));
    }

    @Test
    @DisplayName("更新不存在的项目应该抛出异常")
    void shouldThrowExceptionWhenUpdateNonExistentProject() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.update(nonExistentId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("项目不存在"));
    }

    @Test
    @DisplayName("删除不存在的项目应该抛出异常")
    void shouldThrowExceptionWhenDeleteNonExistentProject() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.delete(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("项目不存在"));
    }

    @Test
    @DisplayName("验证项目存在性应该返回正确结果")
    void shouldCheckProjectExists() {
        // Given
        ProjectResponse created = projectService.create(createRequest);

        // When
        boolean exists = projectService.existsById(created.getUniqueId());
        boolean notExists = projectService.existsById(9999L);

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }
}
