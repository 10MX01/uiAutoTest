# 规格说明：脚本管理模块（重构版）

## 1. 功能概述

### 1.1 核心功能
脚本管理模块负责管理测试脚本的完整生命周期，包括：

- **脚本生成**
  - Excel导入测试用例后自动解析生成
  - AI智能生成（运行测试用例时自动触发）

- **脚本状态管理**
  - 启用/禁用机制（同一测试用例只能有一个启用脚本）
  - 物理删除（删除后不可恢复）

- **AI生成管理**
  - 失败重试机制（最多3次）
  - 错误信息记录
  - 生成状态追踪

### 1.2 设计原则
- ✅ 一个测试用例可以有多个脚本记录
- ✅ 同一测试用例的多个脚本中，只能有一个处于启用状态
- ✅ 不支持编辑脚本内容（删除后可重新生成）
- ✅ 支持手动切换脚本的启用/禁用状态

---

## 2. 数据模型

### 2.1 TestScriptEntity（测试脚本实体）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| uniqueId | Long | 是 | 主键ID |
| scriptName | String | 是 | 脚本名称 |
| scriptDescription | String | 否 | 脚本描述 |
| scriptContent | String | 是 | 脚本内容 |
| language | String | 是 | 脚本语言（typescript/javascript） |
| generationMethod | String | 是 | 生成方式（EXCEL_IMPORT/AI_GENERATED） |
| enabled | Boolean | 是 | 是否启用 |
| aiGenerationStatus | String | 是 | AI生成状态（SUCCESS/FAILED/PENDING） |
| aiRetryCount | Integer | 是 | AI重试次数 |
| aiErrorMessage | String | 否 | AI生成失败错误信息 |
| aiModelUsed | String | 否 | 使用的AI模型 |
| aiGenerationTime | Timestamp | 否 | AI生成时间 |
| testCaseId | Long | 是 | 关联的测试用例ID |
| category | String | 否 | 脚本分类 |
| executionCount | Integer | 是 | 执行次数 |
| lastExecutionTime | Timestamp | 否 | 最后执行时间 |
| lastExecutionResult | String | 否 | 最后执行结果 |
| createdBy | Long | 是 | 创建人ID |
| updatedBy | Long | 否 | 更新人ID |
| createdTime | Timestamp | 是 | 创建时间 |
| updatedTime | Timestamp | 是 | 更新时间 |

---

## 3. API接口规格

### 3.1 脚本CRUD操作

#### 创建脚本
```
POST /scripts
Request Body: TestScriptCreateRequest
Response: ApiResponse<Long> (scriptId)
```

**验证规则**：
- scriptName: 非空
- scriptContent: 非空
- testCaseId: 非空且必须存在
- generationMethod: 非空，必须是EXCEL_IMPORT或AI_GENERATED

**业务逻辑**：
- 创建时自动禁用同一测试用例的其他所有脚本
- 如果是AI生成失败，初始enabled=false

#### 删除脚本（物理删除）
```
POST /scripts/delete?uniqueId={scriptId}
Response: ApiResponse<Void>
```

**业务逻辑**：
- 物理删除记录，删除后不可恢复
- 验证脚本ID是否存在

#### 查询脚本详情
```
GET /scripts?uniqueId={scriptId}
Response: ApiResponse<TestScriptResponse>
```

#### 更新脚本基本信息
```
POST /scripts/update-basic-info?scriptId={scriptId}
Request Body: TestScriptUpdateRequest
Response: ApiResponse<Void>
```

**可更新字段**：
- scriptName
- scriptDescription
- category

**不可更新字段**：
- scriptContent（不允许编辑）
- testCaseId（不允许修改关联）

---

### 3.2 脚本查询操作

#### 查询所有脚本
```
GET /scripts/all
Response: ApiResponse<List<TestScriptResponse>>
```

#### 搜索脚本
```
POST /scripts/search
Request Body: ScriptSearchRequest { keyword }
Response: ApiResponse<List<TestScriptResponse>>
```

**搜索范围**：脚本名称、描述、内容

#### 按分类查询
```
GET /scripts/by-category?category={category}
Response: ApiResponse<List<TestScriptResponse>>
```

#### 按生成方式查询
```
GET /scripts/by-generation-method?generationMethod={method}
Response: ApiResponse<List<TestScriptResponse>>
```

---

### 3.3 测试用例关联查询

#### 查询测试用例的启用脚本
```
GET /scripts/enabled-by-testcase?testCaseId={testCaseId}
Response: ApiResponse<TestScriptResponse>
```

#### 查询测试用例的所有脚本
```
GET /scripts/all-by-testcase?testCaseId={testCaseId}
Response: ApiResponse<List<TestScriptResponse>>
```

#### 统计测试用例的脚本数量
```
GET /scripts/count-by-testcase?testCaseId={testCaseId}
Response: ApiResponse<Long>
```

---

### 3.4 启用/禁用管理

#### 切换启用状态
```
POST /scripts/toggle-enabled?scriptId={scriptId}
Response: ApiResponse<Void>
```

**业务逻辑**：
- 如果当前禁用，启用该脚本并禁用同一测试用例的其他脚本
- 如果当前启用，禁用该脚本

#### 更新启用状态
```
POST /scripts/update-enabled?scriptId={scriptId}&enabled={true/false}
Response: ApiResponse<Void>
```

---

### 3.5 AI生成操作

#### AI生成脚本
```
POST /scripts/generate-by-ai?testCaseId={testCaseId}
Response: ApiResponse<TestScriptResponse>
```

**业务逻辑**：
1. 验证测试用例是否存在
2. 禁用同一测试用例的所有脚本
3. 调用AI服务生成脚本内容
4. 保存新生成的脚本并启用
5. 如果AI服务未配置，创建失败状态的脚本

#### 重试失败AI生成
```
POST /scripts/retry-script?scriptId={scriptId}
Response: ApiResponse<TestScriptResponse>
```

**验证条件**：
- AI生成状态必须为FAILED
- 重试次数必须小于3

**业务逻辑**：
1. 禁用同一测试用例的所有脚本
2. 重新调用AI服务生成
3. 更新脚本内容、状态和重试次数

#### 重试所有失败的AI生成
```
POST /scripts/retry-all-failed
Response: ApiResponse<Void>
```

#### 查询失败的AI生成
```
GET /scripts/failed-ai-generations
Response: ApiResponse<List<TestScriptResponse>>
```

---

### 3.6 执行相关操作

#### 获取测试用例的执行脚本
```
GET /scripts/for-execution?testCaseId={testCaseId}
Response: ApiResponse<TestScriptResponse>
```

**业务逻辑**：
1. 优先查找启用状态的脚本
2. 如果没有启用脚本，自动触发AI生成
3. 返回可执行的脚本

#### 增加执行次数
```
POST /scripts/increment-execution?scriptId={scriptId}
Response: ApiResponse<Void>
```

#### 更新执行结果
```
POST /scripts/update-result?scriptId={scriptId}&result={result}
Response: ApiResponse<Void>
```

**result值**：SUCCESS / FAILED / SKIPPED

---

### 3.7 统计信息

#### 查询最常执行的脚本
```
GET /scripts/top-executed?limit={limit}
Response: ApiResponse<List<TestScriptResponse>>
```

---

## 4. 业务规则

### 4.1 启用状态规则
1. 同一测试用例（testCaseId）的多个脚本中，只能有一个enabled=true
2. 启用某个脚本时，自动禁用同一测试用例的其他所有脚本
3. 测试用例执行时，使用enabled=true的脚本

### 4.2 AI生成规则
1. AI生成失败时，记录错误信息和失败时间
2. 失败后可以重试，最多3次
3. 第3次失败后不再自动重试
4. 重试次数达到上限后，需要手动删除并重新生成

### 4.3 删除规则
1. 脚本删除为物理删除，删除后不可恢复
2. 删除前不进行额外验证
3. 删除操作直接从数据库移除记录

### 4.4 脚本内容规则
1. 脚本内容创建后不允许编辑
2. 如需修改脚本内容，删除后重新生成
3. 脚本基本信息（名称、描述、分类）可以修改

---

## 5. 响应格式

### 5.1 TestScriptResponse

```java
{
  "uniqueId": Long,
  "scriptName": String,
  "scriptDescription": String,
  "scriptContent": String,
  "language": String,
  "generationMethod": String,          // EXCEL_IMPORT / AI_GENERATED
  "generationMethodDisplayName": String, // "Excel导入" / "AI生成"
  "enabled": Boolean,
  "aiGenerationStatus": String,         // SUCCESS / FAILED / PENDING
  "aiGenerationStatusDisplayName": String, // "成功" / "失败" / "生成中"
  "aiRetryCount": Integer,
  "aiErrorMessage": String,
  "aiModelUsed": String,
  "aiGenerationTime": Timestamp,
  "testCaseId": Long,
  "testCaseName": String,              // 关联测试用例名称
  "category": String,
  "executionCount": Integer,
  "lastExecutionTime": Timestamp,
  "lastExecutionResult": String,
  "createdBy": Long,
  "updatedBy": Long,
  "createdTime": Timestamp,
  "updatedTime": Timestamp
}
```

### 5.2 ApiResponse格式

```java
{
  "code": Integer,          // 200表示成功
  "message": String,        // 响应消息
  "data": T,               // 响应数据
  "timestamp": Long        // 时间戳
}
```

---

## 6. 错误处理

### 6.1 常见错误码

| 错误场景 | HTTP状态码 | 错误消息 |
|---------|-----------|---------|
| 脚本不存在 | 500 | "脚本不存在: {scriptId}" |
| 测试用例不存在 | 500 | "测试用例不存在: {testCaseId}" |
| AI生成失败 | 200 | 创建失败状态的脚本记录 |
| 重试次数超限 | 500 | "不满足重试条件" |
| 验证失败 | 400 | 验证错误消息 |

### 6.2 异常处理策略

1. **实体不存在**
   - Service层抛出RuntimeException
   - Controller层返回500状态码

2. **AI服务未配置**
   - 创建失败状态的脚本
   - 记录错误信息"AI服务未配置"
   - 脚本enabled=false

3. **业务规则违反**
   - 返回明确的错误消息
   - 不允许违反规则的操作

---

## 7. 性能要求

1. 查询操作响应时间 < 500ms
2. 创建/更新操作响应时间 < 1s
3. AI生成操作响应时间 < 5s（取决于AI服务）
4. 支持并发操作

---

## 8. 安全要求

1. 创建人ID和更新人ID自动从当前用户获取
2. 不允许脚本内容包含恶意代码
3. AI生成时进行内容验证

---

## 9. 测试要求

### 9.1 单元测试覆盖
- Repository层：查询方法测试
- Service层：业务逻辑测试（创建、删除、启用/禁用、AI生成）
- Controller层：API接口测试

### 9.2 集成测试场景
- Excel导入完整流程
- 运行测试用例时自动生成脚本
- 脚本启用/禁用状态切换
- AI生成失败重试
- 物理删除操作

---

## 10. 非功能需求

1. **可维护性**
   - 代码结构清晰
   - 充分的注释和文档

2. **可扩展性**
   - 预留扩展点
   - 支持新增生成方式

3. **可测试性**
   - 依赖注入
   - Mock支持

---

**文档版本**: 1.0
**最后更新**: 2025-02-08
**变更说明**: 移除软删除功能，改为物理删除；移除版本控制、在线编辑器等高级功能
