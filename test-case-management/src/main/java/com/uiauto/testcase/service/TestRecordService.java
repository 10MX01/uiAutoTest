package com.uiauto.testcase.service;

import com.uiauto.testcase.vo.TestRecordDetailResponse;
import com.uiauto.testcase.vo.TestRecordResponse;
import com.uiauto.testcase.vo.TestRecordStatistics;
import org.springframework.data.domain.Page;

import java.util.Date;

/**
 * 测试记录Service接口
 */
public interface TestRecordService {

    /**
     * 分页查询测试记录
     *
     * @param projectId   项目ID（可选）
     * @param testCaseId  用例ID（可选）
     * @param status      状态（可选）
     * @param executorId  执行人ID（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param search      搜索关键词（可选）
     * @param page        页码（从1开始）
     * @param size        每页大小
     * @return 分页结果
     */
    Page<TestRecordResponse> listRecords(Long projectId, Long testCaseId, String status, Long executorId,
                                          Date startTime, Date endTime, String search,
                                          int page, int size);

    /**
     * 根据ID查询记录详情
     *
     * @param id 记录ID
     * @return 详情数据
     */
    TestRecordDetailResponse getDetail(Long id);

    /**
     * 删除记录
     *
     * @param id 记录ID
     */
    void delete(Long id);

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    TestRecordStatistics getStatistics();
}
