package com.uiauto.aiscript.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 执行响应VO
 */
@Data
@Builder
public class ExecutionResponse {

    private Long executionId;
    private Long testCaseId;
    private String executionUrl;
    private String status;
    private Long duration;
    private String errorMessage;
    private String createdTime;
}
