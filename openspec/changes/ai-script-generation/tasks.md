# AI脚本生成功能实现任务清单

本文档列出AI脚本生成功能的所有实现任务，按依赖关系和模块分组。

---

## Phase 1: 项目结构与依赖配置

### 1.1 模块依赖配置

- [x] TASK-1.1.1: 在ai-script-generation/pom.xml中添加Spring Boot依赖
  - 描述: 添加spring-boot-starter-web、spring-boot-starter-data-jpa依赖
  - 验收: pom.xml包含必要的Spring Boot依赖

- [x] TASK-1.1.2: 添加HTTP客户端依赖
  - 描述: 添加okhttp或apache-httpclient依赖用于调用AI服务
  - 验收: 可以发送HTTP请求

- [x] TASK-1.1.3: 添加Jasypt加密库依赖
  - 描述: 添加jasypt-spring-boot-starter用于API密钥加密
  - 验收: 依赖添加成功

- [x] TASK-1.1.4: 添加Playwright Java依赖
  - 描述: 添加com.microsoft.playwright:playwright依赖
  - 验收: 依赖添加成功

- [x] TASK-1.1.5: 添加JSON处理依赖
  - 描述: 添加gson或jackson依赖用于JSON序列化
  - 验收: 依赖添加成功

- [x] TASK-1.1.6: 验证父pom.xml包含ai-script-generation模块
  - 描述: 确认父pom.xml的modules中包含ai-script-generation
  - 验收: 父pom.xml正确引用子模块

---

## Phase 2: 数据库设计与表创建

### 2.1 建表SQL脚本

- [x] TASK-2.1.1: 在E:\myproject\UiAutoTest\db\table.sql中添加ai_service_configs表
  - 描述: 创建AI服务配置表，包含unique_id、provider、model_name、api_key、api_endpoint、custom_headers、status等字段
  - 验收: 表创建语句包含5个标准字段，使用unique_id作为主键

- [x] TASK-2.1.2: 在table.sql中添加prompt_templates表
  - 描述: 创建Prompt模板表，包含unique_id、template_name、template_type、prompt_content等字段
  - 验收: 表创建语句符合规范

- [x] TASK-2.1.3: 在table.sql中添加ai_call_logs表
  - 描述: 创建AI调用日志表，包含unique_id、call_type、input_text、output_json、success等字段
  - 验收: 表创建语句包含外键引用，使用unique_id

- [x] TASK-2.1.4: 执行table.sql脚本
  - 描述: 在MySQL数据库中执行table.sql，验证表创建成功
  - 验收: 三张表成功创建，包含所有字段和索引

### 2.2 初始化数据

- [x] TASK-2.2.1: 在E:\myproject\UiAutoTest\db\data.sql中添加natural_language_parse模板
  - 描述: 插入自然语言解析的初始Prompt模板
  - 验收: data.sql包含INSERT语句

- [x] TASK-2.2.2: 在data.sql中添加script_generation模板
  - 描述: 插入脚本生成的初始Prompt模板
  - 验收: data.sql包含完整的Prompt内容

- [x] TASK-2.2.3: 执行data.sql脚本
  - 描述: 在数据库中执行data.sql，验证初始数据插入成功
  - 验收: prompt_templates表中包含两条初始记录

---

## Phase 3: 基础实体与Repository层

### 3.1 实体类

- [x] TASK-3.1.1: 创建BaseEntity基类
  - 描述: 在common模块中创建BaseEntity，包含unique_id、created_by、updated_by、created_time、updated_time
  - 验收: 使用JPA注解，Lombok注解

- [x] TASK-3.1.2: 创建AIServiceConfigEntity实体
  - 描述: 继承BaseEntity，添加provider、model_name、api_key、api_endpoint等字段
  - 验收: 使用javax.persistence.*注解，字段与数据库表对应

- [x] TASK-3.1.3: 创建PromptTemplateEntity实体
  - 描述: 继承BaseEntity，添加template_name、template_type、prompt_content等字段
  - 验收: 实体类编译通过

- [x] TASK-3.1.4: 创建AICallLogEntity实体
  - 描述: 继承BaseEntity，添加call_type、input_text、output_json等字段
  - 验收: 实体类包含外键关联关系

### 3.2 Repository层

- [x] TASK-3.2.1: 创建AIServiceConfigRepository接口
  - 描述: 继承JpaRepository，添加findByStatus、findByIsDefault等查询方法
  - 验收: 接口编译通过

- [x] TASK-3.2.2: 创建PromptTemplateRepository接口
  - 描述: 继承JpaRepository，添加findByTemplateTypeAndIsActive等查询方法
  - 验收: 接口编译通过

- [x] TASK-3.2.3: 创建AICallLogRepository接口
  - 描述: 继承JpaRepository，添加findByCallType、findBySuccess等方法
  - 验收: 接口编译通过

---

## Phase 4: AI服务接入功能

### 4.1 HTTP客户端服务

- [x] TASK-4.1.1: 创建LLMService接口
  - 描述: 定义统一的AI服务接口，包含generateText方法
  - 验收: 接口定义清晰

- [x] TASK-4.1.2: 创建HttpLLMService实现类
  - 描述: 实现基于HTTP的AI服务调用，支持自定义端点、自定义请求头
  - 验收: 可以发送POST请求到自定义端点

- [x] TASK-4.1.3: 实现OpenAI兼容格式请求构建
  - 描述: 按照OpenAI API格式构建请求体（model、messages、temperature等）
  - 验收: 请求体符合OpenAI格式

- [x] TASK-4.1.4: 实现自定义请求头模板变量替换
  - 描述: 支持在自定义请求头中使用${timestamp}等变量
  - 验收: 变量可以正确替换

- [x] TASK-4.1.5: 实现API密钥加密/解密工具类
  - 描述: 使用Jasypt实现API密钥的加密存储和解密使用
  - 验收: 加密后的密钥无法直接读取

### 4.2 服务层

- [x] TASK-4.2.1: 创建AIServiceConfigService接口
  - 描述: 定义配置管理的业务方法
  - 验收: 接口定义完整

- [x] TASK-4.2.2: 实现AIServiceConfigServiceImpl类
  - 描述: 实现配置的CRUD操作，包括连接性验证
  - 验收: 服务类可以正常注入Repository

- [x] TASK-4.2.3: 实现连接性验证逻辑
  - 描述: 发送测试请求到AI服务，更新状态为AVAILABLE/UNAVAILABLE
  - 验收: 可以正确验证连接状态

- [x] TASK-4.2.4: 实现API密钥脱敏显示
  - 描述: maskApiKey方法，只显示部分密钥（如sk-proj****1234）
  - 验收: 返回的Response中密钥已脱敏

- [x] TASK-4.2.5: 实现默认服务管理
  - 描述: 确保只有一个is_default=true的服务配置
  - 验收: 设置新默认服务时，旧的自动取消

### 4.3 Controller层（只使用GET/POST）

- [x] TASK-4.3.- [ ] TASK-4.3.1:: 创建AIServiceConfigController类
  - 描述: 创建Controller，注入AIServiceConfigService
  - 验收: 类可以正常启动

- [x] TASK-4.3.- [ ] TASK-4.3.2:: 实现GET /api/ai-service/configs接口
  - 描述: 获取所有AI服务配置列表
  - 验收: 返回ApiResponse格式，data包含配置列表

- [x] TASK-4.3.- [ ] TASK-4.3.3:: 实现GET /api/ai-service/configs/detail接口
  - 描述: 根据unique_id获取单个配置详情
  - 验收: 返回ApiResponse格式

- [x] TASK-4.3.- [ ] TASK-4.3.4:: 实现GET /api/ai-service/available接口
  - 描述: 获取状态为AVAILABLE的服务列表
  - 验收: 返回ApiResponse格式

- [x] TASK-4.3.- [ ] TASK-4.3.5:: 实现POST /api/ai-service/configs/create接口
  - 描述: 创建新的AI服务配置，自动触发连接性验证
  - 验收: 创建成功，状态初始为UNTESTED，验证后更新

- [x] TASK-4.3.- [ ] TASK-4.3.6:: 实现POST /api/ai-service/configs/update接口
  - 描述: 更新AI服务配置，重新触发验证
  - 验收: 更新成功，状态重新验证

- [x] TASK-4.3.- [ ] TASK-4.3.7:: 实现POST /api/ai-service/configs/delete接口
  - 描述: 删除AI服务配置（使用POST代替DELETE）
  - 验收: 删除成功，返回204状态码

- [x] TASK-4.3.- [ ] TASK-4.3.8:: 实现POST /api/ai-service/configs/test接口
  - 描述: 手动触发连接性测试
  - 验收: 返回测试结果和响应时间

### 4.4 DTO与VO

- [x] TASK-4.4.- [ ] TASK-4.4.1:: 创建AIServiceConfigCreateRequest DTO
  - 描述: 包含provider、modelName、apiKey、apiEndpoint等字段
  - 验收: 使用javax.validation注解

- [x] TASK-4.4.- [ ] TASK-4.4.2:: 创建AIServiceConfigUpdateRequest DTO
  - 描述: 包含unique_id和需要更新的字段
  - 验收: DTO定义完整

- [x] TASK-4.4.- [ ] TASK-4.4.3:: 创建AIServiceConfigResponse VO
  - 描述: 返回给前端的配置信息，不包含完整API密钥
  - 验收: VO字段与数据库对应

- [x] TASK-4.4.- [ ] TASK-4.4.4:: 创建ConnectionTestResponse VO
  - 描述: 包含success、responseTime、message等字段
  - 验收: VO定义完整

---

## Phase 5: Prompt模板管理

### 5.1 服务层

- [x] TASK-5.1.- [ ] TASK-5.1.1:: 创建PromptTemplateService接口
  - 描述: 定义Prompt模板管理的业务方法
  - 验收: 接口定义完整

- [x] TASK-5.1.- [ ] TASK-5.1.2:: 实现PromptTemplateServiceImpl类
  - 描述: 实现模板的CRUD操作
  - 验收: 服务类可以正常运行

- [x] TASK-5.1.- [ ] TASK-5.1.3:: 实现getActiveTemplate方法
  - 描述: 根据template_type获取is_active=true的模板
  - 验收: 可以正确获取激活的模板

- [x] TASK-5.1.- [ ] TASK-5.1.4:: 实现createTemplate方法
  - 描述: 创建新的Prompt模板
  - 验收: 创建成功

- [x] TASK-5.1.- [ ] TASK-5.1.5:: 实现updateTemplate方法
  - 描述: 更新模板内容
  - 验收: 更新成功

- [x] TASK-5.1.- [ ] TASK-5.1.6:: 实现listTemplates方法
  - 描述: 列出所有模板
  - 验收: 返回模板列表

---

## Phase 6: AI调用日志记录

### 6.1 服务层

- [x] TASK-6.1.- [ ] TASK-6.1.1:: 创建AICallLogService接口
  - 描述: 定义日志记录的业务方法
  - 验收: 接口定义完整

- [x] TASK-6.1.- [ ] TASK-6.1.2:: 实现AICallLogServiceImpl类
  - 描述: 实现日志的记录和查询
  - 验收: 服务类可以正常运行

- [x] TASK-6.1.- [ ] TASK-6.1.3:: 实现logSuccess方法
  - 描述: 记录成功的AI调用
  - 验收: 日志保存成功

- [x] TASK-6.1.- [ ] TASK-6.1.4:: 实现logFailure方法
  - 描述: 记录失败的AI调用
  - 验收: 错误信息正确记录

- [x] TASK-6.1.- [ ] TASK-6.1.5:: 实现queryLogs方法
  - 描述: 查询调用日志，支持分页和过滤
  - 验收: 可以按条件查询

---

## Phase 7: 页面快照服务（Playwright）

### 7.1 数据模型

- [x] TASK-7.1.- [ ] TASK-7.1.1:: 创建PageSnapshot数据类
  - 描述: 包含url、title、hash、elements等字段
  - 验收: 类定义完整

- [x] TASK-7.1.- [ ] TASK-7.1.2:: 创建InteractiveElement数据类
  - 描述: 包含tagName、id、name、selectors等字段
  - 验收: 类定义完整

- [x] TASK-7.1.- [ ] TASK-7.1.3:: 创建SelectorMetadata数据类
  - 描述: 包含type、expression、priority等字段
  - 验收: 类定义完整

### 7.2 服务层

- [x] TASK-7.2.- [ ] TASK-7.2.1:: 创建PageSnapshotService接口
  - 描述: 定义页面快照相关方法
  - 验收: 接口定义完整

- [x] TASK-7.2.- [ ] TASK-7.2.2:: 实现PageSnapshotServiceImpl类
  - 描述: 使用Playwright实现页面快照捕获
  - 验收: 可以启动浏览器并访问页面

- [x] TASK-7.2.- [ ] TASK-7.2.3:: 实现captureSnapshot方法
  - 描述: 导航到指定URL，提取页面信息
  - 验收: 可以成功捕获页面快照

- [x] TASK-7.2.- [ ] TASK-7.2.4:: 实现extractInteractiveElements方法
  - 描述: 提取input、button、a、select等可交互元素
  - 验收: 可以提取所有可交互元素

- [x] TASK-7.2.- [ ] TASK-7.2.5:: 实现generateSelectors方法
  - 描述: 为每个元素生成多种选择器（testId、id、name、aria、text、css）
  - 验收: 每个元素包含多个选择器选项

- [x] TASK-7.2.- [ ] TASK-7.2.6:: 实现选择器优先级排序
  - 描述: 按testId > id > name > aria > text > css排序
  - 验收: 选择器按优先级排列

- [x] TASK-7.2.- [ ] TASK-7.2.7:: 实现generateSnapshotHash方法
  - 描述: 基于页面结构和元素列表生成哈希值
  - 验收: 相同页面生成相同哈希

- [x] TASK-7.2.- [ ] TASK-7.2.8:: 配置Playwright浏览器选项
  - 描述: 配置headless模式、超时时间等
  - 验收: 浏览器可以无头模式运行

---

## Phase 8: 自然语言解析服务

### 8.1 服务层

- [x] TASK-8.1.- [ ] TASK-8.1.1:: 创建NaturalLanguageParser接口
  - 描述: 定义自然语言解析方法
  - 验收: 接口定义完整

- [x] TASK-8.1.- [ ] TASK-8.1.2:: 实现NaturalLanguageParserImpl类
  - 描述: 使用AI服务将自然语言转换为JSON
  - 验收: 可以正确解析自然语言

- [x] TASK-8.1.- [ ] TASK-8.1.3:: 实现parseSteps方法
  - 描述: 加载Prompt模板，调用AI服务，解析响应
  - 验收: 返回结构化的TestStep列表

- [x] TASK-8.1.- [ ] TASK-8.1.4:: 实现Prompt组装逻辑
  - 描述: 从数据库加载模板，拼接用户输入
  - 验收: Prompt格式正确

- [x] TASK-8.1.- [ ] TASK-8.1.5:: 实现AI响应解析
  - 描述: 提取AI返回的JSON，验证格式
  - 验收: 可以正确解析JSON

- [x] TASK-8.1.- [ ] TASK-8.1.6:: 实现超时处理
  - 描述: 设置30秒超时，超时后返回友好提示
  - 验收: 超时时不阻塞线程

- [x] TASK-8.1.- [ ] TASK-8.1.7:: 实现降级处理
  - 描述: 解析失败时返回错误提示和基础模板
  - 验收: 失败时有友好的错误信息

- [x] TASK-8.1.- [ ] TASK-8.1.8:: 实现调用日志记录
  - 描述: 记录每次AI调用的输入、输出、成功状态
  - 验收: 日志正确保存

---

## Phase 9: 智能脚本生成服务

### 9.1 服务层

- [x] TASK-9.1.- [ ] TASK-9.1.1:: 创建ScriptGenerationService接口
  - 描述: 定义脚本生成方法
  - 验收: 接口定义完整

- [x] TASK-9.1.- [ ] TASK-9.1.2:: 实现ScriptGenerationServiceImpl类
  - 描述: 基于页面快照和自然语言生成脚本
  - 验收: 可以正确生成脚本

- [x] TASK-9.1.- [ ] TASK-9.1.3:: 实现generateScript方法（基于URL）
  - 描述: 捕获页面快照，调用AI生成脚本
  - 验收: 返回生成的步骤列表

- [x] TASK-9.1.- [ ] TASK-9.1.4:: 实现generateScriptWithSnapshot方法（基于已有快照）
  - 描述: 使用传入的快照数据，不重新捕获
  - 验收: 可以基于已有快照生成

- [x] TASK-9.1.- [ ] TASK-9.1.5:: 实现页面快照序列化
  - 描述: 将PageSnapshot对象转换为JSON字符串
  - 验收: JSON格式正确

- [x] TASK-9.1.- [ ] TASK-9.1.6:: 实现选择器存在性验证
  - 描述: 验证AI生成的选择器是否在快照中存在
  - 验收: 不存在的选择器被标记

- [x] TASK-9.1.- [ ] TASK-9.1.7:: 实现备用选择器生成
  - 描述: 为每个步骤生成fallback选择器
  - 验收: 每个步骤包含主选择器和备用选择器

- [x] TASK-9.1.- [ ] TASK-9.1.8:: 实现超时处理（60秒）
  - 描述: 脚本生成设置较长超时
  - 验收: 超时处理正确

- [x] TASK-9.1.- [ ] TASK-9.1.9:: 实现调用日志记录
  - 描述: 记录快照哈希、调用类型等信息
  - 验收: 日志包含快照哈希

### 9.2 两阶段脚本生成服务（核心功能）

- [x] TASK-9.2.- [ ] TASK-9.2.1:: 创建TwoPhaseScriptGenerationService接口
  - 描述: 定义两阶段脚本生成方法（自然语言→JSON→完整脚本）
  - 验收: 接口定义完整

- [x] TASK-9.2.- [ ] TASK-9.2.2:: 实现TwoPhaseScriptGenerationServiceImpl类
  - 描述: 集成NaturalLanguageParser和ScriptGenerationService
  - 验收: 可以串联两个阶段

- [x] TASK-9.2.- [ ] TASK-9.2.3:: 实现generateScriptInTwoPhases方法
  - 描述: 串联执行阶段1（解析）和阶段2（生成），返回完整脚本
  - 验收: 返回包含最优选择器的测试脚本

- [x] TASK-9.2.- [ ] TASK-9.2.4:: 实现generateScriptFromJsonAndSnapshot方法
  - 描述: 单独的阶段2，基于已有JSON和快照生成脚本
  - 验收: 可以匹配元素并生成选择器

- [x] TASK-9.2.- [ ] TASK-9.2.5:: 实现validateAndMetadata方法
  - 描述: 验证生成的步骤，统计匹配结果和置信度
  - 验收: 返回GenerationMetadata对象

- [x] TASK-9.2.- [ ] TASK-9.2.6:: 在data.sql中添加script_generation_from_json模板
  - 描述: 插入基于JSON和快照的Prompt模板
  - 验收: 模板内容完整，包含系统指令和示例

### 9.3 数据模型扩展

- [x] TASK-9.3.- [ ] TASK-9.3.1:: 创建TestStep数据类（阶段1）
  - 描述: 包含stepNumber、action、target（中文描述）、value、description
  - 验收: 类定义完整

- [x] TASK-9.3.- [ ] TASK-9.3.2:: 创建TestStepWithSelectors数据类（阶段2）
  - 描述: 继承TestStep，添加selector、selectorType、confidence、fallbackSelectors等字段
  - 验收: 类定义完整

- [x] TASK-9.3.- [ ] TASK-9.3.3:: 创建GenerationMetadata数据类
  - 描述: 包含totalSteps、matchedElements、unmatchedTargets、averageConfidence
  - 验收: 类定义完整

- [x] TASK-9.3.- [ ] TASK-9.3.4:: 创建TwoPhaseGenerateResult VO
  - 描述: 包含最终步骤、页面快照、生成元数据
  - 验收: VO定义完整

---

## Phase 10: AI脚本生成Controller层（只使用GET/POST）

### 10.1 Controller

- [x] TASK-10.1.- [ ] TASK-10.1.1:: 创建AIScriptController类
  - 描述: 创建Controller，注入各Service
  - 验收: 类可以正常启动

- [x] TASK-10.1.- [ ] TASK-10.1.2:: 实现POST /api/ai/parse-steps接口
  - 描述: 接收自然语言，返回解析后的JSON（阶段1）
  - 验收: 返回ApiResponse格式，包含结构化步骤

- [x] TASK-10.1.- [ ] TASK-10.1.3:: 实现POST /api/ai/parse-steps/batch接口
  - 描述: 批量解析多个自然语言输入
  - 验收: 返回所有解析结果

- [x] TASK-10.1.- [ ] TASK-10.1.4:: 实现POST /api/ai/generate-script接口（基于URL，自然语言直接生成）
  - 描述: 接收URL和自然语言，直接生成脚本（单阶段）
  - 验收: 返回步骤列表和快照信息

- [x] TASK-10.1.- [ ] TASK-10.1.5:: 实现POST /api/ai/generate-script/two-phase接口（两阶段生成）
  - 描述: 接收自然语言和URL，执行两阶段生成
  - 验收: 返回包含最优选择器的完整脚本

- [x] TASK-10.1.- [ ] TASK-10.1.6:: 实现POST /api/ai/generate-script/from-json接口（基于JSON和快照）
  - 描述: 接收结构化JSON和页面快照，生成最终脚本
  - 验收: 为每个步骤匹配最优选择器

- [x] TASK-10.1.- [ ] TASK-10.1.7:: 实现GET /api/ai/snapshot/export接口
  - 描述: 导出页面快照为JSON或HTML
  - 验收: 返回可下载的文件

- [x] TASK-10.1.- [ ] TASK-10.1.8:: 实现异常处理
  - 描述: 统一捕获异常，返回友好的错误信息
  - 验收: 错误时不暴露AI服务细节

### 10.2 DTO与VO

- [x] TASK-10.2.- [ ] TASK-10.2.1:: 创建ParseStepsRequest DTO
  - 描述: 包含stepsText字段
  - 验收: 使用javax.validation注解

- [x] TASK-10.2.- [ ] TASK-10.2.2:: 创建ParseStepsResponse VO
  - 描述: 包含steps列表（TestStep）、generationTime等字段
  - 验收: VO定义完整

- [x] TASK-10.2.- [ ] TASK-10.2.3:: 创建TwoPhaseGenerateRequest DTO
  - 描述: 包含naturalLanguageSteps、url或pageSnapshot
  - 验收: DTO定义完整

- [x] TASK-10.2.- [ ] TASK-10.2.4:: Create GenerateScriptFromJsonRequest DTO
  - 描述: 包含stepsJson、pageSnapshot字段
  - 验收: 使用javax.validation注解

- [x] TASK-10.2.- [ ] TASK-10.2.5:: 创建GenerateScriptResponse VO
  - 描述: 包含steps（TestStepWithSelectors）、snapshot、metadata等字段
  - 验收: VO定义完整

- [x] TASK-10.2.- [ ] TASK-10.2.6:: 创建PageSnapshotResponse VO
  - 描述: 包含url、title、hash、elements等字段
  - 验收: VO定义完整

- [x] TASK-10.2.- [ ] TASK-10.2.7:: 创建SelectorMetadata VO
  - 描述: 包含type、expression、priority字段
  - 验收: VO定义完整

---

## Phase 11: 测试用例创建流程集成

### 11.1 项目管理

- [x] TASK-11.- [ ] TASK-11.1.1:: 修改ProjectEntity实体类
  - 描述: 添加targetUrl、baseUrl字段
  - 验收: 实体类包含新字段

- [x] TASK-11.- [ ] TASK-11.1.2:: 更新projects表的SQL脚本
  - 描述: 在E:\myproject\UiAutoTest\db\table.sql中添加targetUrl、baseUrl字段
  - 验收: 表包含新字段

- [x] TASK-11.- [ ] TASK-11.1.3:: 修改ProjectCreateRequest DTO
  - 描述: 添加targetUrl必填字段
  - 验收: DTO包含targetUrl，使用javax.validation注解

- [x] TASK-11.- [ ] TASK-11.1.4:: 实现POST /api/projects/create接口
  - 描述: 创建项目时必须提供targetUrl
  - 验收: 返回ApiResponse格式

- [x] TASK-11.- [ ] TASK-11.1.5:: 实现URL解析工具类
  - 描述: 创建UrlResolver工具类，处理相对/绝对URL拼接
  - 验收: 可以正确解析最终URL

### 11.2 测试用例创建集成

- [x] TASK-11.- [ ] TASK-11.2.1:: 修改TestCaseEntity实体类
  - 描述: 添加projectId、targetUrl、stepsJson、isAiGenerated字段
  - 验收: 实体类包含新字段，建立与Project的外键关联

- [x] TASK-11.- [ ] TASK-11.2.2:: 更新test_cases表的SQL脚本
  - 描述: 在table.sql中添加新字段和外键约束
  - 验收: 表包含新字段和外键

- [x] TASK-11.- [ ] TASK-11.2.3:: 修改TestCaseCreateRequest DTO
  - 描述: 添加projectId、targetUrl（可选）、naturalLanguageSteps字段
  - 验收: DTO包含新字段

- [x] TASK-11.- [ ] TASK-11.2.4:: 实现创建用例时自然语言解析
  - 描述: 创建用例时调用NaturalLanguageParser，生成stepsJson保存
  - 描述: 注意：此时只生成操作意图JSON，不包含选择器
  - 验收: 保存的stepsJson不包含selector字段

- [x] TASK-11.- [ ] TASK-11.2.5:: 实现URL解析和保存
  - 描述: 创建用例时，解析最终URL（项目+用例），并保存到数据库
  - 验收: 可以正确解析相对/绝对URL

- [x] TASK-11.- [ ] TASK-11.2.6:: 实现POST /api/testcases/create-with-ai接口
  - 描述: 接收自然语言，解析为JSON后保存
  - 验收: 返回ApiResponse格式，包含解析后的stepsJson

---

## Phase 12: 测试用例执行流程

### 12.1 执行实体与Repository

- [x] TASK-12.- [ ] TASK-12.1.1:: 创建TestCaseExecutionEntity实体类
  - 描述: 包含testCaseId、executionUrl、status、duration、generatedScript等字段
  - 验收: 实体类继承BaseEntity

- [x] TASK-12.- [ ] TASK-12.1.2:: 创建test_case_executions表的SQL脚本
  - 描述: 在table.sql中添加执行记录表
  - 验收: 表结构符合规范，包含5个标准字段

- [x] TASK-12.- [ ] TASK-12.1.3:: 创建TestCaseExecutionRepository接口
  - 描述: 继承JpaRepository，添加findByTestCaseId等方法
  - 验收: 接口编译通过

### 12.2 Playwright执行器

- [x] TASK-12.- [ ] TASK-12.2.1:: 创建PlaywrightExecutor接口
  - 描述: 定义脚本执行方法
  - 验收: 接口定义完整

- [x] TASK-12.- [ ] TASK-12.2.2:: 实现PlaywrightExecutorImpl类
  - 描述: 使用Playwright执行TestStepWithSelectors列表
  - 验收: 可以启动浏览器并执行操作

- [x] TASK-12.- [ ] TASK-12.2.3:: 实现execute方法
  - 描述: 接收URL和步骤列表，按顺序执行
  - 验收: 返回ExecutionResult对象

- [x] TASK-12.- [ ] TASK-12.2.4:: 实现步骤执行逻辑
  - 描述: 支持navigate、click、fill、select、wait等操作
  - 验收: 每个操作都能正确执行

- [x] TASK-12.- [ ] TASK-12.2.5:: 实现截图功能
  - 描述: 每步完成后截图（如果配置启用）
  - 验收: 返回Base64编码的截图

- [x] TASK-12.- [ ] TASK-12.2.6:: 实现超时和重试机制
  - 描述: 每步支持超时配置，失败时自动重试
  - 验收: 超时和重试逻辑正确

### 12.3 执行服务层

- [x] TASK-12.- [ ] TASK-12.3.1:: 创建TestCaseExecutionService接口
  - 描述: 定义用例执行的业务方法
  - 验收: 接口定义完整

- [x] TASK-12.- [ ] TASK-12.3.2:: 实现TestCaseExecutionServiceImpl类
  - 描述: 实现executeTestCase方法
  - 验收: 可以正确执行测试用例

- [x] TASK-12.- [ ] TASK-12.3.3:: 实现executeTestCase完整流程
  - 描述: 1.读取stepsJson → 2.解析URL → 3.捕获快照 → 4.生成脚本 → 5.执行脚本 → 6.保存结果
  - 验收: 完整流程可以正常运行

- [x] TASK-12.- [ ] TASK-12.3.4:: 实现URL解析逻辑
  - 描述: 支持绝对URL、相对路径、项目URL、覆盖URL
  - 验收: 可以正确解析各种URL情况

- [x] TASK-12.- [ ] TASK-12.3.5:: 实现执行结果保存
  - 描述: 保存到test_case_executions表
  - 验收: 记录包含generatedScript、stepsResult、screenshots

### 12.4 执行Controller层

- [x] TASK-12.- [ ] TASK-12.4.1:: 创建TestCaseExecutionController类
  - 描述: 创建Controller，注入ExecutionService
  - 验收: 类可以正常启动

- [x] TASK-12.- [ ] TASK-12.4.2:: 实现POST /api/testcases/{id}/execute接口
  - 描述: 接收可选的targetUrl覆盖，执行测试用例
  - 验收: 返回ApiResponse格式，包含执行结果

- [x] TASK-12.- [ ] TASK-12.4.3:: 实现POST /api/testcases/batch-execute接口
  - 描述: 批量执行多个测试用例
  - 验收: 返回所有用例的执行结果

- [x] TASK-12.- [ ] TASK-12.4.4:: 实现GET /api/testcases/{id}/executions接口
  - 描述: 查询测试用例的执行历史
  - 验收: 返回执行记录列表，按时间倒序

- [x] TASK-12.- [ ] TASK-12.4.5:: 实现GET /api/executions/{id}/detail接口
  - 描述: 获取执行记录的详细信息
  - 验收: 返回完整的执行详情和截图

### 12.5 DTO与VO

- [x] TASK-12.- [ ] TASK-12.5.1:: Create ExecutionRequest DTO
  - 描述: 包含targetUrl（可选覆盖）、executionConfig字段
  - 验收: DTO定义完整

- [x] TASK-12.- [ ] TASK-12.5.2:: Create ExecutionConfig VO
  - 描述: 包含timeout、screenshot、headless等配置
  - 验收: VO定义完整

- [x] TASK-12.- [ ] TASK-12.5.3:: 创建ExecutionResponse VO
  - 描述: 包含executionId、status、duration、steps等字段
  - 验收: VO定义完整

- [x] TASK-12.- [ ] TASK-12.5.4:: 创建StepExecutionResult VO
  - 描述: 包含stepNumber、status、screenshot、error等字段
  - 验收: VO定义完整

- [x] TASK-12.- [ ] TASK-12.5.5:: 创建ExecutionDetailResponse VO
  - 描述: 包含完整的执行详情、脚本、步骤结果、截图
  - 验收: VO定义完整

---

## Phase 13: 异常处理与安全配置

### 13.1 异常处理

- [x] TASK-13.- [ ] TASK-13.1.1:: 创建AIServiceException自定义异常
  - 描述: 定义AI服务调用失败异常
  - 验收: 异常类创建成功

- [x] TASK-13.- [ ] TASK-13.1.2:: 创建ParseException自定义异常
  - 描述: 定义自然语言解析失败异常
  - 验收: 异常类创建成功

- [x] TASK-13.- [ ] TASK-13.1.3:: 创建ScriptGenerationException自定义异常
  - 描述: 定义脚本生成失败异常
  - 验收: 异常类创建成功

- [x] TASK-13.- [ ] TASK-13.1.4:: 创建ExecutionException自定义异常
  - 描述: 定义脚本执行失败异常
  - 验收: 异常类创建成功

- [x] TASK-13.- [ ] TASK-13.1.5:: 实现全局异常处理器
  - 描述: 创建GlobalExceptionHandler，统一处理异常
  - 验收: 所有异常返回ApiResponse格式

- [x] TASK-13.- [ ] TASK-13.1.6:: 实现友好错误提示
  - 描述: AI服务失败时返回"解析失败，请检查输入或手动编辑"
  - 验收: 不暴露技术错误信息

### 13.2 安全配置

- [x] TASK-13.- [ ] TASK-13.2.1:: 配置Jasypt加密密钥
  - 描述: 在application.yml中配置jasypt.encryptor.password
  - 验收: 可以加解密API密钥

- [x] TASK-13.- [ ] TASK-13.2.2:: 实现API密钥加密工具类
  - 描述: 创建AESUtil工具类
  - 验收: 可以加密解密字符串

- [x] TASK-13.- [ ] TASK-13.2.3:: 配置AI服务接口权限
  - 描述: AI服务配置接口仅管理员可访问
  - 验收: 普通用户无法访问

- [x] TASK-13.- [ ] TASK-13.2.4:: 配置AI调用权限
  - 描述: 普通用户可以调用AI解析和生成接口
  - 验收: 权限配置正确

---

## Phase 14: 单元测试与集成测试

### 14.1 Service层测试

- [ ] TASK-14.1.1: 编写AIServiceConfigService单元测试
  - 描述: 测试配置的CRUD操作和连接性验证
  - 验收: 覆盖率≥80%

- [ ] TASK-14.1.2: 编写PageSnapshotService单元测试
  - 描述: Mock Playwright，测试快照捕获逻辑
  - 验收: 覆盖率≥80%

- [ ] TASK-14.1.3: 编写NaturalLanguageParser单元测试
  - 描述: Mock AI服务，测试解析逻辑
  - 验收: 覆盖率≥80%

- [ ] TASK-14.1.4: 编写TwoPhaseScriptGenerationService单元测试
  - 描述: Mock解析服务、快照服务和AI服务，测试两阶段生成
  - 验收: 覆盖率≥80%

- [ ] TASK-14.1.5: 编写PlaywrightExecutor单元测试
  - 描述: Mock Playwright，测试脚本执行逻辑
  - 验收: 覆盖率≥80%

- [ ] TASK-14.1.6: 编写TestCaseExecutionService单元测试
  - 描述: 测试完整的执行流程
  - 验收: 覆盖率≥80%

### 14.2 Controller层测试

- [ ] TASK-14.2.1: 编写AIServiceConfigController集成测试
  - 描述: 使用MockMvc测试所有接口
  - 验收: 覆盖率≥60%

- [ ] TASK-14.2.2: 编写AIScriptController集成测试
  - 描述: 使用MockMvc测试所有接口
  - 验收: 覆盖率≥60%

- [ ] TASK-14.2.3: 编写TestCaseExecutionController集成测试
  - 描述: 使用MockMvc测试执行接口
  - 验收: 覆盖率≥60%

### 14.3 端到端测试

- [ ] TASK-14.3.1: 测试完整的创建→执行流程
  - 描述: 从创建项目到执行用例的完整流程
  - 验收: 测试通过

- [ ] TASK-14.3.2: 测试两阶段脚本生成
  - 描述: 自然语言→JSON→脚本→执行的完整流程
  - 验收: 测试通过

- [ ] TASK-14.3.3: 测试URL解析逻辑
  - 描述: 测试绝对URL、相对路径、项目URL、覆盖URL
  - 验收: 测试通过

- [ ] TASK-14.3.4: 测试超时和异常处理
  - 描述: 模拟超时和失败场景
  - 验收: 测试通过

---

## Phase 15: 文档与部署

- [x] TASK-15.- [ ] TASK-15.1:: 添加Swagger注解
  - 描述: 为所有Controller添加API文档注解
  - 验收: Swagger可以访问

- [x] TASK-15.- [ ] TASK-15.2:: 编写管理员配置指南
  - 描述: 如何配置AI服务HTTP端点
  - 验收: 文档清晰易懂

- [x] TASK-15.- [ ] TASK-15.3:: 编写用户使用指南
  - 描述: 如何使用自然语言创建和执行测试用例
  - 验收: 文档包含示例

- [x] TASK-15.- [ ] TASK-15.4:: 准备生产环境配置
  - 描述: 创建application-prod.yml
  - 验收: 配置文件完整

- [x] TASK-15.- [ ] TASK-15.5:: 编写部署检查清单
  - 描述: 环境变量、依赖、配置项检查
  - 验收: 清单完整

---

## Phase 16: 性能优化（可选）

- [ ] TASK-16.1: 实现AI服务连接池
  - 描述: 避免频繁创建连接
  - 验收: 提升调用效率

- [ ] TASK-16.2: 实现Prompt模板缓存
  - 描述: 减少数据库查询
  - 验收: 提升模板加载速度

- [ ] TASK-16.3: 实现页面快照缓存
  - 描述: 避免重复捕获相同页面
  - 验收: 提升执行效率

- [ ] TASK-16.4: 优化AI请求超时配置
  - 描述: 根据实际使用情况调整
  - 验收: 超时配置合理

- [ ] TASK-16.5: 实现异步AI调用
  - 描述: 对于批量场景使用异步调用
  - 验收: 提升批量处理性能

---

## 完成标准

所有任务完成后，应满足以下标准：

1. ✅ 项目创建时提供targetUrl
2. ✅ 测试用例创建时，自然语言→JSON（不含选择器）→保存
3. ✅ 测试用例执行时，JSON + 页面快照 → 脚本（含选择器）→ 执行
4. ✅ 支持URL解析（项目+用例，绝对/相对，覆盖）
5. ✅ 所有业务代码已实现，模块可以正常启动
6. ✅ 所有单元测试已编写并通过，mvn test成功
7. ✅ 代码覆盖率达标（Service≥80%, Controller≥60%）
8. ✅ API接口只使用GET和POST方法
9. ✅ 统一返回ApiResponse格式
10. ✅ 数据库表符合规范（unique_id、5个标准字段）
11. ✅ SQL文件正确放置在db目录下
12. ✅ 遵循Java 8和javax.*命名空间规范

