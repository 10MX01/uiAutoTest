# Spec: 测试用例 CRUD

## 模块概述

本模块提供测试用例的基础增删改查功能，支持创建、查询、更新和删除测试用例，是整个测试用例管理系统的核心基础功能。

## 需求列表

### REQ-1: 创建测试用例
作为测试工程师，我想要创建新的测试用例，以便记录测试场景和预期结果。

### REQ-2: 查询测试用例详情
作为测试工程师，我想要查看测试用例的详细信息，以便了解测试内容和执行步骤。

### REQ-3: 查询所有测试用例
作为测试工程师，我想要查看所有测试用例列表，以便快速浏览和选择目标用例。

### REQ-4: 更新测试用例
作为测试工程师，我想要修改已有的测试用例，以便保持测试用例与系统功能同步。

### REQ-5: 删除测试用例
作为测试工程师，我想要删除不再需要的测试用例，以便维护测试用例库的整洁。

### REQ-6: 按状态查询测试用例
作为测试工程师，我想要按状态（草稿/活跃/归档）筛选测试用例，以便快速定位特定状态的用例。

### REQ-7: 按优先级查询测试用例
作为测试工程师，我想要按优先级（P0/P1/P2/P3）筛选测试用例，以便优先处理高优先级用例。

### REQ-8: 按创建人查询测试用例
作为测试主管，我想要按创建人查看测试用例，以便评估团队成员的工作产出。

### REQ-9: 批量导入测试用例
作为测试工程师，我想要通过 Excel 文件批量导入测试用例，以便快速创建大量测试用例。

## 功能场景

### REQ-1: 创建测试用例

#### Scenario: 成功创建测试用例
- **GIVEN** 系统已正常运行，数据库连接正常
- **WHEN** 测试工程师提交完整的测试用例信息
  - 用例名称：用户登录功能测试
  - 描述：验证用户使用正确的用户名和密码可以成功登录
  - 测试步骤：打开登录页面 -> 输入用户名 -> 输入密码 -> 点击登录按钮
  - 预期结果：成功登录并跳转到首页
  - 优先级：P1
  - 状态：ACTIVE
  - 关联项目：用户管理模块
  - 前置依赖：无
- **THEN** 系统返回成功响应，包含新创建的用例 ID
- **AND** 系统记录创建人和创建时间
- **AND** 数据库中保存完整的测试用例记录

#### Scenario: 创建时关联项目
- **GIVEN** 系统中已存在项目（ID=1，名称=用户管理模块）
- **WHEN** 测试工程师创建测试用例并指定项目 ID 为 1
- **THEN** 系统成功创建测试用例
- **AND** 测试用例的 project_id 字段设置为 1
- **AND** 查询该用例时能看到关联的项目信息

#### Scenario: 创建时定义前置依赖
- **GIVEN** 系统中已存在测试用例（ID=1，名称=用户注册测试）
- **WHEN** 测试工程师创建新用例并指定前置依赖 ID 为 [1]
- **THEN** 系统成功创建测试用例
- **AND** 系统在 `test_case_dependencies` 表中创建依赖关系记录
- **AND** 依赖类型默认为 HARD（强依赖）

#### Scenario: 缺少必填字段
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师提交的测试用例缺少必填字段（名称或测试步骤）
- **THEN** 系统返回 400 错误
- **AND** 错误消息明确指出缺少的字段
- **AND** 数据库不创建任何记录

#### Scenario: 关联不存在的项目
- **GIVEN** 系统中不存在 ID 为 999 的项目
- **WHEN** 测试工程师创建测试用例并指定项目 ID 为 [999]
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示项目不存在

#### Scenario: 依赖不存在的用例
- **GIVEN** 系统中不存在 ID 为 999 的测试用例
- **WHEN** 测试工程师创建新用例并指定前置依赖 ID 为 [999]
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示依赖的用例不存在

### REQ-2: 查询测试用例详情

#### Scenario: 成功查询用例详情
- **GIVEN** 系统中存在测试用例（ID=1）
- **WHEN** 测试工程师请求 GET /test-cases/1
- **THEN** 系统返回 200 成功响应
- **AND** 返回数据包含用例的所有字段（名称、描述、步骤、预期结果等）
- **AND** 返回数据包含关联的项目列表
- **AND** 返回数据包含关联的前置依赖列表

#### Scenario: 查询不存在的用例
- **GIVEN** 系统中不存在 ID 为 999 的测试用例
- **WHEN** 测试工程师请求 GET /test-cases/999
- **THEN** 系统返回 404 错误
- **AND** 错误消息提示测试用例不存在

### REQ-3: 查询所有测试用例

#### Scenario: 成功查询所有用例
- **GIVEN** 系统中存在 3 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases
- **THEN** 系统返回 200 成功响应
- **AND** 返回包含 3 个测试用例的列表
- **AND** 列表按创建时间倒序排列（最新创建的在前）
- **AND** 每个用例只返回基本信息（ID、名称、状态、优先级、创建时间）

#### Scenario: 查询结果为空
- **GIVEN** 系统中没有测试用例
- **WHEN** 测试工程师请求 GET /test-cases
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []

### REQ-4: 更新测试用例

#### Scenario: 成功更新用例基本信息
- **GIVEN** 系统中存在测试用例（ID=1，原名称=用户登录测试）
- **WHEN** 测试工程师提交更新请求，修改名称为"用户登录功能测试（手机号）"
- **THEN** 系统返回 200 成功响应
- **AND** 数据库中用例名称已更新
- **AND** 系统记录更新人和更新时间

#### Scenario: 更新关联的项目
- **GIVEN** 系统中存在测试用例（ID=1），原关联项目为 [1]
- **WHEN** 测试工程师提交更新请求，修改项目 ID 为 [1, 2, 3]
- **THEN** 系统返回 200 成功响应
- **AND** 系统删除原有的项目关联记录
- **AND** 系统创建新的项目关联记录

#### Scenario: 更新不存在的用例
- **GIVEN** 系统中不存在 ID 为 999 的测试用例
- **WHEN** 测试工程师提交更新请求到 ID=999
- **THEN** 系统返回 404 错误
- **AND** 错误消息提示测试用例不存在

#### Scenario: 更新时字段验证
- **GIVEN** 系统中存在测试用例（ID=1）
- **WHEN** 测试工程师提交更新请求，将名称设置为空字符串
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示名称不能为空
- **AND** 数据库中的数据未被修改

### REQ-5: 删除测试用例

#### Scenario: 成功删除用例
- **GIVEN** 系统中存在测试用例（ID=1）
- **WHEN** 测试工程师提交删除请求 POST /test-cases/1/delete
- **THEN** 系统返回 200 成功响应
- **AND** 数据库中删除该用例记录
- **AND** 关联的项目关系记录被级联删除
- **AND** 关联的依赖关系记录被级联删除

#### Scenario: 删除不存在的用例
- **GIVEN** 系统中不存在 ID 为 999 的测试用例
- **WHEN** 测试工程师提交删除请求 POST /test-cases/999/delete
- **THEN** 系统返回 404 错误
- **AND** 错误消息提示测试用例不存在

#### Scenario: 删除被其他用例依赖的用例
- **GIVEN** 系统中存在用例 A（ID=1）和用例 B（ID=2）
- **AND** 用例 B 依赖用例 A
- **WHEN** 测试工程师删除用例 A
- **THEN** 体统提示存在依赖用例A，测试工程师选择继续删除还是取消
- **THEN** 测试工程师选择继续删除
- **AND** 用例 A 被删除
- **AND** 用例 B 的依赖关系记录被级联删除
- **THEN** 测试工程师选择取消删除
- **AND** 用例 A 不被删除

### REQ-6: 按状态查询测试用例

#### Scenario: 按状态成功查询
- **GIVEN** 系统中存在 5 个测试用例，其中 3 个状态为 ACTIVE，2 个状态为 DRAFT
- **WHEN** 测试工程师请求 GET /test-cases/by-status?status=ACTIVE
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 3 个状态为 ACTIVE 的用例
- **AND** 不包含状态为 DRAFT 的用例

#### Scenario: 查询的状态无匹配结果
- **GIVEN** 系统中所有测试用例状态都为 ACTIVE
- **WHEN** 测试工程师请求 GET /test-cases/by-status?status=DRAFT
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []

#### Scenario: 状态参数无效
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师请求 GET /test-cases/by-status?status=INVALID
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示状态值无效，必须为 DRAFT/ACTIVE/ARCHIVED 之一

### REQ-7: 按优先级查询测试用例

#### Scenario: 按优先级成功查询
- **GIVEN** 系统中存在 10 个测试用例，其中 2 个优先级为 P0
- **WHEN** 测试工程师请求 GET /test-cases/by-priority?priority=P0
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 2 个优先级为 P0 的用例
- **AND** 返回列表按创建时间倒序排列

#### Scenario: 查询的优先级无匹配结果
- **GIVEN** 系统中没有优先级为 P0 的测试用例
- **WHEN** 测试工程师请求 GET /test-cases/by-priority?priority=P0
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []

#### Scenario: 优先级参数无效
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师请求 GET /test-cases/by-priority?priority=P5
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示优先级值无效，必须为 P0/P1/P2/P3 之一

### REQ-8: 按创建人查询测试用例

#### Scenario: 按创建人成功查询
- **GIVEN** 系统中存在测试用例，用户 1 创建了 5 个，用户 2 创建了 3 个
- **WHEN** 测试工程师请求 GET /test-cases/by-creator?createdBy=1
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含用户 1 创建的 5 个测试用例
- **AND** 不包含用户 2 创建的用例

#### Scenario: 查询的创建人无匹配结果
- **GIVEN** 系统中不存在用户 ID 为 999 创建的测试用例
- **WHEN** 测试工程师请求 GET /test-cases/by-creator?createdBy=999
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []

### REQ-9: 批量导入测试用例

#### Scenario: 成功导入 Excel 文件
- **GIVEN** 系统已正常运行
- **AND** 测试工程师准备了包含 10 条测试用例的 Excel 文件
- **WHEN** 测试工程师上传 Excel 文件到 POST /test-cases/import
- **THEN** 系统返回 200 成功响应
- **AND** 系统成功创建 10 条测试用例
- **AND** 返回导入结果报告：
  - 总数：10
  - 成功：10
  - 失败：0
  - 错误详情：[]

#### Scenario: Excel 文件格式错误
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师上传文件格式不正确的文件（如 .txt、.doc）
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示只支持 .xlsx 和 .xls 格式

#### Scenario: Excel 文件缺少必填列
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师上传的 Excel 文件缺少"用例名称"列
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示缺少必填列：用例名称

#### Scenario: 部分数据导入成功
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师上传包含 10 条数据的 Excel 文件
- **AND** 其中 7 条数据完整有效，3 条数据缺少必填字段
- **THEN** 系统返回 200 成功响应
- **AND** 系统创建 7 条有效测试用例
- **AND** 返回导入结果报告：
  - 总数：10
  - 成功：7
  - 失败：3
  - 错误详情：[
    {"row": 3, "error": "用例名称不能为空"},
    {"row": 5, "error": "优先级值无效"},
    {"row": 8, "error": "测试步骤不能为空"}
    ]

#### Scenario: 重复的用例名称处理
- **GIVEN** 系统中已存在名为"用户登录功能测试"的用例
- **WHEN** 测试工程师上传的 Excel 文件包含同名的用例
- **AND** 导入策略为 SKIP（跳过重复）
- **THEN** 系统跳过重复的用例
- **AND** 返回报告中提示该行被跳过：{"row": 2, "error": "用例名称已存在，已跳过"}

#### Scenario: 重复的用例名称覆盖
- **GIVEN** 系统中已存在名为"用户登录功能测试"的用例（ID=1）
- **WHEN** 测试工程师上传的 Excel 文件包含同名的用例
- **AND** 导入策略为 UPDATE（更新重复）
- **THEN** 系统更新已存在的用例
- **AND** 返回报告中提示该行已更新：{"row": 2, "status": "updated", "testCaseId": 1}

#### Scenario: Excel 文件为空
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师上传的 Excel 文件只包含表头，没有数据行
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示文件中没有数据

#### Scenario: 关联的项目不存在
- **GIVEN** 系统正常运行
- **AND** Excel 文件中的某行指定了项目代码"NON_EXISTENT"
- **WHEN** 测试工程师上传该 Excel 文件
- **THEN** 系统返回 200 成功响应
- **AND** 该行导入失败
- **AND** 返回错误详情：{"row": 3, "error": "项目代码 NON_EXISTENT 不存在"}

#### Scenario: 依赖的用例不存在
- **GIVEN** 系统正常运行
- **AND** Excel 文件中的某行指定了前置依赖"不存在的前置用例"
- **WHEN** 测试工程师上传该 Excel 文件
- **THEN** 系统返回 200 成功响应
- **AND** 该行导入失败
- **AND** 返回错误详情：{"row": 5, "error": "前置用例"不存在的前置用例"不存在"}

#### Scenario: 大文件导入性能
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师上传包含 500 条测试用例的 Excel 文件
- **THEN** 系统在 30 秒内完成导入
- **AND** 返回导入结果报告

#### Scenario: 下载导入模板
- **GIVEN** 系统已正常运行
- **WHEN** 测试工程师请求 GET /test-cases/import/template
- **THEN** 系统返回 Excel 模板文件
- **AND** 模板包含所有必填列的表头：
  - 用例名称*（必填）
  - 用例描述
  - 测试步骤*（必填）
  - 预期结果
  - 优先级（默认 P2）
  - 状态（默认 DRAFT）
  - 项目代码（多个用逗号分隔）
  - 前置依赖（多个用逗号分隔）

## 验收标准

- [ ] 所有 API 接口符合 GET/POST 规范
- [ ] 所有接口返回统一的 JSON 格式 `{code, message, data, timestamp}`
- [ ] 创建用例时必填字段验证完整
- [ ] 关联项目和依赖关系时正确验证引用存在性
- [ ] 删除用例时正确处理级联删除
- [ ] 所有查询接口支持分页（每页默认 20 条）
- [ ] Service 层单元测试覆盖率 ≥ 80%
- [ ] Controller 层单元测试覆盖率 ≥ 60%

## API 定义

### 1. 创建测试用例

**接口**: `POST /test-cases`

**请求体**:
```json
{
  "name": "用户登录功能测试",
  "description": "验证用户使用正确的用户名和密码可以成功登录",
  "stepsText": "打开登录页面 -> 输入用户名 -> 输入密码 -> 点击登录按钮",
  "stepsJson": "[{\"action\":\"open\",\"target\":\"login_page\"},{\"action\":\"input\",\"target\":\"username\",\"value\":\"testuser\"}]",
  "expectedResult": "成功登录并跳转到首页",
  "priority": "P1",
  "status": "ACTIVE",
  "automationStatus": "MANUAL",
  "projectIds": [1, 2],
  "prerequisiteIds": [5, 7]
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "测试用例创建成功",
  "data": {
    "uniqueId": 1,
    "name": "用户登录功能测试",
    "description": "验证用户使用正确的用户名和密码可以成功登录",
    "stepsText": "打开登录页面 -> 输入用户名 -> 输入密码 -> 点击登录按钮",
    "stepsJson": "[{\"action\":\"open\",\"target\":\"login_page\"}]",
    "expectedResult": "成功登录并跳转到首页",
    "priority": "P1",
    "status": "ACTIVE",
    "automationStatus": "MANUAL",
    "createdBy": 1,
    "updatedBy": 1,
    "createdTime": "2026-02-06T10:00:00",
    "updatedTime": "2026-02-06T10:00:00",
    "projects": [
      {"uniqueId": 1, "name": "用户管理模块", "code": "USER_MGMT"}
    ],
    "dependencies": [
      {"uniqueId": 1, "prerequisiteId": 5, "prerequisiteName": "用户注册测试", "dependencyType": "HARD"}
    ]
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 必填字段缺失或字段值无效
- `400` - 关联的项目不存在
- `400` - 依赖的测试用例不存在

### 2. 查询测试用例详情

**接口**: `GET /test-cases/{uniqueId}`

**路径参数**:
- `uniqueId` (Long) - 测试用例唯一 ID

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "uniqueId": 1,
    "name": "用户登录功能测试",
    "description": "验证用户使用正确的用户名和密码可以成功登录",
    "stepsText": "打开登录页面 -> 输入用户名 -> 输入密码 -> 点击登录按钮",
    "stepsJson": "...",
    "expectedResult": "成功登录并跳转到首页",
    "priority": "P1",
    "status": "ACTIVE",
    "automationStatus": "MANUAL",
    "createdBy": 1,
    "updatedBy": 1,
    "createdTime": "2026-02-06T10:00:00",
    "updatedTime": "2026-02-06T10:00:00",
    "projects": [...],
    "dependencies": [...]
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 测试用例不存在

### 3. 查询所有测试用例

**接口**: `GET /test-cases`

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
    "totalElements": 100,
    "totalPages": 5,
    "pageSize": 20,
    "currentPage": 1
  },
  "timestamp": 1738828800000
}
```

### 4. 更新测试用例

**接口**: `POST /test-cases/{uniqueId}`

**路径参数**:
- `uniqueId` (Long) - 测试用例唯一 ID

**请求体**:
```json
{
  "name": "用户登录功能测试（手机号）",
  "description": "验证用户使用手机号和密码可以成功登录",
  "stepsText": "打开登录页面 -> 输入手机号 -> 输入密码 -> 点击登录按钮",
  "expectedResult": "成功登录并跳转到首页",
  "priority": "P1",
  "status": "ACTIVE",
  "projectIds": [1, 2, 3],
  "prerequisiteIds": [5]
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "测试用例更新成功",
  "data": {
    "uniqueId": 1,
    "name": "用户登录功能测试（手机号）",
    ...
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 必填字段验证失败
- `404` - 测试用例不存在

### 5. 删除测试用例

**接口**: `POST /test-cases/{uniqueId}/delete`

**路径参数**:
- `uniqueId` (Long) - 测试用例唯一 ID

**响应体**:
```json
{
  "code": 200,
  "message": "测试用例已删除",
  "data": null,
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 测试用例不存在

### 6. 按状态查询测试用例

**接口**: `GET /test-cases/by-status`

**查询参数**:
- `status` (String) - 状态值，必须为 DRAFT/ACTIVE/ARCHIVED 之一
- `page` (Integer) - 页码，默认 1
- `size` (Integer) - 每页数量，默认 20

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "totalElements": 50,
    ...
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 状态值无效

### 7. 按优先级查询测试用例

**接口**: `GET /test-cases/by-priority`

**查询参数**:
- `priority` (String) - 优先级值，必须为 P0/P1/P2/P3 之一
- `page` (Integer) - 页码，默认 1
- `size` (Integer) - 每页数量，默认 20

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "totalElements": 20,
    ...
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 优先级值无效

### 8. 按创建人查询测试用例

**接口**: `GET /test-cases/by-creator`

**查询参数**:
- `createdBy` (Long) - 创建人用户 ID
- `page` (Integer) - 页码，默认 1
- `size` (Integer) - 每页数量，默认 20

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "totalElements": 15,
    ...
  },
  "timestamp": 1738828800000
}
```

### 9. 批量导入测试用例

**接口**: `POST /test-cases/import`

**Content-Type**: `multipart/form-data`

**表单参数**:
- `file` (File, 必填) - Excel 文件（.xlsx 或 .xls 格式）
- `strategy` (String, 可选) - 重复数据处理策略，默认 SKIP
  - `SKIP`: 跳过已存在的用例
  - `UPDATE`: 更新已存在的用例

**响应体**:
```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "total": 10,
    "success": 7,
    "failed": 3,
    "errors": [
      {"row": 3, "error": "用例名称不能为空"},
      {"row": 5, "error": "优先级值无效"},
      {"row": 8, "error": "项目代码 NON_EXISTENT 不存在"}
    ],
    "importedTestCases": [
      {"uniqueId": 101, "name": "用户注册功能测试"},
      {"uniqueId": 102, "name": "用户登录功能测试"}
    ]
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 文件格式不正确（仅支持 .xlsx 和 .xls）
- `400` - Excel 文件为空或没有数据行
- `400` - Excel 文件缺少必填列
- `400` - 文件大小超过限制（最大 10MB）

### 10. 下载导入模板

**接口**: `GET /test-cases/import/template`

**响应**:
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename="test_case_import_template.xlsx"`

**Excel 模板结构**:

| 用例名称* | 用例描述 | 测试步骤* | 预期结果 | 优先级 | 状态 | 项目代码 | 前置依赖 |
|---------|---------|----------|---------|-------|-----|---------|---------|
| 用户登录功能测试 | 验证用户使用正确的用户名和密码可以成功登录 | 打开登录页面 -> 输入用户名 -> 输入密码 -> 点击登录按钮 | 成功登录并跳转到首页 | P1 | ACTIVE | USER_MGMT,ORDER_MGMT | 用户注册测试 |
| 用户注册功能测试 | 验证用户可以成功注册账号 | 打开注册页面 -> 填写用户信息 -> 点击注册按钮 | 注册成功并跳转到登录页 | P0 | ACTIVE | USER_MGMT | |

**列说明**:
- **用例名称*** (必填): 测试用例的名称，同一用户下不能重复
- **用例描述**: 测试用例的详细描述
- **测试步骤*** (必填): 测试步骤，支持多种分隔符（->、换行、|）
- **预期结果**: 预期的测试结果
- **优先级**: P0/P1/P2/P3，默认 P2
- **状态**: DRAFT/ACTIVE/ARCHIVED，默认 DRAFT
- **项目代码**: 关联的项目代码，多个项目用逗号分隔
- **前置依赖**: 前置测试用例的名称，多个用例用逗号分隔