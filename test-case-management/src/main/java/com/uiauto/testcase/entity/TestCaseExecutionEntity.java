package com.uiauto.testcase.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 测试用例执行记录实体
 */
@Data
@Entity
@Table(name = "test_case_executions")
public class TestCaseExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unique_id")
    private Long uniqueId;

    /**
     * 测试用例ID
     */
    @Column(name = "test_case_id")
    private Long testCaseId;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 执行时的URL
     */
    @Column(name = "execution_url")
    private String executionUrl;

    /**
     * 执行状态：SUCCESS/FAILED/SKIPPED
     */
    @Column(name = "status")
    private String status;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "duration")
    private Long duration;

    /**
     * 生成的脚本（包含选择器）
     */
    @Column(name = "generated_script", columnDefinition = "JSON")
    private String generatedScript;

    /**
     * 每个步骤的执行结果（JSON）
     */
    @Column(name = "steps_result", columnDefinition = "JSON")
    private String stepsResult;

    /**
     * 截图数组（Base64或路径，JSON）
     */
    @Column(name = "screenshots", columnDefinition = "JSON")
    private String screenshots;

    /**
     * 错误信息
     */
    @Column(name = "error_message")
    private String errorMessage;

    /**
     * 执行人ID
     */
    @Column(name = "executed_by")
    private Long executedBy;

    /**
     * 创建时间（执行时间）
     */
    @Column(name = "created_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
}
