## 1. 核心功能实现

- [x] 1.1 在 `TestStep` 中确认或新增 `action` 字段（CLICK, NAVIGATE, SUBMIT, FILL, SELECT, WAIT, VERIFY）
- [x] 1.2 在 `TestCaseExecutionServiceImpl` 中新增 `shouldCaptureSnapshotBefore()` 方法，判断步骤操作类型（CLICK/NAVIGATE/SUBMIT 返回 true）
- [x] 1.3 在 `TwoPhaseScriptGenerationService` 中新增 `generateSingleStepSelector()` 方法，为单个步骤基于快照生成选择器
- [x] 1.4 修改 `executeTestCaseRecursive()` 方法，实现渐进式执行逻辑：
  - 导航到URL
  - 循环每个步骤：
    - 如果需要，获取当前页面快照
    - 调用AI生成该步骤的选择器
    - 执行该步骤
    - 将生成的脚本缓存到内存
  - 循环结束后，将所有步骤的脚本合并并保存到数据库

## 2. 脚本缓存和合并

- [x] 2.1 在内存中使用 `List<TestStepWithSelectors>` 缓存每个步骤生成的脚本
- [x] 2.2 实现脚本合并逻辑，将所有步骤的脚本整合成一个完整的脚本
- [x] 2.3 修改 `saveGeneratedScript()` 方法，支持保存合并后的完整脚本
- [x] 2.4 确保脚本格式与现有格式兼容（`List<TestStepWithSelectors>`）

## 3. AI服务集成

- [x] 3.1 修改AI调用，支持单步选择器生成（输入：步骤+快照，输出：带选择器的步骤）
- [x] 3.2 编写AI Prompt模板，用于单步选择器生成（复用现有模板）
- [x] 3.3 添加AI调用失败的基本日志记录（已在generateSingleStepSelector中实现）

## 4. 错误处理

- [x] 4.1 添加快照获取失败的异常处理（记录日志并抛出异常）- 已在executeTestCaseRecursive中实现
- [x] 4.2 添加AI调用失败的异常处理（记录日志并抛出异常）- 已在generateSingleStepSelector中实现
- [x] 4.3 添加步骤执行失败的异常处理（记录日志并抛出异常）- 已在executeTestCaseRecursive中实现
- [x] 4.4 添加基本的try-catch块，确保异常时能够正确清理资源 - 已在现有代码中实现

## 5. 日志记录

- [x] 5.1 添加步骤执行开始/结束的日志 - 已在executeTestCaseRecursive中实现
- [x] 5.2 添加快照获取的日志（步骤索引、URL）- 已在executeTestCaseRecursive中实现
- [x] 5.3 添加AI调用的日志（步骤索引、成功/失败）- 已在generateSingleStepSelector中实现
- [x] 5.4 添加脚本保存的日志（步骤数量、保存状态）- 已在saveGeneratedScript中实现

## 6. 测试

- [ ] 6.1 编写单页面用例的手动测试（验证基本流程）- 需要手动测试
- [ ] 6.2 编写跨页面用例的手动测试（验证快照切换）- 需要手动测试
- [ ] 6.3 验证已保存的脚本能够正常执行（向后兼容性）- 需要手动测试
- [ ] 6.4 验证生成的脚本格式正确（可被后续执行复用）- 需要手动测试

## 7. 验收标准

- [ ] 7.1 跨页面测试用例能够成功执行 - 需要实际测试验证
- [ ] 7.2 生成的脚本包含所有步骤的选择器 - 需要实际测试验证
- [ ] 7.3 生成的脚本能够被后续执行复用（无需重新生成）- 需要实际测试验证
- [ ] 7.4 已保存脚本执行成功率保持 >95% - 需要实际测试验证