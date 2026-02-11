# Spec: 测试用例组织管理

## 模块概述

本模块提供测试用例的组织管理功能，包括项目关联管理和测试用例之间的前置依赖关系管理，帮助测试工程师更好地组织和规划测试用例。

**设计变更说明**：测试用例与项目的关系从多对多改为**多对一关系**，一个测试用例只能属于一个项目，通过 `test_cases.project_id` 外键实现。

## 需求列表

### REQ-1: 项目管理
作为测试工程师，我想要创建和维护项目，以便按项目维度组织测试用例。

### REQ-2: 为测试用例分配项目
作为测试工程师，我想要将测试用例关联到项目（每个用例只能关联一个项目），以便按项目查看和管理测试用例。

### REQ-3: 定义测试用例前置依赖
作为测试工程师，我想要定义测试用例之间的前置依赖关系，以便确保测试按正确顺序执行。

### REQ-4: 查询测试用例的依赖关系
作为测试工程师，我想要查看测试用例的前置依赖，以便了解执行该用例需要先完成哪些测试。

### REQ-5: 移除测试用例的依赖关系
作为测试工程师，我想要移除测试用例的前置依赖，以便调整测试执行顺序。

### REQ-6: 按项目查询测试用例
作为测试主管，我想要查看某个项目下的所有测试用例，以便评估项目的测试覆盖度。

## 功能场景

### REQ-1: 项目管理

#### Scenario: 创建新项目
- **GIVEN** 系统已正常运行
- **WHEN** 测试主管创建新项目
  - 项目名称：用户管理模块
  - 项目代码：USER_MGMT
  - 项目描述：包含用户注册、登录、个人信息管理等功能
- **THEN** 系统返回成功响应，包含项目 ID
- **AND** 数据库中保存项目记录
- **AND** 项目代码在系统中唯一

#### Scenario: 项目代码重复
- **GIVEN** 系统中已存在代码为 USER_MGMT 的项目
- **WHEN** 测试主管创建新项目，项目代码也为 USER_MGMT
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示项目代码已存在
- **AND** 数据库不创建新记录

#### Scenario: 查询所有项目
- **GIVEN** 系统中存在 5 个项目
- **WHEN** 测试工程师请求 GET /projects
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 5 个项目
- **AND** 每个项目包含 ID、名称、代码、描述信息

#### Scenario: 删除项目
- **GIVEN** 系统中存在项目（ID=1）
- **AND** 该项目下没有关联的测试用例
- **WHEN** 测试主管删除该项目
- **THEN** 系统返回 200 成功响应
- **AND** 数据库中删除该项目记录

#### Scenario: 删除已关联用例的项目
- **GIVEN** 系统中存在项目（ID=1）
- **AND** 该项目下已关联 3 个测试用例
- **WHEN** 测试主管删除该项目
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示该项目下存在测试用例，无法删除
- **AND** 项目记录未被删除

### REQ-2: 为测试用例分配项目

#### Scenario: 创建用例时关联项目
- **GIVEN** 系统中存在项目（ID=1，名称=用户管理模块）
- **WHEN** 测试工程师创建测试用例并指定项目 ID 为 1
- **THEN** 系统成功创建测试用例
- **AND** 测试用例的 `project_id` 字段设置为 1
- **AND** 查询该用例时能看到关联的项目信息

#### Scenario: 创建用例时不指定项目
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师创建测试用例但不指定项目 ID
- **THEN** 系统成功创建测试用例
- **AND** 测试用例的 `project_id` 字段为 NULL
- **AND** 查询该用例时项目信息为空

#### Scenario: 更新用例的项目关联
- **GIVEN** 测试用例（ID=1）原项目 ID 为 1
- **WHEN** 测试工程师更新用例，将项目 ID 修改为 2
- **THEN** 系统成功更新测试用例
- **AND** 测试用例的 `project_id` 字段更新为 2

#### Scenario: 移除用例的项目关联
- **GIVEN** 测试用例（ID=1）的项目 ID 为 1
- **WHEN** 测试工程师更新用例，将项目 ID 设置为 null
- **THEN** 系统成功更新测试用例
- **AND** 测试用例的 `project_id` 字段设置为 NULL

### REQ-3: 定义测试用例前置依赖

#### Scenario: 创建用例时定义前置依赖
- **GIVEN** 系统中存在测试用例 A（ID=1，名称=用户注册测试）
- **WHEN** 测试工程师创建测试用例 B，指定前置依赖 ID 为 [1]
- **THEN** 系统成功创建测试用例 B
- **AND** `test_case_dependencies` 表中创建依赖关系记录
- **AND** 记录的 test_case_id 指向用例 B
- **AND** 记录的 prerequisite_id 指向用例 A
- **AND** dependency_type = 'HARD'（强依赖）

#### Scenario: 创建用例时定义多个前置依赖
- **GIVEN** 系统中存在用例 A（ID=1）和用例 B（ID=2）
- **WHEN** 测试工程师创建测试用例 C，指定前置依赖 ID 为 [1, 2]
- **THEN** 系统成功创建测试用例 C
- **AND** `test_case_dependencies` 表中创建 2 条依赖关系记录
- **AND** 用例 C 分别依赖用例 A 和用例 B

#### Scenario: 更新用例时添加新的前置依赖
- **GIVEN** 测试用例 C（ID=3）原前置依赖为 [1]
- **AND** 系统中存在用例 B（ID=2）
- **WHEN** 测试工程师更新用例 C，将前置依赖修改为 [1, 2]
- **THEN** 系统成功更新测试用例
- **AND** 保留原有的依赖关系记录（test_case_id=3, prerequisite_id=1）
- **AND** 创建新的依赖关系记录（test_case_id=3, prerequisite_id=2）

#### Scenario: 循环依赖检测
- **GIVEN** 系统中存在用例 A 和用例 B
- **AND** 用例 B 已依赖用例 A
- **WHEN** 测试工程师尝试让用例 A 依赖用例 B
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示存在循环依赖
- **AND** 数据库不创建新的依赖关系记录

#### Scenario: 自依赖检测
- **GIVEN** 系统中存在测试用例 A（ID=1）
- **WHEN** 测试工程师创建或更新用例 A，指定前置依赖包含 [1]
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示测试用例不能依赖自己

### REQ-4: 查询测试用例的依赖关系

#### Scenario: 查询用例的前置依赖
- **GIVEN** 测试用例 C 依赖用例 A 和用例 B
- **WHEN** 测试工程师请求 GET /test-cases/3/dependencies
- **THEN** 系统返回 200 成功响应
- **AND** 返回数据包含 2 条前置依赖记录
- **AND** 每条记录包含：依赖 ID、前置用例 ID、前置用例名称、依赖类型

#### Scenario: 查询无依赖的用例
- **GIVEN** 测试用例 A（ID=1）没有任何前置依赖
- **WHEN** 测试工程师请求 GET /test-cases/1/dependencies
- **THEN** 系统返回 200 成功响应
- **AND** 返回数据为空数组 []

#### Scenario: 查询不存在的用例的依赖
- **GIVEN** 系统中不存在 ID 为 999 的测试用例
- **WHEN** 测试工程师请求 GET /test-cases/999/dependencies
- **THEN** 系统返回 404 错误
- **AND** 错误消息提示测试用例不存在

#### Scenario: 查询反向依赖（哪些用例依赖当前用例）
- **GIVEN** 用例 B 和用例 C 都依赖用例 A
- **WHEN** 测试工程师查询用例 A 的反向依赖关系
- **THEN** 系统返回包含用例 B 和用例 C 的列表
- **AND** 每个记录显示：依赖用例 ID、依赖用例名称、依赖类型

### REQ-5: 移除测试用例的依赖关系

#### Scenario: 移除单个前置依赖
- **GIVEN** 测试用例 C 依赖用例 A 和用例 B
- **WHEN** 测试工程师提交 POST /test-cases/3/dependencies/remove
  - 请求体：{"prerequisiteIds": [1]}
- **THEN** 系统返回 200 成功响应
- **AND** `test_case_dependencies` 表中删除对应记录
- **AND** 用例 C 仍依赖用例 B

#### Scenario: 移除所有前置依赖
- **GIVEN** 测试用例 C 依赖用例 A 和用例 B
- **WHEN** 测试工程师提交 POST /test-cases/3/dependencies/remove
  - 请求体：{"prerequisiteIds": [1, 2]}
- **THEN** 系统返回 200 成功响应
- **AND** 用例 C 的所有依赖关系被删除
- **AND** 查询依赖关系返回空数组

#### Scenario: 移除不存在的依赖关系
- **GIVEN** 测试用例 C 只依赖用例 A（prerequisite_id=1）
- **WHEN** 测试工程师尝试移除依赖 prerequisite_id=999
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示依赖关系不存在

### REQ-6: 按项目查询测试用例

#### Scenario: 查询项目下的所有测试用例
- **GIVEN** 项目（ID=1）下关联了 10 个测试用例
- **WHEN** 测试主管请求 GET /projects/1/test-cases
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 10 个测试用例
- **AND** 每个用例包含基本信息（ID、名称、状态、优先级）

#### Scenario: 查询空项目下的用例
- **GIVEN** 项目（ID=1）下没有关联任何测试用例
- **WHEN** 测试主管请求 GET /projects/1/test-cases
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []

#### Scenario: 查询不存在的项目
- **GIVEN** 系统中不存在 ID 为 999 的项目
- **WHEN** 测试主管请求 GET /projects/999/test-cases
- **THEN** 系统返回 404 错误
- **AND** 错误消息提示项目不存在

## 验收标准

- [ ] 项目代码唯一性约束有效
- [ ] 删除已关联用例的项目时正确阻止
- [ ] 循环依赖检测准确
- [ ] 自依赖检测有效
- [ ] 依赖关系查询支持正向和反向
- [ ] 移除依赖关系时正确验证存在性
- [ ] Service 层单元测试覆盖率 ≥ 80%
- [ ] Controller 层单元测试覆盖率 ≥ 60%

## API 定义

### 1. 创建项目

**接口**: `POST /projects`

**请求体**:
```json
{
  "name": "用户管理模块",
  "code": "USER_MGMT",
  "description": "包含用户注册、登录、个人信息管理等功能"
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "项目创建成功",
  "data": {
    "uniqueId": 1,
    "name": "用户管理模块",
    "code": "USER_MGMT",
    "description": "包含用户注册、登录、个人信息管理等功能",
    "createdBy": 1,
    "updatedBy": 1,
    "createdTime": "2026-02-06T10:00:00",
    "updatedTime": "2026-02-06T10:00:00"
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 项目代码已存在
- `400` - 必填字段缺失

### 2. 查询所有项目

**接口**: `GET /projects`

**查询参数** (可选):
- `page` (Integer) - 页码，默认 1
- `size` (Integer) - 每页数量，默认 20

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "uniqueId": 1,
        "name": "用户管理模块",
        "code": "USER_MGMT",
        "description": "包含用户注册、登录、个人信息管理等功能",
        "createdTime": "2026-02-06T10:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "pageSize": 20,
    "currentPage": 1
  },
  "timestamp": 1738828800000
}
```

### 3. 删除项目

**接口**: `POST /projects/{uniqueId}/delete`

**路径参数**:
- `uniqueId` (Long) - 项目唯一 ID

**响应体**:
```json
{
  "code": 200,
  "message": "项目已删除",
  "data": null,
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 项目不存在
- `400` - 项目下存在测试用例，无法删除

### 4. 查询项目下的测试用例

**接口**: `GET /projects/{uniqueId}/test-cases`

**路径参数**:
- `uniqueId` (Long) - 项目唯一 ID

**查询参数** (可选):
- `page` (Integer) - 页码，默认 1
- `size` (Integer) - 每页数量，默认 20

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "uniqueId": 1,
        "name": "用户登录功能测试",
        "status": "ACTIVE",
        "priority": "P1",
        "createdTime": "2026-02-06T10:00:00"
      }
    ],
    "totalElements": 10,
    ...
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 项目不存在

### 5. 查询测试用例的依赖关系

**接口**: `GET /test-cases/{uniqueId}/dependencies`

**路径参数**:
- `uniqueId` (Long) - 测试用例唯一 ID

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "uniqueId": 1,
      "testCaseId": 3,
      "prerequisiteId": 1,
      "prerequisiteName": "用户注册测试",
      "dependencyType": "HARD",
      "dependencyTypeDesc": "强依赖"
    },
    {
      "uniqueId": 2,
      "testCaseId": 3,
      "prerequisiteId": 2,
      "prerequisiteName": "用户激活测试",
      "dependencyType": "HARD",
      "dependencyTypeDesc": "强依赖"
    }
  ],
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 测试用例不存在

### 6. 移除测试用例的依赖关系

**接口**: `POST /test-cases/{uniqueId}/dependencies/remove`

**路径参数**:
- `uniqueId` (Long) - 测试用例唯一 ID

**请求体**:
```json
{
  "prerequisiteIds": [1, 2]
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "依赖关系已移除",
  "data": null,
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 测试用例不存在
- `400` - 依赖关系不存在
- `400` - 移除后会导致循环依赖
