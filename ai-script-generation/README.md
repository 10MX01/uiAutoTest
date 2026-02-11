# AI脚本生成模块

## 功能概述

本模块提供AI驱动的Web自动化测试脚本生成能力，支持从自然语言描述到可执行测试脚本的完整流程。

## 核心功能

### 1. AI服务配置管理
- 支持多种AI服务接入（OpenAI、Anthropic、自定义HTTP端点）
- API密钥加密存储
- 连接性测试
- 默认服务配置

### 2. 自然语言解析
- 将自然语言测试步骤转换为结构化JSON
- 支持批量解析
- AI调用日志记录

### 3. 页面快照捕获
- 基于Playwright的页面快照
- 提取可交互元素
- 生成多种选择器（按优先级：testId > id > name > aria > text > css）

### 4. 两阶段脚本生成
- **阶段1**: 自然语言 → JSON（操作意图）
- **阶段2**: JSON + 页面快照 → 脚本（含选择器）
- 备用选择器机制
- 置信度评分

### 5. 测试用例执行
- 自动生成包含选择器的测试脚本
- 基于Playwright的脚本执行
- 执行结果记录
- 截图功能

## API接口

### AI服务配置
- `GET /api/ai-service/configs` - 查询所有配置
- `GET /api/ai-service/configs/detail` - 查询配置详情
- `GET /api/ai-service/available` - 查询可用配置
- `POST /api/ai-service/configs/create` - 创建配置
- `POST /api/ai-service/configs/update` - 更新配置
- `POST /api/ai-service/configs/delete` - 删除配置
- `POST /api/ai-service/configs/test` - 测试连接
- `POST /api/ai-service/configs/set-default` - 设置默认配置

### AI脚本生成
- `POST /api/ai/parse-steps` - 解析自然语言步骤
- `POST /api/ai/parse-steps/batch` - 批量解析
- `POST /api/ai/generate-script/two-phase` - 两阶段生成脚本
- `POST /api/ai/generate-script/from-json` - 基于JSON和快照生成
- `GET /api/ai/snapshot/export` - 导出页面快照

### 测试用例集成
- `POST /api/testcases-ai/create-with-ai` - 创建带AI功能的测试用例
- `POST /api/testcases-ai/parse-steps` - 仅解析步骤
- `POST /api/testcases-ai/parse-steps-objects` - 解析并返回对象

### 测试用例执行
- `POST /api/executions/execute` - 执行测试用例
- `GET /api/executions/testcase/{testCaseId}` - 查询执行记录
- `GET /api/executions/{executionId}/detail` - 查询执行详情

## 数据模型

### 核心实体
- `AIServiceConfigEntity` - AI服务配置
- `PromptTemplateEntity` - Prompt模板
- `AICallLogEntity` - AI调用日志
- `TestCaseExecutionEntity` - 测试用例执行记录

### 数据模型
- `TestStep` - 测试步骤（阶段1，操作意图）
- `TestStepWithSelectors` - 测试步骤（阶段2，含选择器）
- `PageSnapshot` - 页面快照
- `InteractiveElement` - 可交互元素

## 配置说明

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/uiaut_test
    username: root
    password: root

  jpa:
    hibernate:
      ddl-auto: none

jasypt:
  encryptor:
    password: uiaut-test-2024-ai

ai:
  service:
    default-timeout: 30000
```

## 使用流程

### 1. 配置AI服务
```bash
POST /api/ai-service/configs/create
{
  "provider": "openai",
  "modelName": "gpt-4",
  "apiKey": "sk-xxx",
  "isDefault": true
}
```

### 2. 创建测试用例（自动解析）
```bash
POST /api/testcases-ai/create-with-ai
{
  "name": "用户登录测试",
  "projectId": 1,
  "targetUrl": "https://example.com/login",
  "stepsText": "1. 打开登录页面\n2. 输入用户名admin\n3. 输入密码123456\n4. 点击登录按钮"
}
```

### 3. 执行测试用例
```bash
POST /api/executions/execute
{
  "testCaseId": 1,
  "overrideUrl": "https://test.example.com/login"
}
```

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Spring Data JPA
- Playwright Java 1.40.0
- MySQL 8.0+
- Lombok
- Gson
- OkHttp
- Jasypt

## 项目结构

```
ai-script-generation/
├── src/main/java/com/uiauto/aiscript/
│   ├── entity/          # 实体类
│   ├── repository/      # 数据访问层
│   ├── service/         # 业务逻辑接口
│   ├── service/impl/    # 业务逻辑实现
│   ├── controller/      # 控制器
│   ├── dto/             # 请求DTO
│   ├── vo/              # 响应VO
│   ├── model/           # 数据模型
│   └── exception/       # 自定义异常
└── src/main/resources/
    └── application.yml  # 配置文件
```

## 数据库表

- `ai_service_configs` - AI服务配置
- `prompt_templates` - Prompt模板
- `ai_call_logs` - AI调用日志
- `test_case_executions` - 测试用例执行记录
- `projects` - 项目表（扩展）
- `test_cases` - 测试用例表（扩展）

## 注意事项

1. **Java 8兼容性**: 所有代码使用Java 8语法，不使用Java 9+特性
2. **javax命名空间**: 使用`javax.persistence.*`而非`jakarta.*`
3. **API规范**: 只使用GET和POST方法
4. **数据库标准**: 所有表包含5个标准字段（unique_id, created_by, updated_by, created_time, updated_time）
5. **异常处理**: 使用友好的错误提示，不暴露技术细节
