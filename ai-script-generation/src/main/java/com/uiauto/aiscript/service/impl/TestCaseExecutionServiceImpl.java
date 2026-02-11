package com.uiauto.aiscript.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uiauto.testcase.entity.TestCaseExecutionEntity;
import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.model.TestStepWithSelectors;
import com.uiauto.testcase.repository.TestCaseExecutionRepository;
import com.uiauto.aiscript.service.PageSnapshotService;
import com.uiauto.aiscript.service.PlaywrightExecutor;
import com.uiauto.aiscript.service.TestCaseExecutionService;
import com.uiauto.aiscript.service.TwoPhaseScriptGenerationService;
import com.uiauto.testcase.entity.ProjectEntity;
import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.repository.ProjectRepository;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.testScript.dto.TestScriptCreateRequest;
import com.uiauto.testScript.service.TestScriptService;
import com.uiauto.testScript.vo.TestScriptResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 测试用例执行Service实现
 */
@Slf4j
@Service
public class TestCaseExecutionServiceImpl implements TestCaseExecutionService {

    @Autowired
    private TestCaseExecutionRepository executionRepository;

    @Autowired
    private PageSnapshotService pageSnapshotService;

    @Autowired
    private TwoPhaseScriptGenerationService twoPhaseService;

    @Autowired
    private PlaywrightExecutor playwrightExecutor;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestScriptService testScriptService;

    @Autowired
    private com.uiauto.aiscript.service.AIServiceConfigService aiServiceConfigService;

    private final Gson gson = new Gson();

    @Override
    @Transactional
    public TestCaseExecutionEntity executeTestCase(Long testCaseId, String overrideUrl, Long executedBy) {
        long startTime = System.currentTimeMillis();

        // 创建执行会话
        PlaywrightExecutor.ExecutionConfig config =
                new PlaywrightExecutor.ExecutionConfig(true, true, 30000, true);
        String sessionId = playwrightExecutor.createSession(config);

        String executionUrl = overrideUrl; // 保存URL用于错误记录

        try {
            // 递归执行（包含前置依赖）
            ExecutionResult result = executeTestCaseRecursive(
                    testCaseId, overrideUrl, executedBy, sessionId, new HashSet<>(), null);

            // 关闭会话
            playwrightExecutor.closeSession(sessionId);

            // 更新executionUrl为实际使用的URL
            if (result.executionUrl != null) {
                executionUrl = result.executionUrl;
            }

            // 保存执行记录
            TestCaseExecutionEntity execution = new TestCaseExecutionEntity();
            execution.setTestCaseId(testCaseId);
            execution.setExecutionUrl(executionUrl);
            execution.setStatus(result.status);
            execution.setDuration(result.duration);
            execution.setGeneratedScript(result.generatedScript);
            execution.setStepsResult(result.stepsResult);
            execution.setScreenshots(result.screenshots);
            execution.setErrorMessage(result.errorMessage);
            execution.setExecutedBy(executedBy);
            execution.setCreatedTime(new Date());
            execution.setUpdatedTime(new Date());

            TestCaseExecutionEntity saved = executionRepository.save(execution);

            log.info("测试用例执行完成: ID={}, 状态: {}, 耗时: {}ms",
                    testCaseId, result.status, result.duration);

            return saved;

        } catch (Exception e) {
            log.error("执行测试用例失败: {}", testCaseId, e);

            // 确保关闭会话
            try {
                playwrightExecutor.closeSession(sessionId);
            } catch (Exception ex) {
                log.warn("关闭会话失败", ex);
            }

            // 保存失败记录
            TestCaseExecutionEntity execution = new TestCaseExecutionEntity();
            execution.setTestCaseId(testCaseId);
            execution.setExecutionUrl(executionUrl != null ? executionUrl : "unknown");
            execution.setStatus("FAILED");
            execution.setDuration(System.currentTimeMillis() - startTime);
            execution.setErrorMessage(e.getMessage());
            execution.setExecutedBy(executedBy);
            execution.setCreatedTime(new Date());
            execution.setUpdatedTime(new Date());

            return executionRepository.save(execution);
        }
    }

    /**
     * 递归执行测试用例（包含前置依赖）
     *
     * @param testCaseId 测试用例ID
     * @param overrideUrl 覆盖URL
     * @param executedBy 执行人
     * @param sessionId 浏览器会话ID
     * @param executingStack 当前执行栈（防止循环依赖）
     * @param initialUrl 初始URL（第一次执行时使用）
     * @return 执行结果
     */
    private ExecutionResult executeTestCaseRecursive(
            Long testCaseId,
            String overrideUrl,
            Long executedBy,
            String sessionId,
            Set<Long> executingStack,
            String initialUrl) {

        // 判断是否是第一个执行的用例
        // initialUrl 为 null 表示这是最外层调用，需要导航到初始URL
        boolean isFirstTestCase = (initialUrl == null);

        // 检查循环依赖
        if (executingStack.contains(testCaseId)) {
            throw new RuntimeException("检测到循环依赖: " + testCaseId);
        }
        executingStack.add(testCaseId);

        try {
            // 1. 获取测试用例
            TestCaseEntity testCase = testCaseRepository.findById(testCaseId)
                    .orElseThrow(() -> new RuntimeException("测试用例不存在: " + testCaseId));

            // 2. 获取项目信息
            ProjectEntity project = null;
            if (testCase.getProjectId() != null) {
                project = projectRepository.findById(testCase.getProjectId()).orElse(null);
            }

            // 3. 确定初始执行URL
            String url = initialUrl;
            if (url == null) {
                if (overrideUrl != null && !overrideUrl.isEmpty()) {
                    url = overrideUrl;
                } else if (project != null && project.getTargetUrl() != null) {
                    url = project.getTargetUrl();
                } else {
                    throw new RuntimeException("无法确定执行URL：请配置项目的目标URL或在请求中提供overrideUrl");
                }
            }

            // 4. 执行前置依赖
            for (com.uiauto.testcase.entity.TestCaseDependencyEntity dependency : testCase.getDependencies()) {
                if (executingStack.contains(dependency.getPrerequisiteId())) {
                    continue; // 已在执行栈中，跳过
                }

                try {
                    executeTestCaseRecursive(
                            dependency.getPrerequisiteId(),
                            overrideUrl,
                            executedBy,
                            sessionId,
                            executingStack,
                            url
                    );
                } catch (Exception e) {
                    // 根据依赖类型决定是否继续
                    if ("HARD".equals(dependency.getDependencyType())) {
                        throw new RuntimeException(
                                String.format("强依赖前置用例[%d]执行失败: %s",
                                        dependency.getPrerequisiteId(), e.getMessage()), e);
                    } else {
                        log.warn("弱依赖前置用例[{}]执行失败，继续执行: {}",
                                dependency.getPrerequisiteId(), e.getMessage());
                    }
                }
            }

            // 5. 检查是否有已启用的JSON脚本
            TestScriptResponse existingScript = testScriptService.getEnabledByTestCaseId(testCaseId);

            List<TestStepWithSelectors> scriptWithSelectors;
            String scriptContent;

            if (existingScript != null && "json".equalsIgnoreCase(existingScript.getLanguage())) {
                // 复用已有脚本
                log.info("找到已启用的JSON脚本，复用: {}", existingScript.getUniqueId());
                Type listType = new TypeToken<List<TestStepWithSelectors>>() {}.getType();
                scriptWithSelectors = gson.fromJson(existingScript.getScriptContent(), listType);
                scriptContent = existingScript.getScriptContent();
            } else {
                // 生成新脚本
                log.info("未找到已启用的脚本，开始生成...");

                // 5.1 检查测试步骤
                if (testCase.getStepsJson() == null || testCase.getStepsJson().isEmpty()) {
                    throw new RuntimeException("测试用例缺少步骤数据，请先通过AI解析生成步骤");
                }

                // 5.2 解析测试步骤
                Type listType = new TypeToken<List<TestStep>>() {}.getType();
                List<TestStep> steps = gson.fromJson(testCase.getStepsJson(), listType);

                // 5.3 先导航到URL（仅当这是第一个执行的用例时导航）
                // 依赖用例会复用前置用例执行后的页面状态，不需要重新导航
                if (isFirstTestCase) {
                    log.info("这是第一个执行的用例，导航到初始URL: {}", url);
                    try {
                        playwrightExecutor.navigate(sessionId, url);
                    } catch (Exception e) {
                        log.error("导航失败，但继续尝试获取快照: {}", e.getMessage());
                    }
                } else {
                    log.info("这是依赖用例，跳过初始导航，保持当前页面状态");
                }

                // 5.4 获取当前页面快照（包含元素信息）
                PageSnapshot snapshot = playwrightExecutor.capturePageSnapshotWithElements(sessionId);
                log.info("获取页面快照成功: URL={}, 元素数量={}", snapshot.getUrl(), snapshot.getElements().size());

                // 5.5 生成包含选择器的脚本
                try {
                    scriptWithSelectors = twoPhaseService.generateScriptFromJsonAndSnapshot(steps, snapshot);
                    scriptContent = gson.toJson(scriptWithSelectors);

                    // 5.6 保存脚本到test_scripts表
                    saveGeneratedScript(testCase, scriptWithSelectors, executedBy);
                } catch (Exception e) {
                    throw new RuntimeException("生成脚本失败: " + e.getMessage(), e);
                }
            }

            // 6. 执行脚本
            log.info("开始执行测试用例: {}", testCase.getName());
            PlaywrightExecutor.ExecutionResult result =
                    playwrightExecutor.executeInSession(sessionId, url, scriptWithSelectors);

            // 7. 更新脚本执行统计
            if (existingScript != null) {
                testScriptService.updateExecutionResult(
                        existingScript.getUniqueId(),
                        result.isSuccess() ? "SUCCESS" : "FAILED"
                );
            }

            // 8. 返回结果
            ExecutionResult executionResult = new ExecutionResult();
            executionResult.status = result.getStatus();
            executionResult.duration = result.getDuration();
            executionResult.executionUrl = url;
            executionResult.generatedScript = scriptContent;
            executionResult.stepsResult = gson.toJson(result.getStepResults());
            executionResult.screenshots = gson.toJson(result.getScreenshots());
            executionResult.errorMessage = result.getErrorMessage();

            return executionResult;

        } finally {
            executingStack.remove(testCaseId);
        }
    }

    /**
     * 保存生成的脚本到test_scripts表
     */
    private void saveGeneratedScript(TestCaseEntity testCase, List<TestStepWithSelectors> script, Long executedBy) {
        try {
            // 获取当前使用的AI模型
            String aiModel = "unknown";
            try {
                com.uiauto.aiscript.entity.AIServiceConfigEntity aiConfig =
                        aiServiceConfigService.getDefaultConfig();
                if (aiConfig != null) {
                    aiModel = aiConfig.getModelName();
                }
            } catch (Exception e) {
                log.warn("获取AI模型信息失败，使用默认值", e);
            }

            // 构建脚本创建请求
            String scriptContent = gson.toJson(script);
            TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                    .scriptName(testCase.getName() + " - 自动生成脚本")
                    .scriptDescription(String.format("基于测试用例[%s]自动生成的测试脚本", testCase.getName()))
                    .scriptContent(scriptContent)
                    .language("json")
                    .generationMethod("AI_GENERATED")
                    .testCaseId(testCase.getUniqueId())
                    .category("自动生成")
                    .aiGenerationStatus("SUCCESS")
                    .aiModelUsed(aiModel)
                    .build();

            // 调用script-management服务保存脚本
            Long scriptId = testScriptService.create(request);

            log.info("成功保存生成的脚本到test_scripts表: scriptId={}, testCaseId={}, stepsCount={}",
                    scriptId, testCase.getUniqueId(), script.size());

        } catch (Exception e) {
            log.error("保存脚本失败: testCaseId=" + testCase.getUniqueId(), e);
            // 不抛出异常，允许执行继续
        }
    }

    /**
     * 执行结果内部类
     */
    private static class ExecutionResult {
        String status;
        long duration;
        String executionUrl;
        String generatedScript;
        String stepsResult;
        String screenshots;
        String errorMessage;
    }

    @Override
    public TestCaseExecutionEntity getExecutionById(Long executionId) {
        return executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("执行记录不存在: " + executionId));
    }

    @Override
    public List<TestCaseExecutionEntity> getExecutionsByTestCaseId(Long testCaseId) {
        return executionRepository.findByTestCaseIdOrderByCreatedTimeDesc(testCaseId);
    }

    /**
     * 转换TestStepWithSelectors为TestStep
     * 用于检查是否需要生成选择器
     */
    private List<TestStep> convertToTestSteps(List<TestStepWithSelectors> stepsWithSelectors) {
        // 简单检查：如果第一个步骤没有selector，认为是TestStep
        if (stepsWithSelectors.isEmpty() || stepsWithSelectors.get(0).getSelector() == null) {
            Type listType = new TypeToken<List<TestStep>>() {}.getType();
            String json = gson.toJson(stepsWithSelectors);
            return gson.fromJson(json, listType);
        }
        throw new IllegalArgumentException("步骤已包含选择器，无需转换");
    }
}
