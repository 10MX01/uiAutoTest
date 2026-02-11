package com.uiauto.aiscript.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uiauto.aiscript.dto.GenerateScriptFromJsonRequest;
import com.uiauto.aiscript.dto.ParseStepsRequest;
import com.uiauto.aiscript.dto.TwoPhaseGenerateRequest;
import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.model.TestStepWithSelectors;
import com.uiauto.common.service.NaturalLanguageParser;
import com.uiauto.aiscript.service.PageSnapshotService;
import com.uiauto.aiscript.service.TwoPhaseScriptGenerationService;
import com.uiauto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.util.List;

/**
 * AI脚本生成Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIScriptController {

    @Autowired
    private NaturalLanguageParser naturalLanguageParser;

    @Autowired
    private TwoPhaseScriptGenerationService twoPhaseService;

    @Autowired
    private PageSnapshotService pageSnapshotService;

    private final Gson gson = new Gson();

    /**
     * 解析自然语言测试步骤
     */
    @PostMapping("/parse-steps")
    public ApiResponse<List<TestStep>> parseSteps(@Valid @RequestBody ParseStepsRequest request) {
        try {
            List<TestStep> steps = naturalLanguageParser.parseSteps(request.getStepsText());
            return ApiResponse.success(steps);
        } catch (Exception e) {
            log.error("解析测试步骤失败", e);
            return ApiResponse.error("解析失败: " + e.getMessage());
        }
    }

    /**
     * 批量解析测试步骤
     */
    @PostMapping("/parse-steps/batch")
    public ApiResponse<List<NaturalLanguageParser.ParseResult>> parseBatch(@RequestBody List<String> stepsTextList) {
        try {
            List<NaturalLanguageParser.ParseResult> results = naturalLanguageParser.parseBatch(stepsTextList);
            return ApiResponse.success(results);
        } catch (Exception e) {
            log.error("批量解析失败", e);
            return ApiResponse.error("批量解析失败: " + e.getMessage());
        }
    }

    /**
     * 两阶段生成脚本
     */
    @PostMapping("/generate-script/two-phase")
    public ApiResponse<Object> generateScriptTwoPhase(@Valid @RequestBody TwoPhaseGenerateRequest request) {
        try {
            TwoPhaseScriptGenerationService.TwoPhaseGenerateResult result =
                    twoPhaseService.generateScriptInTwoPhases(
                            request.getNaturalLanguage(),
                            request.getUrl()
                    );

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("两阶段生成脚本失败", e);
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 基于JSON和快照生成脚本
     */
    @PostMapping("/generate-script/from-json")
    public ApiResponse<List<TestStepWithSelectors>> generateFromJson(
            @Valid @RequestBody GenerateScriptFromJsonRequest request) {

        try {
            // 解析步骤JSON
            Type listType = new TypeToken<List<TestStep>>() {}.getType();
            List<TestStep> steps = gson.fromJson(request.getStepsJson(), listType);

            // 解析页面快照
            PageSnapshot snapshot = pageSnapshotService.fromJson(request.getPageSnapshot());

            // 生成脚本
            List<TestStepWithSelectors> result =
                    twoPhaseService.generateScriptFromJsonAndSnapshot(steps, snapshot);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("基于JSON生成脚本失败", e);
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 导出页面快照
     */
    @GetMapping("/snapshot/export")
    public ApiResponse<String> exportSnapshot(@RequestParam String url) {
        try {
            PageSnapshot snapshot = pageSnapshotService.captureSnapshot(url);
            String json = pageSnapshotService.toJson(snapshot);
            return ApiResponse.success(json);
        } catch (Exception e) {
            log.error("导出页面快照失败", e);
            return ApiResponse.error("导出失败: " + e.getMessage());
        }
    }
}
