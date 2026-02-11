# Spec: 测试用例搜索和过滤

## 模块概述

本模块提供测试用例的高级搜索和过滤功能，支持关键词全文搜索和多维度组合过滤，帮助测试工程师快速定位目标测试用例。

## 需求列表

### REQ-1: 关键词全文搜索
作为测试工程师，我想要通过关键词搜索测试用例，以便快速找到包含特定内容的用例。

### REQ-2: 多维度组合过滤
作为测试工程师，我想要组合多个过滤条件（状态、优先级、项目）查询测试用例，以便精确筛选目标用例。

### REQ-3: 搜索结果排序
作为测试工程师，我想要对搜索结果进行排序，以便按相关性或时间查看用例。

### REQ-4: 搜索结果分页
作为测试工程师，我想要分页查看搜索结果，以便高效浏览大量数据。

## 功能场景

### REQ-1: 关键词全文搜索

#### Scenario: 按名称关键词搜索
- **GIVEN** 系统中存在 100 个测试用例
- **AND** 其中 5 个用例名称包含"登录"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=登录
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 5 个名称包含"登录"的用例
- **AND** 返回列表按创建时间倒序排列

#### Scenario: 按描述关键词搜索
- **GIVEN** 系统中存在测试用例，其中 3 个用例描述中包含"手机号"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=手机号
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 3 个描述包含"手机号"的用例

#### Scenario: 按测试步骤关键词搜索
- **GIVEN** 系统中存在测试用例，其中 2 个用例测试步骤包含"点击登录按钮"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=点击登录按钮
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 2 个测试步骤包含该关键词的用例

#### Scenario: 跨字段全文搜索
- **GIVEN** 系统中存在多个测试用例
- **AND** 用例 A 名称包含"注册"
- **AND** 用例 B 描述包含"注册"
- **AND** 用例 C 测试步骤包含"注册"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=注册
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含用例 A、B、C
- **AND** 名称匹配的用例排在前面（相关性更高）

#### Scenario: 搜索关键词为空
- **GIVEN** 系统中存在 100 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示搜索关键词不能为空

#### Scenario: 搜索无匹配结果
- **GIVEN** 系统中存在 100 个测试用例
- **AND** 没有任何用例包含关键词"区块链"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=区块链
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []

#### Scenario: 特殊字符搜索
- **GIVEN** 系统中存在测试用例名称包含"用户名/密码"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=用户名/密码
- **THEN** 系统正确处理特殊字符
- **AND** 返回匹配的测试用例

### REQ-2: 多维度组合过滤

#### Scenario: 按状态和优先级组合过滤
- **GIVEN** 系统中存在 50 个测试用例
- **AND** 其中 10 个状态为 ACTIVE 且优先级为 P0
- **WHEN** 测试工程师请求 GET /test-cases?status=ACTIVE&priority=P0
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 10 个同时满足两个条件的用例
- **AND** 不包含状态为 DRAFT 或优先级为 P1 的用例

#### Scenario: 按项目和状态组合过滤
- **GIVEN** 项目（ID=1）下存在 20 个测试用例
- **AND** 其中 8 个状态为 ACTIVE
- **WHEN** 测试工程师请求 GET /projects/1/test-cases?status=ACTIVE
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 8 个项目 1 下状态为 ACTIVE 的用例

#### Scenario: 按创建人和优先级组合过滤
- **GIVEN** 用户 1 创建了 15 个测试用例
- **AND** 其中 3 个优先级为 P0
- **WHEN** 测试工程师请求 GET /test-cases/by-creator?createdBy=1&priority=P0
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 3 个用户 1 创建的优先级为 P0 的用例

#### Scenario: 三维度组合过滤
- **GIVEN** 系统中存在大量测试用例
- **WHEN** 测试工程师请求 GET /test-cases?status=ACTIVE&priority=P0&createdBy=1
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表只包含同时满足三个条件的用例

#### Scenario: 无效的过滤参数值
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师请求 GET /test-cases?status=INVALID_STATUS
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示状态值无效

### REQ-3: 搜索结果排序

#### Scenario: 按创建时间倒序排列
- **GIVEN** 系统中存在 10 个测试用例
- **AND** 创建时间分别为最近到最旧
- **WHEN** 测试工程师请求 GET /test-cases?sortBy=createdTime&sortOrder=desc
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表第一个用例是最新的
- **AND** 返回列表最后一个用例是最旧的

#### Scenario: 按创建时间正序排列
- **GIVEN** 系统中存在 10 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases?sortBy=createdTime&sortOrder=asc
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表第一个用例是最旧的
- **AND** 返回列表最后一个用例是最新的

#### Scenario: 按优先级排序（P0 > P1 > P2 > P3）
- **GIVEN** 系统中存在不同优先级的测试用例
- **WHEN** 测试工程师请求 GET /test-cases?sortBy=priority&sortOrder=asc
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表按优先级从高到低排列（P0、P1、P2、P3）

#### Scenario: 按名称字母顺序排序
- **GIVEN** 系统中存在 20 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases?sortBy=name&sortOrder=asc
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表按名称字母顺序（A-Z）排列

#### Scenario: 默认排序规则
- **GIVEN** 系统中存在 10 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases（不指定排序参数）
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表默认按创建时间倒序排列

#### Scenario: 无效的排序字段
- **GIVEN** 系统正常运行
- **WHEN** 测试工程师请求 GET /test-cases?sortBy=invalidField
- **THEN** 系统返回 400 错误
- **AND** 错误消息提示排序字段无效

### REQ-4: 搜索结果分页

#### Scenario: 第一页数据
- **GIVEN** 系统中存在 100 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases?page=1&size=20
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含第 1-20 个用例
- **AND** totalElements = 100
- **AND** totalPages = 5
- **AND** currentPage = 1

#### Scenario: 请求第二页数据
- **GIVEN** 系统中存在 100 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases?page=2&size=20
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含第 21-40 个用例
- **AND** currentPage = 2

#### Scenario: 请求最后一页数据
- **GIVEN** 系统中存在 100 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases?page=5&size=20
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含第 81-100 个用例

#### Scenario: 页码超出范围
- **GIVEN** 系统中存在 100 个测试用例（共 5 页）
- **WHEN** 测试工程师请求 GET /test-cases?page=999&size=20
- **THEN** 系统返回 200 成功响应
- **AND** 返回空数组 []
- **AND** 或者系统返回 400 错误，提示页码超出范围

#### Scenario: 自定义每页数量
- **GIVEN** 系统中存在 50 个测试用例
- **WHEN** 测试工程师请求 GET /test-cases?page=1&size=10
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 10 个用例
- **AND** totalPages = 5

#### Scenario: 每页数量超过最大限制
- **GIVEN** 系统限制每页最多 100 条
- **WHEN** 测试工程师请求 GET /test-cases?page=1&size=200
- **THEN** 系统返回 200 成功响应
- **AND** 系统自动将 size 调整为 100
- **AND** totalPages 根据实际 size=100 计算

#### Scenario: 搜索结果分页
- **GIVEN** 系统中存在 100 个测试用例
- **AND** 其中 30 个用例名称包含"登录"
- **WHEN** 测试工程师请求 GET /test-cases/search?keyword=登录&page=1&size=10
- **THEN** 系统返回 200 成功响应
- **AND** 返回列表包含 10 个匹配的用例
- **AND** totalElements = 30
- **AND** totalPages = 3

## 验收标准

- [ ] 关键词搜索覆盖名称、描述、测试步骤三个字段
- [ ] 多维度组合过滤正确实现 AND 逻辑
- [ ] 排序功能支持所有常用字段
- [ ] 分页功能正确计算总页数和当前页
- [ ] 分页查询性能良好（大数据量下）
- [ ] 搜索结果按相关性排序（关键词匹配多的靠前）
- [ ] Service 层单元测试覆盖率 ≥ 80%
- [ ] Controller 层单元测试覆盖率 ≥ 60%

## API 定义

### 1. 关键词搜索

**接口**: `GET /test-cases/search`

**查询参数**:
- `keyword` (String, 必填) - 搜索关键词
- `page` (Integer, 可选) - 页码，默认 1
- `size` (Integer, 可选) - 每页数量，默认 20，最大 100
- `sortBy` (String, 可选) - 排序字段（createdTime/name/priority），默认 createdTime
- `sortOrder` (String, 可选) - 排序方向（asc/desc），默认 desc

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
        "description": "验证用户使用正确的用户名和密码可以成功登录",
        "status": "ACTIVE",
        "priority": "P1",
        "createdTime": "2026-02-06T10:00:00"
      }
    ],
    "totalElements": 30,
    "totalPages": 3,
    "pageSize": 10,
    "currentPage": 1
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 搜索关键词为空
- `400` - 排序字段或方向无效

### 2. 多维度组合过滤

**接口**: `GET /test-cases`

**查询参数**:
- `status` (String, 可选) - 状态过滤（DRAFT/ACTIVE/ARCHIVED）
- `priority` (String, 可选) - 优先级过滤（P0/P1/P2/P3）
- `createdBy` (Long, 可选) - 创建人 ID 过滤
- `projectId` (Long, 可选) - 项目 ID 过滤
- `page` (Integer, 可选) - 页码，默认 1
- `size` (Integer, 可选) - 每页数量，默认 20
- `sortBy` (String, 可选) - 排序字段
- `sortOrder` (String, 可选) - 排序方向

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
        "priority": "P0",
        "createdBy": 1,
        "createdTime": "2026-02-06T10:00:00",
        "projects": [
          {"uniqueId": 1, "name": "用户管理模块"}
        ]
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "pageSize": 20,
    "currentPage": 1
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `400` - 过滤参数值无效

### 3. 项目下的测试用例过滤

**接口**: `GET /projects/{uniqueId}/test-cases`

**路径参数**:
- `uniqueId` (Long) - 项目唯一 ID

**查询参数**:
- `status` (String, 可选) - 状态过滤
- `priority` (String, 可选) - 优先级过滤
- `page` (Integer, 可选) - 页码
- `size` (Integer, 可选) - 每页数量
- `sortBy` (String, 可选) - 排序字段
- `sortOrder` (String, 可选) - 排序方向

**响应体**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "totalElements": 8,
    ...
  },
  "timestamp": 1738828800000
}
```

**错误码**:
- `404` - 项目不存在
- `400` - 过滤参数值无效

### 4. 创建人下的测试用例过滤

**接口**: `GET /test-cases/by-creator`

**查询参数**:
- `createdBy` (Long, 必填) - 创建人用户 ID
- `status` (String, 可选) - 状态过滤
- `priority` (String, 可选) - 优先级过滤
- `page` (Integer, 可选) - 页码
- `size` (Integer, 可选) - 每页数量
- `sortBy` (String, 可选) - 排序字段
- `sortOrder` (String, 可选) - 排序方向

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

**错误码**:
- `400` - 过滤参数值无效