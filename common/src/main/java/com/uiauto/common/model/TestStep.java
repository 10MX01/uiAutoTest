package com.uiauto.common.model;

import lombok.Builder;
import lombok.Data;

/**
 * 测试步骤（阶段1：自然语言解析后的结构化JSON）
 */
@Data
@Builder
public class TestStep {

    /**
     * 步骤序号
     */
    private Integer stepNumber;

    /**
     * 操作类型：navigate/click/fill/assert/assert_url/wait/select
     */
    private String action;

    /**
     * 目标元素的中文描述（不包含选择器）
     */
    private String target;

    /**
     * 操作值（URL、输入文本等）
     */
    private String value;

    /**
     * 步骤描述
     */
    private String description;
}
