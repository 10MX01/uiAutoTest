package com.uiauto.common.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 测试步骤（阶段2：包含选择器的完整脚本）
 */
@Data
@Builder
public class TestStepWithSelectors {

    /**
     * 步骤序号
     */
    private Integer stepNumber;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 目标描述（中文）
     */
    private String target;

    /**
     * 主选择器
     */
    private String selector;

    /**
     * 选择器类型：testId/id/name/aria/text/css
     */
    private String selectorType;

    /**
     * 置信度（0.0-1.0）
     */
    private Double confidence;

    /**
     * 备用选择器列表
     */
    private List<String> fallbackSelectors;

    /**
     * 操作值
     */
    private String value;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 匹配依据
     */
    private String matchedBy;

    /**
     * 警告信息
     */
    private String warning;
}
