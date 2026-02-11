-- =====================================================
-- 脚本管理测试数据
-- 用于前端脚本管理页面展示
-- =====================================================

USE uiaut_test;

-- =====================================================
-- 插入测试脚本数据
-- =====================================================
INSERT INTO test_scripts (
    script_name,
    script_description,
    script_content,
    language,
    generation_method,
    enabled,
    ai_generation_status,
    ai_retry_count,
    ai_model_used,
    ai_generation_time,
    test_case_id,
    category,
    execution_count,
    last_execution_time,
    last_execution_result,
    created_by,
    updated_by,
    created_time,
    updated_time
) VALUES
-- 1. 用户登录测试脚本（AI生成，已启用，执行成功）
(
    '业务操作员登录系统功能测试脚本',
    'AI自动生成的登录测试脚本，验证用户名密码登录功能',
    'import { test, expect } from ''@playwright/test'';\n\ntest(''业务操作员登录系统'', async ({ page }) => {\n  // 1. 打开登录页面\n  await page.goto(''http://localhost:8080/api/login'');\n\n  // 2. 输入用户名\n  await page.fill(''#username'', ''operator001'');\n\n  // 3. 输入密码\n  await page.fill(''#password'', ''Test@123456'');\n\n  // 4. 点击登录按钮\n  await page.click(''#login-btn'');\n\n  // 5. 验证登录成功\n  await expect(page.locator(''.user-info'')).toContainText(''operator001'');\n\n  // 6. 验证跳转到首页\n  await expect(page).toHaveURL(/.*home|index/);\n});',
    'typescript',
    'AI_GENERATED',
    TRUE,
    'SUCCESS',
    0,
    'gpt-4',
    '2026-01-19 18:00:00',
    1,
    '登录模块',
    15,
    '2026-01-19 18:07:27',
    'SUCCESS',
    1,
    1,
    '2026-01-19 17:50:00',
    '2026-01-19 18:07:27'
),

-- 2. 注册用户信息填写测试脚本（AI生成，已启用）
(
    '注册用户信息填写与校验脚本',
    '验证用户注册时信息填写的完整性和校验规则',
    'import { test, expect } from ''@playwright/test'';\n\ntest(''注册用户信息填写'', async ({ page }) => {\n  await page.goto(''http://localhost:8080/api/register'');\n\n  // 填写用户名\n  await page.fill(''#username'', ''testuser001'');\n\n  // 填写真实姓名\n  await page.fill(''#realname'', ''张三'');\n\n  // 填写身份证号\n  await page.fill(''#idcard'', ''110101199001011234'');\n\n  // 填写手机号\n  await page.fill(''#mobile'', ''13800138000'');\n\n  // 填写邮箱\n  await page.fill(''#email'', ''test@example.com'');\n\n  // 提交注册\n  await page.click(''#submit-btn'');\n\n  // 验证注册成功\n  await expect(page.locator(''.success-msg'')).toContainText(''注册成功'');\n});',
    'typescript',
    'AI_GENERATED',
    TRUE,
    'SUCCESS',
    0,
    'gpt-4',
    '2026-01-19 17:55:00',
    2,
    '注册模块',
    8,
    '2026-01-19 18:08:49',
    'SUCCESS',
    1,
    1,
    '2026-01-19 17:45:00',
    '2026-01-19 18:08:49'
),

-- 3. 姓名为空校验脚本（AI生成，已启用）
(
    '注册用户姓名为空校验脚本',
    '验证用户注册时姓名为空时的校验提示',
    'import { test, expect } from ''@playwright/test'';\n\ntest(''姓名为空校验'', async ({ page }) => {\n  await page.goto(''http://localhost:8080/api/register'');\n  await page.fill(''#username'', ''testuser002'');\n  // 姓名留空\n  await page.fill(''#idcard'', ''110101199001011234'');\n  await page.click(''#submit-btn'');\n\n  await expect(page.locator(''.error-msg'')).toContainText(''姓名不能为空'');\n});',
    'typescript',
    'AI_GENERATED',
    TRUE,
    'SUCCESS',
    0,
    'gpt-4',
    '2026-01-19 17:52:00',
    3,
    '注册模块',
    5,
    '2026-01-19 18:07:22',
    'SUCCESS',
    1,
    1,
    '2026-01-19 17:40:00',
    '2026-01-19 18:07:22'
),

-- 4. 密码加密功能验证脚本（JavaScript，AI生成，已禁用）
(
    '密码加密功能验证脚本',
    '验证用户密码在存储时是否正确加密',
    'const { test, expect } = require(''@playwright/test'');\n\ntest(''密码加密验证'', async ({ page }) => {\n  // 查询用户密码\n  const password = await page.evaluate(() => {\n    return fetch(''/api/users/password?username=testuser'')\n      .then(res => res.json());\n  });\n\n  // 验证密码已加密\n  expect(password).not.toBe(''123456'');\n  expect(password.length).toBeGreaterThan(50);\n});',
    'javascript',
    'AI_GENERATED',
    FALSE,
    'SUCCESS',
    0,
    'gpt-3.5-turbo',
    '2026-01-20 08:50:00',
    6,
    '安全模块',
    3,
    '2026-01-20 09:05:00',
    'SUCCESS',
    1,
    1,
    '2026-01-20 08:45:00',
    '2026-01-20 09:05:00'
),

-- 5. 密码解密接口性能测试脚本（TypeScript，AI生成失败，已禁用）
(
    '密码解密接口性能测试脚本',
    '验证密码解密接口的响应时间',
    'import { test, expect } from ''@playwright/test'';\n\ntest(''解密接口性能测试'', async ({ page }) => {\n  const startTime = Date.now();\n\n  // 执行1000次解密操作\n  for (let i = 0; i < 1000; i++) {\n    await page.evaluate(async () => {\n      const response = await fetch(''/api/decrypt'', {\n        method: ''POST'',\n        body: JSON.stringify({ data: ''test'' })\n      });\n      return response.json();\n    });\n  }\n\n  const endTime = Date.now();\n  const avgTime = (endTime - startTime) / 1000;\n\n  // 验证平均响应时间小于100ms\n  expect(avgTime).toBeLessThan(100);\n\n  console.log(`平均响应时间: ${avgTime}ms`);\n});',
    'typescript',
    'AI_GENERATED',
    FALSE,
    'FAILED',
    3,
    'gpt-4',
    NULL,
    7,
    '安全模块',
    0,
    NULL,
    NULL,
    1,
    1,
    '2026-01-20 09:00:00',
    '2026-01-20 09:00:00'
),

-- 6. 国密算法加密验证脚本（Excel导入，已启用）
(
    '国密SM4算法加密验证脚本',
    '验证系统使用国密SM4算法进行数据加密',
    'import { test, expect } from ''@playwright/test'';\n\ntest(''SM4加密验证'', async ({ page }) => {\n  const testData = ''测试数据'';\n\n  // 调用SM4加密接口\n  const encrypted = await page.evaluate(async (data) => {\n    const response = await fetch(''/api/sm4/encrypt'', {\n      method: ''POST'',\n      headers: { ''Content-Type'': ''application/json'' },\n      body: JSON.stringify({ data })\n    });\n    return response.json();\n  }, testData);\n\n  // 验证返回加密数据\n  expect(encrypted.encryptedData).toBeTruthy();\n\n  // 调用SM4解密接口\n  const decrypted = await page.evaluate(async (encryptedData) => {\n    const response = await fetch(''/api/sm4/decrypt'', {\n      method: ''POST'',\n      headers: { ''Content-Type'': ''application/json'' },\n      body: JSON.stringify({ data: encryptedData })\n    });\n    return response.json();\n  }, encrypted.encryptedData);\n\n  // 验证解密结果与原始数据一致\n  expect(decrypted.decryptedData).toBe(testData);\n});',
    'typescript',
    'EXCEL_IMPORT',
    TRUE,
    'SUCCESS',
    0,
    NULL,
    NULL,
    8,
    '安全模块',
    12,
    '2026-01-20 11:30:00',
    'SUCCESS',
    1,
    1,
    '2026-01-20 10:00:00',
    '2026-01-20 11:30:00'
);

-- =====================================================
-- 验证数据
-- =====================================================
SELECT '✅ 脚本数据插入完成！' AS message;
SELECT COUNT(*) AS '脚本数量' FROM test_scripts;
SELECT
    enabled AS '启用状态',
    ai_generation_status AS 'AI状态',
    language AS '语言',
    COUNT(*) AS '数量'
FROM test_scripts
GROUP BY enabled, ai_generation_status, language
ORDER BY enabled DESC, ai_generation_status;
