package com.uiauto.testcase.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.service.DependencyService;
import com.uiauto.testcase.vo.TestCaseDependencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 测试用例依赖关系Controller
 */
@Slf4j
@RestController
@RequestMapping("/test-cases")
@RequiredArgsConstructor
public class DependencyController {

    private final DependencyService dependencyService;

    /**
     * 添加前置依赖
     */
    @PostMapping("/{uniqueId}/dependencies")
    public ApiResponse<Void> addDependency(
            @PathVariable Long uniqueId,
            @Valid @RequestBody AddDependencyRequest request) {
        log.info("接收到添加依赖请求: testCaseId={}, prerequisites={}",
                uniqueId, request.getPrerequisiteIds());
        dependencyService.addDependency(uniqueId, request.getPrerequisiteIds(), request.getDependencyType());
        return ApiResponse.success("依赖关系添加成功", null);
    }

    /**
     * 查询测试用例的依赖列表
     */
    @GetMapping("/{uniqueId}/dependencies")
    public ApiResponse<List<TestCaseDependencyResponse>> getDependencies(@PathVariable Long uniqueId) {
        log.info("查询依赖列表: testCaseId={}", uniqueId);
        List<TestCaseDependencyResponse> dependencies = dependencyService.getPrerequisites(uniqueId);
        return ApiResponse.success(dependencies);
    }

    /**
     * 移除依赖
     */
    @PostMapping("/{uniqueId}/dependencies/remove")
    public ApiResponse<Void> removeDependency(
            @PathVariable Long uniqueId,
            @Valid @RequestBody RemoveDependencyRequest request) {
        log.info("接收到移除依赖请求: testCaseId={}, prerequisites={}",
                uniqueId, request.getPrerequisiteIds());
        dependencyService.removeDependency(uniqueId, request.getPrerequisiteIds());
        return ApiResponse.success("依赖关系移除成功", null);
    }

    /**
     * 计算执行顺序
     */
    @PostMapping("/calculate-order")
    public ApiResponse<List<Long>> calculateExecutionOrder(
            @Valid @RequestBody CalculateOrderRequest request) {
        log.info("接收到计算执行顺序请求: testCaseIds={}", request.getTestCaseIds());
        List<Long> orderedIds = dependencyService.calculateExecutionOrder(request.getTestCaseIds());
        return ApiResponse.success("执行顺序计算成功", orderedIds);
    }

    /**
     * 查询依赖当前用例的其他用例
     */
    @GetMapping("/{uniqueId}/dependents")
    public ApiResponse<List<TestCaseDependencyResponse>> getDependents(@PathVariable Long uniqueId) {
        log.info("查询后续依赖: testCaseId={}", uniqueId);
        List<TestCaseDependencyResponse> dependents = dependencyService.getDependents(uniqueId);
        return ApiResponse.success(dependents);
    }

    /**
     * 添加依赖请求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AddDependencyRequest {
        /**
         * 前置用例ID列表
         */
        @javax.validation.constraints.NotEmpty(message = "前置用例ID列表不能为空")
        private List<Long> prerequisiteIds;

        /**
         * 依赖类型（HARD/SOFT）
         */
        @javax.validation.constraints.NotBlank(message = "依赖类型不能为空")
        @javax.validation.constraints.Pattern(regexp = "HARD|SOFT", message = "依赖类型必须是HARD或SOFT")
        private String dependencyType;
    }

    /**
     * 移除依赖请求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RemoveDependencyRequest {
        /**
         * 要移除的前置用例ID列表
         */
        @javax.validation.constraints.NotEmpty(message = "前置用例ID列表不能为空")
        private List<Long> prerequisiteIds;
    }

    /**
     * 计算执行顺序请求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CalculateOrderRequest {
        /**
         * 测试用例ID列表
         */
        @javax.validation.constraints.NotEmpty(message = "测试用例ID列表不能为空")
        private List<Long> testCaseIds;
    }
}
