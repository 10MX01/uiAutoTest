package com.uiauto.testScript.service;

import com.uiauto.testScript.dto.ScriptSearchRequest;
import com.uiauto.testScript.dto.TestScriptCreateRequest;
import com.uiauto.testScript.dto.TestScriptUpdateRequest;
import com.uiauto.testScript.vo.TestScriptResponse;

import java.util.List;

/**
 * 测试脚本Service接口（重构版）
 *
 * 核心变更：
 * 1. 移除版本管理功能（revertToVersion、compareVersions等）
 * 2. 移除编辑功能（不支持修改脚本内容）
 * 3. 添加启用/禁用状态管理
 * 4. 添加AI生成和重试功能
 * 5. 添加运行测试用例时自动获取脚本的功能
 */
public interface TestScriptService {

    // ==================== 创建和删除 ====================

    /**
     * 创建脚本（由系统自动调用）
     * - Excel导入时调用
     * - AI生成时调用
     * 新创建的脚本默认为启用状态，会自动禁用该测试用例的其他脚本
     */
    Long create(TestScriptCreateRequest request);

    /**
     * 软删除脚本
     * 删除时自动禁用脚本
     */
    void delete(Long uniqueId);

    // ==================== 查询操作 ====================

    /**
     * 获取脚本详情
     */
    TestScriptResponse getById(Long uniqueId);

    /**
     * 搜索脚本
     */
    List<TestScriptResponse> search(ScriptSearchRequest request);

    /**
     * 查询所有脚本（未删除的）
     */
    List<TestScriptResponse> listAll();

    /**
     * 按分类查询脚本
     */
    List<TestScriptResponse> listByCategory(String category);

    /**
     * 按生成方式查询脚本（EXCEL_IMPORT/AI_GENERATED）
     */
    List<TestScriptResponse> listByGenerationMethod(String generationMethod);

    /**
     * 获取测试用例的启用脚本
     * @param testCaseId 测试用例ID
     * @return 启用的脚本，如果不存在返回null
     */
    TestScriptResponse getEnabledByTestCaseId(Long testCaseId);

    /**
     * 获取测试用例的所有脚本（包括已禁用的）
     * @param testCaseId 测试用例ID
     * @return 该测试用例的所有脚本，按创建时间倒序
     */
    List<TestScriptResponse> getAllByTestCaseId(Long testCaseId);

    // ==================== 启用/禁用管理 ====================

    /**
     * 更新脚本启用/禁用状态
     * @param scriptId 脚本ID
     * @param enabled true=启用，false=禁用
     * 启用时，会自动禁用该测试用例的其他所有脚本
     */
    void updateEnabledStatus(Long scriptId, Boolean enabled);

    /**
     * 更新脚本基本信息（不包含内容）
     * 仅允许修改：scriptName、scriptDescription、category
     * 不允许修改：scriptContent（不支持编辑）
     */
    void updateBasicInfo(Long scriptId, TestScriptUpdateRequest request);

    // ==================== AI生成相关 ====================

    /**
     * AI生成脚本（核心方法）
     * 根据测试用例自动生成脚本
     * - 生成成功：创建启用状态的脚本
     * - 生成失败：创建失败记录（ai_generation_status=FAILED），可后续重试
     *
     * @param testCaseId 测试用例ID
     * @return 生成的脚本信息
     */
    TestScriptResponse generateByAI(Long testCaseId);

    /**
     * 重试失败的AI生成
     * 遍历所有ai_generation_status=FAILED且ai_retry_count<3的脚本，重新调用AI生成
     * - 重试成功：更新脚本内容，标记为SUCCESS，启用
     * - 重试失败：ai_retry_count++，记录错误信息
     * - retry_count >= 3时，不再重试
     */
    void retryFailedGeneration();

    /**
     * 手动重试指定脚本的AI生成
     * @param scriptId 脚本ID
     */
    void retryScriptAIGeneration(Long scriptId);

    // ==================== 执行相关 ====================

    /**
     * 运行测试用例时获取脚本
     * 核心方法，用于测试执行引擎
     *
     * @param testCaseId 测试用例ID
     * @return 脚本信息
     *
     * 逻辑：
     * 1. 查找该测试用例的启用脚本
     * 2. 如果找到启用脚本，直接返回
     * 3. 如果没有启用脚本，自动触发AI生成
     * 4. AI生成成功，返回新脚本
     * 5. AI生成失败，返回失败记录（由调用方决定如何处理）
     */
    TestScriptResponse getScriptForExecution(Long testCaseId);

    /**
     * 增加脚本执行次数
     */
    void incrementExecutionCount(Long scriptId);

    /**
     * 更新脚本执行结果
     * @param scriptId 脚本ID
     * @param result 执行结果：SUCCESS/FAILED/SKIPPED
     */
    void updateExecutionResult(Long scriptId, String result);

    // ==================== 统计信息 ====================

    /**
     * 获取AI生成失败的脚本列表（可用于重试）
     */
    List<TestScriptResponse> listFailedAIGenerations();

    /**
     * 统计测试用例的脚本数量
     */
    Long countByTestCaseId(Long testCaseId);

    /**
     * 获取最常执行的脚本Top N
     */
    List<TestScriptResponse> listTopExecuted(int limit);
}
