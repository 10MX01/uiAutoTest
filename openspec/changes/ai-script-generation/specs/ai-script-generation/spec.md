# AI智能脚本生成能力规范

## ADDED Requirements

### Requirement: 系统支持自然语言测试步骤解析
系统 SHALL 允许用户使用自然语言描述测试步骤，并使用AI将其转换为结构化的测试步骤JSON。

#### Scenario: 解析简单的自然语言步骤
- **WHEN** 用户输入自然语言测试步骤："打开登录页面"
- **THEN** 系统调用AI服务进行解析
- **AND** 系统返回结构化步骤：`{"action": "navigate", "value": "/login"}`
- **AND** 系统在界面上展示解析结果供用户确认

#### Scenario: 解析多步骤测试场景
- **WHEN** 用户输入多步骤自然语言描述："1. 打开登录页面 2. 输入用户名admin 3. 输入密码Password123 4. 点击登录按钮"
- **THEN** 系统解析为4个结构化步骤的数组
- **AND** 每个步骤包含：步骤序号、操作类型、目标、参数值、描述
- **AND** 系统按顺序展示所有步骤供用户编辑

#### Scenario: 解析失败时的降级处理
- **WHEN** AI服务无法解析用户的自然语言输入
- **THEN** 系统返回友好的错误提示："解析失败，请检查输入或手动编辑结构化步骤"
- **AND** 系统提供基础的结构化步骤模板
- **AND** 用户可以手动填写结构化步骤

#### Scenario: 解析超时处理
- **WHEN** AI服务响应超过30秒
- **THEN** 系统中断请求并返回超时提示
- **AND** 系统允许用户重新尝试解析或手动编辑

### Requirement: 系统捕获页面快照并提取可交互元素
系统 SHALL 在请求生成脚本时捕获指定页面的快照，提取所有可交互元素的属性信息。

#### Scenario: 捕获完整页面快照
- **WHEN** 用户请求基于页面生成脚本
- **THEN** 系统导航到指定的URL
- **AND** 系统使用Playwright捕获页面快照
- **AND** 系统提取页面URL和标题

#### Scenario: 提取可交互元素
- **WHEN** 系统捕获页面快照
- **THEN** 系统提取所有可交互元素：<input>、<select>、<textarea>、<button>、<a>
- **AND** 系统提取带有测试属性的元素：[data-testid]、[data-test]、[data-automation]
- **AND** 系统提取带有可访问性属性的元素：[aria-label]、[role]
- **AND** 系统提取每个元素的以下属性：标签名、id、name、class、type、placeholder、文本内容

#### Scenario: 提取元素选择器选项
- **WHEN** 系统提取可交互元素
- **THEN** 系统为每个元素生成多种选择器选项
- **AND** 选择器选项按优先级排序：data-testid > id > name > aria-label > text > CSS > XPath
- **AND** 系统记录每个选择器的类型和完整表达式

#### Scenario: 生成页面快照哈希
- **WHEN** 系统完成页面快照提取
- **THEN** 系统基于页面结构和元素列表生成哈希值
- **AND** 系统在返回结果中包含哈希值供调用方使用

### Requirement: 系统基于页面快照和自然语言生成测试脚本
系统 SHALL 根据用户自然语言描述和页面快照，使用AI自动生成最优选择器的测试脚本。

#### Scenario: 生成单步骤脚本
- **WHEN** 用户输入自然语言："点击登录按钮"
- **AND** 系统已捕获包含登录按钮的页面快照
- **THEN** 系统分析页面快照找到匹配的按钮元素
- **AND** 系统使用最优选择器（优先data-testid，其次id）
- **AND** 系统生成结构化步骤：`{"action": "click", "selector": "[data-testid='login-button']"}`
- **AND** 系统在返回结果中包含选择器类型和置信度

#### Scenario: 生成复杂交互脚本
- **WHEN** 用户输入自然语言："在用户名输入框输入test@example.com"
- **AND** 系统已捕获包含登录表单的页面快照
- **THEN** 系统分析页面快照找到用户名输入框
- **AND** 系统生成结构化步骤：`{"action": "fill", "selector": "#username", "value": "test@example.com"}`
- **AND** 系统为该步骤生成备用选择器作为fallback

#### Scenario: 选择器优先级应用
- **WHEN** 页面元素同时拥有多个可选选择器
- **THEN** 系统按优先级选择：data-testid > id > name > aria-label > text > CSS
- **AND** 系统在生成的步骤中记录选择器类型
- **AND** 系统在元数据中提供备用选择器列表

#### Scenario: 未找到匹配元素时的处理
- **WHEN** AI无法在页面快照中找到匹配用户描述的元素
- **THEN** 系统返回警告提示用户
- **AND** 系统提供可能匹配的元素列表供用户选择
- **AND** 系统允许用户指定选择器后重新生成

### Requirement: 系统提供备用选择器机制
系统 SHALL 为生成的脚本步骤提供备用选择器，提高脚本鲁棒性。

#### Scenario: 生成步骤时包含备用选择器
- **WHEN** 系统生成测试脚本步骤
- **THEN** 系统为主选择器生成至少一个备用选择器
- **AND** 系统按优先级排列选择器（主选择器优先级最高）
- **AND** 系统在步骤元数据中记录所有选择器选项

#### Scenario: 返回完整的选择器列表
- **WHEN** 系统生成测试步骤
- **THEN** 返回结果包含主选择器和所有备用选择器
- **AND** 每个选择器标注类型（testId/id/name/aria/text/css）
- **AND** 调用方可以自主决定如何使用这些选择器

### Requirement: 系统提供自然语言解析API接口
系统 SHALL 提供API接口用于将自然语言测试步骤转换为结构化JSON。

#### Scenario: 解析自然语言步骤
- **WHEN** 客户端调用POST /api/ai/parse-steps
- **AND** 请求体包含：stepsText（自然语言描述）
- **THEN** 系统调用AI服务进行解析
- **AND** 系统返回结构化步骤JSON数组
- **AND** 每个步骤包含：stepNumber、action、selector、value、description

#### Scenario: 批量解析多个测试用例
- **WHEN** 客户端调用POST /api/ai/parse-steps/batch
- **AND** 请求体包含多个stepsText
- **THEN** 系统逐个调用AI服务解析
- **AND** 系统返回所有解析结果
- **AND** 系统标记每个结果是成功还是失败

### Requirement: 系统提供基于页面快照的脚本生成API接口
系统 SHALL 提供API接口用于基于页面快照和自然语言生成测试脚本。

#### Scenario: 请求生成脚本
- **WHEN** 客户端调用POST /api/ai/generate-script
- **AND** 请求体包含：url、naturalLanguage
- **THEN** 系统导航到指定URL捕获页面快照
- **AND** 系统调用AI服务生成脚本
- **AND** 系统返回生成的步骤列表和页面快照信息

#### Scenario: 返回完整的页面快照信息
- **WHEN** 系统生成脚本
- **THEN** 返回结果包含：页面URL、页面标题、快照哈希、可交互元素数量
- **AND** 返回结果包含每个元素的属性概要
- **AND** 调用方可以使用这些信息进行后续处理

#### Scenario: 支持传入已有页面快照
- **WHEN** 客户端调用POST /api/ai/generate-script
- **AND** 请求体包含：pageSnapshot（已有的快照数据）、naturalLanguage
- **THEN** 系统不重新捕获页面，直接使用提供的快照
- **AND** 系统基于提供的快照生成脚本
- **AND** 系统返回生成的步骤列表

### Requirement: 系统支持基于结构化JSON和页面快照生成最终测试脚本
系统 SHALL 提供两阶段脚本生成能力：第一阶段将自然语言转换为结构化JSON（操作意图），第二阶段基于JSON和页面快照生成包含最优选择器的完整测试脚本。

#### Scenario: 两阶段脚本生成流程
- **WHEN** 用户创建测试用例并输入自然语言测试步骤
- **THEN** 系统首先调用自然语言解析服务，将自然语言转换为结构化JSON
- **AND** 结构化JSON包含：stepNumber、action、target（中文描述）、value、description
- **AND** 系统捕获目标页面的快照，提取所有可交互元素和选择器
- **AND** 系统将结构化JSON和页面快照发送给AI服务
- **AND** AI分析每个操作的target字段，在快照中匹配最优元素
- **AND** 系统返回包含精确选择器和备用选择器的完整测试脚本

#### Scenario: 基于JSON和快照生成脚本API接口
- **WHEN** 客户端调用POST /api/ai/generate-script-from-json
- **AND** 请求体包含：stepsJson（结构化操作步骤）、pageSnapshot（页面快照）
- **THEN** 系统分析stepsJson中的每个操作步骤
- **AND** 系统在pageSnapshot中查找与target匹配的元素
- **AND** 系统按优先级选择：data-testid > id > name > aria-label > text > css
- **AND** 系统为主选择器生成至少一个备用选择器
- **AND** 系统返回完整的测试脚本，每个步骤包含：selector、selectorType、confidence、fallbackSelectors

#### Scenario: JSON中的target字段匹配元素
- **WHEN** stepsJson中包含{"action": "fill", "target": "用户名输入框", "value": "admin"}
- **AND** pageSnapshot中包含id="username"且aria-label="用户名"的input元素
- **THEN** 系统匹配该元素并生成selector="#username"
- **AND** 系统在返回结果中标记matchedBy="ariaLabel='用户名' + id='username'"
- **AND** 系统设置confidence=0.98表示高置信度
- **AND** 系统生成fallbackSelectors包含[name='username']、[aria-label='用户名']

#### Scenario: 未找到匹配元素时的处理
- **WHEN** stepsJson中的target在pageSnapshot中找不到匹配元素
- **THEN** 系统在返回结果中标记该步骤的selector为null
- **AND** 系统设置confidence=0.0
- **AND** 系统在warning字段中提示"未找到匹配元素：{target}"
- **AND** 系统提供可能相似的元素列表供用户选择
- **AND** 用户可以手动指定选择器后重新生成

#### Scenario: 返回完整的生成元数据
- **WHEN** 系统完成基于JSON和快照的脚本生成
- **THEN** 返回结果包含generationMetadata字段
- **AND** generationMetadata包含：totalSteps、matchedElements、unmatchedTargets、averageConfidence
- **AND** 调用方可以根据元数据评估脚本质量
- **AND** 如果unmatchedTargets不为空，提示用户需要手动处理

### Requirement: 系统对普通用户屏蔽AI服务细节
系统 SHALL 向普通用户隐藏AI服务的调用细节，仅展示功能结果。

#### Scenario: 用户看到的是解析结果而非AI调用
- **WHEN** 用户使用自然语言解析功能
- **THEN** 系统显示"正在解析..."加载提示
- **AND** 系统不显示使用的AI服务提供商
- **AND** 系统不显示API调用详情、token消耗等信息

#### Scenario: AI服务失败时的友好提示
- **WHEN** AI服务调用失败
- **THEN** 系统显示友好提示："解析失败，请检查输入或手动编辑结构化步骤"
- **AND** 系统不暴露技术错误信息（如API错误码、网络异常等）
- **AND** 系统建议用户联系管理员或使用手动编辑

### Requirement: 系统支持测试用例执行流程
系统 SHALL 提供测试用例执行功能，读取保存的结构化JSON，结合页面快照生成最终脚本并执行。

#### Scenario: 执行测试用例的完整流程
- **WHEN** 用户选择执行测试用例
- **THEN** 系统从test_cases表读取stepsJson（结构化操作步骤）
- **AND** 系统解析最终URL（项目URL + 用例URL，或使用覆盖URL）
- **AND** 系统捕获目标页面的快照
- **AND** 系统调用AI服务，基于stepsJson和页面快照生成包含选择器的脚本
- **AND** 系统使用Playwright执行生成的脚本
- **AND** 系统记录每个步骤的执行结果（状态、截图、耗时）
- **AND** 系统保存执行记录到test_case_executions表

#### Scenario: URL解析规则
- **WHEN** 测试用例指定了绝对URL（以http开头）
- **THEN** 系统直接使用该URL执行
- **WHEN** 测试用例指定了相对路径（以/开头）
- **THEN** 系统拼接项目targetUrl和用例相对路径
- **WHEN** 测试用例未指定targetUrl
- **THEN** 系统使用项目的targetUrl作为执行URL
- **WHEN** 执行请求中提供了覆盖URL
- **THEN** 系统优先使用覆盖URL（用于切换测试环境）

#### Scenario: 生成并执行脚本
- **WHEN** 系统生成包含选择器的最终脚本
- **THEN** 系统使用Playwright启动浏览器
- **AND** 系统按顺序执行每个测试步骤
- **AND** 系统在每步完成后截图（如果配置启用）
- **AND** 系统记录每步的执行状态：SUCCESS、FAILED、SKIPPED
- **AND** 系统返回完整的执行结果和截图

#### Scenario: 执行结果保存
- **WHEN** 测试用例执行完成
- **THEN** 系统创建test_case_executions记录
- **AND** 系统保存executionUrl、status、duration等字段
- **AND** 系统保存generatedScript（包含选择器的完整脚本）
- **AND** 系统保存stepsResult（每个步骤的执行结果）
- **AND** 系统保存screenshots（截图数组）
- **AND** 系统返回executionId供查询

#### Scenario: 执行失败时的处理
- **WHEN** 执行过程中某个步骤失败
- **THEN** 系统停止执行后续步骤
- **AND** 系统记录失败步骤的错误信息
- **AND** 系统标记执行状态为FAILED
- **AND** 系统返回失败截图和错误堆栈

#### Scenario: 查询执行历史
- **WHEN** 客户端调用GET /api/testcases/{id}/executions
- **THEN** 系统返回该用例的所有执行记录
- **AND** 返回信息包含：执行时间、状态、耗时、成功/失败步骤数
- **AND** 系统按created_time倒序排列

#### Scenario: 获取执行详情
- **WHEN** 客户端调用GET /api/executions/{id}/detail
- **THEN** 系统返回执行记录的详细信息
- **AND** 返回信息包含：finalUrl、generatedScript、stepsResult、screenshots
- **AND** 用户可以查看每个步骤的执行情况和截图

### Requirement: 系统集成到测试用例创建流程
系统 SHALL 将自然语言解析和脚本生成功能集成到测试用例创建和编辑流程。

#### Scenario: 创建用例时提供自然语言输入框
- **WHEN** 用户创建新的测试用例
- **THEN** 系统显示"测试步骤（自然语言）"文本框
- **AND** 系统提供"自动解析"按钮
- **AND** 系统在用户停止输入1秒后自动触发解析（防抖）

#### Scenario: 解析结果自动填充到结构化步骤
- **WHEN** 自然语言解析成功
- **THEN** 系统自动将解析结果填充到"结构化步骤"区域
- **AND** 系统显示每个步骤的可编辑卡片
- **AND** 用户可以手动修改任何步骤的细节

#### Scenario: 支持基于页面生成脚本
- **WHEN** 用户在测试用例中点击"基于页面生成脚本"
- **THEN** 系统弹出对话框让用户输入目标URL
- **AND** 系统捕获页面快照
- **AND** 系统显示可交互元素列表供用户参考
- **AND** 系统提供自然语言输入框让用户描述操作
- **AND** 系统生成脚本后填充到测试用例的步骤中

### Requirement: 系统支持页面快照导出
系统 SHALL 允许用户导出页面快照数据，用于离线分析或调试。

#### Scenario: 导出页面快照JSON
- **WHEN** 用户请求导出页面快照
- **THEN** 系统返回完整的页面快照JSON
- **AND** JSON包含：URL、标题、所有可交互元素的属性和选择器
- **AND** 用户可以将JSON保存为文件用于后续分析

#### Scenario: 导出格式化的人类可读报告
- **WHEN** 用户请求导出页面快照
- **AND** 指定格式为"人类可读"
- **THEN** 系统生成包含元素表格的HTML报告
- **AND** 报告包含每个元素的标签、选择器、文本内容等
- **AND** 用户可以直接在报告中查看和选择元素