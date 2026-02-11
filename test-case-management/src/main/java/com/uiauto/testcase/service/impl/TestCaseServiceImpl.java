package com.uiauto.testcase.service.impl;

import com.google.gson.Gson;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.service.NaturalLanguageParser;
import com.uiauto.testcase.dto.TestCaseCreateRequest;
import com.uiauto.testcase.dto.TestCaseUpdateRequest;
import com.uiauto.testcase.entity.*;
import com.uiauto.testcase.repository.*;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.vo.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试用例Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProjectRepository projectRepository;
    private final TestCaseDependencyRepository dependencyRepository;
    private final NaturalLanguageParser naturalLanguageParser;

    private final Gson gson = new Gson();

    @Override
    @Transactional
    public Long create(TestCaseCreateRequest request) {
        log.info("创建测试用例: {}", request.getName());

        // 1. 验证用例编号在项目内的唯一性
        if (request.getCaseNumber() != null && !request.getCaseNumber().trim().isEmpty()
            && request.getProjectId() != null) {
            if (testCaseRepository.existsByCaseNumberAndProjectId(
                    request.getCaseNumber().trim(), request.getProjectId())) {
                throw new RuntimeException(
                    String.format("用例编号[%s]在该项目中已存在", request.getCaseNumber()));
            }
        }

        // 2. 如果stepsJson为空，自动调用AI解析stepsText
        String stepsJson = request.getStepsJson();
        if ((stepsJson == null || stepsJson.isEmpty()) && request.getStepsText() != null && !request.getStepsText().isEmpty()) {
            try {
                log.info("开始AI解析自然语言测试步骤...");
                List<TestStep> steps = naturalLanguageParser.parseSteps(request.getStepsText());
                stepsJson = gson.toJson(steps);
                log.info("AI解析成功，生成{}个测试步骤", steps.size());
            } catch (Exception e) {
                log.error("AI解析失败，将保存原始自然语言", e);
                // AI解析失败时，stepsJson保持为null，只保存stepsText
            }
        }

        // 3. 创建测试用例实体
        TestCaseEntity entity = TestCaseEntity.builder()
                .caseNumber(request.getCaseNumber())
                .name(request.getName())
                .description(request.getDescription())
                .projectId(request.getProjectId())
                .stepsText(request.getStepsText())
                .stepsJson(stepsJson)
                .isAiGenerated(stepsJson != null) // 如果成功解析，标记为AI生成
                .expectedResult(request.getExpectedResult())
                .priority(request.getPriority())
                .status(request.getStatus())
                .automationStatus(request.getAutomationStatus() != null ? request.getAutomationStatus() : "MANUAL")
                .build();

        // 设置审计字段
        entity.setCreatedBy(Long.valueOf(1L)); // TODO: 从当前登录用户获取
        entity.setUpdatedBy(Long.valueOf(1L));

        // 3. 保存测试用例
        TestCaseEntity saved = testCaseRepository.save(entity);

        // 4. 关联前置依赖
        if (request.getPrerequisiteIds() != null && !request.getPrerequisiteIds().isEmpty()) {
            for (Long prereqId : request.getPrerequisiteIds()) {
                TestCaseDependencyEntity dependency = TestCaseDependencyEntity.builder()
                        .testCaseId(saved.getUniqueId())
                        .prerequisiteId(prereqId)
                        .dependencyType("HARD")
                        .build();
                dependency.setCreatedBy(Long.valueOf(1L));
                dependency.setUpdatedBy(Long.valueOf(1L));
                dependencyRepository.save(dependency);
            }
        }

        log.info("测试用例创建成功, ID: {}", saved.getUniqueId());
        return saved.getUniqueId();
    }

    @Override
    @Transactional
    public void update(Long uniqueId, TestCaseUpdateRequest request) {
        log.info("更新测试用例: {}", uniqueId);

        // 1. 查询原实体
        TestCaseEntity entity = testCaseRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + uniqueId));

        // 2. 更新字段
        entity.setCaseNumber(request.getCaseNumber());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (request.getProjectId() != null) {
            entity.setProjectId(request.getProjectId());
        }
        entity.setStepsText(request.getStepsText());
        entity.setStepsJson(request.getStepsJson());
        if (request.getIsAiGenerated() != null) {
            entity.setIsAiGenerated(request.getIsAiGenerated());
        }
        entity.setExpectedResult(request.getExpectedResult());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getAutomationStatus() != null) {
            entity.setAutomationStatus(request.getAutomationStatus());
        }
        entity.setUpdatedBy(1L); // TODO: 从当前登录用户获取
        entity.setUpdatedTime(LocalDateTime.now());

        // 3. 保存更新
        testCaseRepository.save(entity);

        log.info("测试用例更新成功: {}", uniqueId);
    }

    @Override
    @Transactional
    public void delete(Long uniqueId) {
        log.info("删除测试用例: {}", uniqueId);

        TestCaseEntity entity = testCaseRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + uniqueId));

        // 物理删除
        testCaseRepository.delete(entity);

        log.info("测试用例已删除: {}", uniqueId);
    }

    @Override
    public TestCaseResponse getById(Long uniqueId) {
        TestCaseEntity entity = testCaseRepository.findById(uniqueId)
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + uniqueId));

        return convertToResponse(entity);
    }

    @Override
    public List<TestCaseResponse> listAll() {
        List<TestCaseEntity> entities = testCaseRepository.findAll();
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listByStatus(String status) {
        List<TestCaseEntity> entities = testCaseRepository.findByStatus(status);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listByPriority(String priority) {
        List<TestCaseEntity> entities = testCaseRepository.findByPriority(priority);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listByCreator(Long createdBy) {
        List<TestCaseEntity> entities = testCaseRepository.findByCreatedBy(createdBy);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> search(String keyword) {
        List<TestCaseEntity> entities = testCaseRepository.searchByKeyword(keyword);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listByProjectId(Long projectId) {
        List<TestCaseEntity> entities = testCaseRepository.findByProjectId(projectId);
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listAllWithExecution() {
        List<Object[]> results = testCaseRepository.findAllWithLatestExecution();
        return results.stream()
                .map(this::convertToResponseWithExecution)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listByStatusWithExecution(String status) {
        List<Object[]> results = testCaseRepository.findByStatusWithLatestExecution(status);
        return results.stream()
                .map(this::convertToResponseWithExecution)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseResponse> listAllGroupedByProject() {
        log.info("开始查询按项目分组的测试用例");
        List<Object[]> results = testCaseRepository.findAllGroupedByProject();
        log.info("查询到 {} 条测试用例记录", results == null ? 0 : results.size());
        if (results != null && !results.isEmpty()) {
            log.info("第一条记录的数组长度: {}", results.get(0).length);
        }
        List<TestCaseResponse> response = results.stream()
                .map(this::convertToResponseWithExecutionAndProject)
                .collect(Collectors.toList());
        log.info("转换后的响应数量: {}", response.size());
        return response;
    }

    /**
     * 从Object[]转换为响应VO（包含执行记录）
     */
    private TestCaseResponse convertToResponseWithExecution(Object[] row) {
        // 解析字段 - 处理BigInteger类型转换
        Long uniqueId = convertToLong(row[0]);
        String caseNumber = (String) row[1];
        String name = (String) row[2];
        String description = (String) row[3];
        String stepsText = (String) row[4];
        String stepsJson = (String) row[5];
        String expectedResult = (String) row[6];
        String priority = (String) row[7];
        String status = (String) row[8];
        Long createdBy = convertToLong(row[9]);
        Long updatedBy = row[10] != null ? convertToLong(row[10]) : null;
        LocalDateTime createdTime = convertToLocalDateTime(row[11]);
        LocalDateTime updatedTime = convertToLocalDateTime(row[12]);
        Long executedBy = row[13] != null ? convertToLong(row[13]) : null; // 来自test_case_executions
        LocalDateTime executionTime = convertToLocalDateTime(row[14]); // 来自test_case_executions

        // 查询项目信息
        List<ProjectSimpleResponse> projects = new ArrayList<>();
        TestCaseEntity entity = testCaseRepository.findById(uniqueId).orElse(null);
        if (entity != null && entity.getProjectId() != null) {
            try {
                ProjectEntity project = projectRepository.findById(entity.getProjectId()).orElse(null);
                if (project != null) {
                    projects.add(ProjectSimpleResponse.builder()
                            .uniqueId(project.getUniqueId())
                            .name(project.getName())
                            .code(project.getCode())
                            .build());
                }
            } catch (Exception e) {
                log.warn("查询项目失败: {}", entity.getProjectId(), e);
            }
        }

        return TestCaseResponse.builder()
                .uniqueId(uniqueId)
                .caseNumber(caseNumber)
                .name(name)
                .description(description)
                .stepsText(stepsText)
                .stepsJson(stepsJson)
                .expectedResult(expectedResult)
                .priority(priority)
                .status(status)
                .executedBy(executedBy)
                .executionTime(executionTime)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .createdTime(createdTime)
                .updatedTime(updatedTime)
                .projects(projects)
                .dependencies(new ArrayList<>())
                .build();
    }

    /**
     * 从Object[]转换为响应VO（包含执行记录和项目信息）
     * 用于按项目分组的查询
     */
    private TestCaseResponse convertToResponseWithExecutionAndProject(Object[] row) {
        // 解析字段 - 处理BigInteger类型转换
        Long uniqueId = convertToLong(row[0]);
        String caseNumber = (String) row[1];
        String name = (String) row[2];
        String description = (String) row[3];
        String stepsText = (String) row[4];
        String stepsJson = (String) row[5];
        String expectedResult = (String) row[6];
        String priority = (String) row[7];
        String status = (String) row[8];
        Long projectId = convertToLong(row[9]);
        String projectName = (String) row[10];
        Long createdBy = convertToLong(row[11]);
        Long updatedBy = row[12] != null ? convertToLong(row[12]) : null;
        LocalDateTime createdTime = convertToLocalDateTime(row[13]);
        LocalDateTime updatedTime = convertToLocalDateTime(row[14]);
        Long executedBy = row[15] != null ? convertToLong(row[15]) : null; // 来自test_case_executions
        LocalDateTime executionTime = convertToLocalDateTime(row[16]); // 来自test_case_executions

        // 构建项目信息
        List<ProjectSimpleResponse> projects = new ArrayList<>();
        if (projectId != null && projectName != null) {
            projects.add(ProjectSimpleResponse.builder()
                    .uniqueId(projectId)
                    .name(projectName)
                    .code(null) // 分组查询不返回code
                    .build());
        }

        return TestCaseResponse.builder()
                .uniqueId(uniqueId)
                .caseNumber(caseNumber)
                .name(name)
                .description(description)
                .stepsText(stepsText)
                .stepsJson(stepsJson)
                .expectedResult(expectedResult)
                .priority(priority)
                .status(status)
                .executedBy(executedBy)
                .executionTime(executionTime)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .createdTime(createdTime)
                .updatedTime(updatedTime)
                .projects(projects)
                .dependencies(new ArrayList<>())
                .build();
    }

    /**
     * 将对象转换为Long（处理BigInteger）
     */
    private Long convertToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof BigInteger) {
            return ((BigInteger) obj).longValue();
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            log.warn("无法转换为Long: {}", obj);
            return null;
        }
    }

    /**
     * 将对象转换为LocalDateTime（处理Timestamp等类型）
     */
    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate().atStartOfDay();
        }
        if (obj instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) obj).getTime()).toLocalDateTime();
        }
        try {
            // 尝试其他转换方式
            return null;
        } catch (Exception e) {
            log.warn("无法转换为LocalDateTime: {}", obj);
            return null;
        }
    }

    /**
     * 转换为响应VO
     */
    private TestCaseResponse convertToResponse(TestCaseEntity entity) {
        // 转换项目（基于单个projectId）
        List<ProjectSimpleResponse> projects = new ArrayList<>();
        if (entity.getProjectId() != null) {
            try {
                ProjectEntity project = projectRepository.findById(entity.getProjectId()).orElse(null);
                if (project != null) {
                    projects.add(ProjectSimpleResponse.builder()
                            .uniqueId(project.getUniqueId())
                            .name(project.getName())
                            .code(project.getCode())
                            .build());
                }
            } catch (Exception e) {
                log.warn("查询项目失败: {}", entity.getProjectId(), e);
            }
        }

        // 转换依赖关系
        List<TestCaseDependencyResponse> dependencies = entity.getDependencies().stream()
                .map(d -> {
                    // 查询前置用例信息
                    return testCaseRepository.findById(d.getPrerequisiteId())
                            .map(prereq -> TestCaseDependencyResponse.builder()
                                    .uniqueId(d.getUniqueId())
                                    .prerequisiteId(prereq.getUniqueId())
                                    .prerequisiteName(prereq.getName())
                                    .dependencyType(d.getDependencyType())
                                    .dependencyTypeDesc("HARD".equals(d.getDependencyType()) ? "强依赖" : "弱依赖")
                                    .build())
                            .orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return TestCaseResponse.builder()
                .uniqueId(entity.getUniqueId())
                .caseNumber(entity.getCaseNumber())
                .name(entity.getName())
                .description(entity.getDescription())
                .stepsText(entity.getStepsText())
                .stepsJson(entity.getStepsJson())
                .expectedResult(entity.getExpectedResult())
                .priority(entity.getPriority())
                .status(entity.getStatus())
//                .executedBy(entity.getExecutedBy())
//                .executionTime(entity.getExecutionTime())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdTime(entity.getCreatedTime())
                .updatedTime(entity.getUpdatedTime())
                .projects(projects)
                .dependencies(dependencies)
                .build();
    }
}
