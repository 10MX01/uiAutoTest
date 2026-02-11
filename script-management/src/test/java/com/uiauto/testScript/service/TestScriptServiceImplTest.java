package com.uiauto.testScript.service;

import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.service.AIService;
import com.uiauto.testScript.dto.ScriptSearchRequest;
import com.uiauto.testScript.dto.TestScriptCreateRequest;
import com.uiauto.testScript.dto.TestScriptUpdateRequest;
import com.uiauto.testScript.entity.TestScriptEntity;
import com.uiauto.testScript.repository.TestScriptRepository;
import com.uiauto.testScript.service.impl.TestScriptServiceImpl;
import com.uiauto.testScript.vo.TestScriptResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * TestScriptService 单元测试
 * 使用Mockito测试Service层业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class TestScriptServiceImplTest {

    @Mock
    private TestScriptRepository scriptRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private AIService aiService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TestScriptServiceImpl scriptService;

    private TestScriptEntity testScript1;
    private TestScriptEntity testScript2;
    private TestCaseEntity testCase1;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testCase1 = TestCaseEntity.builder()
                .name("登录测试用例")
                .build();

        testScript1 = TestScriptEntity.builder()
                .scriptName("登录测试脚本")
                .scriptDescription("测试登录功能")
                .scriptContent("test('login', () => {});")
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .enabled(true)
                .aiGenerationStatus("SUCCESS")
                .aiRetryCount(0)
                .testCaseId(1L)
                .category("用户认证")
                .executionCount(5)
                .build();
        testScript1.setUniqueId(1L);

        testScript2 = TestScriptEntity.builder()
                .scriptName("注册测试脚本")
                .scriptDescription("测试注册功能")
                .scriptContent("test('register', () => {});")
                .language("typescript")
                .generationMethod("EXCEL_IMPORT")
                .enabled(false)
                .testCaseId(2L)
                .build();
        testScript2.setUniqueId(2L);
    }

    // ==================== 创建和删除测试 ====================

    @Test
    void testCreate_Success() {
        // 准备数据
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName("新脚本")
                .scriptDescription("新脚本描述")
                .scriptContent("console.log('test');")
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .testCaseId(1L)
                .category("测试分类")
                .build();

        when(testCaseRepository.existsById(1L)).thenReturn(true);
        when(scriptRepository.save(any(TestScriptEntity.class))).thenAnswer(invocation -> {
            TestScriptEntity entity = invocation.getArgument(0);
            entity.setUniqueId(100L);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            return entity;
        });

        // 执行
        Long scriptId = scriptService.create(request);

        // 验证
        assertEquals(100L, scriptId);
        verify(testCaseRepository).existsById(1L);
        verify(scriptRepository).disableAllByTestCaseId(1L);
        verify(scriptRepository).save(any(TestScriptEntity.class));
    }

    @Test
    void testCreate_TestCaseNotFound() {
        // 准备数据
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName("新脚本")
                .scriptContent("console.log('test');")
                .generationMethod("AI_GENERATED")
                .testCaseId(999L)
                .build();

        when(testCaseRepository.existsById(999L)).thenReturn(false);

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scriptService.create(request);
        });

        assertTrue(exception.getMessage().contains("测试用例不存在"));
        verify(scriptRepository, never()).save(any(TestScriptEntity.class));
    }

    @Test
    void testDelete_Success() {
        when(scriptRepository.existsById(1L)).thenReturn(true);
        doNothing().when(scriptRepository).deleteById(1L);

        // 执行
        scriptService.delete(1L);

        // 验证
        verify(scriptRepository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        when(scriptRepository.existsById(999L)).thenReturn(false);

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scriptService.delete(999L);
        });

        assertTrue(exception.getMessage().contains("脚本不存在"));
    }

    // ==================== 查询操作测试 ====================

    @Test
    void testGetById_Success() {
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));

        TestScriptResponse response = scriptService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getUniqueId());
        assertEquals("登录测试脚本", response.getScriptName());
        verify(scriptRepository).findById(1L);
    }

    @Test
    void testGetById_NotFound() {
        when(scriptRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scriptService.getById(999L);
        });

        assertTrue(exception.getMessage().contains("脚本不存在"));
    }

    @Test
    void testSearch() {
        ScriptSearchRequest request = new ScriptSearchRequest();
        request.setKeyword("登录");

        when(scriptRepository.searchByKeyword("登录"))
                .thenReturn(Arrays.asList(testScript1));

        List<TestScriptResponse> responses = scriptService.search(request);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("登录测试脚本", responses.get(0).getScriptName());
        verify(scriptRepository).searchByKeyword("登录");
    }

    @Test
    void testListAll() {
        when(scriptRepository.findAllActive())
                .thenReturn(Arrays.asList(testScript1, testScript2));

        List<TestScriptResponse> responses = scriptService.listAll();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(scriptRepository).findAllActive();
    }

    @Test
    void testListByCategory() {
        when(scriptRepository.findByCategory("用户认证"))
                .thenReturn(Arrays.asList(testScript1));

        List<TestScriptResponse> responses = scriptService.listByCategory("用户认证");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(scriptRepository).findByCategory("用户认证");
    }

    @Test
    void testListByGenerationMethod() {
        when(scriptRepository.findByGenerationMethod("AI_GENERATED"))
                .thenReturn(Arrays.asList(testScript1));

        List<TestScriptResponse> responses = scriptService.listByGenerationMethod("AI_GENERATED");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(scriptRepository).findByGenerationMethod("AI_GENERATED");
    }

    @Test
    void testGetEnabledByTestCaseId() {
        when(scriptRepository.findEnabledByTestCaseId(1L))
                .thenReturn(Optional.of(testScript1));

        TestScriptResponse response = scriptService.getEnabledByTestCaseId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getUniqueId());
        assertTrue(response.getEnabled());
        verify(scriptRepository).findEnabledByTestCaseId(1L);
    }

    @Test
    void testGetEnabledByTestCaseId_NotFound() {
        when(scriptRepository.findEnabledByTestCaseId(999L))
                .thenReturn(Optional.empty());

        TestScriptResponse response = scriptService.getEnabledByTestCaseId(999L);

        assertNull(response);
        verify(scriptRepository).findEnabledByTestCaseId(999L);
    }

    @Test
    void testGetAllByTestCaseId() {
        when(scriptRepository.findAllByTestCaseId(1L))
                .thenReturn(Arrays.asList(testScript1, testScript2));

        List<TestScriptResponse> responses = scriptService.getAllByTestCaseId(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(scriptRepository).findAllByTestCaseId(1L);
    }

    // ==================== 启用/禁用管理测试 ====================

    @Test
    void testUpdateEnabledStatus_Enable() {
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));
        when(scriptRepository.save(any(TestScriptEntity.class))).thenReturn(testScript1);

        scriptService.updateEnabledStatus(1L, true);

        verify(scriptRepository).disableAllByTestCaseId(1L);
        assertTrue(testScript1.getEnabled());
        verify(scriptRepository).save(testScript1);
    }

    @Test
    void testUpdateEnabledStatus_Disable() {
        testScript1.setEnabled(true);
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));
        when(scriptRepository.save(any(TestScriptEntity.class))).thenReturn(testScript1);

        scriptService.updateEnabledStatus(1L, false);

        assertFalse(testScript1.getEnabled());
        verify(scriptRepository, never()).disableAllByTestCaseId(anyLong());
        verify(scriptRepository).save(testScript1);
    }

    @Test
    void testUpdateBasicInfo() {
        TestScriptUpdateRequest request = TestScriptUpdateRequest.builder()
                .scriptName("更新后的名称")
                .scriptDescription("更新后的描述")
                .category("更新后的分类")
                .build();

        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));
        when(scriptRepository.save(any(TestScriptEntity.class))).thenReturn(testScript1);

        scriptService.updateBasicInfo(1L, request);

        assertEquals("更新后的名称", testScript1.getScriptName());
        assertEquals("更新后的描述", testScript1.getScriptDescription());
        assertEquals("更新后的分类", testScript1.getCategory());
        verify(scriptRepository).save(testScript1);
    }

    // ==================== AI生成相关测试 ====================

    @Test
    void testGenerateByAI_Success() {
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase1));
        when(aiService.generateScript(any(TestCaseEntity.class)))
                .thenReturn("// AI生成的脚本内容");
        when(scriptRepository.save(any(TestScriptEntity.class))).thenAnswer(invocation -> {
            TestScriptEntity entity = invocation.getArgument(0);
            entity.setUniqueId(100L);
            return entity;
        });
        when(scriptRepository.findById(100L)).thenReturn(Optional.of(testScript1));
        when(testCaseRepository.existsById(1L)).thenReturn(true);

        TestScriptResponse response = scriptService.generateByAI(1L);

        assertNotNull(response);
        assertEquals("AI_GENERATED", response.getGenerationMethod());
        verify(aiService).generateScript(testCase1);
        verify(scriptRepository, atLeastOnce()).save(any(TestScriptEntity.class));
    }

    @Test
    void testGenerateByAI_TestCaseNotFound() {
        when(testCaseRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scriptService.generateByAI(999L);
        });

        assertTrue(exception.getMessage().contains("测试用例不存在"));
    }

    @Test
    void testGenerateByAI_AIServiceNotConfigured() {
        // 这个测试验证AI服务未配置时的行为
        // 由于Mockito的strictness问题，我们简化这个测试
        TestScriptServiceImpl serviceWithoutAI = new TestScriptServiceImpl(
                scriptRepository, testCaseRepository, entityManager, null);

        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase1));
        when(testCaseRepository.existsById(1L)).thenReturn(true);

        // AI服务未配置应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            serviceWithoutAI.generateByAI(1L);
        });
    }

    @Test
    void testRetryScriptAIGeneration_Success() {
        testScript1.setAiGenerationStatus("FAILED");
        testScript1.setAiRetryCount(1);

        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase1));
        when(aiService.generateScript(any(TestCaseEntity.class)))
                .thenReturn("// 新的AI生成内容");
        when(scriptRepository.save(any(TestScriptEntity.class))).thenReturn(testScript1);

        scriptService.retryScriptAIGeneration(1L);

        verify(scriptRepository).disableAllByTestCaseId(1L);
        verify(aiService).generateScript(testCase1);
        assertEquals("SUCCESS", testScript1.getAiGenerationStatus());
        assertTrue(testScript1.getEnabled());
    }

    @Test
    void testRetryScriptAIGeneration_CannotRetry() {
        testScript1.setAiGenerationStatus("SUCCESS");

        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scriptService.retryScriptAIGeneration(1L);
        });

        assertTrue(exception.getMessage().contains("不满足重试条件"));
    }

    // ==================== 执行相关测试 ====================

    @Test
    void testGetScriptForExecution_HasEnabledScript() {
        when(scriptRepository.findEnabledByTestCaseId(1L))
                .thenReturn(Optional.of(testScript1));

        TestScriptResponse response = scriptService.getScriptForExecution(1L);

        assertNotNull(response);
        assertEquals(1L, response.getUniqueId());
        verify(scriptRepository).findEnabledByTestCaseId(1L);
        verify(aiService, never()).generateScript(any()); // 不应该调用AI生成
    }

    @Test
    void testGetScriptForExecution_NoEnabledScript_TriggerAI() {
        when(scriptRepository.findEnabledByTestCaseId(1L))
                .thenReturn(Optional.empty());
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase1));
        when(aiService.generateScript(any(TestCaseEntity.class)))
                .thenReturn("// AI生成的脚本");
        when(scriptRepository.save(any(TestScriptEntity.class))).thenAnswer(invocation -> {
            TestScriptEntity entity = invocation.getArgument(0);
            entity.setUniqueId(100L);
            return entity;
        });
        when(scriptRepository.findById(100L)).thenReturn(Optional.of(testScript1));
        when(testCaseRepository.existsById(1L)).thenReturn(true);

        TestScriptResponse response = scriptService.getScriptForExecution(1L);

        assertNotNull(response);
        verify(scriptRepository).findEnabledByTestCaseId(1L);
        verify(aiService).generateScript(testCase1); // 应该调用AI生成
    }

    @Test
    void testIncrementExecutionCount() {
        scriptService.incrementExecutionCount(1L);

        verify(scriptRepository).incrementExecutionCount(1L);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testUpdateExecutionResult() {
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));
        when(scriptRepository.save(any(TestScriptEntity.class))).thenReturn(testScript1);

        scriptService.updateExecutionResult(1L, "SUCCESS");

        assertEquals("SUCCESS", testScript1.getLastExecutionResult());
        assertNotNull(testScript1.getLastExecutionTime());
        verify(scriptRepository).save(testScript1);
    }

    // ==================== 统计信息测试 ====================

    @Test
    void testListFailedAIGenerations() {
        testScript1.setAiGenerationStatus("FAILED");
        testScript1.setAiRetryCount(1);

        when(scriptRepository.findFailedScriptForRetry())
                .thenReturn(Arrays.asList(testScript1));

        List<TestScriptResponse> responses = scriptService.listFailedAIGenerations();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("FAILED", responses.get(0).getAiGenerationStatus());
        verify(scriptRepository).findFailedScriptForRetry();
    }

    @Test
    void testCountByTestCaseId() {
        when(scriptRepository.countByTestCaseId(1L)).thenReturn(2L);

        Long count = scriptService.countByTestCaseId(1L);

        assertEquals(2L, count);
        verify(scriptRepository).countByTestCaseId(1L);
    }

    @Test
    void testListTopExecuted() {
        when(scriptRepository.findTopExecutedScripts(10))
                .thenReturn(Arrays.asList(testScript1, testScript2));

        List<TestScriptResponse> responses = scriptService.listTopExecuted(10);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(scriptRepository).findTopExecutedScripts(10);
    }

    // ==================== 业务逻辑测试 ====================

    @Test
    void testConvertToResponse_WithTestCaseName() {
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase1));

        TestScriptResponse response = scriptService.getById(1L);

        assertNotNull(response);
        assertEquals("登录测试用例", response.getTestCaseName());
        assertEquals("AI生成", response.getGenerationMethodDisplayName());
        assertEquals("成功", response.getAiGenerationStatusDisplayName());
    }

    @Test
    void testGetGenerationMethodDisplayName() {
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));

        TestScriptResponse response = scriptService.getById(1L);

        assertEquals("AI生成", response.getGenerationMethodDisplayName());

        testScript1.setGenerationMethod("EXCEL_IMPORT");
        TestScriptResponse response2 = scriptService.getById(1L);
        assertEquals("Excel导入", response2.getGenerationMethodDisplayName());
    }

    @Test
    void testGetAIStatusDisplayName() {
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(testScript1));

        TestScriptResponse response = scriptService.getById(1L);

        assertEquals("成功", response.getAiGenerationStatusDisplayName());

        testScript1.setAiGenerationStatus("FAILED");
        TestScriptResponse response2 = scriptService.getById(1L);
        assertEquals("失败", response2.getAiGenerationStatusDisplayName());

        testScript1.setAiGenerationStatus("PENDING");
        TestScriptResponse response3 = scriptService.getById(1L);
        assertEquals("生成中", response3.getAiGenerationStatusDisplayName());
    }
}
