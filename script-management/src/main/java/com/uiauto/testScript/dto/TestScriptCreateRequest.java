package com.uiauto.testScript.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建测试脚本请求DTO（重构版）
 *
 * 核心变更：
 * 1. sourceType改为generationMethod（仅支持EXCEL_IMPORT和AI_GENERATED）
 * 2. testCaseIds改为单个testCaseId（必填）
 * 3. 添加AI生成相关字段
 * 4. 移除tagIds（暂时不需要）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScriptCreateRequest {

    /**
     * 脚本名称
     */
    @NotBlank(message = "脚本名称不能为空")
    private String scriptName;

    /**
     * 脚本描述
     */
    private String scriptDescription;

    /**
     * 脚本内容
     */
    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;

    /**
     * 脚本语言：typescript/javascript
     */
    @Builder.Default
    private String language = "typescript";

    /**
     * 生成方式：EXCEL_IMPORT/AI_GENERATED（必填）
     */
    @NotNull(message = "生成方式不能为空")
    private String generationMethod;

    /**
     * 测试用例ID（必填）
     * 一个脚本只能关联一个测试用例
     */
    @NotNull(message = "测试用例ID不能为空")
    private Long testCaseId;

    /**
     * 脚本分类
     */
    private String category;

    // ==================== AI生成相关字段（可选） ====================

    /**
     * AI生成状态：SUCCESS/FAILED/PENDING
     */
    private String aiGenerationStatus;

    /**
     * AI生成失败错误信息
     */
    private String aiErrorMessage;

    /**
     * 使用的AI模型（如gpt-4）
     */
    private String aiModelUsed;
}
