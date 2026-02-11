package com.uiauto.testcase.service;

import com.uiauto.testcase.vo.TestCaseDependencyResponse;

import java.util.List;

/**
 * 测试用例依赖关系服务接口
 */
public interface DependencyService {

    /**
     * 添加前置依赖
     *
     * @param testCaseId      测试用例ID
     * @param prerequisiteIds 前置用例ID列表
     * @param dependencyType  依赖类型（HARD/SOFT）
     */
    void addDependency(Long testCaseId, List<Long> prerequisiteIds, String dependencyType);

    /**
     * 移除依赖
     *
     * @param testCaseId      测试用例ID
     * @param prerequisiteIds 要移除的前置用例ID列表
     */
    void removeDependency(Long testCaseId, List<Long> prerequisiteIds);

    /**
     * 计算测试用例的执行顺序（拓扑排序）
     *
     * @param testCaseIds 测试用例ID列表
     * @return 按依赖顺序排列的用例ID列表
     */
    List<Long> calculateExecutionOrder(List<Long> testCaseIds);

    /**
     * 查询测试用例的前置依赖
     *
     * @param testCaseId 测试用例ID
     * @return 前置依赖列表
     */
    List<TestCaseDependencyResponse> getPrerequisites(Long testCaseId);

    /**
     * 查询依赖当前用例的其他用例（后续依赖）
     *
     * @param testCaseId 测试用例ID
     * @return 后续依赖列表
     */
    List<TestCaseDependencyResponse> getDependents(Long testCaseId);
}
