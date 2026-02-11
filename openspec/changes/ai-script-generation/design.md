# AI脚本生成模块设计文档

本文档定义了AI驱动的Web自动化测试脚本生成系统的完整设计方案，包括AI执行流程、Prompt设计、数据库设计等。

---

# 设计概述

本系统提供两大核心AI转化能力：

1. **自然语言解析**：将用户输入的自然语言测试步骤转换为结构化的JSON格式
2. **智能脚本生成**：基于页面快照和自然语言意图，生成包含最优选择器的测试脚本

### 设计原则

- **专注转化**：本模块仅负责AI转化，不涉及脚本的生命周期管理
- **用户透明**：对普通用户完全屏蔽AI服务细节
- **灵活接入**：支持多种AI服务接入方式（直接API Key、自定义HTTP端点）
- **可追溯**：记录所有AI调用和Prompt，便于优化和调试

---

# 第一部分：AI执行流程

## 一、自然语言解析流程

### 1.1 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                     自然语言解析流程                              │
└─────────────────────────────────────────────────────────────────┘

步骤1: 接收解析请求
  │
  ├─ 输入：stepsText（自然语言测试步骤）
  ├─ 验证：输入不为空，长度不超过限制
  │
  ▼
步骤2: 构建Prompt
  │
  ├─ 从数据库加载Prompt模板
  ├─ 填充系统指令和示例
  ├─ 组装用户输入
  │
  ▼
步骤3: 选择AI服务
  │
  ├─ 获取默认AI服务配置
  ├─ 解密API密钥
  ├─ 准备请求头
  │
  ▼
步骤4: 调用AI服务
  │
  ├─ 发送请求到AI服务
  ├─ 设置超时（30秒）
  ├─ 等待响应
  │
  ▼
步骤5: 解析响应
  │
  ├─ 提取JSON内容
  ├─ 验证JSON格式
  ├─ 解析为TestStep数组
  │
  ▼
步骤6: 验证结果
  │
  ├─ 验证每个步骤的必需字段
  ├─ 检查操作类型是否合法
  ├─ 验证选择器格式
  │
  ▼
步骤7: 记录调用日志
  │
  ├─ 保存到ai_call_logs表
  ├─ 包含：输入、输出、耗时、成功状态
  │
  ▼
步骤8: 返回结果
```

### 1.2 详细实现

```java
@Service
public class NaturalLanguageParser {

    @Autowired
    private LLMService llmService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private AICallLogService callLogService;

    public ParseResult parseSteps(String stepsText) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 加载激活的Prompt模板
            PromptTemplate template = promptTemplateService
                .getActiveTemplate("natural_language_parse");

            // 2. 构建完整Prompt
            String fullPrompt = template.getPromptContent() + "\n\n" +
                               "用户输入：\n" + stepsText;

            // 3. 调用AI服务
            String jsonResponse = llmService.generateText(fullPrompt);

            // 4. 解析响应
            List<TestStep> steps = parseJSON(jsonResponse);

            // 5. 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logSuccess(
                "natural_language_parse",
                template.getId(),
                fullPrompt,
                jsonResponse,
                duration
            );

            return ParseResult.success(steps);

        } catch (Exception e) {
            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logFailure(
                "natural_language_parse",
                null,
                stepsText,
                e.getMessage(),
                duration
            );

            return ParseResult.failure(e.getMessage());
        }
    }
}
```

## 二、智能脚本生成流程

### 2.1 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                     智能脚本生成流程                              │
└─────────────────────────────────────────────────────────────────┘

步骤1: 接收生成请求
  │
  ├─ 输入：url 或 pageSnapshot、naturalLanguage
  ├─ 验证：URL格式或快照完整性
  │
  ▼
步骤2: 捕获页面快照（如需要）
  │
  ├─ 如果提供URL：
  │  ├─ 启动Playwright浏览器
  │  ├─ 导航到目标URL
  │  ├─ 等待页面加载完成
  │  └─ 提取可交互元素
  └─ 如果提供pageSnapshot：直接使用
  │
  ▼
步骤3: 生成元素选择器
  │
  ├─ 为每个元素生成多种选择器
  ├─ 按优先级排序：testId > id > name > aria > text > css
  ├─ 计算元素位置和状态
  │
  ▼
步骤4: 构建Prompt
  │
  ├─ 从数据库加载脚本生成模板
  ├─ 填充系统指令
  ├─ 序列化页面快照为JSON
  ├─ 组装用户意图
  │
  ▼
步骤5: 选择AI服务
  │
  ├─ 获取默认AI服务配置
  ├─ 解密API密钥
  │
  ▼
步骤6: 调用AI服务
  │
  ├─ 构建请求体（可能很大，包含快照）
  ├─ 设置较长超时（60秒）
  ├─ 等待响应
  │
  ▼
步骤7: 解析响应
  │
  ├─ 提取JSON内容
  ├─ 解析为TestStep数组
  ├─ 提取选择器和元数据
  │
  ▼
步骤8: 验证结果
  │
  ├─ 验证每个步骤
  ├─ 检查选择器是否存在于快照
  ├─ 验证操作类型和参数
  │
  ▼
步骤9: 记录调用日志
  │
  ├─ 保存到ai_call_logs表
  ├─ 保存快照哈希
  ├─ 关联Prompt模板ID
  │
  ▼
步骤10: 返回结果
  │
  ├─ 返回结构化步骤
  ├─ 返回页面快照信息
  └─ 返回元数据
```

### 2.2 详细实现

```java
@Service
public class ScriptGenerationService {

    @Autowired
    private PageSnapshotService snapshotService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private AICallLogService callLogService;

    public GenerateResult generateScript(
        String url,
        PageSnapshot providedSnapshot,
        String naturalLanguage
    ) {
        long startTime = System.currentTimeMillis();
        PageSnapshot snapshot = providedSnapshot;

        try {
            // 1. 获取或捕获页面快照
            if (snapshot == null && url != null) {
                snapshot = snapshotService.captureSnapshot(url);
            }

            if (snapshot == null) {
                throw new IllegalArgumentException("必须提供URL或页面快照");
            }

            // 2. 生成选择器
            snapshotService.generateSelectors(snapshot);

            // 3. 加载Prompt模板
            PromptTemplate template = promptTemplateService
                .getActiveTemplate("script_generation");

            // 4. 构建完整Prompt
            String snapshotJson = serializeSnapshot(snapshot);
            String fullPrompt = template.getPromptContent() + "\n\n" +
                               "用户意图：\n" + naturalLanguage + "\n\n" +
                               "页面快照：\n" + snapshotJson;

            // 5. 调用AI服务
            String jsonResponse = llmService.generateText(fullPrompt);

            // 6. 解析响应
            List<TestStep> steps = parseJSON(jsonResponse);

            // 7. 验证步骤
            validateSteps(steps, snapshot);

            // 8. 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logSuccess(
                "script_generation",
                template.getId(),
                fullPrompt,
                jsonResponse,
                duration,
                snapshot.getHash()
            );

            return GenerateResult.success(steps, snapshot);

        } catch (Exception e) {
            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logFailure(
                "script_generation",
                null,
                naturalLanguage,
                e.getMessage(),
                duration,
                snapshot != null ? snapshot.getHash() : null
            );

            return GenerateResult.failure(e.getMessage(), snapshot);
        }
    }
}
```

## 三、基于JSON和快照的脚本生成流程（两阶段方案）

### 3.1 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│          基于JSON和快照的智能脚本生成流程（两阶段）                │
└─────────────────────────────────────────────────────────────────┘

【阶段1：自然语言 → 结构化JSON】

步骤1: 接收自然语言测试步骤
  │
  ├─ 输入：naturalLanguageSteps（自然语言描述）
  │
  ▼
步骤2: 调用自然语言解析服务
  │
  ├─ 使用natural_language_parse模板
  ├─ AI解析操作意图
  │
  ▼
步骤3: 返回结构化JSON
  │
  ├─ 包含：stepNumber、action、target（中文描述）、value、description
  ├─ 注意：此时selector字段为空或占位符
  │
  ▼
【阶段1完成，获得操作意图JSON】

───────────────────────────────────────────────────────────────

【阶段2：JSON + 页面快照 → 完整脚本】

步骤4: 捕获页面快照（如需要）
  │
  ├─ 如果提供URL：使用Playwright捕获快照
  ├─ 如果提供pageSnapshot：直接使用
  │
  ▼
步骤5: 提取页面可交互元素
  │
  ├─ 提取所有input、button、a等元素
  ├─ 为每个元素生成多种选择器
  │
  ▼
步骤6: 构建阶段2的Prompt
  │
  ├─ 从数据库加载script_generation_from_json模板
  ├─ 填充系统指令
  ├─ 序列化结构化JSON（stepsJson）
  ├─ 序列化页面快照（pageSnapshot）
  │
  ▼
步骤7: 调用AI服务
  │
  ├─ 发送请求（包含stepsJson和pageSnapshot）
  ├─ 设置超时（60秒）
  │
  ▼
步骤8: AI分析和匹配
  │
  ├─ 分析每个步骤的target字段
  ├─ 在pageSnapshot中查找匹配元素
  ├─ 按优先级选择：testId > id > name > aria > text > css
  ├─ 为每个步骤生成主选择器和备用选择器
  │
  ▼
步骤9: 返回完整测试脚本
  │
  ├─ 每个步骤包含：selector、selectorType、confidence、fallbackSelectors
  ├─ 包含generationMetadata（匹配统计、置信度等）
  │
  ▼
步骤10: 记录调用日志
  │
  ├─ 保存到ai_call_logs表
  ├─ 关联两个阶段的调用记录
  │
  ▼
步骤11: 返回最终结果
```

### 3.2 详细实现

```java
@Service
public class TwoPhaseScriptGenerationService {

    @Autowired
    private NaturalLanguageParser naturalLanguageParser;

    @Autowired
    private PageSnapshotService snapshotService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private AICallLogService callLogService;

    /**
     * 两阶段脚本生成：自然语言 → JSON → 完整脚本
     */
    public TwoPhaseGenerateResult generateScriptInTwoPhases(
        String naturalLanguageSteps,
        String url,
        PageSnapshot providedSnapshot
    ) {
        long startTime = System.currentTimeMillis();

        try {
            // ========== 阶段1：自然语言 → 结构化JSON ==========
            ParseResult parseResult = naturalLanguageParser.parseSteps(naturalLanguageSteps);

            if (!parseResult.isSuccess()) {
                return TwoPhaseGenerateResult.failure("阶段1解析失败: " + parseResult.getErrorMessage());
            }

            List<TestStep> stepsJson = parseResult.getSteps();
            String stepsJsonString = JSON.toJSONString(stepsJson);

            // ========== 阶段2：JSON + 页面快照 → 完整脚本 ==========

            // 1. 获取或捕获页面快照
            PageSnapshot snapshot = providedSnapshot;
            if (snapshot == null && url != null) {
                snapshot = snapshotService.captureSnapshot(url);
            }

            if (snapshot == null) {
                return TwoPhaseGenerateResult.failure("必须提供URL或页面快照");
            }

            // 2. 生成选择器
            snapshotService.generateSelectors(snapshot);

            // 3. 加载Prompt模板
            PromptTemplate template = promptTemplateService
                .getActiveTemplate("script_generation_from_json");

            // 4. 构建完整Prompt
            String snapshotJson = JSON.toJSONString(snapshot);
            String fullPrompt = template.getPromptContent() + "\n\n" +
                               "=== 结构化操作步骤（JSON）===\n" +
                               stepsJsonString + "\n\n" +
                               "=== 页面快照 ===\n" +
                               snapshotJson;

            // 5. 调用AI服务
            String jsonResponse = llmService.generateText(fullPrompt);

            // 6. 解析响应
            List<TestStepWithSelectors> finalSteps = parseJSONWithSelectors(jsonResponse);

            // 7. 验证步骤
            GenerationMetadata metadata = validateAndMetadata(finalSteps, snapshot);

            // 8. 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logSuccess(
                "script_generation_from_json",
                template.getUniqueId(),
                fullPrompt,
                jsonResponse,
                duration,
                snapshot.getHash()
            );

            return TwoPhaseGenerateResult.success(finalSteps, snapshot, metadata);

        } catch (Exception e) {
            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logFailure(
                "script_generation_from_json",
                null,
                naturalLanguageSteps,
                e.getMessage(),
                duration,
                null
            );

            return TwoPhaseGenerateResult.failure(e.getMessage());
        }
    }

    /**
     * 单独的阶段2：基于已有JSON和快照生成脚本
     */
    public GenerateResult generateScriptFromJsonAndSnapshot(
        List<TestStep> stepsJson,
        PageSnapshot snapshot
    ) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 生成选择器（如果还没生成）
            if (snapshot.getSelectors() == null || snapshot.getSelectors().isEmpty()) {
                snapshotService.generateSelectors(snapshot);
            }

            // 2. 加载Prompt模板
            PromptTemplate template = promptTemplateService
                .getActiveTemplate("script_generation_from_json");

            // 3. 构建完整Prompt
            String stepsJsonString = JSON.toJSONString(stepsJson);
            String snapshotJson = JSON.toJSONString(snapshot);
            String fullPrompt = template.getPromptContent() + "\n\n" +
                               "=== 结构化操作步骤（JSON）===\n" +
                               stepsJsonString + "\n\n" +
                               "=== 页面快照 ===\n" +
                               snapshotJson;

            // 4. 调用AI服务
            String jsonResponse = llmService.generateText(fullPrompt);

            // 5. 解析响应
            List<TestStepWithSelectors> finalSteps = parseJSONWithSelectors(jsonResponse);

            // 6. 验证步骤并生成元数据
            GenerationMetadata metadata = validateAndMetadata(finalSteps, snapshot);

            // 7. 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logSuccess(
                "script_generation_from_json",
                template.getUniqueId(),
                fullPrompt,
                jsonResponse,
                duration,
                snapshot.getHash()
            );

            return GenerateResult.success(finalSteps, snapshot, metadata);

        } catch (Exception e) {
            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            callLogService.logFailure(
                "script_generation_from_json",
                null,
                JSON.toJSONString(stepsJson),
                e.getMessage(),
                duration,
                snapshot.getHash()
            );

            return GenerateResult.failure(e.getMessage(), snapshot);
        }
    }

    /**
     * 验证步骤并生成元数据
     */
    private GenerationMetadata validateAndMetadata(
        List<TestStepWithSelectors> steps,
        PageSnapshot snapshot
    ) {
        int totalSteps = steps.size();
        int matchedElements = 0;
        int unmatchedTargets = 0;
        double totalConfidence = 0.0;

        List<String> unmatchedList = new ArrayList<>();

        for (TestStepWithSelectors step : steps) {
            if (step.getSelector() != null) {
                matchedElements++;
                totalConfidence += step.getConfidence();
            } else {
                unmatchedTargets++;
                unmatchedList.add(step.getTarget());
            }
        }

        double averageConfidence = matchedElements > 0
            ? totalConfidence / matchedElements
            : 0.0;

        return GenerationMetadata.builder()
            .totalSteps(totalSteps)
            .matchedElements(matchedElements)
            .unmatchedTargets(unmatchedList)
            .averageConfidence(averageConfidence)
            .build();
    }
}
```

### 3.3 数据模型

```java
/**
 * 阶段1的结构化步骤（操作意图）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStep {
    private Integer stepNumber;
    private String action;        // 操作类型
    private String target;        // 目标描述（中文），如"用户名输入框"
    private String value;         // 参数值
    private String description;   // 步骤描述
}

/**
 * 阶段2的完整步骤（包含选择器）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStepWithSelectors {
    private Integer stepNumber;
    private String action;
    private String target;        // 保留原始target描述
    private String value;
    private String description;

    // 新增字段
    private String selector;      // 匹配到的最优选择器
    private String selectorType;  // 选择器类型：testId/id/name/aria/text/css
    private Double confidence;    // 匹配置信度：0.0-1.0
    private String matchedBy;     // 匹配依据说明
    private List<SelectorMetadata> fallbackSelectors;  // 备用选择器
    private String warning;       // 警告信息（如未找到匹配元素）
}

/**
 * 生成元数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationMetadata {
    private Integer totalSteps;           // 总步骤数
    private Integer matchedElements;      // 成功匹配的元素数
    private List<String> unmatchedTargets; // 未匹配的目标列表
    private Double averageConfidence;     // 平均置信度
}
```

---

# 第二部分：Prompt设计

## 一、Prompt模板表

```sql
CREATE TABLE prompt_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型：natural_language_parse, script_generation',

    -- Prompt内容（整合所有内容）
    prompt_content TEXT NOT NULL COMMENT '完整的Prompt内容（包含系统指令、输出格式、示例等）',

    -- 状态
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',

    -- 描述
    description TEXT COMMENT '模板描述',

    -- 审计字段
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(100),

    INDEX idx_template_type (template_type),
    INDEX idx_is_active (is_active)
) COMMENT='Prompt模板表';
```

### Prompt内容结构

`prompt_content` 字段包含完整的Prompt，结构如下：

```
[系统指令部分]

你是一个专业的Web自动化测试专家。你的任务是...

## 支持的操作类型
- navigate: 导航到URL
- click: 点击元素
...

## 选择器优先级策略
1. data-testid: ...
...

## 输出要求
- 必须返回有效的JSON数组
...

[输出格式说明]

请直接返回JSON数组，不要包含任何额外的说明文字。

[示例部分]

用户输入：...
输出：...
...
```

## 二、自然语言解析Prompt

### 2.1 模板内容

**system_prompt**:
```
你是一个专业的Web自动化测试专家。你的任务是将用户输入的自然语言测试步骤转换为结构化的JSON格式。

## 职责说明

1. 理解用户的测试意图，识别测试步骤中的关键操作
2. 将自然语言转换为标准化的操作类型
3. 提取操作的目标对象和参数值
4. 保持步骤的顺序和逻辑关系

## 支持的操作类型

| 操作类型 | 说明 | 必需参数 | 可选参数 |
|---------|------|---------|---------|
| navigate | 导航到URL | value | - |
| click | 点击元素 | target | waitMs |
| fill | 填写输入框 | target, value | clear |
| select | 选择下拉选项 | target, value | - |
| hover | 鼠标悬停 | target | - |
| wait | 等待元素 | target | timeout |
| assert | 验证结果 | expected | actual |
| scroll | 滚动页面 | direction | distance |

## 目标选择规则

1. 如果用户明确提到了元素特征（id、class、name等），使用该特征作为target
2. 如果用户未明确指定，target可以留空或使用描述性文字
3. 支持多种选择器格式：
   - ID选择器：#elementId
   - Class选择器：.element-class
   - 属性选择器：[name='username']
   - 文本选择器：text=登录
   - CSS选择器：form input[type='text']

## 输出要求

1. 必须返回有效的JSON数组
2. 每个步骤必须包含：stepNumber, action, description
3. 根据action类型包含必需的参数
4. stepNumber从1开始连续递增
5. description字段使用简洁的中文描述

## 示例

用户输入：
"打开登录页面，在用户名输入框输入admin，在密码框输入123456，然后点击登录按钮"

输出：
```json
[
  {
    "stepNumber": 1,
    "action": "navigate",
    "value": "/login",
    "description": "打开登录页面"
  },
  {
    "stepNumber": 2,
    "action": "fill",
    "target": "#username",
    "value": "admin",
    "description": "输入用户名"
  },
  {
    "stepNumber": 3,
    "action": "fill",
    "target": "#password",
    "value": "123456",
    "description": "输入密码"
  },
  {
    "stepNumber": 4,
    "action": "click",
    "target": "button[type='submit']",
    "description": "点击登录按钮"
  }
]
```

更多示例：

用户输入：1. 打开首页 2. 点击搜索按钮 3. 输入关键词 4. 按回车搜索
输出：
[
  {"stepNumber": 1, "action": "navigate", "value": "/", "description": "打开首页"},
  {"stepNumber": 2, "action": "click", "target": ".search-button", "description": "点击搜索按钮"},
  {"stepNumber": 3, "action": "fill", "target": ".search-input", "value": "关键词", "description": "输入搜索关键词"},
  {"stepNumber": 4, "action": "click", "target": "button[type='submit']", "description": "提交搜索"}
]

用户输入：进入个人中心，查看订单列表
输出：
[
  {"stepNumber": 1, "action": "navigate", "value": "/user/center", "description": "进入个人中心"},
  {"stepNumber": 2, "action": "navigate", "value": "/user/orders", "description": "查看订单列表"}
]

请直接返回JSON数组，不要包含任何额外的说明文字、代码块标记（如```json）。
输出格式必须严格符合上述示例的结构。
```

## 三、智能脚本生成Prompt

### 3.1 模板内容示例

完整的 `prompt_content` 字段内容：

```
你是一个专业的Web自动化测试专家。你的任务是根据用户的自然语言描述和页面快照，生成包含最优选择器的测试脚本。

## 核心职责

1. 理解用户的测试意图，识别需要操作的目标元素
2. 在页面快照中查找最匹配的元素
3. 为每个操作选择最优的选择器
4. 生成备用选择器以提高鲁棒性

## 选择器优先级策略

你必须按照以下优先级为元素选择选择器：

| 优先级 | 选择器类型 | 说明 | 示例 |
|-------|-----------|------|------|
| 1 | data-testid | 最稳定，由开发专门为测试添加 | [data-testid='login-button'] |
| 2 | id | 通常唯一且相对稳定 | #username |
| 3 | name | 表单元素常用 | [name='username'] |
| 4 | aria-label | 可访问性属性，稳定性好 | [aria-label='搜索'] |
| 5 | text | 按钮和链接的文本内容 | text=登录 |
| 6 | CSS选择器 | 综合选择器，可能受样式影响 | form.login-form input[type='text'] |
| 7 | XPath | 最后的备选方案 | //button[@type='submit'] |

**重要**：
- 优先级高的选择器存在时，必须使用
- 如果某个元素有data-testid属性，必须使用它作为主选择器
- 必须为每个步骤至少提供1个备用选择器

## 页面快照说明

页面快照是一个JSON结构，包含以下信息：
{
  "url": "https://example.com/login",
  "title": "用户登录",
  "hash": "abc123",
  "interactiveElements": [
    {
      "tagName": "input",
      "elementType": "text",
      "id": "username",
      "name": "username",
      "placeholder": "请输入用户名",
      "selectors": {
        "testId": null,
        "id": "#username",
        "name": "[name='username']",
        "aria": null,
        "text": null,
        "css": "input[type='text']"
      }
    }
  ]
}

每个元素的`selectors`字段已经为你生成了所有可能的选择器选项，你只需要根据优先级选择。

## 匹配规则

1. **精确匹配优先**：用户提到的元素特征（id、name、placeholder等）必须在快照中精确匹配
2. **语义匹配**：理解用户的描述（如"用户名输入框"），匹配placeholder、name、aria-label等语义相关字段
3. **位置匹配**：如果多个元素相似，优先选择位置靠前或更显著的元素
4. **文本匹配**：按钮和链接优先使用文本内容匹配

## 生成规则

1. 每个步骤必须包含：
   - stepNumber: 步骤序号（从1开始）
   - action: 操作类型
   - selector: 主选择器（必须使用最优选择器）
   - selectorType: 选择器类型
   - value: 参数值（如需要）
   - description: 中文描述
   - fallbackSelectors: 备用选择器数组（至少1个）
   - confidence: 匹配置信度（0-1之间的浮点数）
   - reasoning: 选择理由（简短说明）

2. 选择器类型必须是以下之一：
   - testId, id, name, aria, text, css, xpath

3. 置信度评估：
   - 0.95-1.00: 元素特征完全匹配，使用最优选择器
   - 0.85-0.94: 元素特征基本匹配，选择器合理
   - 0.70-0.84: 存在多个相似元素，可能不是最优选择
   - < 0.70: 匹配不明确，建议用户确认

4. 如果无法找到匹配的元素：
   - 不要强行生成选择器
   - 在reasoning中说明问题
   - confidence设为0

## 示例

示例1：
用户意图：在登录表单中输入用户名test@example.com
页面快照：
{
  "url": "https://example.com/login",
  "interactiveElements": [
    {
      "tagName": "input",
      "id": "username",
      "name": "username",
      "type": "text",
      "placeholder": "请输入用户名",
      "selectors": {
        "id": "#username",
        "name": "[name='username']",
        "css": "input[type='text']"
      }
    }
  ]
}

输出：
[
  {
    "stepNumber": 1,
    "action": "fill",
    "selector": "#username",
    "selectorType": "id",
    "value": "test@example.com",
    "description": "输入用户名",
    "fallbackSelectors": ["[name='username']", "input[placeholder='请输入用户名']"],
    "confidence": 0.98,
    "reasoning": "找到id为username的输入框，与用户描述完全匹配"
  }
]

示例2：
用户意图：点击登录按钮
页面快照：
{
  "url": "https://example.com/login",
  "interactiveElements": [
    {
      "tagName": "button",
      "testId": "login-button",
      "type": "submit",
      "textContent": "登录",
      "selectors": {
        "testId": "[data-testid='login-button']",
        "css": "button[type='submit']",
        "text": "text=登录"
      }
    }
  ]
}

输出：
[
  {
    "stepNumber": 1,
    "action": "click",
    "selector": "[data-testid='login-button']",
    "selectorType": "testId",
    "description": "点击登录按钮",
    "fallbackSelectors": ["text=登录", "button[type='submit']"],
    "confidence": 1.0,
    "reasoning": "找到data-testid为login-button的按钮，是最优选择器"
  }
]

请直接返回JSON数组，不要包含任何额外的说明文字、代码块标记（如```json）。
数组中的每个步骤必须严格按照上述格式。
```

## 三、基于JSON和快照的脚本生成Prompt

### 3.1 模板类型：script_generation_from_json

**template_name**: `基于JSON和快照的脚本生成`
**template_type**: `script_generation_from_json`

### 3.2 模板内容

**system_prompt**:
```
你是一个专业的Web自动化测试脚本生成专家。你的任务是基于结构化的操作步骤JSON和页面快照，生成包含最优选择器的完整测试脚本。

## 核心职责

1. 分析结构化操作步骤中的每个操作（action）和目标描述（target）
2. 在页面快照中查找与target匹配的可交互元素
3. 按优先级选择最优选择器，并生成备用选择器
4. 评估每个匹配的置信度
5. 输出完整的测试脚本，每个步骤包含精确的选择器信息

## 输入格式

你将接收两部分数据：

### 1. 结构化操作步骤（JSON）
```json
[
  {
    "stepNumber": 1,
    "action": "fill",
    "target": "用户名输入框",
    "value": "admin",
    "description": "在用户名输入框输入admin"
  },
  {
    "stepNumber": 2,
    "action": "fill",
    "target": "密码输入框",
    "value": "123456",
    "description": "在密码输入框输入123456"
  },
  {
    "stepNumber": 3,
    "action": "click",
    "target": "登录按钮",
    "description": "点击登录按钮"
  }
]
```

**关键字段说明**：
- `action`: 操作类型（navigate、click、fill、select、wait、assert等）
- `target`: 目标元素的中文描述（如"用户名输入框"、"登录按钮"）
- `value`: 操作的参数值（如输入的文本内容）
- `description`: 步骤的中文描述

### 2. 页面快照（JSON）
```json
{
  "url": "https://example.com/login",
  "title": "用户登录",
  "hash": "abc123",
  "elements": [
    {
      "tagName": "input",
      "id": "username",
      "name": "username",
      "type": "text",
      "placeholder": "请输入用户名",
      "ariaLabel": "用户名",
      "selectors": [
        {"type": "id", "expression": "#username", "priority": 2},
        {"type": "name", "expression": "[name='username']", "priority": 3},
        {"type": "aria", "expression": "[aria-label='用户名']", "priority": 4}
      ]
    },
    {
      "tagName": "input",
      "id": "password",
      "name": "password",
      "type": "password",
      "placeholder": "请输入密码",
      "ariaLabel": "密码",
      "selectors": [
        {"type": "id", "expression": "#password", "priority": 2},
        {"type": "name", "expression": "[name='password']", "priority": 3}
      ]
    },
    {
      "tagName": "button",
      "id": "login-btn",
      "textContent": "登录",
      "type": "submit",
      "selectors": [
        {"type": "id", "expression": "#login-btn", "priority": 2},
        {"type": "text", "expression": "text='登录'", "priority": 5}
      ]
    }
  ]
}
```

## 选择器优先级策略

你必须严格按照以下优先级选择选择器：

| 优先级 | 类型 | 说明 |
|-------|------|------|
| 1 | testId | data-testid属性 |
| 2 | id | HTML id属性 |
| 3 | name | name属性 |
| 4 | aria | aria-label属性 |
| 5 | text | 文本内容 |
| 6 | css | CSS选择器 |

## 匹配规则

### 1. 语义匹配
分析target字段的中文描述，匹配元素的语义特征：
- `placeholder`、`ariaLabel`、`name`、`id`等字段

**示例**：
- target="用户名输入框" → 匹配 `placeholder="请输入用户名"` 或 `aria-label="用户名"`
- target="登录按钮" → 匹配 `textContent="登录"` 或 `aria-label="登录"`

### 2. 类型匹配
根据action类型匹配元素：
- `fill` → `<input>`、`<textarea>`
- `click` → `<button>`、`<a>`、可点击元素
- `select` → `<select>`

### 3. 置信度评估
基于匹配质量设置confidence值：
- **0.95-1.00**: 完美匹配（所有特征都吻合）
- **0.85-0.94**: 良好匹配（主要特征吻合）
- **0.70-0.84**: 基本匹配（部分特征吻合，可能存在歧义）
- **< 0.70**: 低置信度（匹配不明确，建议人工确认）
- **0.00**: 未找到匹配元素

### 4. 未匹配处理
如果找不到匹配元素：
- `selector` 设为 `null`
- `confidence` 设为 `0.0`
- `warning` 字段说明问题
- 在`matchedBy`中解释原因

## 输出格式

返回一个JSON数组，每个步骤包含以下字段：

```json
[
  {
    "stepNumber": 1,
    "action": "fill",
    "target": "用户名输入框",
    "selector": "#username",
    "value": "admin",
    "description": "在用户名输入框输入admin",
    "selectorType": "id",
    "confidence": 0.98,
    "matchedBy": "ariaLabel='用户名' + id='username'",
    "fallbackSelectors": [
      {"type": "name", "expression": "[name='username']"},
      {"type": "aria", "expression": "[aria-label='用户名']"}
    ],
    "warning": null
  }
]
```

**字段说明**：
- `stepNumber`: 步骤序号（与输入一致）
- `action`: 操作类型（与输入一致）
- `target`: 原始目标描述（保留）
- `selector`: 匹配到的最优选择器表达式
- `value`: 参数值（与输入一致）
- `description`: 步骤描述（与输入一致）
- `selectorType`: 主选择器类型
- `confidence`: 匹配置信度（0.0-1.0）
- `matchedBy`: 匹配依据说明
- `fallbackSelectors`: 备用选择器数组
- `warning`: 警告信息（如有）

## 完整示例

### 输入

**结构化操作步骤**:
```json
[
  {"stepNumber": 1, "action": "fill", "target": "用户名输入框", "value": "test@example.com"},
  {"stepNumber": 2, "action": "fill", "target": "密码输入框", "value": "Password123"},
  {"stepNumber": 3, "action": "click", "target": "登录按钮"}
]
```

**页面快照**:
```json
{
  "url": "https://example.com/login",
  "elements": [
    {
      "tagName": "input",
      "id": "username",
      "name": "username",
      "type": "text",
      "placeholder": "请输入用户名",
      "ariaLabel": "用户名",
      "selectors": [
        {"type": "id", "expression": "#username"},
        {"type": "name", "expression": "[name='username']"},
        {"type": "aria", "expression": "[aria-label='用户名']"}
      ]
    },
    {
      "tagName": "input",
      "id": "password",
      "name": "password",
      "type": "password",
      "placeholder": "请输入密码",
      "ariaLabel": "密码",
      "selectors": [
        {"type": "id", "expression": "#password"},
        {"type": "name", "expression": "[name='password']"}
      ]
    },
    {
      "tagName": "button",
      "id": "login-btn",
      "textContent": "登录",
      "selectors": [
        {"type": "id", "expression": "#login-btn"},
        {"type": "text", "expression": "text='登录'"}
      ]
    }
  ]
}
```

### 输出

```json
[
  {
    "stepNumber": 1,
    "action": "fill",
    "target": "用户名输入框",
    "selector": "#username",
    "value": "test@example.com",
    "description": "在用户名输入框输入test@example.com",
    "selectorType": "id",
    "confidence": 0.98,
    "matchedBy": "ariaLabel='用户名' + id='username' + placeholder='请输入用户名'",
    "fallbackSelectors": [
      {"type": "name", "expression": "[name='username']"},
      {"type": "aria", "expression": "[aria-label='用户名']"}
    ],
    "warning": null
  },
  {
    "stepNumber": 2,
    "action": "fill",
    "target": "密码输入框",
    "selector": "#password",
    "value": "Password123",
    "description": "在密码输入框输入Password123",
    "selectorType": "id",
    "confidence": 0.98,
    "matchedBy": "ariaLabel='密码' + id='password' + placeholder='请输入密码'",
    "fallbackSelectors": [
      {"type": "name", "expression": "[name='password']"}
    ],
    "warning": null
  },
  {
    "stepNumber": 3,
    "action": "click",
    "target": "登录按钮",
    "selector": "#login-btn",
    "description": "点击登录按钮",
    "selectorType": "id",
    "confidence": 0.99,
    "matchedBy": "textContent='登录' + id='login-btn'",
    "fallbackSelectors": [
      {"type": "text", "expression": "text='登录'"}
    ],
    "warning": null
  }
]
```

### 未匹配示例

如果target="注册按钮"但页面快照中没有该元素：

```json
[
  {
    "stepNumber": 1,
    "action": "click",
    "target": "注册按钮",
    "selector": null,
    "description": "点击注册按钮",
    "selectorType": null,
    "confidence": 0.0,
    "matchedBy": "页面快照中未找到匹配的按钮元素",
    "fallbackSelectors": [],
    "warning": "未找到匹配元素：'注册按钮'，请检查页面快照或手动指定选择器"
  }
]
```

## 重要规则

1. **必须为每个步骤生成选择器**（除非确实找不到）
2. **必须包含至少1个备用选择器**（置信度>0.7时）
3. **必须严格按照优先级选择主选择器**
4. **必须基于实际页面快照匹配**，不能凭空推测
5. **必须正确评估置信度**，不要过度自信
6. **未匹配时必须在warning中说明**
7. **返回纯JSON数组**，不要包含代码块标记或额外说明

---

# 第三部分：数据库设计

## 一、AI服务配置表

```sql
CREATE TABLE ai_service_configs (
    unique_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    provider VARCHAR(50) NOT NULL COMMENT '服务提供商：openai, claude, qwen, wenxin, custom',
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',

    -- 认证信息（加密存储）
    api_key VARCHAR(512) COMMENT 'API密钥（AES加密）',
    api_endpoint VARCHAR(512) COMMENT '自定义端点URL',
    custom_headers TEXT COMMENT '自定义请求头JSON',

    -- 连接性状态
    status VARCHAR(20) DEFAULT 'UNTESTED' COMMENT '状态：AVAILABLE, UNAVAILABLE, UNTESTED',
    last_tested_at DATETIME COMMENT '最后测试时间',
    error_message TEXT COMMENT '最后错误信息',
    response_time_ms INT COMMENT '最后响应时间',

    -- 配置选项
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认服务',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',

    -- 标准审计字段
    created_by VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '创建人',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '更新人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (unique_id),
    INDEX idx_status (status),
    INDEX idx_is_default (is_default),
    INDEX idx_priority (priority)
) COMMENT='AI服务配置表';
```

## 二、Prompt模板表

```sql
CREATE TABLE prompt_templates (
    unique_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型：natural_language_parse, script_generation',

    -- Prompt内容（整合所有内容）
    prompt_content TEXT NOT NULL COMMENT '完整的Prompt内容（包含系统指令、输出格式、示例等）',

    -- 状态
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',

    -- 描述
    description TEXT COMMENT '模板描述',

    -- 标准审计字段
    created_by VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '创建人',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '更新人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (unique_id),
    INDEX idx_template_type (template_type),
    INDEX idx_is_active (is_active)
) COMMENT='Prompt模板表';
```

## 三、AI调用日志表

```sql
CREATE TABLE ai_call_logs (
    unique_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 基本信息
    call_type VARCHAR(50) NOT NULL COMMENT '调用类型：natural_language_parse, script_generation',
    template_id BIGINT COMMENT '使用的模板ID',
    config_id BIGINT COMMENT '使用的AI服务配置ID',

    -- 输入输出
    input_text TEXT NOT NULL COMMENT '输入内容（Prompt或用户输入）',
    output_json TEXT COMMENT '输出JSON',
    snapshot_hash VARCHAR(64) COMMENT '页面快照哈希（如适用）',

    -- 结果
    success BOOLEAN NOT NULL COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',
    error_code VARCHAR(50) COMMENT '错误码',

    -- 元数据
    user_id BIGINT COMMENT '用户ID',

    -- 标准审计字段
    created_by VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '创建人',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '更新人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (unique_id),
    INDEX idx_call_type (call_type),
    INDEX idx_template_id (template_id),
    INDEX idx_success (success),
    INDEX idx_created_time (created_time),
    INDEX idx_snapshot_hash (snapshot_hash),
    FOREIGN KEY (template_id) REFERENCES prompt_templates(unique_id),
    FOREIGN KEY (config_id) REFERENCES ai_service_configs(unique_id)
) COMMENT='AI调用日志表';
```

## 四、数据库ER图

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ ai_service_      │       │ prompt_templates │       │ ai_call_logs     │
│ configs          │       │                  │       │                  │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ unique_id (PK)   │──┐    │ unique_id (PK)   │──┐    │ unique_id (PK)   │
│ config_name      │  │    │ template_name    │  │    │ call_type        │
│ provider         │  │    │ template_type    │  │    │ template_id (FK) │◄─────┘
│ model_name       │  │    │ prompt_content   │  │    │ config_id (FK)   │◄─────┐
│ api_key          │  │    │ is_active        │  │    │ input_text       │      │
│ status           │  │    │ description      │  │    │ output_json      │      │
│ ...              │  │    │ created_time     │  │    │ success          │      │
└──────────────────┘  │    │ updated_time     │  │    │ user_id          │      │
                      │    └──────────────────┘  │    │ created_time     │      │
                      │                          │    │ ...              │      │
                      │                          │    └──────────────────┘      │
                      │                          │                              │
                      └──────────────────────────┴──────────────────────────────┘
```

### 表关系说明

- **ai_service_configs** → **ai_call_logs**：一个AI服务配置可以关联多次调用日志（一对多）
- **prompt_templates** → **ai_call_logs**：一个Prompt模板可以关联多次调用日志（一对多）
- **ai_call_logs**：记录每次AI调用的详细信息，关联使用的模板和配置

---

# 第四部分：API接口设计

## 一、AI服务配置API

### 1.1 创建配置

```http
POST /api/ai-service/configs
Content-Type: application/json

Request:
{
  "configName": "OpenAI GPT-4",
  "provider": "openai",
  "modelName": "gpt-4",
  "apiKey": "sk-proj-xxx...",
  "isDefault": false
}

Response:
{
  "id": 1,
  "configName": "OpenAI GPT-4",
  "status": "UNTESTED",
  "createdAt": "2025-02-06T10:00:00Z"
}
```

### 1.2 测试连接

```http
POST /api/ai-service/configs/{id}/test

Response:
{
  "success": true,
  "responseTime": 1250,
  "message": "连接成功"
}
```

## 二、自然语言解析API

```http
POST /api/ai/parse-steps
Content-Type: application/json

Request:
{
  "stepsText": "1. 打开登录页面\n2. 输入用户名admin\n3. 点击登录按钮"
}

Response:
{
  "success": true,
  "steps": [
    {
      "stepNumber": 1,
      "action": "navigate",
      "value": "/login",
      "description": "打开登录页面"
    },
    ...
  ],
  "generationTime": 1500
}
```

## 三、智能脚本生成API

```http
POST /api/ai/generate-script
Content-Type: application/json

Request (方式1 - 提供URL):
{
  "url": "https://example.com/login",
  "naturalLanguage": "输入用户名test@example.com，然后点击登录按钮"
}

Request (方式2 - 提供快照):
{
  "pageSnapshot": {...},
  "naturalLanguage": "输入用户名test@example.com，然后点击登录按钮"
}

Response:
{
  "success": true,
  "steps": [
    {
      "stepNumber": 1,
      "action": "fill",
      "selector": "#username",
      "selectorType": "id",
      "value": "test@example.com",
      "description": "输入用户名",
      "fallbackSelectors": ["[name='username']"],
      "confidence": 0.98
    }
  ],
  "pageSnapshot": {
    "url": "https://example.com/login",
    "hash": "abc123",
    "elementCount": 15
  },
  "metadata": {
    "generationTime": 3500,
    "modelUsed": "gpt-4"
  }
}
```

---

# 第五部分：实施路线图

### Phase 1: AI服务接入（Week 1-2）
- [ ] 创建数据库表（ai_service_configs, prompt_templates, ai_call_logs）
- [ ] 实现LLMService接口和抽象层
- [ ] 实现OpenAI、Claude、通义千问服务
- [ ] 实现自定义HTTP端点服务
- [ ] 实现配置管理API
- [ ] 实现管理页面UI

### Phase 2: Prompt模板系统（Week 2）
- [ ] 设计初始Prompt模板（自然语言解析、脚本生成）
- [ ] 实现Prompt模板CRUD
- [ ] 实现模板统计功能（成功率、响应时间）

### Phase 3: 自然语言解析（Week 2-3）
- [ ] 实现自然语言解析服务
- [ ] 集成Prompt模板
- [ ] 实现解析API接口
- [ ] 实现调用日志记录
- [ ] 集成到测试用例创建流程

### Phase 4: 智能脚本生成（Week 3-5）
- [ ] 实现页面快照捕获服务
- [ ] 实现选择器生成器
- [ ] 实现基于快照的脚本生成
- [ ] 优化Prompt（Few-Shot Learning）
- [ ] 实现脚本生成API接口

### Phase 5: 监控与测试（Week 5-6）
- [ ] 实现AI调用监控Dashboard
- [ ] 单元测试和集成测试
- [ ] 性能调优
- [ ] 文档完善

---

**文档版本**: v3.2
**最后更新**: 2025-02-06
**维护者**: AI测试团队