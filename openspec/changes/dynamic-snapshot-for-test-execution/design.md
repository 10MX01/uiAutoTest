## Context

### 当前状态

**现有实现**：
- 测试用例执行时，在脚本生成阶段只获取一次页面快照（第273行：`capturePageSnapshotWithElements`）
- 基于这个单一快照为所有步骤生成选择器（第278行：`generateScriptFromJsonAndSnapshot`）
- 生成的脚本保存到 `test_scripts` 表后，后续执行直接复用

**核心问题**：
当测试用例涉及多个页面时（如从首页导航到管理员页面），基于首页快照生成的选择器无法匹配新页面的元素，导致后续步骤执行失败。

### 受影响的模块

**主要模块**：
- `ai-script-generation/.../TestCaseExecutionServiceImpl.java` - 测试用例执行服务
- `TwoPhaseScriptGenerationService` - 选择器生成服务
- `PlaywrightExecutor` - 浏览器自动化执行引擎

**数据模型**：
- `TestCaseEntity` - 测试用例实体
- `TestScriptEntity` - 脚本存储实体
- `PageSnapshot` - 页面快照模型

### 约束条件

- **向后兼容**：已保存的脚本必须能继续执行，不能破坏现有功能
- **性能要求**：首次执行虽然会增加快照获取次数，但不能显著影响执行速度
- **AI调用成本**：需要合理控制AI调用次数，避免成本过高
- **快照存储**：快照仅用于生成脚本阶段，不需要持久化保存

---

## Goals / Non-Goals

### Goals

1. ✅ **解决跨页面选择器失效问题**：每个测试步骤都基于正确的页面快照生成选择器
2. ✅ **最小化改动**：不破坏现有架构，保持API兼容性
3. ✅ **性能优化**：通过智能判断同页面步骤，减少不必要的快照获取和AI调用
4. ✅ **向后兼容**：已保存的脚本继续有效，无需重新生成
5. ✅ **渐进式执行**：逐步骤生成和执行，确保每个步骤都有正确的选择器

### Non-Goals

❌ **不保存快照数据**：快照仅用于生成脚本阶段，用完即丢弃
❌ **不实现页面预测**：不需要AI预先预测页面变化，而是实时获取快照
❌ **不修改脚本存储格式**：保持现有的 `TestStepWithSelectors` 格式不变
❌ **不支持分布式快照存储**：快照仅在内存中临时使用，不需要跨服务共享

---

## Decisions

### 决策1：采用渐进式执行模式（而非一次性生成）

**选择**：逐步骤生成选择器并执行，而不是预先获取所有快照后一次性生成

**理由**：
- ✅ 避免了"鸡生蛋"问题：不需要先执行才能获取快照
- ✅ 实现简单清晰：流程线性，易于理解和维护
- ✅ 自适应能力强：页面变化后立即获取新快照，无需复杂预测

**替代方案（未采用）**：
- **Dry-run模式**：先快速执行一遍收集所有快照，再生成选择器
  - ❌ 仍然需要选择器才能执行，无法解决根本问题
- **AI预分析模式**：AI预测页面URL，自动导航收集快照
  - ❌ AI预测不准确时难以处理，实现复杂度高
  - ❌ URL变化时无法适应

### 决策2：只在跳转操作后获取新快照

**选择**：仅在 `CLICK`、`NAVIGATE`、`SUBMIT` 操作后获取新快照，其他操作复用当前快照

**理由**：
- ✅ 显著减少快照获取次数（从N次降低到~2-3次）
- ✅ 符合页面变化的实际规律：只有跳转类操作才会改变页面
- ✅ 性能优化：减少浏览器操作和AI调用

**跳转操作类型**：
```java
CLICK    // 点击链接/按钮，可能导航
NAVIGATE // 显式导航（如 open(url)）
SUBMIT   // 表单提交，通常导航
```

**非跳转操作类型**（复用快照）：
```java
FILL   // 填写表单，不跳转
SELECT // 选择下拉框，不跳转
WAIT   // 等待操作，不跳转
VERIFY // 验证操作，不跳转
```

### 决策3：简单渐进式执行（无批量优化）

**选择**：逐个步骤处理，每个步骤都独立获取快照、生成选择器、执行

**理由**：
- ✅ 实现简单直观，易于理解和维护
- ✅ 无需复杂的AI批量判断逻辑
- ✅ 每个步骤都是独立的，便于调试和错误定位
- ✅ 功能完整，满足所有需求

**不采用批量优化的原因**：
- 批量判断增加实现复杂度
- 单步处理已经足够，性能可接受
- 后续可以根据实际使用情况再优化

### 决策4：快照不持久化，仅在执行过程中临时使用

**选择**：快照仅在首次执行（脚本生成阶段）的内存中使用，不保存到数据库

**理由**：
- ✅ 简化实现：不需要设计快照存储结构
- ✅ 节省存储：快照包含大量DOM数据，持久化成本高
- ✅ 后续执行不需要：脚本保存后直接执行，无需快照
- ✅ 避免过期问题：页面结构变化时，旧快照反而会误导

### 决策5：保持现有脚本格式不变

**选择**：生成的脚本格式保持 `List<TestStepWithSelectors>`，不增加快照元数据

**理由**：
- ✅ 向后兼容：旧版本生成的脚本继续有效
- ✅ 简化迁移：无需修改脚本存储和解析逻辑
- ✅ 减少冗余：快照元数据在执行阶段无用

**现有脚本格式**：
```json
[
  {
    "stepDescription": "点击管理员管理",
    "action": "click",
    "selector": "#main-menu > li:nth-child(2) > a"
  }
]
```

### 决策6：脚本复用逻辑不变

**选择**：保留现有的脚本查询和复用逻辑（第234-244行）

**理由**：
- ✅ 已有脚本直接执行，避免重复调用AI
- ✅ 首次执行后才生成新脚本
- ✅ 保持用户体验一致性

**现有逻辑**：
```java
TestScriptResponse existingScript = testScriptService.getEnabledByTestCaseId(testCaseId);
if (existingScript != null) {
    // 复用已有脚本
    scriptWithSelectors = parse(existingScript.getScriptContent());
} else {
    // 生成新脚本（本次改进的核心）
}
```

---

## 实现方案设计

### 核心流程

```
首次执行流程：
┌─────────────────────────────────────┐
│ 1. 导航到初始URL                     │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ 2. 获取初始页面快照                  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ 3. 循环处理每个步骤                  │
│   a. 用当前快照生成当前步骤的选择器   │
│   b. 执行该步骤                      │
│   c. 如果是跳转操作，获取新快照       │
│   d. 否则，复用当前快照              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ 4. 保存完整脚本                      │
└─────────────────────────────────────┘
```

### 代码结构改动

**主要修改**：`TestCaseExecutionServiceImpl.executeTestCaseRecursive()`

```java
// 新增方法：判断是否需要获取新快照
private boolean shouldCaptureSnapshot(TestStep step) {
    return step.getAction() == TestStepAction.CLICK
        || step.getAction() == TestStepAction.NAVIGATE
        || step.getAction() == TestStepAction.SUBMIT;
}

// 修改后的执行逻辑
private ExecutionResult executeTestCaseRecursive(...) {
    // ... 前置逻辑不变 ...

    // 新增：首次执行时的渐进式生成
    if (existingScript == null) {
        // 导航到URL
        if (!hasNavigated.get()) {
            playwrightExecutor.navigate(sessionId, url);
            hasNavigated.set(true);
        }

        // 获取初始快照
        PageSnapshot currentSnapshot = playwrightExecutor.capturePageSnapshotWithElements(sessionId);

        // 渐进式生成+执行
        List<TestStepWithSelectors> completeScript = new ArrayList<>();

        for (int i = 0; i < steps.size(); i++) {
            TestStep step = steps.get(i);

            // 用当前快照生成选择器
            TestStepWithSelectors stepWithSelector =
                twoPhaseService.generateSingleStepSelector(step, currentSnapshot);

            // 执行步骤
            executor.executeStep(sessionId, stepWithSelector);
            completeScript.add(stepWithSelector);

            // 关键：只在跳转操作后获取新快照
            if (shouldCaptureSnapshot(step)) {
                currentSnapshot = playwrightExecutor.capturePageSnapshotWithElements(sessionId);
                log.info("步骤{}是{}操作，获取新快照", i, step.getAction());
            }
        }

        // 保存完整脚本
        saveGeneratedScript(testCase, completeScript, executedBy);

        // 记录执行结果
        return buildExecutionResult(completeScript);
    }

    // ... 已有脚本执行逻辑不变 ...
}
```

### 新增/修改的接口

**TwoPhaseScriptGenerationService** 新增方法：

```java
/**
 * 为单个步骤生成选择器
 * @param step 测试步骤
 * @param snapshot 当前页面快照
 * @return 包含选择器的步骤
 */
TestStepWithSelectors generateSingleStepSelector(TestStep step, PageSnapshot snapshot);
```

**PlaywrightExecutor** 新增方法：

```java
/**
 * 执行单个步骤（用于渐进式执行）
 * @param sessionId 会话ID
 * @param step 包含选择器的步骤
 */
void executeStep(String sessionId, TestStepWithSelectors step);
```

---

## 预期效果

### 功能改进

- ✅ **跨页面用例成功率显著提升**：从当前的失败状态提升到正常执行
- ✅ **向后兼容**：已保存的脚本继续有效
- ✅ **实现简单**：无需复杂的状态管理或缓存机制

### 性能影响

- 首次执行：需要多次获取快照和调用AI，时间增加但可接受
- 后续执行：直接使用保存的脚本，无性能影响
- 内存使用：快照仅用临时变量，执行完即释放

---

## 附录

### 示例：完整执行流程

```java
// 测试用例：创建管理员
List<TestStep> steps = [
    TestStep(action=CLICK, target="管理员管理"),
    TestStep(action=CLICK, target="创建管理员"),
    TestStep(action=FILL, target="用户名", value="admin001"),
    TestStep(action=SELECT, target="角色", value="超级管理员"),
    TestStep(action=CLICK, target="保存"),
    TestStep(action=VERIFY, target="成功消息")
];

// 执行过程
navigate(url);                    // 1. 导航
snapshot1 = capture();            // 2. 初始快照（首页）

// 步骤1：点击管理员管理
step1 = generateSelector(steps[0], snapshot1);
execute(step1);                   // 执行后页面跳转
snapshot2 = capture();            // 3. 新快照（管理员页面）

// 步骤2：点击创建管理员
step2 = generateSelector(steps[1], snapshot2);
execute(step2);                   // 执行后页面跳转
snapshot3 = capture();            // 4. 新快照（创建页面）

// 步骤3：填写用户名
step3 = generateSelector(steps[2], snapshot3);  // 复用snapshot3
execute(step3);                   // 不跳转，不获取快照

// 步骤4：选择角色
step4 = generateSelector(steps[3], snapshot3);  // 复用snapshot3
execute(step4);                   // 不跳转，不获取快照

// 步骤5：点击保存
step5 = generateSelector(steps[4], snapshot3);
execute(step5);                   // 执行后页面跳转
snapshot4 = capture();            // 5. 新快照（结果页）

// 步骤6：验证成功消息
step6 = generateSelector(steps[5], snapshot4);
execute(step6);

// 保存脚本
save([step1, step2, step3, step4, step5, step6]);
// 快照丢弃，不保存
```

### 数据流图

```
┌─────────────┐
│ 测试用例    │
│ steps.json  │
└──────┬──────┘
       ↓
┌─────────────────────────────────┐
│ TestCaseExecutionServiceImpl   │
│ - 解析步骤                      │
│ - 判断是否有脚本                │
└──────┬──────────────────────────┘
       ↓ 无脚本
┌─────────────────────────────────┐
│ 渐进式生成模块                  │
│ - 获取快照                      │
│ - 调用AI生成选择器              │
│ - 执行步骤                      │
│ - 检测跳转                      │
└──────┬──────────────────────────┘
       ↓
┌─────────────┐      ┌──────────────┐
│ AI服务      │      │ Playwright   │
│ - 生成选择器│      │ - 执行操作   │
│ - 分析步骤  │      │ - 捕获快照   │
└─────────────┘      └──────────────┘
       ↓
┌─────────────┐
│ 保存脚本    │
│ test_scripts│
└─────────────┘
```
