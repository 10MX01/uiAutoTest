package com.uiauto.testcase.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.dto.ProjectCreateRequest;
import com.uiauto.testcase.dto.ProjectUpdateRequest;
import com.uiauto.testcase.service.ProjectService;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.vo.ProjectResponse;
import com.uiauto.testcase.vo.TestCaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 项目Controller
 */
@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TestCaseService testCaseService;

    /**
     * 创建项目
     */
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ProjectCreateRequest request) {
        log.info("接收到创建项目请求: {}", request.getName());
        ProjectResponse response = projectService.create(request);
        return ApiResponse.success("项目创建成功", response.getUniqueId());
    }

    /**
     * 查询所有项目
     */
    @GetMapping
    public ApiResponse<List<ProjectResponse>> listAll() {
        log.info("查询所有项目");
        List<ProjectResponse> responses = projectService.listAll();
        return ApiResponse.success(responses);
    }

    /**
     * 根据ID查询项目详情
     */
    @GetMapping("/{uniqueId}")
    public ApiResponse<ProjectResponse> getById(@PathVariable Long uniqueId) {
        log.info("查询项目详情: {}", uniqueId);
        ProjectResponse response = projectService.getById(uniqueId);
        return ApiResponse.success(response);
    }

    /**
     * 更新项目
     */
    @PostMapping("/{uniqueId}")
    public ApiResponse<Void> update(
            @PathVariable Long uniqueId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        log.info("接收到更新项目请求: {}", uniqueId);
        projectService.update(uniqueId, request);
        return ApiResponse.success("项目更新成功", null);
    }

    /**
     * 删除项目
     */
    @PostMapping("/{uniqueId}/delete")
    public ApiResponse<Void> delete(@PathVariable Long uniqueId) {
        log.info("接收到删除项目请求: {}", uniqueId);
        projectService.delete(uniqueId);
        return ApiResponse.success("项目已删除", null);
    }

    /**
     * 查询项目下的测试用例
     */
    @GetMapping("/{uniqueId}/test-cases")
    public ApiResponse<List<TestCaseResponse>> getTestCases(@PathVariable Long uniqueId) {
        log.info("查询项目下的测试用例: {}", uniqueId);
        List<TestCaseResponse> responses = testCaseService.listByProjectId(uniqueId);
        return ApiResponse.success(responses);
    }
}
