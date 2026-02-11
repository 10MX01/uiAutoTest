package com.uiauto.testcase.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.entity.TestCaseExecutionEntity;
import com.uiauto.testcase.repository.TestCaseExecutionRepository;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.testcase.service.TestRecordService;
import com.uiauto.testcase.vo.StepResult;
import com.uiauto.testcase.vo.TestRecordDetailResponse;
import com.uiauto.testcase.vo.TestRecordResponse;
import com.uiauto.testcase.vo.TestRecordStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 测试记录Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRecordServiceImpl implements TestRecordService {

    private final TestCaseExecutionRepository repository;
    private final TestCaseRepository testCaseRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Page<TestRecordResponse> listRecords(Long projectId, Long testCaseId, String status, Long executorId,
                                                  Date startTime, Date endTime, String search,
                                                  int page, int size) {
        log.info("查询测试记录列表: projectId={}, testCaseId={}, status={}, executorId={}, search={}, page={}, size={}",
                projectId, testCaseId, status, executorId, search, page, size);

        // 创建分页对象，按创建时间倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        // 构建搜索模式（添加通配符）
        String searchPattern = null;
        if (search != null && !search.trim().isEmpty()) {
            searchPattern = "%" + search.trim() + "%";
        } else {
            searchPattern = ""; // 空字符串表示不搜索
        }

        // Repository 直接返回 TestRecordResponse
        return repository.findByConditions(
                projectId, testCaseId, status, executorId, startTime, endTime, search, searchPattern, pageable);
    }

    @Override
    public TestRecordDetailResponse getDetail(Long id) {
        log.info("查询测试记录详情: id={}", id);

        TestCaseExecutionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("测试记录不存在: " + id));

        return convertToDetailResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除测试记录: id={}", id);

        if (!repository.existsById(id)) {
            throw new RuntimeException("测试记录不存在: " + id);
        }

        repository.deleteById(id);
        log.info("测试记录删除成功: id={}", id);
    }

    @Override
    public TestRecordStatistics getStatistics() {
        log.info("查询测试记录统计数据");

        TestRecordStatistics statistics = repository.getStatistics();
        if (statistics == null) {
            return new TestRecordStatistics(0L, 0L, 0L, 0L);
        }

        return statistics;
    }

    /**
     * 转换为详情响应对象
     */
    private TestRecordDetailResponse convertToDetailResponse(TestCaseExecutionEntity entity) {
        TestRecordDetailResponse response = new TestRecordDetailResponse();

        // 基本信息
        response.setUniqueId(entity.getUniqueId());
        response.setExecutionUrl(entity.getExecutionUrl());
        response.setStatus(entity.getStatus());
        response.setDuration(entity.getDuration());
        response.setExecutedBy(entity.getExecutedBy());
        response.setCreatedTime(entity.getCreatedTime());
        response.setErrorMessage(entity.getErrorMessage());

        // 从测试用例获取信息
        if (entity.getTestCaseId() != null) {
            TestCaseEntity testCase = testCaseRepository.findById(entity.getTestCaseId()).orElse(null);
            if (testCase != null) {
                response.setCaseNumber(testCase.getCaseNumber());
                response.setCaseName(testCase.getName());
            }
        }

        // 执行人姓名（临时方案）
        if (entity.getExecutedBy() != null) {
            response.setExecutorName("用户" + entity.getExecutedBy());
        }

        // 生成的脚本
        response.setGeneratedScript(entity.getGeneratedScript());

        // 解析步骤结果
        if (entity.getStepsResult() != null && !entity.getStepsResult().isEmpty()) {
            try {
                List<StepResult> steps = objectMapper.readValue(entity.getStepsResult(),
                        new TypeReference<List<StepResult>>() {});
                response.setStepsResult(steps);
            } catch (Exception e) {
                log.error("解析步骤结果失败: {}", e.getMessage());
                response.setStepsResult(new ArrayList<>());
            }
        } else {
            response.setStepsResult(new ArrayList<>());
        }

        return response;
    }
}
