# 设计文档：测试用例管理模块

## Context

### 背景
测试用例管理模块是AI驱动Web测试自动化平台的核心基础模块。该模块负责测试用例的完整生命周期管理，为后续的AI脚本生成、测试执行和报告分析提供数据支撑。

### 当前状态
这是一个全新的项目，目前没有现有的测试用例管理系统。团队需要从零开始构建测试用例管理能力。

### 约束条件
- **技术栈约束**：前端必须使用Vue 3 + TypeScript + Element UI，后端必须使用Spring Boot 2.7.18 + JPA
- **数据存储**：使用MySQL 8.0作为主数据库，Redis用于缓存
- **开发周期**：作为第一个模块开发，需要为后续模块建立良好的架构基础
- **用户规模**：初期支持小团队使用（10-50人），未来需扩展到企业级（100+用户）
- **Java版本**：Java 8（严格兼容，不可使用Java 9+特性）

### 利益相关者
- **测试工程师**：主要用户，需要快速创建和管理测试用例
- **测试主管**：需要查看测试覆盖率和团队工作进度
- **开发人员**：需要了解测试用例需求以实现对应功能
- **AI服务**：后续模块需要读取测试用例数据生成脚本

---

## Goals / Non-Goals

### Goals
1. **提供完整的测试用例CRUD功能**，支持创建、查询、更新、删除
2. **实现项目管理**，支持按项目组织测试用例
3. **提供强大的搜索和过滤能力**，快速定位目标用例
4. **支持测试用例依赖关系管理**，定义前置条件和执行顺序

### Non-Goals
- 本模块不包含测试执行功能（由test-execution模块负责）
- 本模块不包含AI脚本生成功能（由ai-script-generation模块负责）
- 本模块不包含用户权限和团队管理（可作为后续增强功能）
- 本模块不包含标签系统（已移除）
- 本模块不包含版本控制（已移除）
- 本模块不包含模板系统（已移除，后续可能添加）
- 本模块不包含软删除功能（已移除，使用物理删除）

---

## Decisions

### 1. 统一数据库字段规范

**决策**：所有数据库表必须包含以下5个标准字段

```sql
-- 统一字段规范
unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '主键ID（自增）',
created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
updated_by       BIGINT                                    COMMENT '最后更新人ID',
created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
updated_time     DATETIME     NOT NULL                      COMMENT '最后更新时间'
```

**选择自增ID的原因**：
- ✅ **性能优异**：MySQL自增ID的插入性能比UUID高约3-5倍，查询性能快约30%
- ✅ **开发友好**：调试方便，日志清晰，简单直观
- ✅ **MySQL原生支持**：无需额外配置，机制成熟稳定
- ✅ **适合本项目**：单数据中心部署，数据量在10万级别

**实施方式**：
```java
// JPA实体基类
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unique_id")
    private Long uniqueId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_time", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
```

---

### 2. 测试用例依赖关系管理

**决策**：实现测试用例之间的前置依赖关系，支持依赖链管理和执行顺序计算

#### 2.1 依赖关系的业务价值

在实际测试场景中，很多测试用例存在**执行顺序依赖**：

**场景1：用户注册后才能登录**
```
用例A：用户注册功能测试
  ↓ 依赖
用例B：用户登录功能测试（需要先有一个可用的账号）
```

**场景2：添加商品后才能购物**
```
用例A：添加商品到购物车
  ↓ 依赖
用例B：修改购物车商品数量
  ↓ 依赖
用例C：提交订单
```

**依赖关系的作用**：
1. **保证数据准备**：前置用例创建了后置用例需要的数据
2. **保证执行顺序**：自动按依赖顺序执行测试
3. **失败影响分析**：前置用例失败时，后置用例可以跳过或标记为阻塞

#### 2.2 数据库设计

**依赖关系表**：

```sql
CREATE TABLE test_case_dependencies (
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '主键ID',
    test_case_id     BIGINT       NOT NULL                      COMMENT '当前测试用例ID',
    prerequisite_id  BIGINT       NOT NULL                      COMMENT '前置测试用例ID',
    dependency_type  VARCHAR(20)  DEFAULT 'HARD'                COMMENT '依赖类型：HARD-强依赖/SOFT-弱依赖',

    -- 审计字段
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    FOREIGN KEY (test_case_id) REFERENCES test_cases(unique_id),
    FOREIGN KEY (prerequisite_id) REFERENCES test_cases(unique_id),
    UNIQUE KEY uk_dependency (test_case_id, prerequisite_id),
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_prerequisite_id (prerequisite_id)
) COMMENT='测试用例依赖关系表';
```

**字段说明**：
- **test_case_id**：当前测试用例（后置用例）
- **prerequisite_id**：前置测试用例（必须先执行）
- **dependency_type**：
  - `HARD`：强依赖，前置用例**必须成功**才能执行当前用例
  - `SOFT`：弱依赖，前置用例失败时仍可执行当前用例

**设计约束**：
- **禁止自依赖**：test_case_id ≠ prerequisite_id
- **禁止循环依赖**：A依赖B，B依赖A → 检测并阻止
- **多个前置用例**：一个用例可以有多个前置用例（AND关系）

#### 2.3 依赖关系管理功能

**功能1：添加前置用例**

```java
POST /test-cases/{uniqueId}/dependencies
Request: {
  "prerequisiteId": 10001,
  "dependencyType": "HARD"
}
```

**功能2：循环依赖检测**

```java
public void addDependency(Long testCaseId, Long prerequisiteId) {
    // 1. 检查自依赖
    if (testCaseId.equals(prerequisiteId)) {
        throw new BusinessException("测试用例不能依赖自己");
    }

    // 2. 检测循环依赖（DFS算法）
    if (willCreateCircularDependency(testCaseId, prerequisiteId)) {
        throw new BusinessException("添加该依赖会导致循环依赖");
    }

    // 3. 创建依赖关系
    TestCaseDependencyEntity dependency = TestCaseDependencyEntity.builder()
            .testCaseId(testCaseId)
            .prerequisiteId(prerequisiteId)
            .dependencyType("HARD")
            .build();
    dependencyRepository.save(dependency);
}

// 检测是否会产生循环依赖
private boolean willCreateCircularDependency(Long testCaseId, Long prerequisiteId) {
    Set<Long> visited = new HashSet<>();
    return hasPath(prerequisiteId, testCaseId, visited);
}

// DFS检测路径
private boolean hasPath(Long from, Long to, Set<Long> visited) {
    if (from.equals(to)) {
        return true;  // 找到路径，会形成环
    }

    if (visited.contains(from)) {
        return false;  // 已访问，避免无限递归
    }

    visited.add(from);

    List<TestCaseDependencyEntity> dependencies =
        dependencyRepository.findByPrerequisiteId(from);

    for (TestCaseDependencyEntity dep : dependencies) {
        if (hasPath(dep.getTestCaseId(), to, visited)) {
            return true;
        }
    }

    return false;
}
```

**功能3：拓扑排序（执行顺序计算）**

```java
/**
 * 计算测试用例的执行顺序（拓扑排序）
 */
public List<Long> calculateExecutionOrder(List<Long> testCaseIds) {
    // 1. 构建依赖图
    Map<Long, Set<Long>> graph = new HashMap<>();
    Map<Long, Integer> inDegree = new HashMap<>();

    // 2. Kahn算法进行拓扑排序
    List<Long> result = new ArrayList<>();
    Queue<Long> queue = new LinkedList<>();

    // 找到所有入度为0的节点
    for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
        if (entry.getValue() == 0) {
            queue.offer(entry.getKey());
        }
    }

    while (!queue.isEmpty()) {
        Long current = queue.poll();
        result.add(current);

        // 减少所有依赖current的节点的入度
        for (Long dependent : getDependents(current, graph)) {
            inDegree.put(dependent, inDegree.get(dependent) - 1);
            if (inDegree.get(dependent) == 0) {
                queue.offer(dependent);
            }
        }
    }

    return result;
}
```

---

### 3. 完整的数据库表设计

#### 3.1 测试用例主表 (test_cases)

```sql
CREATE TABLE test_cases (
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '主键ID（自增）',

    -- 基本信息
    name             VARCHAR(255) NOT NULL                      COMMENT '测试用例名称',
    description      TEXT                                         COMMENT '测试用例描述',

    -- 测试步骤：双字段存储
    steps_text       TEXT         NOT NULL                      COMMENT '测试步骤（自然语言描述）',
    steps_json       JSON                                         COMMENT '测试步骤（AI结构化后的JSON，可选）',

    expected_result  TEXT                                         COMMENT '预期结果（自然语言）',

    -- 分类和状态
    priority         VARCHAR(10)  DEFAULT 'P2'                   COMMENT '优先级：P0/P1/P2/P3',
    status           VARCHAR(20)  DEFAULT 'DRAFT'                COMMENT '状态：DRAFT/ACTIVE/ARCHIVED',

    -- 审计字段（统一规范）
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '最后更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_created_by (created_by)
) COMMENT='测试用例主表';
```

#### 3.2 项目表 (projects)

```sql
CREATE TABLE projects (
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '主键ID（自增）',
    name             VARCHAR(255) NOT NULL                      COMMENT '项目名称',
    description      TEXT                                         COMMENT '项目描述',
    code             VARCHAR(50)  NOT NULL                      COMMENT '项目代码（唯一）',

    -- 审计字段
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    UNIQUE KEY uk_code (code),
    INDEX idx_name (name)
) COMMENT='项目表';
```

**设计说明**：测试用例与项目的关系从多对多改为**多对一关系**，通过 test_cases.project_id 外键实现。一个测试用例只能属于一个项目，简化数据模型和查询逻辑。

#### 3.3 测试用例依赖关系表 (test_case_dependencies)

```sql
CREATE TABLE test_case_dependencies (
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '主键ID（自增）',
    test_case_id     BIGINT       NOT NULL                      COMMENT '当前测试用例ID',
    prerequisite_id  BIGINT       NOT NULL                      COMMENT '前置测试用例ID',
    dependency_type  VARCHAR(20)  DEFAULT 'HARD'                COMMENT '依赖类型：HARD-强依赖/SOFT-弱依赖',

    -- 审计字段
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    FOREIGN KEY (test_case_id) REFERENCES test_cases(unique_id),
    FOREIGN KEY (prerequisite_id) REFERENCES test_cases(unique_id),
    UNIQUE KEY uk_dependency (test_case_id, prerequisite_id),
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_prerequisite_id (prerequisite_id)
) COMMENT='测试用例依赖关系表';
```

---

### 4. Excel 批量导入设计

#### 4.1 技术选型

**Apache POI**：用于解析和生成 Excel 文件

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

**选择理由**：
- ✅ 原生 Java 库，无需外部依赖
- ✅ 支持 .xlsx 和 .xls 格式
- ✅ 成熟稳定，社区活跃
- ✅ 性能良好，支持大文件流式读取

#### 4.2 Excel 模板设计

**列定义**：

| 列名 | 必填 | 默认值 | 说明 |
|-----|-----|-------|-----|
| 用例名称 | ✅ | - | 测试用例名称 |
| 用例描述 | ❌ | - | 测试用例描述 |
| 测试步骤 | ✅ | - | 支持 ->、\n、| 分隔符 |
| 预期结果 | ❌ | - | 预期结果 |
| 优先级 | ❌ | P2 | P0/P1/P2/P3 |
| 状态 | ❌ | DRAFT | DRAFT/ACTIVE/ARCHIVED |
| 项目代码 | ❌ | - | 单个项目代码 |
| 前置依赖 | ❌ | - | 多个用例名称，逗号分隔 |

#### 4.3 导入流程设计

**完整流程**：

```
1. 文件上传验证
   ├─ 检查文件格式（.xlsx/.xls）
   ├─ 检查文件大小（≤10MB）
   └─ 检查文件是否为空

2. Excel 文件解析
   ├─ 读取表头，验证必填列存在
   ├─ 逐行读取数据
   └─ 构建验证上下文

3. 数据验证（逐行）
   ├─ 验证必填字段
   ├─ 验证枚举值（priority、status）
   ├─ 验证项目代码存在性
   ├─ 验证前置依赖用例存在性
   └─ 验证用例名称唯一性（根据策略）

4. 批量插入数据库
   ├─ 事务管理
   ├─ 批量保存测试用例
   ├─ 批量保存项目关联关系
   └─ 批量保存依赖关系

5. 生成导入结果报告
   ├─ 统计总数、成功数、失败数
   ├─ 记录每行的错误信息
   └─ 返回导入的用例列表
```

#### 4.4 核心实现代码

**Excel 导入 Service**：

```java
@Service
public class TestCaseImportService {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public ImportResultDTO importTestCases(MultipartFile file,
                                           ImportStrategy strategy,
                                           Long currentUserId) throws IOException {
        // 1. 验证文件格式
        validateFileFormat(file);

        // 2. 解析 Excel 文件
        List<TestCaseImportRow> rows = parseExcelFile(file);

        // 3. 验证数据并分组
        ImportValidationResult validationResult = validateRows(rows, strategy, currentUserId);

        // 4. 批量导入
        List<TestCaseEntity> importedCases = batchImport(
            validationResult.getValidRows(),
            strategy,
            currentUserId
        );

        // 5. 构建结果报告
        return ImportResultDTO.builder()
            .total(rows.size())
            .success(importedCases.size())
            .failed(validationResult.getErrors().size())
            .errors(validationResult.getErrors())
            .importedTestCases(convertToResponse(importedCases))
            .build();
    }

    private void validateFileFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null ||
            (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException("文件格式不正确，仅支持 .xlsx 和 .xls 格式");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("文件大小超过限制（最大 10MB）");
        }
    }

    private List<TestCaseImportRow> parseExcelFile(MultipartFile file) throws IOException {
        List<TestCaseImportRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 读取表头
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnMap = buildColumnMap(headerRow);

            // 验证必填列
            validateRequiredColumns(columnMap);

            // 逐行读取数据（跳过表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                TestCaseImportRow importRow = parseRow(row, columnMap, i + 1);
                rows.add(importRow);
            }
        }

        return rows;
    }

    private ImportValidationResult validateRows(List<TestCaseImportRow> rows,
                                                ImportStrategy strategy,
                                                Long currentUserId) {
        List<ImportError> errors = new ArrayList<>();
        List<TestCaseImportRow> validRows = new ArrayList<>();

        // 批量查询项目缓存
        Set<String> projectCodes = extractProjectCodes(rows);
        Map<String, ProjectEntity> projectMap = projectRepository
            .findAllByCodeIn(projectCodes)
            .stream()
            .collect(Collectors.toMap(ProjectEntity::getCode, p -> p));

        // 批量查询测试用例缓存
        Set<String> caseNames = extractCaseNames(rows);
        Map<String, TestCaseEntity> caseMap = testCaseRepository
            .findAllByNameInAndCreatedBy(caseNames, currentUserId)
            .stream()
            .collect(Collectors.toMap(TestCaseEntity::getName, c -> c));

        for (TestCaseImportRow row : rows) {
            List<String> rowErrors = new ArrayList<>();

            // 1. 验证必填字段
            if (StringUtils.isBlank(row.getName())) {
                rowErrors.add("用例名称不能为空");
            }
            if (StringUtils.isBlank(row.getStepsText())) {
                rowErrors.add("测试步骤不能为空");
            }

            // 2. 验证枚举值
            if (!isValidPriority(row.getPriority())) {
                rowErrors.add("优先级值无效：必须是 P0/P1/P2/P3 之一");
            }
            if (!isValidStatus(row.getStatus())) {
                rowErrors.add("状态值无效：必须是 DRAFT/ACTIVE/ARCHIVED 之一");
            }

            // 3. 验证项目代码存在性
            if (StringUtils.isNotBlank(row.getProjectCodes())) {
                List<String> codes = parseCommaSeparated(row.getProjectCodes());
                for (String code : codes) {
                    if (!projectMap.containsKey(code)) {
                        rowErrors.add("项目代码 " + code + " 不存在");
                    }
                }
            }

            // 4. 验证前置依赖存在性
            if (StringUtils.isNotBlank(row.getPrerequisites())) {
                List<String> depNames = parseCommaSeparated(row.getPrerequisites());
                for (String depName : depNames) {
                    if (!caseMap.containsKey(depName)) {
                        rowErrors.add("前置用例 \"" + depName + "\" 不存在");
                    }
                }
            }

            // 5. 验证用例名称唯一性（根据策略）
            if (caseMap.containsKey(row.getName())) {
                if (strategy == ImportStrategy.SKIP) {
                    rowErrors.add("用例名称已存在（策略：跳过）");
                }
                // UPDATE 策略允许重复，后续会更新
            }

            if (rowErrors.isEmpty()) {
                validRows.add(row);
            } else {
                errors.add(ImportError.builder()
                    .row(row.getRowNumber())
                    .error(String.join("; ", rowErrors))
                    .build());
            }
        }

        return ImportValidationResult.builder()
            .validRows(validRows)
            .errors(errors)
            .projectMap(projectMap)
            .existingCasesMap(caseMap)
            .build();
    }

    @Transactional
    protected List<TestCaseEntity> batchImport(List<TestCaseImportRow> rows,
                                               ImportStrategy strategy,
                                               Long currentUserId) {
        List<TestCaseEntity> result = new ArrayList<>();

        for (TestCaseImportRow row : rows) {
            try {
                TestCaseEntity entity = buildTestCaseEntity(row, currentUserId);

                // 检查是否需要更新
                if (strategy == ImportStrategy.UPDATE &&
                    entity.getUniqueId() != null) {
                    entity = testCaseRepository.save(entity);
                    // 更新关联关系
                    updateAssociations(entity, row);
                } else {
                    // 新建
                    entity = testCaseRepository.save(entity);
                    // 创建关联关系
                    createAssociations(entity, row);
                }

                result.add(entity);
            } catch (Exception e) {
                log.error("导入测试用例失败，行号：{}，错误：{}",
                    row.getRowNumber(), e.getMessage(), e);
                // 此错误已计入验证阶段，此处忽略
            }
        }

        return result;
    }

    private void createAssociations(TestCaseEntity entity,
                                   TestCaseImportRow row) {
        // 设置项目关联（多对一关系）
        if (StringUtils.isNotBlank(row.getProjectCodes())) {
            String code = row.getProjectCodes().split(",")[0].trim(); // 只取第一个项目
            ProjectEntity project = projectMap.get(code);
            if (project != null) {
                entity.setProjectId(project.getUniqueId());
            }
        }

        // 创建依赖关系
        if (StringUtils.isNotBlank(row.getPrerequisites())) {
            List<String> depNames = parseCommaSeparated(row.getPrerequisites());
            // ... 创建 test_case_dependencies 记录
        }
    }
}
```

#### 4.5 性能优化策略

**1. 流式读取大文件**：
```java
// 使用 SAX 模式解析大型 Excel 文件
XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
```

**2. 批量插入优化**：
```java
// 使用 JPA saveAll 批量保存
List<TestCaseEntity> batch = new ArrayList<>();
for (TestCaseImportRow row : validRows) {
    batch.add(buildTestCaseEntity(row, currentUserId));
    if (batch.size() >= 100) {
        testCaseRepository.saveAll(batch);
        batch.clear();
    }
}
```

**3. 缓存优化**：
- 批量查询项目代码（一次性查询所有）
- 批量查询测试用例名称（一次性查询所有）
- 避免在循环中查询数据库

**4. 事务管理**：
- 整个导入过程在一个事务中
- 失败时全部回滚

#### 4.6 错误处理策略

**文件级错误**（返回 400）：
- 文件格式不正确
- 文件大小超限
- Excel 文件为空
- 缺少必填列

**数据级错误**（继续导入，记录错误）：
- 某一行数据不完整
- 项目代码不存在
- 前置依赖用例不存在
- 重复的用例名称（SKIP 策略下）

**错误报告格式**：
```json
{
  "total": 10,
  "success": 7,
  "failed": 3,
  "errors": [
    {"row": 3, "error": "用例名称不能为空"},
    {"row": 5, "error": "优先级值无效"},
    {"row": 8, "error": "项目代码 NON_EXISTENT 不存在"}
  ]
}
```

#### 4.7 生成导入模板

**模板生成接口**：

```java
@GetMapping("/test-cases/import/template")
public void downloadImportTemplate(HttpServletResponse response) throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition",
        "attachment; filename=\"test_case_import_template.xlsx\"");

    // 创建 Excel 工作簿
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("测试用例导入");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "用例名称*", "用例描述", "测试步骤*", "预期结果",
            "优先级", "状态", "项目代码", "前置依赖"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            // 设置表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // 添加示例数据
        Row exampleRow1 = sheet.createRow(1);
        exampleRow1.createCell(0).setCellValue("用户登录功能测试");
        exampleRow1.createCell(1).setCellValue("验证用户使用正确的用户名和密码可以成功登录");
        exampleRow1.createCell(2).setCellValue("打开登录页面 -> 输入用户名 -> 输入密码 -> 点击登录按钮");
        exampleRow1.createCell(3).setCellValue("成功登录并跳转到首页");
        exampleRow1.createCell(4).setCellValue("P1");
        exampleRow1.createCell(5).setCellValue("ACTIVE");
        exampleRow1.createCell(6).setCellValue("USER_MGMT");
        exampleRow1.createCell(7).setCellValue("用户注册测试");

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入响应流
        workbook.write(response.getOutputStream());
    }
}
```

---

## Risks / Trade-offs

### Risk 1: 依赖关系过于复杂导致执行顺序计算慢
**缓解措施**：
- 限制依赖深度（最多5层）
- 使用缓存存储计算结果
- 提供手动调整执行顺序的功能

### Risk 2: 循环依赖检测性能问题
**缓解措施**：
- 添加依赖时实时检测（DFS算法）
- 使用缓存记录已检测的路径
- 后台任务定期全量检测

### Trade-off 1: 自增ID vs UUID
**选择**：自增ID (BIGINT)
**理由**：性能更优、开发更简单、本系统不需要分布式ID
**代价**：ID可预测（可通过权限控制保护）

### Trade-off 2: 自然语言 vs JSON
**选择**：双字段存储
**理由**：用户体验 + AI能力兼顾
**代价**：需要同步机制保证一致性

### Trade-off 3: 强依赖 vs 弱依赖
**选择**：支持两种类型
**理由**：灵活性
**代价**：执行逻辑更复杂

---

## Migration Plan

### 4周实施计划

**Week 1：环境准备和数据库**
- 数据库建表（执行SQL脚本）
- 后端项目初始化（Spring Boot + JPA）
- 前端项目初始化（Vue 3 + Element UI）

**Week 2：核心功能**
- 测试用例CRUD（后端Service + Controller）
- 项目管理功能
- 测试用例表单组件（前端）

**Week 3：高级功能**
- 依赖关系管理（循环依赖检测）
- 搜索和过滤功能
- 拓扑排序（执行顺序计算）

**Week 4：测试和优化**
- 单元测试（覆盖率达标）
- 集成测试
- 性能优化（索引、缓存）

---

## Open Questions

1. **依赖深度限制**：是否限制依赖链的最大深度（建议5层）？
2. **循环依赖的处理**：是否允许用户强制添加循环依赖（仅警告）？
3. **批量执行时的依赖处理**：前置用例失败时，后置用例是跳过还是标记为阻塞？
4. **依赖关系的版本控制**：依赖关系是否也需要版本控制？

---

## 附录

### A. 技术栈
- **前端**：Vue 3.3+、TypeScript 5.0+、Element UI 2.4+、Vite 4.0+
- **后端**：Spring Boot 2.7.18、JPA、MySQL 8.0+
- **Java版本**：Java 8（严格兼容）
- **数据库**：MySQL 8.0+

### B. API设计规范
- 只使用GET和POST（不使用PUT/DELETE/PATCH）
- GET用于查询，参数通过query string传递
- POST用于数据变更，参数通过body传递
- 统一返回格式: `{code, message, data, timestamp}`

### C. 参考资料
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://vuejs.org/)
- [Element UI组件库](https://element.eleme.io/)
- [拓扑排序算法](https://en.wikipedia.org/wiki/Topological_sorting)