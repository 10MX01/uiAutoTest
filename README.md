# AI驱动的Playwright自动化测试演示

这是一个演示项目，展示如何将自然语言测试用例通过AI转换为Playwright脚本并执行。

## 功能特性

- ✅ **AI自动转换**: 将自然语言测试步骤转换为结构化的Playwright脚本
- ✅ **Playwright执行**: 支持Chromium、Firefox、WebKit浏览器
- ✅ **多种操作**: 支持导航、点击、填写、选择、等待、验证等操作
- ✅ **可视化执行**: 支持有头/无头模式切换
- ✅ **详细日志**: 完整的执行日志和结果报告

## 快速开始

### 1. 环境要求

- Java 8或更高版本
- Maven 3.6+
- OpenAI API Key（用于AI转换功能，可选）

### 2. 配置API Key

**Windows (CMD):**
```cmd
set OPENAI_API_KEY=sk-your-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-your-api-key-here"
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY=sk-your-api-key-here
```

### 3. 编译项目

```bash
mvn clean compile
```

### 4. 运行测试

**运行所有测试:**
```bash
mvn test
```

**运行特定测试:**
```bash
# 测试1: AI转换的百度搜索
mvn test -Dtest=AiPlaywrightDemoTest#testBaiduSearchWithAI

# 测试2: 手动定义的百度搜索
mvn test -Dtest=AiPlaywrightDemoTest#testBaiduSearchManual

# 测试3: GitHub登录流程
mvn test -Dtest=AiPlaywrightDemoTest#testGitHubLogin

# 测试4: 复杂场景测试
mvn test -Dtest=AiPlaywrightDemoTest#testComplexScenario
```

## 测试用例示例

### 示例1: 自然语言输入（AI转换）

```java
String naturalLanguageSteps = """
    1. 打开百度首页 https://www.baidu.com
    2. 在搜索框中输入"Playwright Java"
    3. 点击"百度一下"搜索按钮
    4. 等待2秒查看结果
    """;

// AI自动转换为结构化步骤
List<TestStep> steps = aiService.convertToScript(naturalLanguageSteps);
```

### 示例2: 手动定义步骤

```java
List<TestStep> steps = List.of(
    new TestStep(1, "打开百度首页", "navigate", null, "https://www.baidu.com", null, null, false),
    new TestStep(2, "输入搜索关键词", "fill", "#kw", "Playwright", null, null, false),
    new TestStep(3, "点击搜索按钮", "click", "#su", null, null, null, false),
    new TestStep(4, "等待结果加载", "wait", null, null, null, 2000, false)
);
```

## 支持的操作类型

| 操作类型 | 说明 | 必需参数 |
|---------|------|---------|
| `navigate` | 导航到URL | `value` (URL) |
| `click` | 点击元素 | `selector` |
| `fill` | 填写表单 | `selector`, `value` |
| `select` | 下拉选择 | `selector`, `value` |
| `type` | 输入文本(不清除) | `selector`, `value` |
| `hover` | 鼠标悬停 | `selector` |
| `wait` | 等待 | `waitMs` |
| `assert_text` | 验证文本 | `value` |
| `assert_visible` | 验证元素可见 | `selector` |
| `assert_url` | 验证URL | `value` |

## 自定义测试用例

你可以创建自己的测试用例，有两种方式：

### 方式1: 使用AI转换（推荐）

```java
@Test
public void testMyCase() {
    String steps = """
        1. 打开 https://example.com
        2. 点击登录按钮
        3. 输入用户名 "admin"
        4. 输入密码 "password"
        5. 点击提交
        """;

    TestCase testCase = TestCase.builder()
        .name("我的测试")
        .stepsText(steps)
        .build();

    List<TestStep> convertedSteps = aiService.convertToScript(steps);
    testCase.setStepsJson(convertedSteps);

    ScriptExecutionResult result = executor.execute(testCase);
    assertTrue(result.getSuccess());
}
```

### 方式2: 手动定义步骤

```java
@Test
public void testMyManualCase() {
    List<TestStep> steps = List.of(
        new TestStep(1, "步骤描述", "action", "selector", "value", "expected", waitMs, false)
        // ... 更多步骤
    );

    TestCase testCase = TestCase.builder()
        .name("我的测试")
        .stepsJson(steps)
        .build();

    ScriptExecutionResult result = executor.execute(testCase);
    assertTrue(result.getSuccess());
}
```

## 配置选项

编辑 `src/main/resources/application.yml`:

```yaml
# AI配置
ai:
  provider: openai
  api-key: ${OPENAI_API_KEY:}
  model: gpt-4  # 或 gpt-3.5-turbo
  temperature: 0.3
  max-tokens: 2000

# Playwright配置
playwright:
  browser-type: chromium  # chromium, firefox, webkit
  headless: false  # true为无头模式
  timeout: 30000
```

## 项目结构

```
src/
├── main/
│   ├── java/com/uiauto/
│   │   ├── model/          # 数据模型
│   │   │   ├── TestCase.java
│   │   │   ├── TestStep.java
│   │   │   └── ScriptExecutionResult.java
│   │   ├── service/        # AI服务
│   │   │   └── AIService.java
│   │   └── engine/         # 执行引擎
│   │       └── PlaywrightExecutor.java
│   └── resources/
│       ├── application.yml
│       └── logback.xml
└── test/
    └── java/com/uiauto/
        └── AiPlaywrightDemoTest.java  # 测试类
```

## 常见问题

### Q1: 没有API Key怎么办？

如果未配置API Key，系统会使用模拟转换功能，返回一个占位步骤。要使用真正的AI转换，需要配置OpenAI API Key。

### Q2: 如何切换到无头模式？

在测试用例中设置:
```java
.headless(true)
```

或在application.yml中修改默认配置。

### Q3: 支持哪些选择器？

支持所有CSS选择器和XPath，例如：
- ID选择器: `#elementId`
- Class选择器: `.className`
- 属性选择器: `[data-test="submit"]`
- XPath: `xpath=//button[@type='submit']`

### Q4: 测试失败怎么办？

1. 检查日志输出的详细错误信息
2. 确认选择器是否正确
3. 增加等待时间（wait操作）
4. 设置headless=false观察执行过程

## 下一步

- [ ] 添加更多操作类型（scroll、screenshot等）
- [ ] 支持测试用例的导入导出
- [ ] 添加测试报告生成
- [ ] 集成到CI/CD流程
- [ ] 支持多浏览器并行测试

## 许可证

MIT License
