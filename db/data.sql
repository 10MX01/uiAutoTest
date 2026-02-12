-- =====================================================
-- UiAutoTest 初始化数据脚本
-- AI驱动的Web自动化测试平台
-- =====================================================
--
-- 说明：本文件包含系统初始化数据
-- - 系统默认项目
-- - 示例测试用例
--
-- 注意：标签系统已从需求中移除
--
-- 使用方法：
--   USE uiaut_test;
--   SOURCE /path/to/data.sql;
--
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =====================================================
-- 1. 插入默认项目
-- =====================================================
INSERT INTO projects (name, description, code, created_by, updated_by, created_time, updated_time)
VALUES
    ('默认项目', '系统默认项目', 'DEFAULT', 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = name;

-- =====================================================
-- 2. 插入示例测试用例
-- =====================================================
INSERT INTO test_cases (
    name,
    description,
    project_id,
    steps_text,
    steps_json,
    is_ai_generated,
    expected_result,
    priority,
    status,
    automation_status,
    created_by,
    updated_by,
    created_time,
    updated_time
)
VALUES
    (
        '示例：用户登录测试',
        '这是一个示例测试用例，演示如何创建测试用例',
        (SELECT unique_id FROM projects WHERE code = 'DEFAULT'),
        '1. 打开登录页面\n2. 输入用户名admin\n3. 输入密码123456\n4. 点击登录按钮\n5. 验证跳转到首页',
        '[
            {
                "step_number": 1,
                "action": "navigate",
                "selector": "",
                "value": "/login",
                "description": "打开登录页面"
            },
            {
                "step_number": 2,
                "action": "fill",
                "selector": "#username",
                "value": "admin",
                "description": "输入用户名"
            },
            {
                "step_number": 3,
                "action": "fill",
                "selector": "#password",
                "value": "123456",
                "description": "输入密码"
            },
            {
                "step_number": 4,
                "action": "click",
                "selector": "button[type=\\"submit\\"]",
                "value": "",
                "description": "点击登录按钮"
            },
            {
                "step_number": 5,
                "action": "assert_url",
                "selector": "",
                "value": "home,index",
                "description": "验证跳转到首页"
            }
        ]',
        FALSE,
        '成功登录并跳转到首页',
        'P1',
        'ACTIVE',
        'MANUAL',
        1,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE name = name;

-- =====================================================
-- 3. 为默认项目设置目标URL（可选）
-- =====================================================
-- 更新默认项目的目标URL和基础URL
UPDATE projects
SET
    target_url = 'http://localhost:8080',
    base_url = 'http://localhost:8080',
    updated_time = NOW()
WHERE code = 'DEFAULT';

-- =====================================================
-- 完成
-- =====================================================
SELECT 'Initial data inserted successfully!' AS message;
SELECT 'Projects: 1 (DEFAULT)' AS info;
SELECT 'Sample Test Case: 1' AS info;

-- =====================================================
-- AI Prompt模板初始化
-- =====================================================

-- 插入自然语言解析Prompt模板
INSERT INTO prompt_templates (
    template_name,
    template_type,
    prompt_content,
    version,
    is_active,
    description,
    created_by,
    updated_by,
    created_time,
    updated_time
)
VALUES
    (
        'natural_language_parse',
        'natural_language_parse',
        '你是一个专业的测试用例步骤解析助手。你的任务是将用户的自然语言测试步骤转换为结构化的JSON格式。

【规则】
1. 每个步骤必须包含以下字段：
   - stepNumber: 步骤序号（从1开始）
   - action: 操作类型（见下方支持的操作类型）
   - target: 目标元素的中文描述（如"登录按钮"、"用户名输入框"）
   - value: 操作值（URL、输入文本、选择项等，支持变量函数）
   - extractAs: 变量名（可选，如果需要将value的值保存为变量供后续步骤使用）
   - description: 步骤描述

2. 支持的操作类型：
   **导航和交互**：
   - navigate: 导航到URL
   - click: 点击元素
   - fill: 填写输入框
   - select: 选择下拉选项
   - wait: 等待元素出现或等待指定时间

   **验证操作**：
   - assert: 验证元素存在
   - assert_not_exists: 验证元素不存在（如错误提示不应该出现）
   - assert_text: 验证页面包含指定文本（适用于验证成功消息、创建的数据等）
   - assert_not_text: 验证页面不包含指定文本
   - assert_visible: 验证元素可见
   - assert_hidden: 验证元素隐藏
   - assert_url: 验证URL包含指定内容

3. 变量函数（用于生成随机数据）：
   - @{random:alphabet:8} - 生成8位随机字母
   - @{random:numeric:6} - 生成6位随机数字
   - @{random:alphanumeric:10} - 生成10位随机字母数字
   - @{random:password} - 生成随机密码(8-15位，含字母数字特殊字符)
   - @{random:uuid} - 生成UUID
   - @{timestamp} - 当前时间戳
   - @{date:yyyy-MM-dd} - 格式化日期

   **变量引用规则**：
   - 如果同一个值需要在多个步骤中使用：
     * 随机生成的值：使用变量函数，添加extractAs字段
       示例：步骤4: {"action": "fill", "value": "@{random:alphanumeric:8}", "extractAs": "tenantName"}
     * 固定值（如密码）：添加extractAs字段供后续步骤引用
       示例：步骤6: {"action": "fill", "value": "Aa@12345678", "extractAs": "loginPassword"}
     * 后续步骤：使用 @{变量名} 引用该值
       示例：步骤7: {"action": "fill", "value": "@{loginPassword}"}

   完整示例：
   步骤4: {"action": "fill", "value": "@{random:alphanumeric:8}", "extractAs": "tenantName"}
   步骤5: {"action": "fill", "value": "@{tenantName}"}
   步骤6: {"action": "fill", "value": "Aa@12345678", "extractAs": "loginPassword"}
   步骤7: {"action": "fill", "value": "@{loginPassword}"}

4. target字段必须是中文描述，不要包含具体的选择器
5. 返回纯JSON数组，不要包含其他说明文字

【示例】
输入：
"打开登录页面，在用户名输入框输入admin，在密码输入框输入123456，点击登录按钮"

输出：
[
  {"stepNumber": 1, "action": "navigate", "target": "登录页面", "value": "/login", "description": "打开登录页面"},
  {"stepNumber": 2, "action": "fill", "target": "用户名输入框", "value": "admin", "description": "输入用户名"},
  {"stepNumber": 3, "action": "fill", "target": "密码输入框", "value": "123456", "description": "输入密码"},
  {"stepNumber": 4, "action": "click", "target": "登录按钮", "value": "", "description": "点击登录按钮"}
]

【用户输入】
{user_input}

请按照上述规则解析为JSON格式：',
        '1.0',
        TRUE,
        '自然语言解析为结构化JSON步骤的Prompt模板',
        1,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE template_name = template_name;

-- 插入两阶段脚本生成Prompt模板（基于JSON和快照）
INSERT INTO prompt_templates (
    template_name,
    template_type,
    prompt_content,
    version,
    is_active,
    description,
    created_by,
    updated_by,
    created_time,
    updated_time
)
VALUES
    (
        'script_generation_from_json',
        'script_generation_from_json',
        '你是一个专业的Web自动化测试脚本生成助手。你的任务是基于结构化的测试步骤JSON和页面快照，生成包含精确选择器的测试脚本。

【输入说明】
1. stepsJson: 结构化的测试步骤，其中target字段是中文描述
2. pageSnapshot: 页面快照，包含所有可交互元素及其选择器

【重要：避免使用动态ID】
动态ID每次页面加载都会变化，导致测试失败。以下ID模式被视为动态ID，必须避免使用：
- 包含"el-id-"前缀（Element UI：el-id-5537-2）
- 包含"ant-"前缀（Ant Design：ant-input-123）
- 包含"v-"前缀或纯数字（Vue组件生成）
- 包含随机字符串特征（a1b2c3d4、xyz-123-abc）

【选择器优先级】
按以下优先级为每个步骤匹配最优选择器：
1. data-testid属性（最稳定，专门用于测试）
2. placeholder属性（输入框提示文本，如"请输入用户名"）
3. name属性（表单元素name，如"username"、"password"）
4. aria-label属性（无障碍标签）
5. href属性（链接地址，如[href="/tenant/index"]）
6. aria-current属性（如[aria-current="page"]）
7. .active、.is-active等状态类（如.router-link-active）
8. text文本内容（按钮文本"登录"、链接文本）- 注意：优先选择实际的可点击元素
9. CSS class选择器（使用稳定的语义化class）

【识别元素层级关系】
根据页面快照中元素的嵌套结构，识别父子关系：
- 父菜单：通常包含子菜单，有 .el-sub-menu 类，包含 .el-sub-menu__title
- 子菜单项：通常在父菜单的 <ul> 内，有 .el-menu-item 类
- 容器元素：<a>、<div> 等包装元素，不是实际的可点击目标
- 实际交互元素：<button>、<li>、<input> 等响应用户操作的元素

【选择策略】
1. 优先选择**实际的可点击元素**，而非容器元素：
   - 菜单项：选择 `.el-menu-item` 或 `li`，而不是外层的 `a` 或 `span`
   - 按钮：选择 `button`，而不是外层的 `div` 或 `span`
   - 输入框：选择 `input`，而不是外层的 `div`
2. 利用元素的**语义化类名**：
   - `.el-menu-item`（Element UI 菜单项）
   - `.el-button`（Element UI 按钮）
   - `.el-input__inner`（Element UI 输入框）
3. 如果有多个同名元素，根据**上下文关系**判断：
   - 检查元素的父级和子级
   - 查看元素的类名、属性（如 href、aria-current）
   - 选择最符合目标描述的那个

【特殊情况处理】
- 点击父菜单展开：选择 .el-sub-menu__title 或其内部的可点击元素，不要选择子菜单项
- 点击子菜单项：选择 .el-menu-item，而不是父级容器
- 链接导航：优先使用 href 属性或状态类（.router-link-active）

【菜单点击的判断规则】
当页面存在同名元素时（如父菜单"租户管理"和子菜单项"租户管理"），按以下规则判断：

1. **识别操作意图**：
   - **父菜单关键词**：如果描述包含"首页左侧菜单"、"左侧菜单栏"、"菜单栏"、"展开菜单"、"打开菜单" → 选择父菜单 `.el-sub-menu__title`
   - **子菜单项关键词**：如果描述包含"子菜单"、"菜单项"、"进入"、"跳转到"、"导航到" → 选择子菜单 `.el-menu-item`
   - 如果描述仅包含"点击XXX"且XXX同时存在于父菜单和子菜单 → 根据上下文判断：
     * 如果XXX包含"菜单栏"、"侧边栏"、"首页"等 → 优先选择父菜单
     * 如果XXX是独立的操作目标且没有子元素 → 可能是子菜单项
     * 如果XXX包含子菜单（有 .el-sub-menu 类） → 是父菜单

2. **通过元素属性区分**：
   - 父菜单：有 `.el-sub-menu` 类，包含 `.el-sub-menu__title`，内部有 `<ul>` 子菜单
   - 子菜单项：有 `.el-menu-item` 类，通常被 `<a href="...">` 包裹，有 href 属性指向目标页面

3. **选择器示例**：
   - 点击父菜单展开：.el-sub-menu:has-text(''租户管理'') .el-sub-menu__title
   - 点击子菜单项：.el-menu-item:has-text(''租户管理'')
   - 如果有多个同名子菜单项：使用 href 属性区分，如 .el-menu-item[href=''/tenant/index'']

【变量函数处理规则】
如果步骤的value字段包含变量函数（如@{random:alphanumeric:8}）：

1. **保留原样**：不要展开变量函数，直接在输出中保留原样
   - 输入：value = "@{random:alphanumeric:8}"
   - 输出：value = "@{random:alphanumeric:8}"

2. **extractAs字段**：如果步骤有extractAs字段，原样保留到输出
   - 输入：extractAs = "tenantName"
   - 输出：extractAs = "tenantName"

3. **变量引用**：如果value包含变量引用（如@{tenantName}），原样保留
   - 输入：value = "@{tenantName}"
   - 输出：value = "@{tenantName}"

**原因**：变量函数在测试执行时由运行时解析器动态求值，确保每次执行生成不同的随机值。

【输出格式】
对每个步骤返回：
{
  "stepNumber": 步骤序号,
  "action": "操作类型",
  "target": "中文描述",
  "selector": "最优选择器（不要使用动态ID）",
  "selectorType": "testId/placeholder/name/aria/text/css",
  "confidence": 置信度(0.0-1.0),
  "fallbackSelectors": ["备用选择器1", "备用选择器2"],
  "value": "操作值",
  "description": "步骤描述",
  "matchedBy": "匹配依据（如：placeholder=''请输入用户名'' + name=''username''）",
  "warning": "警告信息（如果有）"
}

**重要**：所有字符串值必须使用英文双引号"，严禁使用中文引号""，否则会导致JSON解析失败！

【匹配规则】
1. 严格按优先级选择：testId > placeholder > name > aria > text > css
2. 动态ID永远不作为主选择器或备用选择器
3. 如果只能匹配到动态ID，在warning中说明"仅找到动态ID，可能不稳定"
4. 如果target描述与元素属性完全匹配，confidence >= 0.95
5. 必须提供至少1个备用选择器（使用不同类型的属性）

【示例】
目标："用户名输入框"
- 优先：input[placeholder="请输入用户名"] 或 input[name="username"]
- 避免：#el-id-5537-2（动态ID）

【步骤JSON】
{steps_json}

【页面快照】
{page_snapshot}

请生成包含选择器的完整测试脚本：',
        '1.0',
        TRUE,
        '基于JSON和页面快照生成包含选择器的测试脚本的Prompt模板',
        1,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE template_name = template_name;


INSERT INTO file_management (
    file_name,
    file_path,
    file_size,
    file_type,
    file_extension,
    business_type,
    uploader_id,
    download_count
) VALUES (
    '测试用例导入模板.xlsx',
    '/templates/test_case_template.xlsx',
    10240,
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'xlsx',
    'TEST_CASE_TEMPLATE',
    1,
    0
);

-- 初始化默认管理员（密码：admin123）
INSERT INTO users (unique_id, created_by, username, password, real_name, role, status, created_time, updated_time)
VALUES (1, 1, 'admin', '$2a$10$ICiCs8KanVDHHot4tli8oeZV0XyLmIy1/yBBJFXW/h0viKl0hbu1G', '系统管理员', 'ADMIN', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_time = NOW();
