package com.uiauto.testcase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.dto.ProjectCreateRequest;
import com.uiauto.testcase.dto.ProjectUpdateRequest;
import com.uiauto.testcase.service.ProjectService;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.vo.ProjectResponse;
import com.uiauto.testcase.vo.TestCaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController单元测试
 */
@WebMvcTest(ProjectController.class)
@DisplayName("项目管理Controller测试")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private TestCaseService testCaseService;

    private ProjectCreateRequest createRequest;
    private ProjectUpdateRequest updateRequest;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
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

        projectResponse = ProjectResponse.builder()
                .uniqueId(1L)
                .name("测试项目")
                .description("这是一个测试项目")
                .code("TEST-001")
                .createdBy(1L)
                .updatedBy(1L)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .testCaseCount(5L)
                .build();
    }

    @Test
    @DisplayName("应该成功创建项目")
    void shouldCreateProject() throws Exception {
        // Given
        when(projectService.create(any(ProjectCreateRequest.class))).thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("项目创建成功"))
                .andExpect(jsonPath("$.data").value(1));

        verify(projectService, times(1)).create(any(ProjectCreateRequest.class));
    }

    @Test
    @DisplayName("应该成功查询所有项目")
    void shouldListAllProjects() throws Exception {
        // Given
        List<ProjectResponse> responses = Arrays.asList(
                ProjectResponse.builder().uniqueId(1L).name("项目1").code("PROJ-001").build(),
                ProjectResponse.builder().uniqueId(2L).name("项目2").code("PROJ-002").build()
        );
        when(projectService.listAll()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("项目1"));

        verify(projectService, times(1)).listAll();
    }

    @Test
    @DisplayName("应该成功查询项目详情")
    void shouldGetProjectById() throws Exception {
        // Given
        Long uniqueId = 1L;
        when(projectService.getById(uniqueId)).thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(get("/projects/{uniqueId}", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uniqueId").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"))
                .andExpect(jsonPath("$.data.code").value("TEST-001"))
                .andExpect(jsonPath("$.data.testCaseCount").value(5));

        verify(projectService, times(1)).getById(uniqueId);
    }

    @Test
    @DisplayName("应该成功更新项目")
    void shouldUpdateProject() throws Exception {
        // Given
        Long uniqueId = 1L;
        doNothing().when(projectService).update(eq(uniqueId), any(ProjectUpdateRequest.class));

        // When & Then
        mockMvc.perform(post("/projects/{uniqueId}", uniqueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("项目更新成功"));

        verify(projectService, times(1)).update(eq(uniqueId), any(ProjectUpdateRequest.class));
    }

    @Test
    @DisplayName("应该成功删除项目")
    void shouldDeleteProject() throws Exception {
        // Given
        Long uniqueId = 1L;
        doNothing().when(projectService).delete(uniqueId);

        // When & Then
        mockMvc.perform(post("/projects/{uniqueId}/delete", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("项目已删除"));

        verify(projectService, times(1)).delete(uniqueId);
    }

    @Test
    @DisplayName("应该成功查询项目下的测试用例")
    void shouldGetTestCasesByProject() throws Exception {
        // Given
        Long uniqueId = 1L;
        List<TestCaseResponse> testCases = Arrays.asList(
                TestCaseResponse.builder().uniqueId(1L).name("用例1").build(),
                TestCaseResponse.builder().uniqueId(2L).name("用例2").build()
        );
        when(testCaseService.listByProjectId(uniqueId)).thenReturn(testCases);

        // When & Then
        mockMvc.perform(get("/projects/{uniqueId}/test-cases", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("用例1"));

        verify(testCaseService, times(1)).listByProjectId(uniqueId);
    }

    @Test
    @DisplayName("创建项目时参数验证失败应该返回错误")
    void shouldReturnErrorWhenCreateRequestValidationFails() throws Exception {
        // Given - 创建一个name为空的请求
        ProjectCreateRequest invalidRequest = ProjectCreateRequest.builder()
                .name("") // 空名称应该验证失败
                .code("TEST-001")
                .build();

        // When & Then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).create(any(ProjectCreateRequest.class));
    }

    @Test
    @DisplayName("创建项目时代码为空应该验证失败")
    void shouldReturnErrorWhenCodeIsEmpty() throws Exception {
        // Given - 创建一个code为空的请求
        ProjectCreateRequest invalidRequest = ProjectCreateRequest.builder()
                .name("测试项目")
                .code("") // 空代码应该验证失败
                .build();

        // When & Then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).create(any(ProjectCreateRequest.class));
    }

    @Test
    @DisplayName("更新项目时参数验证失败应该返回错误")
    void shouldReturnErrorWhenUpdateRequestValidationFails() throws Exception {
        // Given
        Long uniqueId = 1L;
        ProjectUpdateRequest invalidRequest = ProjectUpdateRequest.builder()
                .name("") // 空名称应该验证失败
                .code("TEST-002")
                .build();

        // When & Then
        mockMvc.perform(post("/projects/{uniqueId}", uniqueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).update(eq(uniqueId), any(ProjectUpdateRequest.class));
    }
}
