package com.uiauto.testcase.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试用例响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResponse {

    /**
     * 唯一标识ID
     */
    private Long uniqueId;

    /**
     * 用例编号
     */
    private String caseNumber;

    /**
     * 测试用例名称
     */
    private String name;

    /**
     * 测试用例描述
     */
    private String description;

    /**
     * 测试步骤（自然语言）
     */
    private String stepsText;

    /**
     * 测试步骤（JSON格式）
     */
    private String stepsJson;

    /**
     * 预期结果
     */
    private String expectedResult;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 状态
     */
    private String status;

    /**
     * 执行人ID
     */
    private Long executedBy;

    /**
     * 最后执行时间
     */
    private LocalDateTime executionTime;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 关联的项目
     */
    private List<ProjectSimpleResponse> projects;

    /**
     * 前置依赖用例
     */
    private List<TestCaseDependencyResponse> dependencies;
}
