package com.uiauto.testScript.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 测试脚本响应VO（重构版）
 *
 * 核心变更：
 * 1. 移除版本相关字段（currentVersion、versions等）
 * 2. 移除标签关联（tags）
 * 3. 测试用例改为单个（testCaseId、testCaseName）
 * 4. 添加启用状态字段（enabled）
 * 5. 添加生成方式字段（generationMethod）
 * 6. 添加AI生成相关字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScriptResponse {

    /**
     * 唯一标识ID
     */
    private Long uniqueId;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 脚本描述
     */
    private String scriptDescription;

    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 脚本语言
     */
    private String language;

    // ==================== 核心变更字段 ====================

    /**
     * 生成方式：EXCEL_IMPORT/AI_GENERATED
     */
    private String generationMethod;

    /**
     * 生成方式显示名称
     */
    private String generationMethodDisplayName;

    /**
     * 启用状态：true=启用，false=禁用
     */
    private Boolean enabled;

    // ==================== AI生成相关字段 ====================

    /**
     * AI生成状态：SUCCESS/FAILED/PENDING
     */
    private String aiGenerationStatus;

    /**
     * AI生成状态显示名称
     */
    private String aiGenerationStatusDisplayName;

    /**
     * AI重试次数
     */
    private Integer aiRetryCount;

    /**
     * AI生成失败错误信息
     */
    private String aiErrorMessage;

    /**
     * 使用的AI模型
     */
    private String aiModelUsed;

    /**
     * AI生成时间
     */
    private LocalDateTime aiGenerationTime;

    /**
     * 是否可以重试AI生成
     */
    private Boolean canRetryAIGeneration;

    // ==================== 关联测试用例 ====================

    /**
     * 关联的测试用例ID
     */
    private Long testCaseId;

    /**
     * 关联的测试用例名称
     */
    private String testCaseName;

    // ==================== 分类和统计字段 ====================

    /**
     * 脚本分类
     */
    private String category;

    /**
     * 执行次数
     */
    private Integer executionCount;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecutionTime;

    /**
     * 最后执行结果
     */
    private String lastExecutionResult;

    // ==================== 审计字段 ====================

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
