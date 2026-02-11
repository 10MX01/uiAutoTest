package com.uiauto.testScript.entity;

import com.uiauto.common.BaseEntity;
import com.uiauto.testcase.entity.TestCaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 测试脚本实体类（重构版）
 * 对应数据库表：test_scripts
 *
 * 核心变更：
 * 1. 移除版本控制功能（currentVersion、versions等）
 * 2. 添加启用状态管理（enabled）
 * 3. 添加生成方式字段（generationMethod）
 * 4. 添加AI生成相关字段
 * 5. 强制关联测试用例（testCaseId必填）
 */
@Entity
@Table(name = "test_scripts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestScriptEntity extends BaseEntity {

    /**
     * 脚本名称
     */
    @Column(name = "script_name", nullable = false, length = 255)
    private String scriptName;

    /**
     * 脚本描述
     */
    @Lob
    @Column(name = "script_description")
    private String scriptDescription;

    /**
     * 脚本内容（TypeScript/JavaScript代码）
     */
    @Column(name = "script_content", nullable = false, columnDefinition = "JSON")
    private String scriptContent;

    /**
     * 脚本语言：typescript/javascript
     */
    @Column(name = "language", length = 20)
    @Builder.Default
    private String language = "typescript";

    // ==================== 核心变更字段 ====================

    /**
     * 生成方式：EXCEL_IMPORT/AI_GENERATED
     * 仅支持两种方式：
     * - EXCEL_IMPORT：Excel导入测试用例后自动解析生成
     * - AI_GENERATED：AI智能运行测试用例时自动生成
     */
    @Column(name = "generation_method", nullable = false, length = 50)
    private String generationMethod;

    /**
     * 启用状态：true=启用，false=禁用
     * 同一测试用例只能有一个脚本处于启用状态
     */
    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    // ==================== AI生成相关字段 ====================

    /**
     * AI生成状态：SUCCESS/FAILED/PENDING
     */
    @Column(name = "ai_generation_status", length = 20)
    @Builder.Default
    private String aiGenerationStatus = "SUCCESS";

    /**
     * AI重试次数（最多3次）
     */
    @Column(name = "ai_retry_count")
    @Builder.Default
    private Integer aiRetryCount = 0;

    /**
     * AI生成失败错误信息
     */
    @Lob
    @Column(name = "ai_error_message")
    private String aiErrorMessage;

    /**
     * 使用的AI模型（如gpt-4、gpt-3.5-turbo等）
     */
    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed;

    /**
     * AI生成时间
     */
    @Column(name = "ai_generation_time")
    private LocalDateTime aiGenerationTime;

    // ==================== 关联测试用例（必需字段） ====================

    /**
     * 关联的测试用例ID（必填）
     * 一个脚本只能属于一个测试用例
     */
    @Column(name = "test_case_id", nullable = false)
    private Long testCaseId;

    /**
     * 关联的测试用例实体（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", insertable = false, updatable = false)
    private TestCaseEntity testCase;

    // ==================== 分类和统计字段 ====================

    /**
     * 脚本分类
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * 执行次数
     */
    @Column(name = "execution_count")
    @Builder.Default
    private Integer executionCount = 0;

    /**
     * 最后执行时间
     */
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;

    /**
     * 最后执行结果：SUCCESS/FAILED/SKIPPED
     */
    @Column(name = "last_execution_result", length = 20)
    private String lastExecutionResult;

    // ==================== 业务方法 ====================
    /**
     * 判断是否为AI生成
     */
    public boolean isAIGenerated() {
        return "AI_GENERATED".equals(this.generationMethod);
    }

    /**
     * 判断是否为Excel导入生成
     */
    public boolean isExcelImported() {
        return "EXCEL_IMPORT".equals(this.generationMethod);
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }

    /**
     * 判断AI生成是否失败
     */
    public boolean isAIGenerationFailed() {
        return "FAILED".equals(this.aiGenerationStatus);
    }

    /**
     * 判断是否可以重试AI生成
     */
    public boolean canRetryAIGeneration() {
        return isAIGenerationFailed() && this.aiRetryCount < 3;
    }

    /**
     * 标记AI生成失败
     */
    public void markAIGenerationFailed(String errorMessage) {
        this.aiGenerationStatus = "FAILED";
        this.aiErrorMessage = errorMessage;
        this.aiGenerationTime = LocalDateTime.now();
        this.enabled = false; // AI生成失败时自动禁用
    }

    /**
     * 标记AI生成成功
     */
    public void markAIGenerationSuccess(String content, String modelUsed) {
        this.aiGenerationStatus = "SUCCESS";
        this.scriptContent = content;
        this.aiModelUsed = modelUsed;
        this.aiErrorMessage = null;
        this.aiGenerationTime = LocalDateTime.now();
        this.enabled = true; // AI生成成功时自动启用
    }

    /**
     * 增加AI重试次数
     */
    public void incrementAIRetryCount() {
        this.aiRetryCount = (this.aiRetryCount == null ? 0 : this.aiRetryCount) + 1;
    }
}
