# AI服务接入能力规范

## ADDED Requirements

### Requirement: 管理员可以配置AI服务
系统 SHALL 提供管理页面界面，允许管理员配置和管理AI服务接入信息。

#### Scenario: 创建新的AI服务配置
- **WHEN** 管理员在管理页面填写完整的AI服务配置信息（服务提供商、API密钥、模型名称）
- **THEN** 系统保存配置并返回成功响应
- **AND** 系统自动触发连接性验证

#### Scenario: 更新现有AI服务配置
- **WHEN** 管理员修改已存在的AI服务配置
- **THEN** 系统更新配置信息
- **AND** 系统重新触发连接性验证以确认新配置可用

#### Scenario: 删除AI服务配置
- **WHEN** 管理员删除AI服务配置
- **THEN** 系统移除该配置
- **AND** 系统标记该AI服务为不可用状态

### Requirement: 系统支持多家AI服务提供商
系统 SHALL 支持接入多家主流AI服务提供商，包括但不限于：OpenAI、Claude（Anthropic）、通义千问（阿里云）、文心一言（百度）。

#### Scenario: 配置OpenAI服务
- **WHEN** 管理员选择服务提供商为"OpenAI"并填写API密钥
- **THEN** 系统使用OpenAI SDK进行连接
- **AND** 系统支持配置自定义API端点（可选）

#### Scenario: 配置Claude服务
- **WHEN** 管理员选择服务提供商为"Claude"并填写API密钥
- **THEN** 系统使用Anthropic SDK进行连接
- **AND** 系统支持配置自定义API端点（可选）

#### Scenario: 配置通义千问服务
- **WHEN** 管理员选择服务提供商为"通义千问"并填写API密钥
- **THEN** 系统使用阿里云SDK进行连接
- **AND** 系统支持配置自定义API端点（可选）

#### Scenario: 配置文心一言服务
- **WHEN** 管理员选择服务提供商为"文心一言"并填写API密钥
- **THEN** 系统使用百度SDK进行连接
- **AND** 系统支持配置自定义API端点（可选）

### Requirement: 系统支持自定义HTTP端点接入方式
系统 SHALL 支持通过自定义HTTP端点接入AI服务，允许用户配置自己的AI服务代理或兼容OpenAI格式的服务。

#### Scenario: 配置自定义HTTP端点
- **WHEN** 管理员选择服务提供商为"自定义端点（Custom Endpoint）"
- **THEN** 系统要求填写：端点URL（必填）、请求格式（可选，默认OpenAI兼容）
- **AND** 系统可选配置：自定义请求头、API密钥（如端点需要）
- **AND** 系统不强制要求特定认证方式

#### Scenario: 使用OpenAI兼容格式调用自定义端点
- **WHEN** 系统调用配置为自定义端点的AI服务
- **THEN** 系统按照OpenAI API格式发送POST请求到配置的URL
- **AND** 请求体包含：`model`、`messages`、`temperature`、`max_tokens`等标准字段
- **AND** 系统解析返回的OpenAI格式响应：`choices[0].message.content`
- **AND** 用户服务端负责处理请求并返回兼容格式的响应

#### Scenario: 自定义请求头
- **WHEN** 管理员配置自定义HTTP端点
- **AND** 添加自定义请求头（如：`{"X-Custom-Auth": "token123"}`）
- **THEN** 系统在每次请求时附加这些请求头
- **AND** 系统支持请求头模板变量（如：`${timestamp}`）

#### Scenario: 自定义端点不需要API密钥
- **WHEN** 管理员配置自定义HTTP端点
- **AND** 用户服务端自行处理认证（如通过请求头或IP白名单）
- **THEN** 系统允许不填写API密钥
- **AND** 系统不发送Authorization请求头（除非用户自定义）

#### Scenario: 验证自定义端点连接性
- **WHEN** 管理员保存自定义HTTP端点配置
- **THEN** 系统发送简单的测试请求到该端点
- **AND** 测试请求包含标准的prompt文本
- **AND** 系统根据HTTP响应判断连接是否成功（2xx状态码且返回有效响应）

#### Scenario: 企业内部AI服务网关接入
- **WHEN** 企业部署了统一的AI服务网关
- **AND** 网关提供OpenAI兼容的HTTP接口
- **THEN** 管理员可以通过自定义端点方式接入该网关
- **AND** 系统不需要知道网关后端实际使用哪个AI服务
- **AND** 网关负责负载均衡、认证、计费等逻辑

#### Scenario: 私有化部署模型接入
- **WHEN** 用户使用私有化部署的开源模型（如Llama、Qwen等）
- **AND** 模型服务提供OpenAI兼容的HTTP接口
- **THEN** 管理员可以通过自定义端点方式接入
- **AND** 系统可以直接调用内网地址的模型服务
- **AND** 数据不经过公网，更安全

### Requirement: 配置项仅包含接入必需信息
系统 SHALL 仅要求管理员填写AI服务接入必需的信息，不包括token管理、流量控制、成本统计等额外功能。

#### Scenario: 最小化配置项
- **WHEN** 管理员配置AI服务
- **THEN** 系统仅要求填写：服务提供商、API密钥、模型名称
- **AND** 系统提供可选的API端点配置（用于私有化部署或代理）

#### Scenario: 不包含token管理
- **WHEN** 管理员查看AI服务配置
- **THEN** 系统不显示或要求配置token限制、quota等信息
- **AND** 系统不统计token使用量

#### Scenario: 不包含流量控制
- **WHEN** 管理员查看AI服务配置
- **THEN** 系统不提供每分钟请求数、并发限制等流量控制配置
- **AND** 系统不实施任何速率限制

#### Scenario: 不包含成本统计
- **WHEN** 管理员查看AI服务管理页面
- **THEN** 系统不显示AI服务成本统计
- **AND** 系统不计算或展示费用信息

### Requirement: 系统自动验证AI服务连接性
系统 SHALL 在配置保存后自动发送测试请求验证API可用性。

#### Scenario: 保存配置后自动验证
- **WHEN** 管理员保存AI服务配置
- **THEN** 系统立即发送一个简单的测试请求到AI服务
- **AND** 系统根据响应更新服务状态为"可用"或"不可用"
- **AND** 系统显示验证结果给管理员

#### Scenario: 手动触发连接测试
- **WHEN** 管理员点击"测试连接"按钮
- **THEN** 系统发送测试请求到AI服务
- **AND** 系统返回验证结果和响应时间

#### Scenario: 验证失败显示错误信息
- **WHEN** AI服务连接性验证失败
- **THEN** 系统标记服务状态为"不可用"
- **AND** 系统显示具体的错误信息（如：API密钥无效、网络连接失败、超时等）

### Requirement: 系统维护AI服务状态
系统 SHALL 为每个AI服务配置维护状态标识：可用、不可用、未测试。

#### Scenario: 新建配置初始状态
- **WHEN** 管理员创建新的AI服务配置但尚未测试
- **THEN** 系统标记该服务状态为"未测试"

#### Scenario: 验证成功更新状态
- **WHEN** AI服务连接性验证成功
- **THEN** 系统更新服务状态为"可用"
- **AND** 系统记录最后验证时间

#### Scenario: 验证失败更新状态
- **WHEN** AI服务连接性验证失败
- **THEN** 系统更新服务状态为"不可用"
- **AND** 系统记录失败原因和时间

### Requirement: 系统提供统一的LLM服务抽象层
系统 SHALL 提供统一的LLM服务接口，屏蔽不同AI服务商的差异。

#### Scenario: 统一的文本生成接口
- **WHEN** 业务代码调用LLM服务
- **THEN** 系统提供统一的接口方法：`generateText(prompt, model)`
- **AND** 系统根据配置路由到具体的AI服务实现
- **AND** 业务代码无需关心底层使用的AI服务提供商

#### Scenario: 运行时动态切换AI服务
- **WHEN** 管理员修改默认AI服务配置
- **THEN** 系统在下次请求时使用新的AI服务
- **AND** 系统无需重启即可生效

#### Scenario: AI服务实现类的隔离
- **WHEN** 系统加载AI服务实现
- **THEN** 系统为每个AI服务提供商创建独立的实现类
- **AND** 所有实现类都实现统一的`LLMService`接口
- **AND** 系统根据配置动态选择使用哪个实现类

### Requirement: 系统支持多AI服务配置并存
系统 SHALL 允许管理员同时配置多个AI服务，并支持指定默认服务。

#### Scenario: 配置多个AI服务
- **WHEN** 管理员配置多个不同的AI服务（如OpenAI和Claude）
- **THEN** 系统保存所有配置
- **AND** 系统为每个配置维护独立的状态

#### Scenario: 设置默认AI服务
- **WHEN** 管理员将某个AI服务标记为"默认"
- **THEN** 系统在没有明确指定服务时使用该默认服务
- **AND** 系统在同一时间只允许一个默认服务

#### Scenario: 调用特定AI服务
- **WHEN** 业务代码在调用时指定AI服务ID
- **THEN** 系统使用指定的AI服务执行请求
- **AND** 系统忽略默认服务设置

### Requirement: API密钥安全存储
系统 SHALL 安全地存储AI服务的API密钥，防止泄露。

#### Scenario: 加密存储API密钥
- **WHEN** 系统保存AI服务配置
- **THEN** 系统对API密钥进行加密后存储
- **AND** 系统在内存中解密后使用

#### Scenario: 查看配置时脱敏显示
- **WHEN** 管理员查看AI服务配置列表
- **THEN** 系统不显示完整的API密钥
- **AND** 系统仅显示部分密钥（如：sk-proj••••1234）

#### Scenario: 导出配置时不包含密钥
- **WHEN** 管理员导出AI服务配置
- **THEN** 系统不导出API密钥
- **AND** 导出的配置需要重新填写密钥后才能使用

### Requirement: AI服务配置查询接口
系统 SHALL 提供API接口用于查询AI服务配置和状态。

#### Scenario: 获取所有AI服务配置
- **WHEN** 客户端调用GET /api/ai-service/configs
- **THEN** 系统返回所有AI服务配置的列表
- **AND** 返回信息包含：服务提供商、模型名称、状态、创建时间
- **AND** 返回信息不包含完整的API密钥

#### Scenario: 获取单个AI服务配置详情
- **WHEN** 客户端调用GET /api/ai-service/configs/{id}
- **THEN** 系统返回指定AI服务的详细配置
- **AND** 返回信息不包含完整的API密钥

#### Scenario: 获取当前可用的AI服务列表
- **WHEN** 客户端调用GET /api/ai-service/available
- **THEN** 系统仅返回状态为"可用"的AI服务
- **AND** 系统过滤掉"不可用"和"未测试"的服务

### Requirement: AI服务配置CRUD接口
系统 SHALL 提供完整的API接口用于创建、读取、更新、删除AI服务配置。

#### Scenario: 创建AI服务配置
- **WHEN** 客户端调用POST /api/ai-service/configs
- **AND** 请求体包含：provider、apiKey、modelName、apiEndpoint（可选）
- **THEN** 系统创建新的AI服务配置
- **AND** 系统自动触发连接性验证
- **AND** 系统返回创建的配置ID和初始状态

#### Scenario: 更新AI服务配置
- **WHEN** 客户端调用PUT /api/ai-service/configs/{id}
- **AND** 请求体包含需要更新的字段
- **THEN** 系统更新指定的配置字段
- **AND** 系统重新触发连接性验证
- **AND** 系统返回更新后的配置

#### Scenario: 删除AI服务配置
- **WHEN** 客户端调用DELETE /api/ai-service/configs/{id}
- **THEN** 系统删除指定的AI服务配置
- **AND** 系统返回204 No Content状态码

#### Scenario: 删除默认服务时的验证
- **WHEN** 客户端尝试删除当前标记为默认的AI服务
- **AND** 存在其他可用的AI服务配置
- **THEN** 系统允许删除
- **AND** 系统自动将另一个服务标记为默认
- **OR** 如果不存在其他服务，系统返回错误提示"至少需要保留一个AI服务配置"

### Requirement: AI服务测试接口
系统 SHALL 提供API接口用于手动触发AI服务连接性测试。

#### Scenario: 测试指定AI服务
- **WHEN** 客户端调用POST /api/ai-service/configs/{id}/test
- **THEN** 系统发送测试请求到指定的AI服务
- **AND** 系统返回测试结果（成功/失败）、响应时间、错误信息（如适用）

#### Scenario: 测试前验证配置完整性
- **WHEN** 客户端请求测试AI服务
- **AND** 配置缺少必需字段（如API密钥）
- **THEN** 系统返回400错误
- **AND** 系统提示缺少必需的配置项