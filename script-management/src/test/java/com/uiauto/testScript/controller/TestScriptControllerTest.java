package com.uiauto.testScript.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.common.ApiResponse;
import com.uiauto.testScript.dto.ScriptSearchRequest;
import com.uiauto.testScript.dto.TestScriptCreateRequest;
import com.uiauto.testScript.dto.TestScriptUpdateRequest;
import com.uiauto.testScript.service.TestScriptService;
import com.uiauto.testScript.vo.TestScriptResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TestScriptController 单元测试
 * 使用MockMvc测试Controller层API接口
 */
@WebMvcTest(controllers = TestScriptController.class)
@ActiveProfiles("test")
class TestScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestScriptService scriptService;

    private TestScriptResponse testResponse1;
    private TestScriptResponse testResponse2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testResponse1 = TestScriptResponse.builder()
                .uniqueId(1L)
                .scriptName("登录测试脚本")
                .scriptDescription("测试登录功能")
                .scriptContent("test('login', () => {});")
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .generationMethodDisplayName("AI生成")
                .enabled(true)
                .aiGenerationStatus("SUCCESS")
                .aiGenerationStatusDisplayName("成功")
                .aiRetryCount(0)
                .testCaseId(1L)
                .testCaseName("登录测试用例")
                .category("用户认证")
                .executionCount(5)
                .lastExecutionTime(now.minusDays(1))
                .lastExecutionResult("SUCCESS")
                .createdBy(1L)
                .updatedBy(1L)
                .createdTime(now)
                .updatedTime(now)
                .build();

        testResponse2 = TestScriptResponse.builder()
                .uniqueId(2L)
                .scriptName("注册测试脚本")
                .scriptDescription("测试注册功能")
                .scriptContent("test('register', () => {});")
                .language("typescript")
                .generationMethod("EXCEL_IMPORT")
                .generationMethodDisplayName("Excel导入")
                .enabled(false)
                .aiGenerationStatus("SUCCESS")
                .testCaseId(2L)
                .testCaseName("注册测试用例")
                .category("用户认证")
                .executionCount(0)
                .createdTime(now.minusDays(1))
                .build();
    }

    // ==================== 基础CRUD测试 ====================

    @Test
    void testCreate_Success() throws Exception {
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName("新脚本")
                .scriptDescription("新脚本描述")
                .scriptContent("console.log('test');")
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .testCaseId(1L)
                .category("测试分类")
                .build();

        when(scriptService.create(any(TestScriptCreateRequest.class))).thenReturn(100L);

        mockMvc.perform(post("/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本创建成功"))
                .andExpect(jsonPath("$.data").value(100));

        verify(scriptService).create(any(TestScriptCreateRequest.class));
    }

    @Test
    void testCreate_ValidationError() throws Exception {
        // 缺少必填字段
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName("新脚本")
                // scriptContent缺失
                .generationMethod("AI_GENERATED")
                .testCaseId(1L)
                .build();

        mockMvc.perform(post("/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(scriptService, never()).create(any(TestScriptCreateRequest.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        doNothing().when(scriptService).delete(1L);

        mockMvc.perform(post("/scripts/delete")
                        .param("uniqueId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本已删除"));

        verify(scriptService).delete(1L);
    }

    @Test
    void testGetById_Success() throws Exception {
        when(scriptService.getById(1L)).thenReturn(testResponse1);

        mockMvc.perform(get("/scripts")
                        .param("uniqueId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uniqueId").value(1))
                .andExpect(jsonPath("$.data.scriptName").value("登录测试脚本"))
                .andExpect(jsonPath("$.data.enabled").value(true));

        verify(scriptService).getById(1L);
    }

    @Test
    void testSearch_Success() throws Exception {
        ScriptSearchRequest request = new ScriptSearchRequest();
        request.setKeyword("登录");

        List<TestScriptResponse> responses = Arrays.asList(testResponse1);
        when(scriptService.search(any(ScriptSearchRequest.class))).thenReturn(responses);

        mockMvc.perform(post("/scripts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].scriptName").value("登录测试脚本"));

        verify(scriptService).search(any(ScriptSearchRequest.class));
    }

    @Test
    void testListAll_Success() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1, testResponse2);
        when(scriptService.listAll()).thenReturn(responses);

        mockMvc.perform(get("/scripts/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(scriptService).listAll();
    }

    @Test
    void testListByCategory_Success() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1, testResponse2);
        when(scriptService.listByCategory("用户认证")).thenReturn(responses);

        mockMvc.perform(get("/scripts/by-category")
                        .param("category", "用户认证"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(scriptService).listByCategory("用户认证");
    }

    @Test
    void testListByGenerationMethod_Success() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1);
        when(scriptService.listByGenerationMethod("AI_GENERATED")).thenReturn(responses);

        mockMvc.perform(get("/scripts/by-generation-method")
                        .param("generationMethod", "AI_GENERATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].generationMethod").value("AI_GENERATED"));

        verify(scriptService).listByGenerationMethod("AI_GENERATED");
    }

    // ==================== 测试用例关联查询测试 ====================

    @Test
    void testGetEnabledByTestCaseId_Success() throws Exception {
        when(scriptService.getEnabledByTestCaseId(1L)).thenReturn(testResponse1);

        mockMvc.perform(get("/scripts/enabled-by-testcase")
                        .param("testCaseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uniqueId").value(1))
                .andExpect(jsonPath("$.data.enabled").value(true));

        verify(scriptService).getEnabledByTestCaseId(1L);
    }

    @Test
    void testGetEnabledByTestCaseId_NotFound() throws Exception {
        when(scriptService.getEnabledByTestCaseId(999L)).thenReturn(null);

        mockMvc.perform(get("/scripts/enabled-by-testcase")
                        .param("testCaseId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(scriptService).getEnabledByTestCaseId(999L);
    }

    @Test
    void testGetAllByTestCaseId_Success() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1, testResponse2);
        when(scriptService.getAllByTestCaseId(1L)).thenReturn(responses);

        mockMvc.perform(get("/scripts/all-by-testcase")
                        .param("testCaseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(scriptService).getAllByTestCaseId(1L);
    }

    @Test
    void testGetScriptForExecution_Success() throws Exception {
        when(scriptService.getScriptForExecution(1L)).thenReturn(testResponse1);

        mockMvc.perform(get("/scripts/for-execution")
                        .param("testCaseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uniqueId").value(1));

        verify(scriptService).getScriptForExecution(1L);
    }

    // ==================== 启用/禁用管理测试 ====================

    @Test
    void testToggleEnabled_Enable() throws Exception {
        when(scriptService.getById(1L)).thenReturn(testResponse1);
        doNothing().when(scriptService).updateEnabledStatus(1L, false);

        mockMvc.perform(post("/scripts/toggle-enabled")
                        .param("scriptId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本已禁用"));

        verify(scriptService).getById(1L);
        verify(scriptService).updateEnabledStatus(1L, false);
    }

    @Test
    void testToggleEnabled_Disable() throws Exception {
        testResponse1.setEnabled(false);
        when(scriptService.getById(1L)).thenReturn(testResponse1);
        doNothing().when(scriptService).updateEnabledStatus(1L, true);

        mockMvc.perform(post("/scripts/toggle-enabled")
                        .param("scriptId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本已启用"));

        verify(scriptService).updateEnabledStatus(1L, true);
    }

    @Test
    void testUpdateEnabledStatus_Enable() throws Exception {
        doNothing().when(scriptService).updateEnabledStatus(1L, true);

        mockMvc.perform(post("/scripts/update-enabled")
                        .param("scriptId", "1")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本已启用"));

        verify(scriptService).updateEnabledStatus(1L, true);
    }

    @Test
    void testUpdateEnabledStatus_Disable() throws Exception {
        doNothing().when(scriptService).updateEnabledStatus(1L, false);

        mockMvc.perform(post("/scripts/update-enabled")
                        .param("scriptId", "1")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本已禁用"));

        verify(scriptService).updateEnabledStatus(1L, false);
    }

    @Test
    void testUpdateBasicInfo_Success() throws Exception {
        TestScriptUpdateRequest request = TestScriptUpdateRequest.builder()
                .uniqueId(1L)
                .scriptName("更新后的名称")
                .scriptDescription("更新后的描述")
                .category("更新后的分类")
                .build();

        doNothing().when(scriptService).updateBasicInfo(eq(1L), any(TestScriptUpdateRequest.class));

        mockMvc.perform(post("/scripts/update-basic-info")
                        .param("scriptId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("脚本基本信息更新成功"));

        verify(scriptService).updateBasicInfo(eq(1L), any(TestScriptUpdateRequest.class));
    }

    // ==================== AI生成相关测试 ====================

    @Test
    void testGenerateByAI_Success() throws Exception {
        when(scriptService.generateByAI(1L)).thenReturn(testResponse1);

        mockMvc.perform(post("/scripts/generate-by-ai")
                        .param("testCaseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("AI脚本生成完成"))
                .andExpect(jsonPath("$.data.uniqueId").value(1));

        verify(scriptService).generateByAI(1L);
    }

    @Test
    void testRetryAllFailed_Success() throws Exception {
        doNothing().when(scriptService).retryFailedGeneration();

        mockMvc.perform(post("/scripts/retry-all-failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("失败AI生成重试完成"));

        verify(scriptService).retryFailedGeneration();
    }

    @Test
    void testRetryScriptAIGeneration_Success() throws Exception {
        when(scriptService.getById(1L)).thenReturn(testResponse1);
        doNothing().when(scriptService).retryScriptAIGeneration(1L);

        mockMvc.perform(post("/scripts/retry-script")
                        .param("scriptId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("AI生成重试完成"))
                .andExpect(jsonPath("$.data.uniqueId").value(1));

        verify(scriptService).getById(1L);
        verify(scriptService).retryScriptAIGeneration(1L);
    }

    @Test
    void testListFailedAIGenerations_Success() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1);
        when(scriptService.listFailedAIGenerations()).thenReturn(responses);

        mockMvc.perform(get("/scripts/failed-ai-generations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(scriptService).listFailedAIGenerations();
    }

    @Test
    void testListFailedAIGenerations_Empty() throws Exception {
        when(scriptService.listFailedAIGenerations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/scripts/failed-ai-generations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(scriptService).listFailedAIGenerations();
    }

    // ==================== 执行相关测试 ====================

    @Test
    void testIncrementExecutionCount_Success() throws Exception {
        doNothing().when(scriptService).incrementExecutionCount(1L);

        mockMvc.perform(post("/scripts/increment-execution")
                        .param("scriptId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(scriptService).incrementExecutionCount(1L);
    }

    @Test
    void testUpdateExecutionResult_Success() throws Exception {
        doNothing().when(scriptService).updateExecutionResult(1L, "SUCCESS");

        mockMvc.perform(post("/scripts/update-result")
                        .param("scriptId", "1")
                        .param("result", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(scriptService).updateExecutionResult(1L, "SUCCESS");
    }

    // ==================== 统计信息测试 ====================

    @Test
    void testCountByTestCaseId_Success() throws Exception {
        when(scriptService.countByTestCaseId(1L)).thenReturn(5L);

        mockMvc.perform(get("/scripts/count-by-testcase")
                        .param("testCaseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(scriptService).countByTestCaseId(1L);
    }

    @Test
    void testListTopExecuted_Success() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1, testResponse2);
        when(scriptService.listTopExecuted(10)).thenReturn(responses);

        mockMvc.perform(get("/scripts/top-executed")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(scriptService).listTopExecuted(10);
    }

    @Test
    void testListTopExecuted_DefaultLimit() throws Exception {
        List<TestScriptResponse> responses = Arrays.asList(testResponse1);
        when(scriptService.listTopExecuted(10)).thenReturn(responses);

        mockMvc.perform(get("/scripts/top-executed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(scriptService).listTopExecuted(10); // 默认limit=10
    }

    // ==================== 异常处理测试 ====================

    @Test
    void testGetById_NotFound() throws Exception {
        when(scriptService.getById(999L))
                .thenThrow(new RuntimeException("脚本不存在: 999"));

        mockMvc.perform(get("/scripts")
                        .param("uniqueId", "999"))
                .andExpect(status().isInternalServerError());

        verify(scriptService).getById(999L);
    }

    @Test
    void testCreate_ServiceException() throws Exception {
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName("新脚本")
                .scriptContent("console.log('test');")
                .generationMethod("AI_GENERATED")
                .testCaseId(1L)
                .build();

        when(scriptService.create(any(TestScriptCreateRequest.class)))
                .thenThrow(new RuntimeException("测试用例不存在"));

        mockMvc.perform(post("/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(scriptService).create(any(TestScriptCreateRequest.class));
    }

    @Test
    void testMissingRequiredParameter() throws Exception {
        // 缺少必填参数
        mockMvc.perform(get("/scripts")
                        // 没有提供uniqueId参数
                        )
                .andExpect(status().isBadRequest());

        verify(scriptService, never()).getById(anyLong());
    }
}
