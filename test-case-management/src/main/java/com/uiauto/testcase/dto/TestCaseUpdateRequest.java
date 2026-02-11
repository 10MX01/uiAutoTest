package com.uiauto.testcase.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新测试用例请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseUpdateRequest {

    /**
     * 用例编号
     */
    private String caseNumber;

    /**
     * 测试用例名称
     */
    @NotBlank(message = "测试用例名称不能为空")
    private String name;

    /**
     * 测试用例描述
     */
    private String description;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 测试步骤（自然语言）
     */
    @NotBlank(message = "测试步骤不能为空")
    private String stepsText;

    /**
     * 测试步骤（JSON格式）
     */
    private String stepsJson;

    /**
     * 是否由AI生成
     */
    private Boolean isAiGenerated;

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
     * 自动化状态
     */
    private String automationStatus;

    /**
     * 前置依赖用例ID列表
     */
    private List<Long> prerequisiteIds;
}
