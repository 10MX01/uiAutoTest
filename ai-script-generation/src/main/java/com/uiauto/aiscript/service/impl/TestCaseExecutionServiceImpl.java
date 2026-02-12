package com.uiauto.aiscript.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uiauto.testcase.entity.TestCaseExecutionEntity;
import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.model.TestStepAction;
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
import java.time.LocalDateTime;
import java.util.*;

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

    /**
     * 首次导航标记（线程局部变量，避免并发问题）
     */
    private ThreadLocal<Boolean> hasNavigated = ThreadLocal.withInitial(() -> false);

    @Override
    @Transactional
    public TestCaseExecutionEntity executeTestCase(Long testCaseId, String overrideUrl, Long executedBy) {
        long startTime = System.currentTimeMillis();

        // 创建执行会话（开发阶段：headless=false 显示浏览器窗口）
        PlaywrightExecutor.ExecutionConfig config =
                new PlaywrightExecutor.ExecutionConfig(false, true, 30000, true);
        String sessionId = playwrightExecutor.createSession(config);
        log.info("【开发模式】浏览器窗口已显示，sessionId: {}", sessionId);

        // 重置首次导航标记
        hasNavigated.set(false);

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
            execution.setCreatedTime(LocalDateTime.now());
            execution.setUpdatedTime(LocalDateTime.now());

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
            execution.setCreatedTime(LocalDateTime.now());
            execution.setUpdatedTime(LocalDateTime.now());

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

        // 记录开始时间
        long startTime = System.currentTimeMillis();

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

            // 4. 执行前置依赖（在执行依赖前，最外层用例先导航）
            // 判断是否是最外层测试用例（executingStack.size() == 1 表示这是最外层调用）
            boolean isOutermostTestCase = (executingStack.size() == 1);

            // 最外层测试用例：在执行依赖之前先导航到初始页面
            if (isOutermostTestCase && !hasNavigated.get()) {
                log.info("最外层测试用例，先导航到初始URL再执行依赖: {}", url);
                try {
                    playwrightExecutor.navigate(sessionId, url);
                    hasNavigated.set(true);
                    // 【调试日志】导航完成后，检查当前页面URL
                    String currentUrlAfterNav = playwrightExecutor.getCurrentUrl(sessionId);
                    log.info("【调试】初始导航完成，当前浏览器页面URL: {}", currentUrlAfterNav);
                } catch (Exception e) {
                    log.error("导航失败: {}", e.getMessage());
                    throw new RuntimeException("导航失败: " + e.getMessage(), e);
                }
            }

            // 执行前置依赖
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

            // 【调试日志】依赖执行完成后，检查当前页面状态
            String currentUrlAfterDeps = playwrightExecutor.getCurrentUrl(sessionId);
            log.info("【调试】所有依赖执行完成，当前浏览器页面URL: {}", currentUrlAfterDeps);

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
                // 生成新脚本 - 使用按页分组批量生成模式
                log.info("未找到已启用的脚本，开始按页分组生成...");

                // 5.1 检查测试步骤
                if (testCase.getStepsJson() == null || testCase.getStepsJson().isEmpty()) {
                    throw new RuntimeException("测试用例缺少步骤数据，请先通过AI解析生成步骤");
                }

                // 5.2 解析测试步骤
                Type listType = new TypeToken<List<TestStep>>() {}.getType();
                List<TestStep> steps = gson.fromJson(testCase.getStepsJson(), listType);
                log.info("解析测试步骤成功，共{}个步骤", steps.size());

                // 5.3 获取当前页面快照（导航已在执行依赖前完成）
                // 【调试日志】获取快照前，再次检查当前页面URL
                String currentUrlBeforeSnapshot = playwrightExecutor.getCurrentUrl(sessionId);
                log.info("【调试】准备获取页面快照，当前浏览器页面URL: {}", currentUrlBeforeSnapshot);
                PageSnapshot currentSnapshot;
                try {
                    currentSnapshot = playwrightExecutor.capturePageSnapshotWithElements(sessionId);
                    log.info("获取初始页面快照成功: URL={}, 元素数量={}",
                            currentSnapshot.getUrl(), currentSnapshot.getElements().size());
                } catch (Exception e) {
                    log.error("获取初始快照失败: {}", e.getMessage());
                    throw new RuntimeException("获取初始快照失败: " + e.getMessage(), e);
                }

                // 5.5 按页分组批量生成和执行
                List<TestStepWithSelectors> completeScript = new ArrayList<>();
                int groupIndex = 0;
                int currentStepStart = 0;

                for (int i = 0; i < steps.size(); i++) {
                    TestStep step = steps.get(i);
                    boolean isNavigationStep = shouldCaptureSnapshotBefore(step);

                    // 如果是最后一步，或者是导航步骤，则处理当前组
                    if (i == steps.size() - 1 || isNavigationStep) {
                        int currentStepEnd = i + 1;
                        List<TestStep> currentGroupSteps = steps.subList(currentStepStart, currentStepEnd);

                        groupIndex++;
                        log.info("=== 处理分组 {} === 步骤 {}-{}, 共{}个步骤, 是否导航: {}",
                                groupIndex, currentStepStart + 1, currentStepEnd, currentGroupSteps.size(), isNavigationStep);

                        try {
                            // a. 批量生成当前组的选择器
                            log.info("开始批量生成分组 {} 的选择器...", groupIndex);
                            List<TestStepWithSelectors> groupScript =
                                    twoPhaseService.generateScriptFromJsonAndSnapshot(currentGroupSteps, currentSnapshot);

                            log.info("分组 {} 选择器生成成功，共{}个步骤", groupIndex, groupScript.size());
                            completeScript.addAll(groupScript);

                            // b. 执行当前组的步骤
                            log.info("执行分组 {} 的步骤...", groupIndex);
                            playwrightExecutor.executeInSession(sessionId, url, groupScript);
                            log.info("分组 {} 执行成功", groupIndex);

                            // c. 如果是导航操作，获取新快照（为下一组做准备）
                            if (isNavigationStep && i < steps.size() - 1) {
                                log.info("分组 {} 包含导航操作，获取新页面快照", groupIndex);
                                try {
                                    currentSnapshot = playwrightExecutor.capturePageSnapshotWithElements(sessionId);
                                    log.info("获取新页面快照成功: URL={}, 元素数量={}",
                                            currentSnapshot.getUrl(), currentSnapshot.getElements().size());
                                } catch (Exception e) {
                                    log.error("获取新快照失败: {}", e.getMessage());
                                    throw new RuntimeException("分组" + groupIndex + "执行后获取快照失败: " + e.getMessage(), e);
                                }
                            }

                            // d. 更新下一组的起始索引
                            currentStepStart = currentStepEnd;

                        } catch (Exception e) {
                            log.error("处理分组 {} 失败: error={}", groupIndex, e.getMessage());
                            throw new RuntimeException("分组" + groupIndex + "处理失败: " + e.getMessage(), e);
                        }
                    }
                }

                // 5.6 所有分组处理完成，保存完整脚本
                scriptWithSelectors = completeScript;
                scriptContent = gson.toJson(completeScript);

                log.info("所有分组处理完成，共{}个分组，开始保存脚本，共{}个步骤", groupIndex, completeScript.size());
                saveGeneratedScript(testCase, completeScript, executedBy);
                log.info("脚本保存成功");
            }

            // 6. 执行脚本
            // 注意：如果是新生成的脚本（在生成过程中已执行），则跳过执行
            // 只有复用已有脚本时才需要执行
            PlaywrightExecutor.ExecutionResult result;

            if (existingScript != null) {
                // 复用已有脚本，需要执行
                log.info("开始执行测试用例（复用脚本）: {}", testCase.getName());
                result = playwrightExecutor.executeInSession(sessionId, url, scriptWithSelectors);
            } else {
                // 新生成的脚本，在生成过程中已执行，构造虚拟结果
                log.info("新脚本已在生成过程中执行，构造执行结果");
                result = new PlaywrightExecutor.ExecutionResult(
                        true,  // success
                        "SUCCESS",
                        System.currentTimeMillis() - startTime,
                        new ArrayList<>(),  // stepResults
                        new ArrayList<>(),  // screenshots
                        null  // errorMessage
                );
            }

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

    /**
     * 判断步骤是否需要获取新快照（在步骤执行前）
     * 对于可能导致页面跳转的操作，需要在新页面执行前获取快照
     *
     * @param step 测试步骤
     * @return true表示需要获取新快照
     */
    private boolean shouldCaptureSnapshotBefore(TestStep step) {
        if (step == null || step.getAction() == null) {
            return false;
        }

        TestStepAction action = TestStepAction.fromString(step.getAction());
        return action != null && action.isNavigation();
    }
}
