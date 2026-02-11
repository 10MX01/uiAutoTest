package com.uiauto.testcase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.dto.TestCaseCreateRequest;
import com.uiauto.testcase.dto.TestCaseUpdateRequest;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.vo.TestCaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TestCaseController单元测试
 */
@WebMvcTest(TestCaseController.class)
@DisplayName("测试用例Controller测试")
class TestCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestCaseService testCaseService;

    private TestCaseCreateRequest createRequest;
    private TestCaseUpdateRequest updateRequest;
    private TestCaseResponse response;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        createRequest = TestCaseCreateRequest.builder()
                .name("测试用例名称")
                .description("测试用例描述")
                .stepsText("1. 打开首页\n2. 点击搜索")
                .stepsJson("{\"action\":\"navigate\"}")
                .expectedResult("操作成功")
                .priority("P1")
                .status("ACTIVE")
                .build();

        updateRequest = TestCaseUpdateRequest.builder()
                .name("更新后的测试用例")
                .description("更新后的描述")
                .stepsText("1. 打开首页\n2. 点击搜索\n3. 验证结果")
                .expectedResult("验证通过")
                .priority("P0")
                .status("ACTIVE")
                .build();

        response = TestCaseResponse.builder()
                .uniqueId(1L)
                .name("测试用例")
                .description("测试描述")
                .stepsText("测试步骤")
                .priority("P1")
                .status("ACTIVE")
                .createdBy(1L)
                .updatedBy(1L)
                .build();
    }

    @Test
    @DisplayName("应该成功创建测试用例")
    void shouldCreateTestCase() throws Exception {
        // Given
        when(testCaseService.create(any(TestCaseCreateRequest.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/test-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("测试用例创建成功"))
                .andExpect(jsonPath("$.data").value(1));

        verify(testCaseService, times(1)).create(any(TestCaseCreateRequest.class));
    }

    @Test
    @DisplayName("应该成功更新测试用例")
    void shouldUpdateTestCase() throws Exception {
        // Given
        Long uniqueId = 1L;
        doNothing().when(testCaseService).update(eq(uniqueId), any(TestCaseUpdateRequest.class));

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}", uniqueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("测试用例更新成功"));

        verify(testCaseService, times(1)).update(eq(uniqueId), any(TestCaseUpdateRequest.class));
    }

    @Test
    @DisplayName("应该成功删除测试用例")
    void shouldDeleteTestCase() throws Exception {
        // Given
        Long uniqueId = 1L;
        doNothing().when(testCaseService).delete(uniqueId);

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}/delete", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("测试用例已删除"));

        verify(testCaseService, times(1)).delete(uniqueId);
    }

    @Test
    @DisplayName("应该成功查询测试用例详情")
    void shouldGetTestCaseById() throws Exception {
        // Given
        Long uniqueId = 1L;
        when(testCaseService.getById(uniqueId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/test-cases/{uniqueId}", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uniqueId").value(1))
                .andExpect(jsonPath("$.data.name").value("测试用例"));

        verify(testCaseService, times(1)).getById(uniqueId);
    }

    @Test
    @DisplayName("应该成功查询所有测试用例")
    void shouldListAllTestCases() throws Exception {
        // Given
        List<TestCaseResponse> responses = Arrays.asList(
                TestCaseResponse.builder().uniqueId(1L).name("测试用例1").build(),
                TestCaseResponse.builder().uniqueId(2L).name("测试用例2").build()
        );
        when(testCaseService.listAll()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/test-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(testCaseService, times(1)).listAll();
    }

    @Test
    @DisplayName("应该成功按状态查询测试用例")
    void shouldListTestCasesByStatus() throws Exception {
        // Given
        String status = "ACTIVE";
        List<TestCaseResponse> responses = Arrays.asList(
                TestCaseResponse.builder().uniqueId(1L).name("活跃用例").status(status).build()
        );
        when(testCaseService.listByStatus(status)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/test-cases/by-status")
                        .param("status", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(testCaseService, times(1)).listByStatus(status);
    }

    @Test
    @DisplayName("应该成功按优先级查询测试用例")
    void shouldListTestCasesByPriority() throws Exception {
        // Given
        String priority = "P1";
        List<TestCaseResponse> responses = Arrays.asList(
                TestCaseResponse.builder().uniqueId(1L).name("紧急用例").priority(priority).build()
        );
        when(testCaseService.listByPriority(priority)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/test-cases/by-priority")
                        .param("priority", priority))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(testCaseService, times(1)).listByPriority(priority);
    }

    @Test
    @DisplayName("应该成功按创建人查询测试用例")
    void shouldListTestCasesByCreator() throws Exception {
        // Given
        Long createdBy = 1L;
        List<TestCaseResponse> responses = Arrays.asList(
                TestCaseResponse.builder().uniqueId(1L).name("我的用例").createdBy(createdBy).build()
        );
        when(testCaseService.listByCreator(createdBy)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/test-cases/by-creator")
                        .param("createdBy", String.valueOf(createdBy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(testCaseService, times(1)).listByCreator(createdBy);
    }

    @Test
    @DisplayName("应该成功搜索测试用例")
    void shouldSearchTestCases() throws Exception {
        // Given
        String keyword = "登录";
        List<TestCaseResponse> responses = Arrays.asList(
                TestCaseResponse.builder().uniqueId(1L).name("用户登录测试").build()
        );
        when(testCaseService.search(keyword)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/test-cases/search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(testCaseService, times(1)).search(keyword);
    }

    @Test
    @DisplayName("创建测试用例参数验证失败应该返回错误")
    void shouldReturnErrorWhenCreateRequestValidationFails() throws Exception {
        // Given - 创建一个name为空的请求
        TestCaseCreateRequest invalidRequest = TestCaseCreateRequest.builder()
                .name("") // 空名称应该验证失败
                .stepsText("测试步骤")
                .build();

        // When & Then
        mockMvc.perform(post("/test-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(testCaseService, never()).create(any(TestCaseCreateRequest.class));
    }
}
