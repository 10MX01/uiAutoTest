package com.uiauto.testcase.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.service.TestRecordService;
import com.uiauto.testcase.vo.TestRecordDetailResponse;
import com.uiauto.testcase.vo.TestRecordResponse;
import com.uiauto.testcase.vo.TestRecordStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 测试记录Controller
 */
@Slf4j
@RestController
@RequestMapping("/test-records")
@RequiredArgsConstructor
public class TestRecordController {

    private final TestRecordService testRecordService;

    /**
     * 分页查询测试记录
     */
    @GetMapping
    public ApiResponse<Page<TestRecordResponse>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long testCaseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long executorId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("接收到查询测试记录请求: projectId={}, testCaseId={}, status={}, executorId={}, search={}, page={}, size={}",
                projectId, testCaseId, status, executorId, search, page, size);

        Page<TestRecordResponse> result = testRecordService.listRecords(
                projectId, testCaseId, status, executorId, startTime, endTime, search, page, size);

        return ApiResponse.success(result);
    }

    /**
     * 查询记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TestRecordDetailResponse> detail(@PathVariable Long id) {
        log.info("接收到查询测试记录详情请求: id={}", id);

        TestRecordDetailResponse detail = testRecordService.getDetail(id);

        return ApiResponse.success(detail);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("接收到删除测试记录请求: id={}", id);

        testRecordService.delete(id);

        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics/summary")
    public ApiResponse<TestRecordStatistics> getStatistics() {
        log.info("接收到查询测试记录统计请求");

        TestRecordStatistics statistics = testRecordService.getStatistics();

        return ApiResponse.success(statistics);
    }
}
