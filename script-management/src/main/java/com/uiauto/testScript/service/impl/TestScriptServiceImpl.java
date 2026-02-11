package com.uiauto.testScript.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.service.AIService;
import com.uiauto.testScript.dto.ScriptSearchRequest;
import com.uiauto.testScript.dto.TestScriptCreateRequest;
import com.uiauto.testScript.dto.TestScriptUpdateRequest;
import com.uiauto.testScript.entity.TestScriptEntity;
import com.uiauto.testScript.repository.TestScriptRepository;
import com.uiauto.testScript.service.TestScriptService;
import com.uiauto.testScript.vo.TestScriptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试脚本Service实现（重构版）
 *
 * 核心变更：
 * 1. 移除版本管理逻辑
 * 2. 添加启用/禁用状态管理
 * 3. 添加AI生成和重试功能
 * 4. 添加运行测试用例时自动获取脚本的功能
 */
@Slf4j
@Service
public class TestScriptServiceImpl implements TestScriptService {

    private final TestScriptRepository scriptRepository;
    private final TestCaseRepository testCaseRepository;
    private final AIService aiService; // AI服务（可选）
    private final EntityManager entityManager;

    // 构造函数注入，AIService 为可选
    public TestScriptServiceImpl(TestScriptRepository scriptRepository,
                                 TestCaseRepository testCaseRepository,
                                 EntityManager entityManager,
                                 @org.springframework.beans.factory.annotation.Autowired(required = false) AIService aiService) {
        this.scriptRepository = scriptRepository;
        this.testCaseRepository = testCaseRepository;
        this.entityManager = entityManager;
        this.aiService = aiService; // 可能为null
    }

    // ==================== 创建和删除 ====================

    @Override
    @Transactional
    public Long create(TestScriptCreateRequest request) {
        log.info("创建脚本: {} for 测试用例: {}", request.getScriptName(), request.getTestCaseId());

        // 1. 校验测试用例是否存在
        if (!testCaseRepository.existsById(request.getTestCaseId())) {
            throw new RuntimeException("测试用例不存在: " + request.getTestCaseId());
        }

        // 2. 禁用该测试用例的其他所有脚本（新脚本默认启用）
        scriptRepository.disableAllByTestCaseId(request.getTestCaseId());

        // 3. 构建脚本实体
        TestScriptEntity entity = TestScriptEntity.builder()
                .scriptName(request.getScriptName())
                .scriptDescription(request.getScriptDescription())
                .scriptContent(request.getScriptContent())
                .language(request.getLanguage())
                .generationMethod(request.getGenerationMethod())
                .enabled(true) // 新创建的脚本默认启用
                .aiGenerationStatus(request.getAiGenerationStatus() != null
                        ? request.getAiGenerationStatus() : "SUCCESS")
                .aiRetryCount(0)
                .aiErrorMessage(request.getAiErrorMessage())
                .aiModelUsed(request.getAiModelUsed())
                .aiGenerationTime(LocalDateTime.now())
                .testCaseId(request.getTestCaseId())
                .category(request.getCategory())
                .executionCount(0)
                .build();

        // 4. 设置审计字段
        entity.setCreatedBy(getCurrentUserId());
        entity.setUpdatedBy(getCurrentUserId());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        // 5. 保存
        TestScriptEntity saved = scriptRepository.save(entity);

        log.info("脚本创建成功, ID: {}, 生成方式: {}", saved.getUniqueId(), request.getGenerationMethod());
        return saved.getUniqueId();
    }

    @Override
    @Transactional
    public void delete(Long uniqueId) {
        log.info("删除脚本: {}", uniqueId);

        if (!scriptRepository.existsById(uniqueId)) {
            throw new RuntimeException("脚本不存在: " + uniqueId);
        }

        scriptRepository.deleteById(uniqueId);
        log.info("脚本已删除, ID: {}", uniqueId);
    }

    // ==================== 查询操作 ====================

    @Override
    public TestScriptResponse getById(Long uniqueId) {
        TestScriptEntity entity = scriptRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + uniqueId));

        return convertToResponse(entity);
    }

    @Override
    public List<TestScriptResponse> search(ScriptSearchRequest request) {
        log.info("搜索脚本, 关键词: {}", request.getKeyword());

        List<TestScriptEntity> entities = scriptRepository.searchByKeyword(request.getKeyword());
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestScriptResponse> listAll() {
        List<TestScriptEntity> entities = scriptRepository.findAllActive();
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestScriptResponse> listByCategory(String category) {
        List<TestScriptEntity> entities = scriptRepository.findByCategory(category);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestScriptResponse> listByGenerationMethod(String generationMethod) {
        List<TestScriptEntity> entities = scriptRepository.findByGenerationMethod(generationMethod);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TestScriptResponse getEnabledByTestCaseId(Long testCaseId) {
        Optional<TestScriptEntity> entity = scriptRepository.findEnabledByTestCaseId(testCaseId);

        if (entity.isPresent()) {
            return convertToResponse(entity.get());
        }

        return null;
    }

    @Override
    public List<TestScriptResponse> getAllByTestCaseId(Long testCaseId) {
        List<TestScriptEntity> entities = scriptRepository.findAllByTestCaseId(testCaseId);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==================== 启用/禁用管理 ====================

    @Override
    @Transactional
    public void updateEnabledStatus(Long scriptId, Boolean enabled) {
        log.info("更新脚本启用状态: {} -> {}", scriptId, enabled);

        TestScriptEntity entity = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + scriptId));

        if (enabled) {
            // 启用该脚本时，先禁用同一测试用例的其他所有脚本
            scriptRepository.disableAllByTestCaseId(entity.getTestCaseId());
        }

        entity.setEnabled(enabled);
        entity.setUpdatedBy(getCurrentUserId());
        entity.setUpdatedTime(LocalDateTime.now());

        scriptRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        log.info("脚本启用状态更新成功, ID: {}, enabled: {}", scriptId, enabled);
    }

    @Override
    @Transactional
    public void updateBasicInfo(Long scriptId, TestScriptUpdateRequest request) {
        log.info("更新脚本基本信息: {}", scriptId);

        TestScriptEntity entity = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + scriptId));

        // 仅允许修改基本信息，不允许修改内容
        if (request.getScriptName() != null) {
            entity.setScriptName(request.getScriptName());
        }
        if (request.getScriptDescription() != null) {
            entity.setScriptDescription(request.getScriptDescription());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }

        entity.setUpdatedBy(getCurrentUserId());
        entity.setUpdatedTime(LocalDateTime.now());

        scriptRepository.save(entity);
        log.info("脚本基本信息更新成功, ID: {}", scriptId);
    }

    // ==================== AI生成相关 ====================

    @Override
    @Transactional
    public TestScriptResponse generateByAI(Long testCaseId) {
        log.info("AI生成脚本 for 测试用例: {}", testCaseId);

        // 1. 查询测试用例信息
        TestCaseEntity testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + testCaseId));

        // 2. 调用AI服务生成脚本
        String generatedScript;
        String errorMessage = null;
        String status = "SUCCESS";
        String modelUsed = "gpt-4"; // 默认模型

        try {
            if (aiService == null) {
                throw new RuntimeException("AI服务未配置");
            }
            generatedScript = aiService.generateScript(testCase);
            log.info("AI脚本生成成功, 测试用例: {}", testCaseId);
        } catch (Exception e) {
            log.error("AI脚本生成失败, 测试用例: {}", testCaseId, e);
            generatedScript = "// AI生成失败，请手动编写或重试\n" +
                           "// 测试用例: " + testCase.getName() + "\n" +
                           "// 错误信息: " + e.getMessage();
            errorMessage = e.getMessage();
            status = "FAILED";
        }

        // 3. 创建脚本记录
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName(testCase.getName() + " - AI生成脚本")
                .scriptDescription("由AI自动生成 for " + testCase.getName())
                .scriptContent(generatedScript)
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .testCaseId(testCaseId)
                .category("自动化测试") // 使用默认分类
                .aiGenerationStatus(status)
                .aiErrorMessage(errorMessage)
                .aiModelUsed(modelUsed)
                .build();

        Long scriptId = create(request);

        // 4. 如果失败，记录日志
        if ("FAILED".equals(status)) {
            log.warn("AI生成失败，已记录，可后续重试, 脚本ID: {}", scriptId);
        }

        return getById(scriptId);
    }

    @Override
    @Transactional
    public void retryFailedGeneration() {
        log.info("开始重试失败的AI生成");

        List<TestScriptEntity> failedScripts = scriptRepository.findFailedScriptForRetry();
        log.info("找到 {} 个可重试的失败脚本", failedScripts.size());

        for (TestScriptEntity script : failedScripts) {
            try {
                // 重新调用AI生成
                TestCaseEntity testCase = testCaseRepository.findById(script.getTestCaseId())
                        .orElse(null);

                if (testCase == null) {
                    log.warn("测试用例不存在，跳过重试, 脚本ID: {}", script.getUniqueId());
                    continue;
                }

                log.info("重试AI生成, 脚本ID: {}, 测试用例: {}", script.getUniqueId(), testCase.getName());

                String newContent = aiService.generateScript(testCase);
                Long currentScriptId = script.getUniqueId();

                // 先禁用其他脚本（包括当前脚本）
                scriptRepository.disableAllByTestCaseId(script.getTestCaseId());
                entityManager.flush();
                entityManager.clear();

                // 重新加载脚本并标记为成功（会自动设置enabled=true）
                TestScriptEntity reloadedScript = scriptRepository.findById(currentScriptId).orElseThrow(
                        () -> new RuntimeException("脚本不存在: " + currentScriptId)
                );
                reloadedScript.markAIGenerationSuccess(newContent, reloadedScript.getAiModelUsed());
                reloadedScript.setUpdatedBy(getCurrentUserId());
                reloadedScript.setUpdatedTime(LocalDateTime.now());

                scriptRepository.save(reloadedScript);
                entityManager.flush();
                entityManager.clear();

                log.info("重试成功: 脚本ID {}", currentScriptId);

            } catch (Exception e) {
                log.error("重试失败: 脚本ID {}", script.getUniqueId(), e);

                // 增加重试次数
                script.incrementAIRetryCount();
                script.setAiErrorMessage(e.getMessage());
                script.setUpdatedTime(LocalDateTime.now());

                scriptRepository.save(script);

                if (script.getAiRetryCount() >= 3) {
                    log.error("重试次数已达上限，放弃重试: 脚本ID {}", script.getUniqueId());
                }
            }
        }

        log.info("AI生成重试完成");
    }

    @Override
    @Transactional
    public void retryScriptAIGeneration(Long scriptId) {
        log.info("手动重试脚本AI生成: {}", scriptId);

        TestScriptEntity script = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + scriptId));

        if (!script.canRetryAIGeneration()) {
            throw new RuntimeException("脚本不满足重试条件: " + scriptId);
        }

        TestCaseEntity testCase = testCaseRepository.findById(script.getTestCaseId())
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + script.getTestCaseId()));

        try {
            String newContent = aiService.generateScript(testCase);

            // 先禁用其他脚本（包括当前脚本）
            scriptRepository.disableAllByTestCaseId(script.getTestCaseId());
            entityManager.flush();
            entityManager.clear();

            // 重新加载脚本并标记为成功（会自动设置enabled=true）
            TestScriptEntity reloadedScript = scriptRepository.findById(scriptId).orElseThrow(
                    () -> new RuntimeException("脚本不存在: " + scriptId)
            );
            reloadedScript.markAIGenerationSuccess(newContent, reloadedScript.getAiModelUsed());
            reloadedScript.setUpdatedBy(getCurrentUserId());
            reloadedScript.setUpdatedTime(LocalDateTime.now());

            scriptRepository.save(reloadedScript);
            entityManager.flush();
            entityManager.clear();

            log.info("手动重试成功: 脚本ID {}", scriptId);

        } catch (Exception e) {
            log.error("手动重试失败: 脚本ID {}", scriptId, e);

            script.incrementAIRetryCount();
            script.setAiErrorMessage(e.getMessage());
            script.setUpdatedTime(LocalDateTime.now());

            scriptRepository.save(script);

            throw new RuntimeException("AI生成失败: " + e.getMessage(), e);
        }
    }

    // ==================== 执行相关 ====================

    @Override
    public TestScriptResponse getScriptForExecution(Long testCaseId) {
        log.info("获取测试用例的执行脚本: {}", testCaseId);

        // 1. 查找启用状态的脚本
        Optional<TestScriptEntity> enabledScript =
                scriptRepository.findEnabledByTestCaseId(testCaseId);

        if (enabledScript.isPresent()) {
            log.info("找到启用的脚本: {}", enabledScript.get().getUniqueId());
            return convertToResponse(enabledScript.get());
        }

        // 2. 没有启用脚本，触发AI生成
        log.info("没有启用脚本，触发AI生成, 测试用例: {}", testCaseId);
        return generateByAI(testCaseId);
    }

    @Override
    @Transactional
    public void incrementExecutionCount(Long scriptId) {
        scriptRepository.incrementExecutionCount(scriptId);
        entityManager.flush();
        entityManager.clear();
        log.debug("增加脚本执行次数: {}", scriptId);
    }

    @Override
    @Transactional
    public void updateExecutionResult(Long scriptId, String result) {
        TestScriptEntity entity = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + scriptId));

        entity.setLastExecutionResult(result);
        entity.setLastExecutionTime(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUserId());
        entity.setUpdatedTime(LocalDateTime.now());

        scriptRepository.save(entity);
        log.info("更新脚本执行结果: {}, 结果: {}", scriptId, result);
    }

    // ==================== 统计信息 ====================

    @Override
    public List<TestScriptResponse> listFailedAIGenerations() {
        List<TestScriptEntity> entities = scriptRepository.findFailedScriptForRetry();
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByTestCaseId(Long testCaseId) {
        return scriptRepository.countByTestCaseId(testCaseId);
    }

    @Override
    public List<TestScriptResponse> listTopExecuted(int limit) {
        List<TestScriptEntity> entities = scriptRepository.findTopExecutedScripts(limit);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为响应VO
     */
    private TestScriptResponse convertToResponse(TestScriptEntity entity) {
        // 查询关联的测试用例名称
        String testCaseName = null;
        if (entity.getTestCaseId() != null) {
            TestCaseEntity testCase = testCaseRepository.findById(entity.getTestCaseId()).orElse(null);
            if (testCase != null) {
                testCaseName = testCase.getName();
            }
        }

        return TestScriptResponse.builder()
                .uniqueId(entity.getUniqueId())
                .scriptName(entity.getScriptName())
                .scriptDescription(entity.getScriptDescription())
                .scriptContent(entity.getScriptContent())
                .language(entity.getLanguage())
                .generationMethod(entity.getGenerationMethod())
                .generationMethodDisplayName(getGenerationMethodDisplayName(entity.getGenerationMethod()))
                .enabled(entity.getEnabled())
                .aiGenerationStatus(entity.getAiGenerationStatus())
                .aiGenerationStatusDisplayName(getAIStatusDisplayName(entity.getAiGenerationStatus()))
                .aiRetryCount(entity.getAiRetryCount())
                .aiErrorMessage(entity.getAiErrorMessage())
                .aiModelUsed(entity.getAiModelUsed())
                .aiGenerationTime(entity.getAiGenerationTime())
                .canRetryAIGeneration(entity.canRetryAIGeneration())
                .testCaseId(entity.getTestCaseId())
                .testCaseName(testCaseName)
                .category(entity.getCategory())
                .executionCount(entity.getExecutionCount())
                .lastExecutionTime(entity.getLastExecutionTime())
                .lastExecutionResult(entity.getLastExecutionResult())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdTime(entity.getCreatedTime())
                .updatedTime(entity.getUpdatedTime())
                .build();
    }

    /**
     * 获取生成方式显示名称
     */
    private String getGenerationMethodDisplayName(String method) {
        if (method == null) return "未知";
        switch (method) {
            case "EXCEL_IMPORT":
                return "Excel导入";
            case "AI_GENERATED":
                return "AI生成";
            default:
                return method;
        }
    }

    /**
     * 获取AI状态显示名称
     */
    private String getAIStatusDisplayName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "SUCCESS":
                return "成功";
            case "FAILED":
                return "失败";
            case "PENDING":
                return "生成中";
            default:
                return status;
        }
    }

    /**
     * 获取当前用户ID
     * TODO: 从Spring Security上下文获取
     */
    private Long getCurrentUserId() {
        return 0L; // 暂时返回系统用户ID
    }
}
