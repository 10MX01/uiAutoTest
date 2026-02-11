package com.uiauto.aiscript.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 测试用例执行请求DTO
 */
@Data
public class ExecutionRequest {

    /**
     * 测试用例ID
     */
    @NotNull(message = "测试用例ID不能为空")
    private Long testCaseId;

    /**
     * 覆盖URL（可选，用于切换测试环境）
     */
    private String overrideUrl;

    /**
     * 执行配置
     */
    private ExecutionConfig config;

    /**
     * 执行配置
     */
    @Data
    public static class ExecutionConfig {
        private boolean headless = true;
        private boolean screenshot = true;
        private int timeout = 30000;
        private boolean screenshotOnFailure = true;
    }
}
