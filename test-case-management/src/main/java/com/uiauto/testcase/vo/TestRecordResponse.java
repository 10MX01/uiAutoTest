package com.uiauto.testcase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uiauto.common.ApiResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 测试记录响应DTO
 */
@Data
public class TestRecordResponse{
    /**
     * 记录ID
     */
    private Long uniqueId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 用例编号
     */
    private String caseNumber;

    /**
     * 用例名称
     */
    private String caseName;

    /**
     * 执行URL
     */
    private String executionUrl;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 执行人ID
     */
    private Long executedBy;

    /**
     * 执行人姓名
     */
    private String executorName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 无参构造函数
     */
    public TestRecordResponse() {
    }

    /**
     * 全参构造函数（用于JPQL查询）
     */
    public TestRecordResponse(Long uniqueId, Long projectId, String projectName,
                              String caseNumber, String caseName,
                              String executionUrl, String status, Long duration,
                              Long executedBy, String executorName, LocalDateTime createdTime) {
        this.uniqueId = uniqueId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.caseNumber = caseNumber;
        this.caseName = caseName;
        this.executionUrl = executionUrl;
        this.status = status;
        this.duration = duration;
        this.executedBy = executedBy;
        this.executorName = executorName;
        this.createdTime = createdTime;
    }
}
