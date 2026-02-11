package com.uiauto.testcase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.testcase.service.DependencyService;
import com.uiauto.testcase.vo.TestCaseDependencyResponse;
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
 * DependencyController单元测试
 */
@WebMvcTest(DependencyController.class)
@DisplayName("依赖关系Controller测试")
class DependencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DependencyService dependencyService;

    private DependencyController.AddDependencyRequest addRequest;
    private DependencyController.RemoveDependencyRequest removeRequest;
    private DependencyController.CalculateOrderRequest orderRequest;
    private TestCaseDependencyResponse dependencyResponse;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        addRequest = new DependencyController.AddDependencyRequest();
        addRequest.setPrerequisiteIds(Arrays.asList(2L, 3L));
        addRequest.setDependencyType("HARD");

        removeRequest = new DependencyController.RemoveDependencyRequest();
        removeRequest.setPrerequisiteIds(Arrays.asList(2L));

        orderRequest = new DependencyController.CalculateOrderRequest();
        orderRequest.setTestCaseIds(Arrays.asList(1L, 2L, 3L));

        dependencyResponse = TestCaseDependencyResponse.builder()
                .uniqueId(1L)
                .testCaseId(1L)
                .testCaseName("测试用例1")
                .prerequisiteId(2L)
                .prerequisiteName("前置用例2")
                .dependencyType("HARD")
                .createdBy(1L)
                .updatedBy(1L)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("应该成功添加依赖关系")
    void shouldAddDependency() throws Exception {
        // Given
        Long uniqueId = 1L;
        doNothing().when(dependencyService).addDependency(eq(uniqueId), any(List.class), any(String.class));

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}/dependencies", uniqueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("依赖关系添加成功"));

        verify(dependencyService, times(1)).addDependency(eq(uniqueId), any(List.class), eq("HARD"));
    }

    @Test
    @DisplayName("应该成功查询依赖列表")
    void shouldGetDependencies() throws Exception {
        // Given
        Long uniqueId = 1L;
        List<TestCaseDependencyResponse> dependencies = Arrays.asList(dependencyResponse);
        when(dependencyService.getPrerequisites(uniqueId)).thenReturn(dependencies);

        // When & Then
        mockMvc.perform(get("/test-cases/{uniqueId}/dependencies", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].testCaseId").value(1))
                .andExpect(jsonPath("$.data[0].prerequisiteId").value(2))
                .andExpect(jsonPath("$.data[0].dependencyType").value("HARD"));

        verify(dependencyService, times(1)).getPrerequisites(uniqueId);
    }

    @Test
    @DisplayName("应该成功移除依赖关系")
    void shouldRemoveDependency() throws Exception {
        // Given
        Long uniqueId = 1L;
        doNothing().when(dependencyService).removeDependency(eq(uniqueId), any(List.class));

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}/dependencies/remove", uniqueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("依赖关系移除成功"));

        verify(dependencyService, times(1)).removeDependency(eq(uniqueId), any(List.class));
    }

    @Test
    @DisplayName("应该成功计算执行顺序")
    void shouldCalculateExecutionOrder() throws Exception {
        // Given
        List<Long> orderedIds = Arrays.asList(1L, 2L, 3L);
        when(dependencyService.calculateExecutionOrder(any(List.class))).thenReturn(orderedIds);

        // When & Then
        mockMvc.perform(post("/test-cases/calculate-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("执行顺序计算成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value(1))
                .andExpect(jsonPath("$.data[1]").value(2))
                .andExpect(jsonPath("$.data[2]").value(3));

        verify(dependencyService, times(1)).calculateExecutionOrder(any(List.class));
    }

    @Test
    @DisplayName("应该成功查询后续依赖")
    void shouldGetDependents() throws Exception {
        // Given
        Long uniqueId = 1L;
        TestCaseDependencyResponse dependent = TestCaseDependencyResponse.builder()
                .uniqueId(2L)
                .testCaseId(3L)
                .testCaseName("后续用例3")
                .prerequisiteId(1L)
                .prerequisiteName("测试用例1")
                .dependencyType("SOFT")
                .build();
        List<TestCaseDependencyResponse> dependents = Arrays.asList(dependent);
        when(dependencyService.getDependents(uniqueId)).thenReturn(dependents);

        // When & Then
        mockMvc.perform(get("/test-cases/{uniqueId}/dependents", uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].testCaseId").value(3))
                .andExpect(jsonPath("$.data[0].prerequisiteId").value(1))
                .andExpect(jsonPath("$.data[0].dependencyType").value("SOFT"));

        verify(dependencyService, times(1)).getDependents(uniqueId);
    }

    @Test
    @DisplayName("添加依赖时前置用例ID列表为空应该验证失败")
    void shouldReturnErrorWhenPrerequisiteIdsIsEmpty() throws Exception {
        // Given - 创建一个prerequisiteIds为空的请求
        DependencyController.AddDependencyRequest invalidRequest = new DependencyController.AddDependencyRequest();
        invalidRequest.setPrerequisiteIds(Arrays.asList()); // 空列表应该验证失败
        invalidRequest.setDependencyType("HARD");

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}/dependencies", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(dependencyService, never()).addDependency(any(Long.class), any(List.class), any(String.class));
    }

    @Test
    @DisplayName("添加依赖时依赖类型为空应该验证失败")
    void shouldReturnErrorWhenDependencyTypeIsEmpty() throws Exception {
        // Given - 创建一个dependencyType为空的请求
        DependencyController.AddDependencyRequest invalidRequest = new DependencyController.AddDependencyRequest();
        invalidRequest.setPrerequisiteIds(Arrays.asList(2L, 3L));
        invalidRequest.setDependencyType(""); // 空类型应该验证失败

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}/dependencies", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(dependencyService, never()).addDependency(any(Long.class), any(List.class), any(String.class));
    }

    @Test
    @DisplayName("添加依赖时依赖类型不合法应该验证失败")
    void shouldReturnErrorWhenDependencyTypeIsInvalid() throws Exception {
        // Given - 创建一个dependencyType不合法的请求
        DependencyController.AddDependencyRequest invalidRequest = new DependencyController.AddDependencyRequest();
        invalidRequest.setPrerequisiteIds(Arrays.asList(2L, 3L));
        invalidRequest.setDependencyType("INVALID"); // 不合法的类型

        // When & Then
        mockMvc.perform(post("/test-cases/{uniqueId}/dependencies", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(dependencyService, never()).addDependency(any(Long.class), any(List.class), any(String.class));
    }

    @Test
    @DisplayName("计算执行顺序时用例ID列表为空应该验证失败")
    void shouldReturnErrorWhenTestCaseIdsIsEmpty() throws Exception {
        // Given - 创建一个testCaseIds为空的请求
        DependencyController.CalculateOrderRequest invalidRequest = new DependencyController.CalculateOrderRequest();
        invalidRequest.setTestCaseIds(Arrays.asList()); // 空列表应该验证失败

        // When & Then
        mockMvc.perform(post("/test-cases/calculate-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(dependencyService, never()).calculateExecutionOrder(any(List.class));
    }

    @Test
    @DisplayName("计算执行顺序应该返回有序的用例ID列表")
    void shouldCalculateOrderWithDependencies() throws Exception {
        // Given - 模拟有依赖关系的用例
        List<Long> orderedIds = Arrays.asList(1L, 2L, 3L);
        when(dependencyService.calculateExecutionOrder(any(List.class))).thenReturn(orderedIds);

        // When & Then
        mockMvc.perform(post("/test-cases/calculate-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value(1))
                .andExpect(jsonPath("$.data[1]").value(2))
                .andExpect(jsonPath("$.data[2]").value(3));

        verify(dependencyService, times(1)).calculateExecutionOrder(any(List.class));
    }
}
