package com.uiauto.testScript.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestScriptEntity 单元测试
 * 测试实体的业务方法
 */
class TestScriptEntityTest {

    @Test
    void testBuilderAndDefaults() {
        // 测试Builder和默认值
        TestScriptEntity entity = TestScriptEntity.builder()
                .scriptName("测试脚本")
                .scriptContent("console.log('test');")
                .generationMethod("AI_GENERATED")
                .testCaseId(1L)
                .build();

        assertNotNull(entity);
        assertEquals("测试脚本", entity.getScriptName());
        assertEquals("console.log('test');", entity.getScriptContent());
        assertEquals("AI_GENERATED", entity.getGenerationMethod());
        assertEquals(1L, entity.getTestCaseId());

        // 验证默认值
        assertEquals("typescript", entity.getLanguage());
        assertTrue(entity.getEnabled());
        assertEquals("SUCCESS", entity.getAiGenerationStatus());
        assertEquals(0, entity.getAiRetryCount());
        assertEquals(0, entity.getExecutionCount());
    }

    @Test
    void testIsAIGenerated() {
        // 测试是否为AI生成
        TestScriptEntity aiGenerated = TestScriptEntity.builder()
                .generationMethod("AI_GENERATED")
                .build();

        TestScriptEntity excelImported = TestScriptEntity.builder()
                .generationMethod("EXCEL_IMPORT")
                .build();

        assertTrue(aiGenerated.isAIGenerated());
        assertFalse(excelImported.isAIGenerated());
    }

    @Test
    void testIsExcelImported() {
        // 测试是否为Excel导入
        TestScriptEntity excelImported = TestScriptEntity.builder()
                .generationMethod("EXCEL_IMPORT")
                .build();

        TestScriptEntity aiGenerated = TestScriptEntity.builder()
                .generationMethod("AI_GENERATED")
                .build();

        assertTrue(excelImported.isExcelImported());
        assertFalse(aiGenerated.isExcelImported());
    }

    @Test
    void testIsEnabled() {
        // 测试是否启用
        TestScriptEntity enabled = TestScriptEntity.builder()
                .enabled(true)
                .build();

        TestScriptEntity disabled = TestScriptEntity.builder()
                .enabled(false)
                .build();

        TestScriptEntity nullEnabled = TestScriptEntity.builder()
                .enabled(null)
                .build();

        assertTrue(enabled.isEnabled());
        assertFalse(disabled.isEnabled());
        assertFalse(nullEnabled.isEnabled()); // null应视为false
    }

    @Test
    void testIsAIGenerationFailed() {
        // 测试AI生成是否失败
        TestScriptEntity failed = TestScriptEntity.builder()
                .aiGenerationStatus("FAILED")
                .build();

        TestScriptEntity success = TestScriptEntity.builder()
                .aiGenerationStatus("SUCCESS")
                .build();

        TestScriptEntity pending = TestScriptEntity.builder()
                .aiGenerationStatus("PENDING")
                .build();

        assertTrue(failed.isAIGenerationFailed());
        assertFalse(success.isAIGenerationFailed());
        assertFalse(pending.isAIGenerationFailed());
    }

    @Test
    void testCanRetryAIGeneration() {
        // 测试是否可以重试AI生成
        TestScriptEntity canRetry = TestScriptEntity.builder()
                .aiGenerationStatus("FAILED")
                .aiRetryCount(2)
                .build();

        TestScriptEntity cannotRetryDueToStatus = TestScriptEntity.builder()
                .aiGenerationStatus("SUCCESS")
                .aiRetryCount(0)
                .build();

        TestScriptEntity cannotRetryDueToCount = TestScriptEntity.builder()
                .aiGenerationStatus("FAILED")
                .aiRetryCount(3)
                .build();

        TestScriptEntity canRetryAtLimit = TestScriptEntity.builder()
                .aiGenerationStatus("FAILED")
                .aiRetryCount(2)
                .build();

        assertTrue(canRetry.canRetryAIGeneration());
        assertFalse(cannotRetryDueToStatus.canRetryAIGeneration());
        assertFalse(cannotRetryDueToCount.canRetryAIGeneration());
        assertTrue(canRetryAtLimit.canRetryAIGeneration());
    }

    @Test
    void testMarkAIGenerationFailed() {
        // 测试标记AI生成失败
        TestScriptEntity entity = TestScriptEntity.builder()
                .enabled(true)
                .aiGenerationStatus("PENDING")
                .build();

        String errorMessage = "网络连接失败";
        entity.markAIGenerationFailed(errorMessage);

        assertEquals("FAILED", entity.getAiGenerationStatus());
        assertEquals(errorMessage, entity.getAiErrorMessage());
        assertNotNull(entity.getAiGenerationTime());
        assertFalse(entity.getEnabled()); // 失败后应禁用
    }

    @Test
    void testMarkAIGenerationSuccess() {
        // 测试标记AI生成成功
        TestScriptEntity entity = TestScriptEntity.builder()
                .enabled(false)
                .aiGenerationStatus("PENDING")
                .aiErrorMessage("之前的错误")
                .build();

        String content = "console.log('AI生成的脚本');";
        String modelUsed = "gpt-4";

        entity.markAIGenerationSuccess(content, modelUsed);

        assertEquals("SUCCESS", entity.getAiGenerationStatus());
        assertEquals(content, entity.getScriptContent());
        assertEquals(modelUsed, entity.getAiModelUsed());
        assertNull(entity.getAiErrorMessage()); // 成功后清除错误信息
        assertNotNull(entity.getAiGenerationTime());
        assertTrue(entity.getEnabled()); // 成功后应启用
    }

    @Test
    void testIncrementAIRetryCount() {
        // 测试增加AI重试次数
        TestScriptEntity entity = TestScriptEntity.builder()
                .aiRetryCount(0)
                .build();

        entity.incrementAIRetryCount();
        assertEquals(1, entity.getAiRetryCount());

        entity.incrementAIRetryCount();
        assertEquals(2, entity.getAiRetryCount());

        // 测试null情况
        TestScriptEntity nullEntity = TestScriptEntity.builder()
                .aiRetryCount(null)
                .build();

        nullEntity.incrementAIRetryCount();
        assertEquals(1, nullEntity.getAiRetryCount());
    }

    @Test
    void testSettersAndGetters() {
        // 测试所有setter和getter方法
        TestScriptEntity entity = new TestScriptEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setUniqueId(1L);
        entity.setScriptName("测试脚本");
        entity.setScriptDescription("测试描述");
        entity.setScriptContent("console.log('test');");
        entity.setLanguage("javascript");
        entity.setGenerationMethod("AI_GENERATED");
        entity.setEnabled(true);
        entity.setAiGenerationStatus("SUCCESS");
        entity.setAiRetryCount(0);
        entity.setAiErrorMessage(null);
        entity.setAiModelUsed("gpt-4");
        entity.setAiGenerationTime(now);
        entity.setTestCaseId(1L);
        entity.setCategory("测试分类");
        entity.setExecutionCount(5);
        entity.setLastExecutionTime(now);
        entity.setLastExecutionResult("SUCCESS");

        assertEquals(1L, entity.getUniqueId());
        assertEquals("测试脚本", entity.getScriptName());
        assertEquals("javascript", entity.getLanguage());
        assertEquals("AI_GENERATED", entity.getGenerationMethod());
        assertTrue(entity.getEnabled());
        assertEquals(5, entity.getExecutionCount());
    }
}
