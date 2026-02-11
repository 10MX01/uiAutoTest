package com.uiauto.testcase.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建测试用例请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseCreateRequest {

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
     * 测试步骤（JSON格式，由AI生成）
     */
    private String stepsJson;

    /**
     * 是否由AI生成
     */
    @Builder.Default
    private Boolean isAiGenerated = false;

    /**
     * 预期结果
     */
    private String expectedResult;

    /**
     * 优先级
     */
    @Builder.Default
    private String priority = "P2";

    /**
     * 状态
     */
    @Builder.Default
    private String status = "DRAFT";

    /**
     * 自动化状态
     */
    @Builder.Default
    private String automationStatus = "MANUAL";

    /**
     * 前置依赖用例ID列表
     */
    private List<Long> prerequisiteIds;
}
