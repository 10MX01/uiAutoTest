# 实施任务清单：测试用例管理模块

## ✅ 完成进度统计

- **已完成任务**: 47/70 (67.1%)
- **核心后端功能**: 基础测试用例CRUD、项目管理CRUD、依赖关系管理已完成
- **单元测试**: Service层和Controller层测试已完成
- **当前阶段**: Phase 2 - 批量导入功能开发中

---

## Phase 1: 数据库和基础实体 ✅

### 1.1 数据库表设计

- [x] 1.1.1 编写建表SQL脚本 ✅
  - 创建test_cases表（包含unique_id、name、description、steps_text、steps_json等字段）✅
  - 创建projects表 ✅
  - 创建test_case_dependencies依赖关系表 ✅
  - ~~创建test_case_projects关联表~~ (已改为test_cases.project_id多对一关系)

- [x] 1.1.2 添加索引和约束 ✅
  - 为外键添加索引 ✅
  - 为name、status、priority等常用查询字段添加索引 ✅
  - 添加UNIQUE约束（project.code）✅
  - 添加FOREIGN KEY约束 ✅

- [x] 1.1.3 初始化预置数据 ✅
  - 插入示例项目数据 ✅

**位置**: `pacakge/src/main/resources/db/table.sql`, `pacakge/src/main/resources/db/data.sql`

---

### 1.2 创建实体类（Entity）✅

- [x] 1.2.1 创建BaseEntity基类（common模块）✅
  - 定义unique_id（自增BIGINT）、created_by、updated_by、created_time、updated_time ✅
  - 使用@MappedSuperclass ✅
  - 配置@CreationTimestamp和@UpdateTimestamp ✅

- [x] 1.2.2 创建TestCaseEntity ✅
  - 继承BaseEntity ✅
  - 定义业务字段（name、description、steps_text、steps_json、expected_result、priority、status等）✅
  - 配置JPA注解 ✅

- [x] 1.2.3 创建ProjectEntity ✅
- [x] 1.2.4 创建TestCaseDependencyEntity ✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/entity/`

---

### 1.3 创建Repository接口 ✅

- [x] 1.3.1 创建TestCaseRepository ✅
  - 继承JpaRepository<TestCaseEntity, Long> ✅
  - 添加自定义查询方法（按name、status、priority查询）✅

- [x] 1.3.2 创建ProjectRepository ✅
- [x] 1.3.3 创建TestCaseDependencyRepository ✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/repository/`

---

## Phase 2: 核心业务功能开发 ⚠️

### 2.1 TestCaseService核心业务 ✅

- [x] 2.1.1 实现创建测试用例（create） ✅
  - 验证必填字段 ✅
  - 保存到数据库 ✅
  - 关联项目和依赖关系 ✅

- [x] 2.1.2 实现更新测试用例（update） ✅
  - 验证用例存在 ✅
  - 更新字段 ✅
  - 更新关联关系 ✅

- [x] 2.1.3 实现删除测试用例（delete） ✅
  - 物理删除（从数据库删除记录）✅

- [x] 2.1.4 实现查询列表（listAll、listByStatus、listByPriority、listByCreator） ✅
  - 支持按status、priority过滤 ✅
  - 支持按project过滤 ✅

- [x] 2.1.5 实现详情查询（getById） ✅
  - 查询用例基本信息 ✅
  - 查询关联的项目、依赖 ✅

- [x] 2.1.6 实现搜索功能（search） ✅
  - 支持关键词搜索（name、description、steps_text）✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/service/`

---

### 2.2 ProjectService项目管理 ✅

- [x] 2.2.1 实现项目CRUD功能 ✅
  - 创建项目（验证code唯一性）✅
  - 更新项目 ✅
  - 删除项目（检查是否有关联用例）✅
  - 查询项目列表 ✅

- [x] 2.2.2 实现项目关联管理 ✅
  - 查询项目下的测试用例 ✅
  - 验证项目存在性 ✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/service/ProjectService.java`

---

### 2.3 DependencyService依赖关系管理 ✅

- [x] 2.3.1 实现添加前置依赖（addDependency）✅
  - 验证不自依赖 ✅
  - 验证不重复 ✅
  - **实现循环依赖检测（DFS算法）** ✅
  - 保存依赖关系 ✅

- [x] 2.3.2 实现移除依赖（removeDependency）✅
  - 验证依赖存在性 ✅
  - 删除依赖关系 ✅

- [x] 2.3.3 实现拓扑排序（calculateExecutionOrder）✅
  - 使用Kahn算法 ✅
  - 返回按依赖顺序排列的用例ID列表 ✅

- [x] 2.3.4 实现依赖链查询 ✅
  - 查询前置依赖（prerequisites）✅
  - 查询后续依赖（dependents）✅
  - 返回完整依赖链 ✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/service/DependencyService.java`

---

### 2.4 TestCaseImportService批量导入 ❌

- [ ] 2.4.1 添加Apache POI依赖
  - 在test-case-management/pom.xml中添加poi-ooxml依赖（5.2.3）✅

- [ ] 2.4.2 创建Excel导入DTO
  - TestCaseImportRow（导入行数据）
  - ImportResultDTO（导入结果）
  - ImportError（错误信息）
  - ImportStrategy枚举（SKIP/UPDATE）

- [ ] 2.4.3 实现文件验证
  - 验证文件格式（.xlsx/.xls）
  - 验证文件大小（≤10MB）
  - 验证文件非空

- [ ] 2.4.4 实现Excel解析
  - 读取表头并验证必填列
  - 逐行读取数据
  - 构建TestCaseImportRow对象列表

- [ ] 2.4.5 实现数据验证
  - 验证必填字段（name、stepsText）
  - 验证枚举值（priority、status）
  - 批量查询项目代码并验证存在性
  - 批量查询测试用例并验证前置依赖
  - 验证用例名称唯一性（根据策略）

- [ ] 2.4.6 实现批量导入
  - 事务管理
  - 批量保存测试用例（使用saveAll）
  - 批量创建项目关联关系
  - 批量创建依赖关系

- [ ] 2.4.7 实现导入结果报告
  - 统计总数、成功数、失败数
  - 记录每行的错误详情
  - 返回导入的用例列表

- [ ] 2.4.8 实现下载导入模板
  - 生成Excel模板文件
  - 添加表头和示例数据
  - 自动调整列宽

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/service/TestCaseImportService.java`

---

## Phase 3: Controller层开发 ⚠️

**API设计规范**：
- **GET请求**：仅用于查询数据，参数通过Query String传递
- **POST请求**：仅用于改变数据（创建、更新、删除），参数通过Request Body传递
- **统一返回格式**: `{code, message, data, timestamp}`

### 3.1 TestCaseController ✅

- [x] 3.1.1 实现CRUD接口 ✅
  - **POST /test-cases**（创建用例）✅
  - **GET /test-cases**（分页查询）✅
  - **GET /test-cases/{uniqueId}**（查询详情）✅
  - **POST /test-cases/{uniqueId}**（更新用例）✅
  - **POST /test-cases/{uniqueId}/delete**（删除用例）✅

- [x] 3.1.2 实现搜索和过滤接口 ✅
  - **GET /test-cases/search?keyword=xxx** ✅
  - **GET /test-cases/by-status?status=xxx** ✅
  - **GET /test-cases/by-priority?priority=xxx** ✅
  - **GET /test-cases/by-creator?createdBy=xxx** ✅

- [ ] 3.1.3 实现批量导入接口 ❌
  - **POST /test-cases/import**（批量导入）
    - Content-Type: multipart/form-data
    - 参数：file（文件）、strategy（策略：SKIP/UPDATE）
    - 返回导入结果报告
  - **GET /test-cases/import/template**（下载模板）
    - 生成Excel模板文件
    - 设置响应头为文件下载

- [x] 3.1.4 添加参数验证（@Valid） ✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/controller/TestCaseController.java`

---

### 3.2 ProjectController ✅

- [x] 3.2.1 **POST /projects**（创建项目）✅
  - 参数在body：`{name, description, code}`
  - 验证code唯一性

- [x] 3.2.2 **GET /projects**（查询项目列表）✅
  - 参数在query：`?page=1&size=20`

- [x] 3.2.3 **GET /projects/{uniqueId}**（查询项目详情）✅
  - 参数在path：uniqueId

- [x] 3.2.4 **POST /projects/{uniqueId}**（更新项目）✅
  - 参数在path：uniqueId, body：`{name, description, code}`

- [x] 3.2.5 **POST /projects/{uniqueId}/delete**（删除项目）✅
  - 参数在path：uniqueId
  - 检查是否有关联用例

- [x] 3.2.6 **GET /projects/{uniqueId}/test-cases**（查询项目下的测试用例）✅
  - 参数在path：uniqueId
  - 支持分页和过滤

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/controller/ProjectController.java`

---

### 3.3 DependencyController ✅

- [x] 3.3.1 **POST /test-cases/{uniqueId}/dependencies**（添加依赖）✅
  - 参数在path：uniqueId, body：`{prerequisiteIds: [1, 2], dependencyType: "HARD"}`

- [x] 3.3.2 **GET /test-cases/{uniqueId}/dependencies**（查询依赖列表）✅
  - 参数在path：uniqueId

- [x] 3.3.3 **POST /test-cases/{uniqueId}/dependencies/remove**（移除依赖）✅
  - 参数在path：uniqueId, body：`{prerequisiteIds: [1, 2]}`

- [x] 3.3.4 **POST /test-cases/calculate-order**（计算执行顺序）✅
  - 参数在body：`{testCaseIds: [1, 2, 3]}`

- [x] 3.3.5 **GET /test-cases/{uniqueId}/dependents**（查询后续依赖）✅
  - 参数在path：uniqueId

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/controller/DependencyController.java`

---

## Phase 4: DTO和VO开发 ✅

### 4.1 Request DTO ✅

- [x] 4.1.1 TestCaseCreateRequest ✅
- [x] 4.1.2 TestCaseUpdateRequest ✅

### 4.2 Response VO ✅

- [x] 4.2.1 TestCaseResponse ✅
- [x] 4.2.2 ProjectSimpleResponse ✅
- [x] 4.2.3 TagResponse ✅（可删除，不需要标签）
- [x] 4.2.4 TestCaseDependencyResponse ✅

**位置**: `test-case-management/src/main/java/com/uiauto/testcase/dto/`, `test-case-management/src/main/java/com/uiauto/testcase/vo/`

---

## Phase 5: 单元测试 ⚠️

### 5.1 Service层测试 ⚠️

- [x] 5.1.1 TestCaseService测试 ✅
  - 测试CRUD操作
  - 测试搜索功能
  - 测试异常场景

- [x] 5.1.2 ProjectService测试 ✅
  - 测试CRUD操作
  - 测试代码唯一性验证
  - 测试删除项目时的关联检查

- [x] 5.1.3 DependencyService测试 ✅
  - 测试循环依赖检测（DFS算法）
  - 测试拓扑排序（Kahn算法）
  - 测试钻石型依赖
  - 测试自依赖和重复依赖验证

- [ ] 5.1.4 TestCaseImportService测试 ❌
  - 测试文件格式验证
  - 测试Excel解析
  - 测试数据验证
  - 测试批量导入（成功/失败场景）
  - 测试重复用例处理（SKIP/UPDATE策略）
  - 测试模板下载

### 5.2 Controller层测试 ✅

- [x] 5.2.1 TestCaseController测试 ✅
  - 测试所有API接口
  - 测试参数验证
  - 测试异常处理

- [x] 5.2.2 ProjectController测试 ✅
  - 测试所有CRUD接口
  - 测试参数验证
  - 测试查询项目下的测试用例

- [x] 5.2.3 DependencyController测试 ✅
  - 测试所有依赖管理接口
  - 测试参数验证
  - 测试执行顺序计算

**测试覆盖率要求**：
- Service层 ≥ 80%
- Controller层 ≥ 60%
- Repository层 ≥ 70%

**位置**: `test-case-management/src/test/java/com/uiauto/testcase/`

---

## Phase 6: 前端开发（未开始）

### 6.1 测试用例列表页

- [ ] 6.1.1 创建TestCaseList组件
  - el-table展示用例列表
  - 分页功能
  - 排序功能

- [ ] 6.1.2 实现搜索和过滤
  - 关键词搜索
  - 项目、优先级、状态过滤器
  - 多条件组合过滤

### 6.2 测试用例表单组件

- [ ] 6.2.1 创建TestCaseForm组件
  - 基本信息、测试步骤、预期结果表单
  - 表单验证

- [ ] 6.2.2 实现项目选择器（多选）
- [ ] 6.2.3 实现依赖关系选择器
  - 前置用例选择
  - 依赖类型选择（HARD/SOFT）
  - 循环依赖实时检测和警告

### 6.3 测试用例详情页

- [ ] 6.3.1 创建TestCaseDetail组件
  - 显示完整信息
  - 显示项目和依赖关系

### 6.4 项目管理页

- [ ] 6.4.1 创建ProjectList组件
- [ ] 6.4.2 创建ProjectForm组件

---

## 任务统计

- **总任务数**：70个
- **已完成**：47个
- **待完成**：23个
- **完成进度**：67.1%

### 关键里程碑

- [x] **Phase 1**: 数据库和基础实体（已完成）
- [x] **Phase 2.1**: TestCaseService和Controller（已完成）
- [x] **Phase 2.2**: ProjectService和Controller（已完成）
- [x] **Phase 2.3**: DependencyService和Controller（已完成）
- [ ] **Phase 2.4**: TestCaseImportService批量导入（待完成）
- [x] **Phase 5**: 单元测试 - Service和Controller层（已完成）
- [ ] **Phase 6**: 前端开发（未开始）

### 预计工作量

- **后端开发**: 2周
- **单元测试**: 1周
- **前端开发**: 2周
- **总计**: 5周

---

## 附录：已移除的功能

以下功能已在需求评审中移除，不再实施：

- ❌ 标签系统
- ❌ 版本控制
- ❌ 模板系统
- ❌ 软删除和回收站
- ❌ AI集成功能（移至后续模块）