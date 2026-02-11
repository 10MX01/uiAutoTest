package com.uiauto.testScript.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例简单响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseSimpleResponse {

    /**
     * 唯一标识ID
     */
    private Long uniqueId;

    /**
     * 测试用例名称
     */
    private String name;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 状态
     */
    private String status;

    /**
     * 自动化状态
     */
    private String automationStatus;
}
