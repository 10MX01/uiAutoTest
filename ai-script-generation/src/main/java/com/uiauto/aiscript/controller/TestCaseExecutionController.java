package com.uiauto.aiscript.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uiauto.aiscript.dto.ExecutionRequest;
import com.uiauto.testcase.entity.TestCaseExecutionEntity;
import com.uiauto.testcase.repository.TestCaseExecutionRepository;
import com.uiauto.aiscript.service.TestCaseExecutionService;
import com.uiauto.aiscript.vo.ExecutionDetailResponse;
import com.uiauto.aiscript.vo.ExecutionResponse;
import com.uiauto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用例执行Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/executions")
public class TestCaseExecutionController {

    @Autowired
    private TestCaseExecutionService executionService;

    @Autowired
    private TestCaseExecutionRepository executionRepository;

    private final Gson gson = new Gson();

    /**
     * 执行测试用例
     */
    @PostMapping("/execute")
    public ApiResponse<ExecutionResponse> execute(@Valid @RequestBody ExecutionRequest request) {
        try {
            Long userId = 1L; // TODO: 从上下文获取用户ID

            TestCaseExecutionEntity execution = executionService.executeTestCase(
                    request.getTestCaseId(),
                    request.getOverrideUrl(),
                    userId
            );

            ExecutionResponse response = ExecutionResponse.builder()
                    .executionId(execution.getUniqueId())
                    .testCaseId(execution.getTestCaseId())
                    .executionUrl(execution.getExecutionUrl())
                    .status(execution.getStatus())
                    .duration(execution.getDuration())
                    .errorMessage(execution.getErrorMessage())
                    .createdTime(execution.getCreatedTime().toString())
                    .build();

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("执行测试用例失败", e);
            return ApiResponse.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 查询测试用例的所有执行记录
     */
    @GetMapping("/testcase/{testCaseId}")
    public ApiResponse<List<ExecutionResponse>> getExecutionsByTestCaseId(@PathVariable Long testCaseId) {
        try {
            List<TestCaseExecutionEntity> executions = executionService.getExecutionsByTestCaseId(testCaseId);

            List<ExecutionResponse> responses = executions.stream()
                    .map(exec -> ExecutionResponse.builder()
                            .executionId(exec.getUniqueId())
                            .testCaseId(exec.getTestCaseId())
                            .executionUrl(exec.getExecutionUrl())
                            .status(exec.getStatus())
                            .duration(exec.getDuration())
                            .errorMessage(exec.getErrorMessage())
                            .createdTime(exec.getCreatedTime().toString())
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.success(responses);

        } catch (Exception e) {
            log.error("查询执行记录失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询执行详情
     */
    @GetMapping("/{executionId}/detail")
    public ApiResponse<ExecutionDetailResponse> getExecutionDetail(@PathVariable Long executionId) {
        try {
            TestCaseExecutionEntity execution = executionService.getExecutionById(executionId);

            // 解析步骤结果
            Type listType = new TypeToken<List<Object>>() {}.getType();
            List<Object> stepsResult = gson.fromJson(execution.getStepsResult(), listType);

            // 解析截图
            Type screenshotListType = new TypeToken<List<String>>() {}.getType();
            List<String> screenshots = gson.fromJson(execution.getScreenshots(), screenshotListType);

            ExecutionDetailResponse response = ExecutionDetailResponse.builder()
                    .executionId(execution.getUniqueId())
                    .testCaseId(execution.getTestCaseId())
                    .executionUrl(execution.getExecutionUrl())
                    .status(execution.getStatus())
                    .duration(execution.getDuration())
                    .generatedScript(execution.getGeneratedScript())
                    .stepsResult(stepsResult)
                    .screenshots(screenshots)
                    .errorMessage(execution.getErrorMessage())
                    .executedBy(execution.getExecutedBy())
                    .createdTime(execution.getCreatedTime().toString())
                    .build();

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("查询执行详情失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
}
