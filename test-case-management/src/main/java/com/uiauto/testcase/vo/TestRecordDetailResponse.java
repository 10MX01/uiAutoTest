package com.uiauto.testcase.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 测试记录详情响应DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestRecordDetailResponse extends TestRecordResponse {
    /**
     * 生成的脚本
     */
    private String generatedScript;

    /**
     * 步骤执行结果列表
     */
    private List<StepResult> stepsResult;

    /**
     * 错误信息
     */
    private String errorMessage;
}
