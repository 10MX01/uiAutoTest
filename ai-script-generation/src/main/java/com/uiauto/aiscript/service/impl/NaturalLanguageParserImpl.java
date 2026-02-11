package com.uiauto.aiscript.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import com.uiauto.aiscript.entity.PromptTemplateEntity;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.service.NaturalLanguageParser;
import com.uiauto.aiscript.service.AICallLogService;
import com.uiauto.aiscript.service.AIServiceConfigService;
import com.uiauto.aiscript.service.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 自然语言解析Service实现
 */
@Slf4j
@Service
public class NaturalLanguageParserImpl implements NaturalLanguageParser {

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
    public List<TestStep> parseSteps(String stepsText) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // 获取默认AI服务配置
            AIServiceConfigEntity config = configService.getDefaultConfig();

            // 获取Prompt模板
            Optional<PromptTemplateEntity> templateOpt =
                    promptTemplateService.getActiveTemplate("natural_language_parse");

            if (!templateOpt.isPresent()) {
                throw new RuntimeException("未找到自然语言解析Prompt模板");
            }

            String prompt = buildPrompt(templateOpt.get().getPromptContent(), stepsText);

            // 调用AI服务（30秒超时）
            String response = llmService.callLLM(config, prompt, 60000L);

            // 解析响应
            List<TestStep> steps = parseResponse(response);

            // 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logSuccess(
                    "natural_language_parse",
                    stepsText,
                    gson.toJson(steps),
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
                    "natural_language_parse",
                    stepsText,
                    e.getMessage(),
                    duration,
                    null
            );

            throw new Exception("自然语言解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ParseResult> parseBatch(List<String> stepsTextList) {
        List<ParseResult> results = new ArrayList<>();

        for (String stepsText : stepsTextList) {
            try {
                List<TestStep> steps = parseSteps(stepsText);
                results.add(new ParseResult(true, steps, null));
            } catch (Exception e) {
                results.add(new ParseResult(false, null, e.getMessage()));
            }
        }

        return results;
    }

    /**
     * 构建Prompt
     */
    private String buildPrompt(String template, String userInput) {
        return template.replace("{user_input}", userInput);
    }

    /**
     * 解析AI响应
     */
    private List<TestStep> parseResponse(String response) {
        try {
            log.debug("原始AI响应: {}", response);

            // 提取纯JSON内容（去除markdown代码块标记）
            String jsonContent = extractJsonFromMarkdown(response);
            log.debug("提取的JSON内容: {}", jsonContent);

            // 解析JSON数组
            JsonElement element = JsonParser.parseString(jsonContent);

            if (element.isJsonArray()) {
                Type listType = new TypeToken<List<TestStep>>() {}.getType();
                return gson.fromJson(element, listType);
            } else if (element.isJsonObject()) {
                // 如果是对象，尝试查找特定字段
                JsonArray array = element.getAsJsonArray();
                if (array != null) {
                    Type listType = new TypeToken<List<TestStep>>() {}.getType();
                    return gson.fromJson(array, listType);
                }
            }

            // 尝试直接解析
            Type listType = new TypeToken<List<TestStep>>() {}.getType();
            return gson.fromJson(jsonContent, listType);

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            throw new RuntimeException("AI响应格式错误，无法解析为测试步骤");
        }
    }

    /**
     * 从Markdown代码块中提取JSON内容
     */
    private String extractJsonFromMarkdown(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }

        // 去除前后空白
        String content = response.trim();

        // 匹配 ```json ... ``` 或 ``` ... ``` 格式
        if (content.startsWith("```")) {
            int firstNewline = content.indexOf('\n');
            int lastBackticks = content.lastIndexOf("```");

            if (firstNewline > 0 && lastBackticks > firstNewline) {
                // 提取代码块内容
                content = content.substring(firstNewline + 1, lastBackticks).trim();

                // 去除可能存在的语言标记（如 "json"）
                if (content.startsWith("json")) {
                    content = content.substring(4).trim();
                } else if (content.startsWith("JSON")) {
                    content = content.substring(4).trim();
                }
            }
        }

        return content;
    }
}
