package com.uiauto.testcase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 测试用例依赖关系响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseDependencyResponse {

    /**
     * 唯一标识ID
     */
    private Long uniqueId;

    /**
     * 测试用例ID
     */
    private Long testCaseId;

    /**
     * 测试用例名称
     */
    private String testCaseName;

    /**
     * 前置测试用例ID
     */
    private Long prerequisiteId;

    /**
     * 前置测试用例名称
     */
    private String prerequisiteName;

    /**
     * 依赖类型
     */
    private String dependencyType;

    /**
     * 依赖类型描述
     */
    private String dependencyTypeDesc;

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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedTime;
}
