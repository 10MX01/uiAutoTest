package com.uiauto.aiscript.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import com.uiauto.aiscript.entity.PromptTemplateEntity;
import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.model.TestStepWithSelectors;
import com.uiauto.common.service.NaturalLanguageParser;
import com.uiauto.aiscript.service.AICallLogService;
import com.uiauto.aiscript.service.AIServiceConfigService;
import com.uiauto.aiscript.service.LLMService;
import com.uiauto.aiscript.service.PageSnapshotService;
import com.uiauto.aiscript.service.TwoPhaseScriptGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonSyntaxException;

/**
 * 两阶段脚本生成Service实现
 */
@Slf4j
@Service
public class TwoPhaseScriptGenerationServiceImpl implements TwoPhaseScriptGenerationService {

    @Autowired
    private NaturalLanguageParser naturalLanguageParser;

    @Autowired
    private PageSnapshotService pageSnapshotService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private AIServiceConfigService configService;

    @Autowired
    private com.uiauto.aiscript.service.PromptTemplateService promptTemplateService;

    @Autowired
    private AICallLogService callLogService;

    private final Gson gson = new Gson();

    @Override
    public TwoPhaseGenerateResult generateScriptInTwoPhases(String naturalLanguage, String url) throws Exception {
        // 阶段1：自然语言 → JSON
        List<TestStep> phase1Result = naturalLanguageParser.parseSteps(naturalLanguage);

        // 捕获页面快照
        PageSnapshot snapshot = pageSnapshotService.captureSnapshot(url);

        // 阶段2：JSON + 快照 → 脚本
        List<TestStepWithSelectors> phase2Result =
                generateScriptFromJsonAndSnapshot(phase1Result, snapshot);

        // 生成元数据
        GenerationMetadata metadata = validateAndMetadata(phase2Result);

        return new TwoPhaseGenerateResult(phase1Result, phase2Result, snapshot, metadata);
    }

    @Override
    public List<TestStepWithSelectors> generateScriptFromJsonAndSnapshot(
            List<TestStep> stepsJson, PageSnapshot snapshot) throws Exception {

        long startTime = System.currentTimeMillis();

        try {
            // 获取默认AI服务配置
            AIServiceConfigEntity config = configService.getDefaultConfig();

            // 获取Prompt模板
            Optional<PromptTemplateEntity> templateOpt =
                    promptTemplateService.getActiveTemplate("script_generation_from_json");

            if (!templateOpt.isPresent()) {
                throw new RuntimeException("未找到脚本生成Prompt模板");
            }

            // 构建Prompt
            String prompt = buildPrompt(
                    templateOpt.get().getPromptContent(),
                    stepsJson,
                    snapshot
            );

            // 调用AI服务（60秒超时）
            String response = llmService.callLLM(config, prompt);

            // 从响应中提取纯JSON（去除markdown代码块）
            String extractedJson = extractJsonFromMarkdown(response);

            // 解析响应
            List<TestStepWithSelectors> steps = parseScriptResponse(response);

            // 记录成功日志 - 如果提取的JSON为空，使用实际解析后的结果
            long duration = System.currentTimeMillis() - startTime;
            String outputJson = extractedJson;
            if (outputJson == null || outputJson.trim().isEmpty()) {
                outputJson = gson.toJson(steps);
                log.warn("AI响应提取的JSON为空，使用解析后的结果保存日志");
            }
            callLogService.logSuccess(
                    "script_generation_from_json",
                    gson.toJson(stepsJson),
                    outputJson,
                    duration,
                    0,
                    config.getModelName(),
                    config.getUniqueId()
            );

            return steps;

        } catch (Exception e) {
            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logFailure(
                    "script_generation_from_json",
                    gson.toJson(stepsJson),
                    e.getMessage(),
                    duration,
                    null
            );

            throw new Exception("脚本生成失败: " + e.getMessage(), e);
        }
    }

    public GenerationMetadata validateAndMetadata(List<TestStepWithSelectors> steps) {
        int totalSteps = steps.size();
        int matchedElements = 0;
        int unmatchedTargets = 0;
        double totalConfidence = 0.0;

        for (TestStepWithSelectors step : steps) {
            if (step.getSelector() != null && !step.getSelector().isEmpty()) {
                matchedElements++;
                totalConfidence += step.getConfidence() != null ? step.getConfidence() : 0.0;
            } else {
                unmatchedTargets++;
            }
        }

        double averageConfidence = matchedElements > 0 ? totalConfidence / matchedElements : 0.0;

        return new GenerationMetadata(totalSteps, matchedElements, unmatchedTargets, averageConfidence);
    }

    /**
     * 构建Prompt
     */
    private String buildPrompt(String template, List<TestStep> stepsJson, PageSnapshot snapshot) {
        String stepsJsonStr = gson.toJson(stepsJson);
        String snapshotStr = pageSnapshotService.toJson(snapshot);

        return template
                .replace("{steps_json}", stepsJsonStr)
                .replace("{page_snapshot}", snapshotStr);
    }

    /**
     * 解析AI响应
     */
    private List<TestStepWithSelectors> parseScriptResponse(String response) {
        try {
            log.info("【AI原始响应】{}", response);

            // 提取纯JSON内容（去除markdown代码块标记）
            String jsonContent = extractJsonFromMarkdown(response);
            log.info("【提取的JSON】{}", jsonContent);

            // 尝试解析为数组
            Type listType = new TypeToken<List<TestStepWithSelectors>>() {}.getType();
            List<TestStepWithSelectors> result;

            try {
                result = gson.fromJson(jsonContent, listType);
            } catch (JsonSyntaxException e) {
                // 如果解析数组失败，尝试解析为单个对象
                log.info("【解析】解析为数组失败，尝试解析为单个对象");
                TestStepWithSelectors singleStep = gson.fromJson(jsonContent, TestStepWithSelectors.class);
                result = new ArrayList<>();
                result.add(singleStep);
                log.info("【解析成功】单个对象转换为数组，共1个步骤");
            }

            log.info("【解析成功】共{}个步骤", result.size());
            return result;
        } catch (Exception e) {
            log.error("【解析失败】原始响应: {}", response, e);
            log.error("【解析失败】错误信息: {}", e.getMessage());
            throw new RuntimeException("AI响应格式错误，无法解析为测试脚本: " + e.getMessage());
        }
    }

    /**
     * 从Markdown代码块中提取JSON内容
     * 支持格式：
     * 1. ```json ... ```
     * 2. ``` ... ```
     * 3. [文字] ```json ... ``` [文字]
     */
    private String extractJsonFromMarkdown(String response) {
        if (response == null || response.isEmpty()) {
            log.warn("【提取JSON】响应为空");
            return response;
        }

        String content = response.trim();
        log.info("【提取JSON】原始响应长度: {}", content.length());

        // 查找第一个代码块开始标记
        int codeBlockStart = content.indexOf("```");
        if (codeBlockStart == -1) {
            // 没有代码块标记，直接返回
            log.info("【提取JSON】未找到代码块标记，直接返回原始内容");
            return content;
        }

        // 从代码块开始位置查找换行符
        int firstNewline = content.indexOf('\n', codeBlockStart);
        if (firstNewline == -1) {
            log.warn("【提取JSON】代码块标记后没有换行符");
            return content;
        }

        // 查找代码块结束标记（从换行符之后开始查找）
        int codeBlockEnd = content.indexOf("```", firstNewline + 1);
        if (codeBlockEnd == -1) {
            log.warn("【提取JSON】未找到代码块结束标记");
            return content;
        }

        // 提取代码块内容
        String extracted = content.substring(firstNewline + 1, codeBlockEnd).trim();
        log.info("【提取JSON】提取代码块内容，长度: {}", extracted.length());

        // 去除可能存在的语言标记（如 "json" 或 "JSON"）
        if (extracted.startsWith("json")) {
            log.info("【提取JSON】检测到 'json' 前缀，去除");
            extracted = extracted.substring(4).trim();
        } else if (extracted.startsWith("JSON")) {
            log.info("【提取JSON】检测到 'JSON' 前缀，去除");
            extracted = extracted.substring(4).trim();
        }

        log.info("【提取JSON】最终提取的JSON: {}", extracted);
        return extracted;
    }
}
