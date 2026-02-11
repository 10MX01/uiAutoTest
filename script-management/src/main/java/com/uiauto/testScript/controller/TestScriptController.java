package com.uiauto.testScript.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testScript.dto.ScriptSearchRequest;
import com.uiauto.testScript.dto.TestScriptCreateRequest;
import com.uiauto.testScript.dto.TestScriptUpdateRequest;
import com.uiauto.testScript.service.TestScriptService;
import com.uiauto.testScript.vo.TestScriptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 测试脚本Controller（重构版）
 *
 * 核心变更：
 * 1. 移除版本管理接口（revertToVersion、compareVersions、getVersionHistory）
 * 2. 移除标签关联接口（linkTags、listByTagId）
 * 3. 移除测试用例关联接口（linkTestCases）
 * 4. 移除导出接口（exportScripts）
 * 5. 移除update接口（不支持编辑脚本内容）
 * 6. 添加启用/禁用管理接口（toggleEnabled、updateBasicInfo）
 * 7. 添加AI生成相关接口（generateByAI、retryFailed、retryScript）
 * 8. 添加测试用例关联查询接口（getEnabledByTestCaseId、getAllByTestCaseId、getScriptForExecution）
 */
@Slf4j
@RestController
@RequestMapping("/scripts")
@RequiredArgsConstructor
public class TestScriptController {

    private final TestScriptService scriptService;

    // ==================== 基础CRUD ====================

    /**
     * 创建脚本
     */
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TestScriptCreateRequest request) {
        log.info("接收到创建脚本请求: {} for 测试用例: {}", request.getScriptName(), request.getTestCaseId());
        Long id = scriptService.create(request);
        return ApiResponse.success("脚本创建成功", id);
    }

    /**
     * 删除脚本（软删除）
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam Long uniqueId) {
        log.info("接收到删除脚本请求: {}", uniqueId);
        scriptService.delete(uniqueId);
        return ApiResponse.success("脚本已删除", null);
    }

    /**
     * 获取脚本详情
     */
    @GetMapping
    public ApiResponse<TestScriptResponse> getById(@RequestParam Long uniqueId) {
        log.info("获取脚本详情: {}", uniqueId);
        TestScriptResponse response = scriptService.getById(uniqueId);
        return ApiResponse.success(response);
    }

    /**
     * 搜索脚本（关键词搜索：名称、描述、内容）
     */
    @PostMapping("/search")
    public ApiResponse<List<TestScriptResponse>> search(@RequestBody ScriptSearchRequest request) {
        log.info("搜索脚本, 关键词: {}", request.getKeyword());
        List<TestScriptResponse> responses = scriptService.search(request);
        return ApiResponse.success(responses);
    }

    /**
     * 查询所有脚本
     */
    @GetMapping("/all")
    public ApiResponse<List<TestScriptResponse>> listAll() {
        log.info("查询所有脚本");
        List<TestScriptResponse> responses = scriptService.listAll();
        return ApiResponse.success(responses);
    }

    /**
     * 按分类查询脚本
     */
    @GetMapping("/by-category")
    public ApiResponse<List<TestScriptResponse>> listByCategory(@RequestParam String category) {
        log.info("按分类查询脚本: {}", category);
        List<TestScriptResponse> responses = scriptService.listByCategory(category);
        return ApiResponse.success(responses);
    }

    /**
     * 按生成方式查询脚本
     */
    @GetMapping("/by-generation-method")
    public ApiResponse<List<TestScriptResponse>> listByGenerationMethod(@RequestParam String generationMethod) {
        log.info("按生成方式查询脚本: {}", generationMethod);
        List<TestScriptResponse> responses = scriptService.listByGenerationMethod(generationMethod);
        return ApiResponse.success(responses);
    }

    // ==================== 测试用例关联查询 ====================

    /**
     * 获取测试用例的启用脚本
     */
    @GetMapping("/enabled-by-testcase")
    public ApiResponse<TestScriptResponse> getEnabledByTestCaseId(@RequestParam Long testCaseId) {
        log.info("获取测试用例的启用脚本: {}", testCaseId);
        TestScriptResponse response = scriptService.getEnabledByTestCaseId(testCaseId);
        return ApiResponse.success(response);
    }

    /**
     * 获取测试用例的所有脚本（包括已禁用）
     */
    @GetMapping("/all-by-testcase")
    public ApiResponse<List<TestScriptResponse>> getAllByTestCaseId(@RequestParam Long testCaseId) {
        log.info("获取测试用例的所有脚本: {}", testCaseId);
        List<TestScriptResponse> responses = scriptService.getAllByTestCaseId(testCaseId);
        return ApiResponse.success(responses);
    }

    /**
     * 获取测试用例的执行脚本（如果没有启用脚本则自动AI生成）
     */
    @GetMapping("/for-execution")
    public ApiResponse<TestScriptResponse> getScriptForExecution(@RequestParam Long testCaseId) {
        log.info("获取测试用例的执行脚本: {}", testCaseId);
        TestScriptResponse response = scriptService.getScriptForExecution(testCaseId);
        return ApiResponse.success(response);
    }

    // ==================== 启用/禁用管理 ====================

    /**
     * 切换脚本启用状态
     */
    @PostMapping("/toggle-enabled")
    public ApiResponse<Void> toggleEnabled(@RequestParam Long scriptId) {
        log.info("切换脚本启用状态: {}", scriptId);

        // 获取当前状态并切换
        TestScriptResponse current = scriptService.getById(scriptId);
        Boolean newStatus = !current.getEnabled();

        scriptService.updateEnabledStatus(scriptId, newStatus);
        String statusText = newStatus ? "启用" : "禁用";
        return ApiResponse.success("脚本已" + statusText, null);
    }

    /**
     * 更新脚本启用状态
     */
    @PostMapping("/update-enabled")
    public ApiResponse<Void> updateEnabledStatus(
            @RequestParam Long scriptId,
            @RequestParam Boolean enabled) {
        log.info("更新脚本启用状态: {} -> {}", scriptId, enabled);
        scriptService.updateEnabledStatus(scriptId, enabled);
        String statusText = enabled ? "启用" : "禁用";
        return ApiResponse.success("脚本已" + statusText, null);
    }

    /**
     * 更新脚本基本信息（不包含内容）
     */
    @PostMapping("/update-basic-info")
    public ApiResponse<Void> updateBasicInfo(
            @RequestParam Long scriptId,
            @Valid @RequestBody TestScriptUpdateRequest request) {
        log.info("更新脚本基本信息: {}", scriptId);
        scriptService.updateBasicInfo(scriptId, request);
        return ApiResponse.success("脚本基本信息更新成功", null);
    }

    // ==================== AI生成相关 ====================

    /**
     * AI生成脚本
     */
    @PostMapping("/generate-by-ai")
    public ApiResponse<TestScriptResponse> generateByAI(@RequestParam Long testCaseId) {
        log.info("AI生成脚本 for 测试用例: {}", testCaseId);
        TestScriptResponse response = scriptService.generateByAI(testCaseId);
        return ApiResponse.success("AI脚本生成完成", response);
    }

    /**
     * 重试所有失败的AI生成
     */
    @PostMapping("/retry-all-failed")
    public ApiResponse<Void> retryAllFailed() {
        log.info("开始重试所有失败的AI生成");
        scriptService.retryFailedGeneration();
        return ApiResponse.success("失败AI生成重试完成", null);
    }

    /**
     * 手动重试单个脚本的AI生成
     */
    @PostMapping("/retry-script")
    public ApiResponse<TestScriptResponse> retryScriptAIGeneration(@RequestParam Long scriptId) {
        log.info("手动重试脚本AI生成: {}", scriptId);
        TestScriptResponse response = scriptService.getById(scriptId);
        scriptService.retryScriptAIGeneration(scriptId);
        return ApiResponse.success("AI生成重试完成", response);
    }

    /**
     * 查询AI生成失败的脚本列表
     */
    @GetMapping("/failed-ai-generations")
    public ApiResponse<List<TestScriptResponse>> listFailedAIGenerations() {
        log.info("查询AI生成失败的脚本列表");
        List<TestScriptResponse> responses = scriptService.listFailedAIGenerations();
        return ApiResponse.success(responses);
    }

    // ==================== 执行相关 ====================

    /**
     * 增加脚本执行次数
     */
    @PostMapping("/increment-execution")
    public ApiResponse<Void> incrementExecutionCount(@RequestParam Long scriptId) {
        log.info("增加脚本执行次数: {}", scriptId);
        scriptService.incrementExecutionCount(scriptId);
        return ApiResponse.success(null);
    }

    /**
     * 更新脚本执行结果
     */
    @PostMapping("/update-result")
    public ApiResponse<Void> updateExecutionResult(
            @RequestParam Long scriptId,
            @RequestParam String result) {
        log.info("更新脚本执行结果: {}, 结果: {}", scriptId, result);
        scriptService.updateExecutionResult(scriptId, result);
        return ApiResponse.success(null);
    }

    // ==================== 统计信息 ====================

    /**
     * 统计测试用例的脚本数量
     */
    @GetMapping("/count-by-testcase")
    public ApiResponse<Long> countByTestCaseId(@RequestParam Long testCaseId) {
        log.info("统计测试用例的脚本数量: {}", testCaseId);
        Long count = scriptService.countByTestCaseId(testCaseId);
        return ApiResponse.success(count);
    }

    /**
     * 获取最常执行的脚本
     */
    @GetMapping("/top-executed")
    public ApiResponse<List<TestScriptResponse>> listTopExecuted(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取最常执行的脚本，数量: {}", limit);
        List<TestScriptResponse> responses = scriptService.listTopExecuted(limit);
        return ApiResponse.success(responses);
    }
}
