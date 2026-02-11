package com.uiauto.testcase.vo;

import lombok.Data;

/**
 * 步骤执行结果
 */
@Data
public class StepResult {
    /**
     * 步骤序号
     */
    private Integer step;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 截图（Base64）
     */
    private String screenshot;
}
