package com.uiauto.aiscript.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 执行详情响应VO
 */
@Data
@Builder
public class ExecutionDetailResponse {

    private Long executionId;
    private Long testCaseId;
    private String executionUrl;
    private String status;
    private Long duration;
    private String generatedScript;
    private List<Object> stepsResult;
    private List<String> screenshots;
    private String errorMessage;
    private Long executedBy;
    private String createdTime;
}
