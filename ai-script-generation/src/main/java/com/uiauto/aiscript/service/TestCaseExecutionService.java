package com.uiauto.aiscript.service;

import com.uiauto.testcase.entity.TestCaseExecutionEntity;

import java.util.List;

/**
 * 测试用例执行Service接口
 */
public interface TestCaseExecutionService {

    /**
     * 执行测试用例
     *
     * @param testCaseId 测试用例ID
     * @param overrideUrl 覆盖URL（可选，用于切换测试环境）
     * @param executedBy 执行人ID
     * @return 执行记录
     */
    TestCaseExecutionEntity executeTestCase(
            Long testCaseId,
            String overrideUrl,
            Long executedBy
    );

    /**
     * 查询执行记录
     */
    TestCaseExecutionEntity getExecutionById(Long executionId);

    /**
     * 查询测试用例的所有执行记录
     */
    List<TestCaseExecutionEntity> getExecutionsByTestCaseId(Long testCaseId);
}
