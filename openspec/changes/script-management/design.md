# 设计文档：脚本管理模块（重构版）

## 1. 需求概述

### 1.1 核心需求
- **脚本生成方式**：仅支持两种方式
  - Excel导入测试用例后自动解析生成脚本
  - AI智能运行测试用例时自动生成（发现无所需脚本时）
- **脚本状态管理**：启用/禁用机制（不是版本控制）
- **脚本与测试用例关系**：1:N（一个测试用例可对应多个脚本，但只有一个启用）
- **AI生成失败处理**：保留失败记录，支持重试机制

### 1.2 关键规则
- ✅ 一个测试用例可以有多个脚本记录
- ✅ 同一测试用例的多个脚本中，只能有一个处于启用状态
- ✅ 运行测试用例时，优先查找并使用启用状态的脚本
- ✅ 如果该测试用例没有启用的脚本，则AI自动生成新脚本并启用
- ✅ 支持手动切换脚本的启用/禁用状态
- ✅ 不支持编辑脚本内容（删除后可重新生成）
- ✅ AI生成失败时保留记录，支持重试（最多3次）

---

## 2. 数据库设计

### 2.1 表结构设计

#### 2.1.1 test_scripts（测试脚本表）

**表说明**：存储测试脚本的基本信息、内容和状态

```sql
CREATE TABLE test_scripts (
  unique_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  script_name VARCHAR(255) NOT NULL COMMENT '脚本名称',
  script_description TEXT COMMENT '脚本描述',
  script_content LONGTEXT NOT NULL COMMENT '脚本内容',
  language VARCHAR(20) DEFAULT 'typescript' COMMENT '脚本语言：typescript/javascript',

  -- 生成方式（核心字段）
  generation_method VARCHAR(50) NOT NULL COMMENT '生成方式：EXCEL_IMPORT/AI_GENERATED',

  -- 启用状态（核心字段）
  enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用：同一测试用例只能有一个启用',

  -- AI生成相关字段
  ai_generation_status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT 'AI生成状态：SUCCESS/FAILED/PENDING',
  ai_retry_count INT DEFAULT 0 COMMENT 'AI重试次数',
  ai_error_message TEXT COMMENT 'AI生成失败错误信息',
  ai_model_used VARCHAR(100) COMMENT '使用的AI模型',
  ai_generation_time TIMESTAMP NULL COMMENT 'AI生成时间',

  -- 关联测试用例（必需字段）
  test_case_id BIGINT NOT NULL COMMENT '关联的测试用例ID',

  -- 分类和统计信息
  category VARCHAR(100) COMMENT '脚本分类',
  execution_count INT DEFAULT 0 COMMENT '执行次数',
  last_execution_time TIMESTAMP NULL COMMENT '最后执行时间',
  last_execution_result VARCHAR(20) COMMENT '最后执行结果：SUCCESS/FAILED/SKIPPED',

  -- 标准审计字段
  created_by BIGINT NOT NULL COMMENT '创建人ID（系统生成时为0）',
  updated_by BIGINT COMMENT '更新人ID',
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_script_name (script_name),
  INDEX idx_generation_method (generation_method),
  INDEX idx_enabled (enabled),
  INDEX idx_test_case_id (test_case_id),
  INDEX idx_ai_generation_status (ai_generation_status),
  INDEX idx_category (category),
  INDEX idx_created_by (created_by),

  UNIQUE KEY uk_test_case_enabled (test_case_id, enabled) COMMENT '确保同一测试用例只有一个启用脚本',
  FOREIGN KEY (test_case_id) REFERENCES test_cases(unique_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试脚本表';
```

**关键字段说明**：
- `generation_method`：生成方式，仅支持 `EXCEL_IMPORT`（Excel导入解析）和 `AI_GENERATED`（AI生成）
- `enabled`：启用状态，`TRUE` 表示启用，`FALSE` 表示禁用
- `test_case_id`：关联的测试用例ID，**必填字段**
- `ai_generation_status`：AI生成状态
  - `SUCCESS`：生成成功
  - `FAILED`：生成失败
  - `PENDING`：生成中
- `ai_retry_count`：重试次数（最多3次）
- `uk_test_case_enabled`：唯一约束，确保同一测试用例只有一个启用状态的脚本

#### 2.1.2 删除的表

**不再需要以下表**：
- ❌ `script_versions`（脚本版本历史表）- 新设计不需要版本控制
- ❌ `script_execution_previews`（脚本执行预览缓存表）- 简化设计，暂时不需要
- ❌ `script_permissions`（脚本权限表）- 简化设计，暂时不需要
- ❌ `script_snippets`（代码片段表）- 独立模块，后续单独设计

---

## 3. 类设计

### 3.1 Entity 层

#### TestScriptEntity

```java
@Entity
@Table(name = "test_scripts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScriptEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unique_id")
    private Long uniqueId;

    @Column(name = "script_name", nullable = false)
    private String scriptName;

    @Column(name = "script_description", columnDefinition = "TEXT")
    private String scriptDescription;

    @Column(name = "script_content", nullable = false, columnDefinition = "LONGTEXT")
    private String scriptContent;

    @Column(name = "language", length = 20)
    private String language;

    @Column(name = "generation_method", nullable = false, length = 50)
    private String generationMethod; // EXCEL_IMPORT, AI_GENERATED

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "ai_generation_status", length = 20)
    private String aiGenerationStatus; // SUCCESS, FAILED, PENDING

    @Column(name = "ai_retry_count")
    private Integer aiRetryCount;

    @Column(name = "ai_error_message", columnDefinition = "TEXT")
    private String aiErrorMessage;

    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed;

    @Column(name = "ai_generation_time")
    private LocalDateTime aiGenerationTime;

    @Column(name = "test_case_id", nullable = false)
    private Long testCaseId;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "execution_count")
    private Integer executionCount;

    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;

    @Column(name = "last_execution_result", length = 20)
    private String lastExecutionResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", insertable = false, updatable = false)
    private TestCaseEntity testCase;
}
```

---

### 3.2 DTO 层

#### TestScriptCreateRequest

```java
@Data
@Builder
public class TestScriptCreateRequest {
    @NotBlank(message = "脚本名称不能为空")
    private String scriptName;

    private String scriptDescription;

    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;

    @NotBlank(message = "脚本语言不能为空")
    private String language;

    @NotNull(message = "生成方式不能为空")
    private String generationMethod; // EXCEL_IMPORT, AI_GENERATED

    @NotNull(message = "测试用例ID不能为空")
    private Long testCaseId;

    private String category;

    // AI生成相关字段（可选）
    private String aiModelUsed;
    private String aiErrorMessage;
    private String aiGenerationStatus; // SUCCESS, FAILED, PENDING
}
```

#### TestScriptUpdateRequest（仅用于启用/禁用）

```java
@Data
@Builder
public class TestScriptUpdateRequest {
    @NotNull(message = "脚本ID不能为空")
    private Long uniqueId;

    /**
     * 启用状态（true=启用，false=禁用）
     * 启用时，会自动禁用该测试用例的其他脚本
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    private String scriptName; // 可选：允许修改名称
    private String scriptDescription; // 可选：允许修改描述
    private String category; // 可选：允许修改分类
}
```

#### TestScriptResponse

```java
@Data
@Builder
public class TestScriptResponse {
    private Long uniqueId;
    private String scriptName;
    private String scriptDescription;
    private String scriptContent;
    private String language;
    private String generationMethod; // EXCEL_IMPORT, AI_GENERATED
    private Boolean enabled;
    private String aiGenerationStatus;
    private Integer aiRetryCount;
    private String aiErrorMessage;
    private String aiModelUsed;
    private LocalDateTime aiGenerationTime;
    private Long testCaseId;
    private String testCaseName; // 关联查询
    private String category;
    private Integer executionCount;
    private LocalDateTime lastExecutionTime;
    private String lastExecutionResult;
    private Long createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

---

### 3.3 Repository 层

#### TestScriptRepository

```java
@Repository
public interface TestScriptRepository extends JpaRepository<TestScriptEntity, Long> {

    /**
     * 查询测试用例的启用脚本
     */
    @Query("SELECT t FROM TestScriptEntity t WHERE t.testCaseId = :testCaseId AND t.enabled = TRUE")
    Optional<TestScriptEntity> findEnabledByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 查询测试用例的所有脚本（包括已禁用）
     */
    @Query("SELECT t FROM TestScriptEntity t WHERE t.testCaseId = :testCaseId ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findAllByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 禁用测试用例的所有脚本
     */
    @Query("UPDATE TestScriptEntity t SET t.enabled = FALSE WHERE t.testCaseId = :testCaseId")
    void disableAllByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 查询AI生成失败的脚本（可重试）
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.aiGenerationStatus = 'FAILED' " +
           "AND t.aiRetryCount < 3 " +
           "")
    List<TestScriptEntity> findFailedScriptForRetry();

    /**
     * 统计测试用例的脚本数量
     */
    @Query("SELECT COUNT(t) FROM TestScriptEntity t " +
           "WHERE t.testCaseId = :testCaseId ")
    Long countByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 搜索脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE " +
           "(LOWER(t.scriptName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.scriptContent) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TestScriptEntity> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 按生成方式查询脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.generationMethod = :generationMethod  " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findByGenerationMethod(@Param("generationMethod") String generationMethod);
}
```

---

### 3.4 Service 层

#### TestScriptService 接口

```java
public interface TestScriptService {

    /**
     * 创建脚本（由系统自动调用）
     * - Excel导入时调用
     * - AI生成时调用
     */
    Long create(TestScriptCreateRequest request);

    /**
     * 更新脚本启用/禁用状态
     * - 启用时，自动禁用该测试用例的其他脚本
     */
    void updateEnabledStatus(Long scriptId, Boolean enabled);

    /**
     * 更新脚本基本信息（不包含内容）
     */
    void updateBasicInfo(Long scriptId, TestScriptUpdateRequest request);

    /**
     */
    void delete(Long scriptId);

    /**
     * 获取脚本详情
     */
    TestScriptResponse getById(Long scriptId);

    /**
     * 获取测试用例的启用脚本
     */
    TestScriptResponse getEnabledByTestCaseId(Long testCaseId);

    /**
     * 获取测试用例的所有脚本
     */
    List<TestScriptResponse> getAllByTestCaseId(Long testCaseId);

    /**
     * 搜索脚本
     */
    List<TestScriptResponse> search(String keyword);

    /**
     * AI生成脚本（核心方法）
     * - 根据测试用例自动生成脚本
     * - 失败时保留记录并支持重试
     */
    TestScriptResponse generateByAI(Long testCaseId);

    /**
     * 重试失败的AI生成
     */
    void retryFailedGeneration();

    /**
     * 运行测试用例时获取脚本
     * - 优先返回启用状态的脚本
     * - 如果没有启用脚本，自动触发AI生成
     */
    TestScriptResponse getScriptForExecution(Long testCaseId);
}
```

#### TestScriptServiceImpl 实现类关键方法

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class TestScriptServiceImpl implements TestScriptService {

    private final TestScriptRepository scriptRepository;
    private final TestCaseRepository testCaseRepository;
    private final AIService aiService; // AI服务（假设已存在）
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Long create(TestScriptCreateRequest request) {
        log.info("创建脚本: {} for 测试用例: {}", request.getScriptName(), request.getTestCaseId());

        // 1. 校验测试用例是否存在
        if (!testCaseRepository.existsById(request.getTestCaseId())) {
            throw new RuntimeException("测试用例不存在: " + request.getTestCaseId());
        }

        // 2. 如果是启用状态，先禁用该测试用例的其他脚本
        if (request.getGenerationMethod().equals("AI_GENERATED") ||
            request.getGenerationMethod().equals("EXCEL_IMPORT")) {
            // Excel导入和AI生成的脚本，默认为启用状态
            scriptRepository.disableAllByTestCaseId(request.getTestCaseId());
        }

        // 3. 构建脚本实体
        TestScriptEntity entity = TestScriptEntity.builder()
                .scriptName(request.getScriptName())
                .scriptDescription(request.getScriptDescription())
                .scriptContent(request.getScriptContent())
                .language(request.getLanguage())
                .generationMethod(request.getGenerationMethod())
                .enabled(true) // 新创建的脚本默认启用
                .aiGenerationStatus(request.getAiGenerationStatus() != null ?
                    request.getAiGenerationStatus() : "SUCCESS")
                .aiRetryCount(0)
                .aiModelUsed(request.getAiModelUsed())
                .aiErrorMessage(request.getAiErrorMessage())
                .aiGenerationTime(LocalDateTime.now())
                .testCaseId(request.getTestCaseId())
                .category(request.getCategory())
                .executionCount(0)
                .build();

        // 4. 设置审计字段
        entity.setCreatedBy(getCurrentUserId());
        entity.setUpdatedBy(getCurrentUserId());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        // 5. 保存
        TestScriptEntity saved = scriptRepository.save(entity);

        log.info("脚本创建成功, ID: {}, 生成方式: {}", saved.getUniqueId(), request.getGenerationMethod());
        return saved.getUniqueId();
    }

    @Override
    @Transactional
    public void updateEnabledStatus(Long scriptId, Boolean enabled) {
        log.info("更新脚本启用状态: {} -> {}", scriptId, enabled);

        TestScriptEntity entity = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("脚本不存在: " + scriptId));

        if (enabled) {
            // 启用该脚本时，先禁用同一测试用例的其他所有脚本
            scriptRepository.disableAllByTestCaseId(entity.getTestCaseId());
        }

        entity.setEnabled(enabled);
        entity.setUpdatedBy(getCurrentUserId());
        entity.setUpdatedTime(LocalDateTime.now());

        scriptRepository.save(entity);

        log.info("脚本启用状态更新成功, ID: {}, enabled: {}", scriptId, enabled);
    }

    @Override
    public TestScriptResponse getScriptForExecution(Long testCaseId) {
        log.info("获取测试用例的执行脚本: {}", testCaseId);

        // 1. 查找启用状态的脚本
        Optional<TestScriptEntity> enabledScript =
                scriptRepository.findEnabledByTestCaseId(testCaseId);

        if (enabledScript.isPresent()) {
            log.info("找到启用的脚本: {}", enabledScript.get().getUniqueId());
            return convertToResponse(enabledScript.get());
        }

        // 2. 没有启用脚本，触发AI生成
        log.info("没有启用脚本，触发AI生成");
        return generateByAI(testCaseId);
    }

    @Override
    @Transactional
    public TestScriptResponse generateByAI(Long testCaseId) {
        log.info("AI生成脚本 for 测试用例: {}", testCaseId);

        // 1. 查询测试用例信息
        TestCaseEntity testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + testCaseId));

        // 2. 调用AI服务生成脚本
        String generatedScript;
        String errorMessage = null;
        String status = "SUCCESS";
        String modelUsed = "gpt-4"; // 默认模型

        try {
            generatedScript = aiService.generateScript(testCase);
            log.info("AI脚本生成成功");
        } catch (Exception e) {
            log.error("AI脚本生成失败", e);
            generatedScript = "// AI生成失败，请手动编写或重试\n" +
                           "// 测试用例: " + testCase.getTestCaseName();
            errorMessage = e.getMessage();
            status = "FAILED";
        }

        // 3. 创建脚本记录
        TestScriptCreateRequest request = TestScriptCreateRequest.builder()
                .scriptName(testCase.getTestCaseName() + " - AI生成脚本")
                .scriptDescription("由AI自动生成 for " + testCase.getTestCaseName())
                .scriptContent(generatedScript)
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .testCaseId(testCaseId)
                .category(testCase.getCategory())
                .aiGenerationStatus(status)
                .aiErrorMessage(errorMessage)
                .aiModelUsed(modelUsed)
                .build();

        Long scriptId = create(request);

        // 4. 如果失败，加入重试队列
        if ("FAILED".equals(status)) {
            log.warn("AI生成失败，已记录，可后续重试");
        }

        return getById(scriptId);
    }

    @Override
    @Transactional
    public void retryFailedGeneration() {
        log.info("开始重试失败的AI生成");

        List<TestScriptEntity> failedScripts = scriptRepository.findFailedScriptForRetry();

        for (TestScriptEntity script : failedScripts) {
            try {
                // 重新调用AI生成
                TestCaseEntity testCase = testCaseRepository.findById(script.getTestCaseId())
                        .orElse(null);

                if (testCase == null) continue;

                String newContent = aiService.generateScript(testCase);

                // 更新脚本内容
                script.setScriptContent(newContent);
                script.setAiGenerationStatus("SUCCESS");
                script.setAiErrorMessage(null);
                script.setAiGenerationTime(LocalDateTime.now());
                script.setEnabled(true); // 重试成功后自动启用

                // 禁用其他脚本
                scriptRepository.disableAllByTestCaseId(script.getTestCaseId());

                scriptRepository.save(script);

                log.info("重试成功: 脚本ID {}", script.getUniqueId());

            } catch (Exception e) {
                log.error("重试失败: 脚本ID {}", script.getUniqueId(), e);

                // 增加重试次数
                script.setAiRetryCount(script.getAiRetryCount() + 1);
                script.setAiErrorMessage(e.getMessage());
                scriptRepository.save(script);

                if (script.getAiRetryCount() >= 3) {
                    log.error("重试次数已达上限，放弃重试: 脚本ID {}", script.getUniqueId());
                }
            }
        }
    }
}
```

---

### 3.5 Controller 层

#### TestScriptController

```java
@RestController
@RequestMapping("/scripts")
@RequiredArgsConstructor
@Slf4j
public class TestScriptController {

    private final TestScriptService scriptService;

    /**
     * 获取脚本详情
     */
    @GetMapping
    public ApiResponse<TestScriptResponse> getById(@RequestParam Long uniqueId) {
        TestScriptResponse response = scriptService.getById(uniqueId);
        return ApiResponse.success("查询成功", response);
    }

    /**
     * 获取测试用例的启用脚本
     */
    @GetMapping("/enabled")
    public ApiResponse<TestScriptResponse> getEnabledByTestCaseId(
            @RequestParam Long testCaseId) {
        TestScriptResponse response = scriptService.getEnabledByTestCaseId(testCaseId);
        return ApiResponse.success("查询成功", response);
    }

    /**
     * 获取测试用例的所有脚本
     */
    @GetMapping("/by-test-case")
    public ApiResponse<List<TestScriptResponse>> getAllByTestCaseId(
            @RequestParam Long testCaseId) {
        List<TestScriptResponse> responses = scriptService.getAllByTestCaseId(testCaseId);
        return ApiResponse.success("查询成功", responses);
    }

    /**
     * 搜索脚本
     */
    @PostMapping("/search")
    public ApiResponse<List<TestScriptResponse>> search(@RequestBody ScriptSearchRequest request) {
        List<TestScriptResponse> responses = scriptService.search(request.getKeyword());
        return ApiResponse.success("搜索成功", responses);
    }

    /**
     * 更新脚本启用/禁用状态
     */
    @PostMapping("/toggle-enabled")
    public ApiResponse<Void> toggleEnabled(@RequestParam Long scriptId,
                                           @RequestParam Boolean enabled) {
        scriptService.updateEnabledStatus(scriptId, enabled);
        String message = enabled ? "脚本已启用" : "脚本已禁用";
        return ApiResponse.success(message, null);
    }

    /**
     * 更新脚本基本信息
     */
    @PostMapping("/update-basic")
    public ApiResponse<Void> updateBasicInfo(@Valid @RequestBody TestScriptUpdateRequest request) {
        scriptService.updateBasicInfo(request.getUniqueId(), request);
        return ApiResponse.success("更新成功", null);
    }

    /**
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam Long uniqueId) {
        scriptService.delete(uniqueId);
        return ApiResponse.success("脚本已删除", null);
    }

    /**
     * AI生成脚本（手动触发）
     */
    @PostMapping("/generate-by-ai")
    public ApiResponse<TestScriptResponse> generateByAI(@RequestParam Long testCaseId) {
        TestScriptResponse response = scriptService.generateByAI(testCaseId);
        return ApiResponse.success("AI生成完成", response);
    }

    /**
     * 重试失败的AI生成
     */
    @PostMapping("/retry-failed")
    public ApiResponse<Void> retryFailed() {
        scriptService.retryFailedGeneration();
        return ApiResponse.success("重试任务已提交", null);
    }

    /**
     * 按生成方式查询脚本
     */
    @GetMapping("/by-generation-method")
    public ApiResponse<List<TestScriptResponse>> getByGenerationMethod(
            @RequestParam String generationMethod) {
        List<TestScriptResponse> responses =
                scriptService.findByGenerationMethod(generationMethod);
        return ApiResponse.success("查询成功", responses);
    }
}
```

---

## 4. 核心业务流程

### 4.1 Excel导入流程

```
1. 用户上传Excel文件
2. 系统解析Excel，提取测试用例数据
3. 对每个测试用例：
   a. 查询是否已有脚本
   b. 如果没有脚本，根据测试用例描述自动生成脚本内容
   c. 创建脚本记录（generation_method=EXCEL_IMPORT, enabled=TRUE）
   d. 禁用该测试用例的其他所有脚本
4. 返回导入结果
```

### 4.2 运行测试用例流程

```
1. 用户选择测试用例执行
2. 系统查询该测试用例的启用脚本
3. 如果找到启用脚本：
   - 使用该脚本执行
4. 如果没有启用脚本：
   - 自动触发AI生成
   - AI生成成功：启用新脚本，执行
   - AI生成失败：记录失败信息，提示用户
```

### 4.3 脚本启用/禁用流程

```
1. 用户选择某个脚本，点击"启用"
2. 系统校验：
   - 该脚本是否属于同一测试用例
3. 系统执行：
   - 禁用该测试用例的所有其他脚本
   - 启用当前脚本
4. 返回操作结果
```

### 4.4 AI生成失败重试流程

```
1. 系统定时任务扫描失败脚本
2. 对每个失败脚本（retry_count < 3）：
   a. 重新调用AI服务生成
   b. 成功：更新脚本内容，标记为SUCCESS，启用
   c. 失败：retry_count++，记录错误信息
3. retry_count >= 3 时，不再重试
```

---

## 5. 技术依赖

### 5.1 核心依赖
- Spring Boot 2.7.18
- Spring Data JPA
- Hibernate 5.6.x
- H2 Database（测试）/ MySQL（生产）
- Lombok
- JUnit 5

### 5.2 AI服务依赖
- OpenAI API（或其他AI服务）
- 需要预先配置的AI Service

### 5.3 Excel处理依赖
- Apache POI（Excel解析）

---

## 6. 配置说明

### 6.1 application.yml

```yaml
# AI配置
ai:
  enabled: true
  model: gpt-4
  max-retry-count: 3
  timeout: 30000

# 脚本配置
script:
  default-language: typescript
  auto-generate-on-missing: true
  enable-on-create: true

# 重试任务配置
scheduling:
  retry-failed-generation:
    enabled: true
    cron: "0 0 */2 * * *"  # 每2小时执行一次
```

---

## 7. 测试策略

### 7.1 单元测试覆盖
- Repository层：查询方法测试
- Service层：业务逻辑测试
  - 创建脚本（Excel导入、AI生成）
  - 启用/禁用切换
  - AI生成重试
- Controller层：API接口测试

### 7.2 集成测试场景
- Excel导入完整流程
- 运行测试用例时自动生成脚本
- 脚本启用/禁用状态切换
- AI生成失败重试

---

## 8. 数据迁移

### 8.1 从旧版本迁移

如果已有旧数据，需要执行以下迁移：

```sql
-- 1. 删除版本相关数据
DELETE FROM script_versions WHERE script_id IN (
  SELECT unique_id FROM test_scripts
);

-- 2. 更新test_scripts表结构
ALTER TABLE test_scripts
  ADD COLUMN generation_method VARCHAR(50) NOT NULL DEFAULT 'AI_GENERATED',
  ADD COLUMN enabled BOOLEAN DEFAULT TRUE,
  ADD COLUMN ai_generation_status VARCHAR(20) DEFAULT 'SUCCESS',
  ADD COLUMN ai_retry_count INT DEFAULT 0,
  ADD COLUMN ai_error_message TEXT,
  ADD COLUMN ai_model_used VARCHAR(100),
  ADD COLUMN ai_generation_time TIMESTAMP NULL,
  ADD COLUMN test_case_id BIGINT NOT NULL,
  DROP INDEX uk_test_case_enabled;

-- 3. 添加唯一约束
ALTER TABLE test_scripts
  ADD UNIQUE KEY uk_test_case_enabled (test_case_id, enabled);

-- 4. 清理不再需要的表
DROP TABLE IF EXISTS script_versions;
DROP TABLE IF EXISTS script_execution_previews;
DROP TABLE IF EXISTS script_permissions;
DROP TABLE IF EXISTS script_snippets;
```

---

## 9. 总结

### 9.1 核心变更点
1. ✅ 移除版本控制功能
2. ✅ 添加启用/禁用状态管理
3. ✅ 限制脚本生成方式为Excel导入和AI生成
4. ✅ 强制关联测试用例
5. ✅ 添加AI生成失败重试机制

### 9.2 优势
- 简化了脚本管理逻辑
- 提供了更灵活的脚本切换机制
- 保证了脚本与测试用例的关联性
- 增强了AI生成的容错能力

### 9.3 注意事项
- ⚠️ 需要确保AI服务的稳定性
- ⚠️ Excel导入解析逻辑需要准确
- ⚠️ 唯一约束需要正确维护
