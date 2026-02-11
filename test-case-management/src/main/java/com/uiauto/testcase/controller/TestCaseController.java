package com.uiauto.testcase.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.dto.TestCaseCreateRequest;
import com.uiauto.testcase.dto.TestCaseUpdateRequest;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.vo.TestCaseResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用例Controller
 */
@Slf4j
@RestController
@RequestMapping("/test-cases")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    /**
     * 创建测试用例
     */
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TestCaseCreateRequest request) {
        log.info("接收到创建测试用例请求: {}", request.getName());
        Long id = testCaseService.create(request);
        return ApiResponse.success("测试用例创建成功", id);
    }

    /**
     * 更新测试用例
     */
    @PostMapping("/{uniqueId}")
    public ApiResponse<Void> update(
            @PathVariable Long uniqueId,
            @Valid @RequestBody TestCaseUpdateRequest request) {
        log.info("接收到更新测试用例请求: {}", uniqueId);
        testCaseService.update(uniqueId, request);
        return ApiResponse.success("测试用例更新成功", null);
    }

    /**
     * 删除测试用例（软删除）
     */
    @PostMapping("/{uniqueId}/delete")
    public ApiResponse<Void> delete(@PathVariable Long uniqueId) {
        log.info("接收到删除测试用例请求: {}", uniqueId);
        testCaseService.delete(uniqueId);
        return ApiResponse.success("测试用例已删除", null);
    }

    /**
     * 查询所有测试用例
     */
    @GetMapping
    public ApiResponse<List<TestCaseResponse>> listAll() {
        log.info("查询所有测试用例（包含最新执行记录）");
        List<TestCaseResponse> responses = testCaseService.listAllWithExecution();
        return ApiResponse.success(responses);
    }

    /**
     * 根据状态查询测试用例
     */
    @GetMapping("/by-status")
    public ApiResponse<List<TestCaseResponse>> listByStatus(@RequestParam String status) {
        log.info("按状态查询测试用例（包含最新执行记录）: {}", status);
        List<TestCaseResponse> responses = testCaseService.listByStatusWithExecution(status);
        return ApiResponse.success(responses);
    }

    /**
     * 根据优先级查询测试用例
     */
    @GetMapping("/by-priority")
    public ApiResponse<List<TestCaseResponse>> listByPriority(@RequestParam String priority) {
        log.info("按优先级查询测试用例: {}", priority);
        List<TestCaseResponse> responses = testCaseService.listByPriority(priority);
        return ApiResponse.success(responses);
    }

    /**
     * 根据创建人查询测试用例
     */
    @GetMapping("/by-creator")
    public ApiResponse<List<TestCaseResponse>> listByCreator(@RequestParam Long createdBy) {
        log.info("按创建人查询测试用例: {}", createdBy);
        List<TestCaseResponse> responses = testCaseService.listByCreator(createdBy);
        return ApiResponse.success(responses);
    }

    /**
     * 搜索测试用例
     */
    @GetMapping("/search")
    public ApiResponse<List<TestCaseResponse>> search(@RequestParam String keyword) {
        log.info("搜索测试用例: {}", keyword);
        List<TestCaseResponse> responses = testCaseService.search(keyword);
        return ApiResponse.success(responses);
    }

    /**
     * 查询所有测试用例按项目分组
     * 项目按最后更新时间排序，最近更新的项目在前
     */
    @GetMapping("/grouped-by-project")
    public ApiResponse<List<TestCaseResponse>> listAllGroupedByProject() {
        log.info("接收到查询所有测试用例（按项目分组）的请求");
        List<TestCaseResponse> responses = testCaseService.listAllGroupedByProject();
        log.info("返回响应数据，数量: {}", responses == null ? 0 : responses.size());
        return ApiResponse.success(responses);
    }

    /**
     * 根据ID查询测试用例
     */
    @GetMapping("/{uniqueId}")
    public ApiResponse<TestCaseResponse> getById(@PathVariable Long uniqueId) {
        log.info("查询测试用例: {}", uniqueId);
        TestCaseResponse response = testCaseService.getById(uniqueId);
        return ApiResponse.success(response);
    }
}
