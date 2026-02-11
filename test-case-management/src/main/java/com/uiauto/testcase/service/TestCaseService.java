package com.uiauto.testcase.service;

import com.uiauto.testcase.dto.TestCaseCreateRequest;
import com.uiauto.testcase.dto.TestCaseUpdateRequest;
import com.uiauto.testcase.vo.TestCaseResponse;

import java.util.List;

/**
 * 测试用例Service接口
 */
public interface TestCaseService {

    /**
     * 创建测试用例
     */
    Long create(TestCaseCreateRequest request);

    /**
     * 更新测试用例
     */
    void update(Long uniqueId, TestCaseUpdateRequest request);

    /**
     * 删除测试用例（软删除）
     */
    void delete(Long uniqueId);

    /**
     * 根据ID查询测试用例
     */
    TestCaseResponse getById(Long uniqueId);

    /**
     * 查询所有测试用例（未删除的）
     */
    List<TestCaseResponse> listAll();

    /**
     * 根据状态查询测试用例
     */
    List<TestCaseResponse> listByStatus(String status);

    /**
     * 根据优先级查询测试用例
     */
    List<TestCaseResponse> listByPriority(String priority);

    /**
     * 根据创建人查询测试用例
     */
    List<TestCaseResponse> listByCreator(Long createdBy);

    /**
     * 搜索测试用例（按名称或描述）
     */
    List<TestCaseResponse> search(String keyword);

    /**
     * 根据项目ID查询测试用例
     */
    List<TestCaseResponse> listByProjectId(Long projectId);

    /**
     * 查询所有测试用例（包含最新执行记录）
     */
    List<TestCaseResponse> listAllWithExecution();

    /**
     * 根据状态查询测试用例（包含最新执行记录）
     */
    List<TestCaseResponse> listByStatusWithExecution(String status);

    /**
     * 查询所有测试用例按项目分组（包含最新执行记录）
     * 项目按最后更新时间排序，最近更新的项目在前
     */
    List<TestCaseResponse> listAllGroupedByProject();
}
