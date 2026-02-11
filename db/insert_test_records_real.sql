-- =====================================================
-- 测试记录数据插入脚本（基于真实测试用例）
-- 说明：基于现有的test_cases数据（ID 23-28）生成测试执行记录
-- =====================================================

USE uiaut_test;

-- =====================================================
-- 第一步：查看现有测试用例
-- =====================================================
SELECT '========== 现有测试用例 ==========' AS info;
SELECT unique_id, case_number, name, project_id FROM test_cases WHERE unique_id IN (23,24,25,26,27,28);

-- =====================================================
-- 第二步：临时禁用外键检查
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 第三步：插入测试执行记录
-- =====================================================
INSERT INTO test_case_executions (unique_id, test_case_id, execution_url, status, duration, generated_script, steps_result, screenshots, error_message, executed_by, created_time, updated_time)
VALUES
-- =====================================================
-- 用例ID 23: 系统管理员登录系统功能验证 (project_id=1)
-- =====================================================
-- 成功执行
(1, 23, 'https://10.0.108.6:9028/', 'SUCCESS', 5240,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问登录页面"},{"action":"type","target":"username","selector":"#username","value":"xt","description":"输入管理员账号"},{"action":"type","target":"password","selector":"#password","value":"Aa12345678","description":"输入密码"},{"action":"click","target":"loginButton","selector":"button[type=\'submit\']","description":"点击登录按钮"}]',
'[{"stepNumber":1,"description":"访问登录页面","status":"SUCCESS","duration":1200,"screenshot":null},{"stepNumber":2,"description":"输入管理员账号","status":"SUCCESS","duration":350,"screenshot":null},{"stepNumber":3,"description":"输入密码","status":"SUCCESS","duration":280,"screenshot":null},{"stepNumber":4,"description":"点击登录按钮","status":"SUCCESS","duration":3410,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
null, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),

-- 失败执行（密码错误）
(2, 23, 'https://10.0.108.6:9028/', 'FAILED', 3200,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问登录页面"},{"action":"type","target":"username","selector":"#username","value":"xt","description":"输入管理员账号"},{"action":"type","target":"password","selector":"#password","value":"WrongPass123","description":"输入错误密码"},{"action":"click","target":"loginButton","selector":"button[type=\'submit\']","description":"点击登录按钮"}]',
'[{"stepNumber":1,"description":"访问登录页面","status":"SUCCESS","duration":1150,"screenshot":null},{"stepNumber":2,"description":"输入管理员账号","status":"SUCCESS","duration":320,"screenshot":null},{"stepNumber":3,"description":"输入密码","status":"SUCCESS","duration":290,"screenshot":null},{"stepNumber":4,"description":"点击登录按钮","status":"FAILED","duration":1440,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
'登录失败：密码错误，页面显示"用户名或密码错误"提示信息', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),

-- =====================================================
-- 用例ID 24: 新增租户功能验证 (project_id=1)
-- =====================================================
-- 成功执行
(3, 24, 'https://10.0.108.6:9028/', 'SUCCESS', 12500,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问首页"},{"action":"click","target":"tenantMenu","selector":"a[href=\'/tenant\']","description":"点击租户管理菜单"},{"action":"click","target":"tenantManage","selector":"a[href=\'/tenant/manage\']","description":"点击租户管理按钮"},{"action":"click","target":"addButton","selector":"button.add-btn","description":"点击新增按钮"},{"action":"type","target":"tenantName","selector":"#tenantName","value":"测试租户ABC","description":"输入租户名称"},{"action":"type","target":"shortName","selector":"#shortName","value":"TABC","description":"输入简称"},{"action":"select","target":"resourceZone","selector":"#resourceZone","value":"zone-01","description":"选择资源区"},{"action":"type","target":"email","selector":"#email","value":"test@example.com","description":"输入电子邮件"},{"action":"type","target":"contact","selector":"#contact","value":"张三","description":"输入联系人"},{"action":"type","target":"address","selector":"#address","value":"北京市朝阳区测试路123号","description":"输入联系地址"},{"action":"type","target":"phone","selector":"#phone","value":"13800138000","description":"输入电话"},{"action":"click","target":"saveButton","selector":"button.save-btn","description":"点击保存按钮"}]',
'[{"stepNumber":1,"description":"访问首页","status":"SUCCESS","duration":1100,"screenshot":null},{"stepNumber":2,"description":"点击租户管理菜单","status":"SUCCESS","duration":450,"screenshot":null},{"stepNumber":3,"description":"点击租户管理按钮","status":"SUCCESS","duration":380,"screenshot":null},{"stepNumber":4,"description":"点击新增按钮","status":"SUCCESS","duration":520,"screenshot":null},{"stepNumber":5,"description":"输入租户名称","status":"SUCCESS","duration":290,"screenshot":null},{"stepNumber":6,"description":"输入简称","status":"SUCCESS","duration":180,"screenshot":null},{"stepNumber":7,"description":"选择资源区","status":"SUCCESS","duration":350,"screenshot":null},{"stepNumber":8,"description":"输入电子邮件","status":"SUCCESS","duration":220,"screenshot":null},{"stepNumber":9,"description":"输入联系人","status":"SUCCESS","duration":170,"screenshot":null},{"stepNumber":10,"description":"输入联系地址","status":"SUCCESS","duration":240,"screenshot":null},{"stepNumber":11,"description":"输入电话","status":"SUCCESS","duration":190,"screenshot":null},{"stepNumber":12,"description":"点击保存按钮","status":"SUCCESS","duration":8410,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
null, 1, DATE_SUB(NOW(), INTERVAL 1.5 HOUR), DATE_SUB(NOW(), INTERVAL 1.5 HOUR)),

-- 失败执行（必填项未填）
(4, 24, 'https://10.0.108.6:9028/', 'FAILED', 8200,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问首页"},{"action":"click","target":"tenantMenu","selector":"a[href=\'/tenant\']","description":"点击租户管理菜单"},{"action":"click","target":"tenantManage","selector":"a[href=\'/tenant/manage\']","description":"点击租户管理按钮"},{"action":"click","target":"addButton","selector":"button.add-btn","description":"点击新增按钮"},{"action":"type","target":"tenantName","selector":"#tenantName","value":"","description":"输入租户名称（空）"},{"action":"type","target":"shortName","selector":"#shortName","value":"T123","description":"输入简称"},{"action":"click","target":"saveButton","selector":"button.save-btn","description":"点击保存按钮"}]',
'[{"stepNumber":1,"description":"访问首页","status":"SUCCESS","duration":1080,"screenshot":null},{"stepNumber":2,"description":"点击租户管理菜单","status":"SUCCESS","duration":470,"screenshot":null},{"stepNumber":3,"description":"点击租户管理按钮","status":"SUCCESS","duration":360,"screenshot":null},{"stepNumber":4,"description":"点击新增按钮","status":"SUCCESS","duration":540,"screenshot":null},{"stepNumber":5,"description":"输入租户名称","status":"SUCCESS","duration":200,"screenshot":null},{"stepNumber":6,"description":"输入简称","status":"SUCCESS","duration":170,"screenshot":null},{"stepNumber":7,"description":"点击保存按钮","status":"FAILED","duration":5380,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
'保存失败：页面显示"租户名称不能为空"的验证提示信息', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),

-- =====================================================
-- 用例ID 25: 新增租户管理员功能验证 (project_id=1)
-- =====================================================
-- 成功执行
(5, 25, 'https://10.0.108.6:9028/', 'SUCCESS', 14200,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问首页"},{"action":"click","target":"tenantMenu","selector":"a[href=\'/tenant\']","description":"点击租户管理菜单"},{"action":"click","target":"tenantAdminManage","selector":"a[href=\'/tenant/admin\']","description":"点击租户管理员管理"},{"action":"click","target":"addButton","selector":"button.add-btn","description":"点击新增按钮"},{"action":"type","target":"userAccount","selector":"#userAccount","value":"admin01","description":"输入用户账号"},{"action":"type","target":"jobNumber","selector":"#jobNumber","value":"JOB001","description":"输入工号"},{"action":"type","target":"password","selector":"#password","value":"Pass@1234","description":"输入登录密码"},{"action":"type","target":"confirmPassword","selector":"#confirmPassword","value":"Pass@1234","description":"确认密码"},{"action":"type","target":"userName","selector":"#userName","value":"管理员01","description":"输入用户名称"},{"action":"select","target":"tenantSelect","selector":"#tenantSelect","value":"tenant-01","description":"选择所属租户"},{"action":"click","target":"saveButton","selector":"button.save-btn","description":"点击保存按钮"}]',
'[{"stepNumber":1,"description":"访问首页","status":"SUCCESS","duration":1120,"screenshot":null},{"stepNumber":2,"description":"点击租户管理菜单","status":"SUCCESS","duration":480,"screenshot":null},{"stepNumber":3,"description":"点击租户管理员管理","status":"SUCCESS","duration":410,"screenshot":null},{"stepNumber":4,"description":"点击新增按钮","status":"SUCCESS","duration":560,"screenshot":null},{"stepNumber":5,"description":"输入用户账号","status":"SUCCESS","duration":310,"screenshot":null},{"stepNumber":6,"description":"输入工号","status":"SUCCESS","duration":220,"screenshot":null},{"stepNumber":7,"description":"输入登录密码","status":"SUCCESS","duration":280,"screenshot":null},{"stepNumber":8,"description":"确认密码","status":"SUCCESS","duration":190,"screenshot":null},{"stepNumber":9,"description":"输入用户名称","status":"SUCCESS","duration":210,"screenshot":null},{"stepNumber":10,"description":"选择所属租户","status":"SUCCESS","duration":390,"screenshot":null},{"stepNumber":11,"description":"点击保存按钮","status":"SUCCESS","duration":10120,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
null, 1, DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE)),

-- =====================================================
-- 用例ID 26: 系统管理员登录系统功能验证 (project_id=2)
-- =====================================================
-- 成功执行
(6, 26, 'https://10.0.108.6:9028/', 'SUCCESS', 4890,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问登录页面"},{"action":"type","target":"username","selector":"#username","value":"xt","description":"输入管理员账号"},{"action":"type","target":"password","selector":"#password","value":"Aa12345678","description":"输入密码"},{"action":"click","target":"loginButton","selector":"button[type=\'submit\']","description":"点击登录按钮"}]',
'[{"stepNumber":1,"description":"访问登录页面","status":"SUCCESS","duration":1180,"screenshot":null},{"stepNumber":2,"description":"输入管理员账号","status":"SUCCESS","duration":340,"screenshot":null},{"stepNumber":3,"description":"输入密码","status":"SUCCESS","duration":270,"screenshot":null},{"stepNumber":4,"description":"点击登录按钮","status":"SUCCESS","duration":3100,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
null, 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),

-- =====================================================
-- 用例ID 27: 新增租户功能验证 (project_id=2)
-- =====================================================
-- 跳过执行
(7, 27, 'https://10.0.108.6:9028/', 'SKIPPED', 0,
'[]',
'[]',
'[]',
'前置条件不满足：项目2的租户管理功能暂未开通，跳过此测试', 2, DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),

-- =====================================================
-- 用例ID 28: 新增租户管理员功能验证 (project_id=2)
-- =====================================================
-- 失败执行（超时）
(8, 28, 'https://10.0.108.6:9028/', 'FAILED', 30000,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问首页"},{"action":"click","target":"tenantMenu","selector":"a[href=\'/tenant\']","description":"点击租户管理菜单","timeout":30000},{"action":"click","target":"tenantAdminManage","selector":"a[href=\'/tenant/admin\']","description":"点击租户管理员管理"}]',
'[{"stepNumber":1,"description":"访问首页","status":"SUCCESS","duration":29500,"screenshot":null},{"stepNumber":2,"description":"点击租户管理菜单","status":"FAILED","duration":500,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
'页面加载超时：30秒内租户管理菜单未加载完成，可能网络问题或服务器响应慢', 2, DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE)),

-- 最近执行 - 系统管理员登录（成功）
(9, 23, 'https://10.0.108.6:9028/', 'SUCCESS', 5180,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问登录页面"},{"action":"type","target":"username","selector":"#username","value":"xt","description":"输入管理员账号"},{"action":"type","target":"password","selector":"#password","value":"Aa12345678","description":"输入密码"},{"action":"click","target":"loginButton","selector":"button[type=\'submit\']","description":"点击登录按钮"}]',
'[{"stepNumber":1,"description":"访问登录页面","status":"SUCCESS","duration":1190,"screenshot":null},{"stepNumber":2,"description":"输入管理员账号","status":"SUCCESS","duration":360,"screenshot":null},{"stepNumber":3,"description":"输入密码","status":"SUCCESS","duration":290,"screenshot":null},{"stepNumber":4,"description":"点击登录按钮","status":"SUCCESS","duration":3340,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
null, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE)),

-- 最近执行 - 新增租户（成功）
(10, 24, 'https://10.0.108.6:9028/', 'SUCCESS', 12800,
'[{"action":"navigate","url":"https://10.0.108.6:9028/","selector":"","description":"访问首页"},{"action":"click","target":"tenantMenu","selector":"a[href=\'/tenant\']","description":"点击租户管理菜单"},{"action":"click","target":"tenantManage","selector":"a[href=\'/tenant/manage\']","description":"点击租户管理按钮"},{"action":"click","target":"addButton","selector":"button.add-btn","description":"点击新增按钮"},{"action":"type","target":"tenantName","selector":"#tenantName","value":"租户XYZ","description":"输入租户名称"},{"action":"type","target":"shortName","selector":"#shortName","value":"TXYZ","description":"输入简称"},{"action":"select","target":"resourceZone","selector":"#resourceZone","value":"zone-02","description":"选择资源区"},{"action":"type","target":"email","selector":"#email","value":"tenant@example.com","description":"输入电子邮件"},{"action":"type","target":"contact","selector":"#contact","value":"李四","description":"输入联系人"},{"action":"type","target":"address","selector":"#address","value":"上海市浦东新区测试路456号","description":"输入联系地址"},{"action":"type","target":"phone","selector":"#phone","value":"13900139000","description":"输入电话"},{"action":"click","target":"saveButton","selector":"button.save-btn","description":"点击保存按钮"}]',
'[{"stepNumber":1,"description":"访问首页","status":"SUCCESS","duration":1150,"screenshot":null},{"stepNumber":2,"description":"点击租户管理菜单","status":"SUCCESS","duration":460,"screenshot":null},{"stepNumber":3,"description":"点击租户管理按钮","status":"SUCCESS","duration":390,"screenshot":null},{"stepNumber":4,"description":"点击新增按钮","status":"SUCCESS","duration":530,"screenshot":null},{"stepNumber":5,"description":"输入租户名称","status":"SUCCESS","duration":300,"screenshot":null},{"stepNumber":6,"description":"输入简称","status":"SUCCESS","duration":190,"screenshot":null},{"stepNumber":7,"description":"选择资源区","status":"SUCCESS","duration":360,"screenshot":null},{"stepNumber":8,"description":"输入电子邮件","status":"SUCCESS","duration":230,"screenshot":null},{"stepNumber":9,"description":"输入联系人","status":"SUCCESS","duration":180,"screenshot":null},{"stepNumber":10,"description":"输入联系地址","status":"SUCCESS","duration":250,"screenshot":null},{"stepNumber":11,"description":"输入电话","status":"SUCCESS","duration":200,"screenshot":null},{"stepNumber":12,"description":"点击保存按钮","status":"SUCCESS","duration":8460,"screenshot":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="}]',
'["data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==","data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="]',
null, 1, DATE_SUB(NOW(), INTERVAL 2 MINUTE), DATE_SUB(NOW(), INTERVAL 2 MINUTE));

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 第四步：验证插入的数据
-- =====================================================
SELECT '========== 插入完成！数据统计 ==========' AS info;

SELECT '测试执行记录总数：' AS info, COUNT(*) AS count FROM test_case_executions;

SELECT '========== 执行记录列表（按时间倒序） ==========' AS info;
SELECT
    e.unique_id AS '记录ID',
    tc.case_number AS '用例编号',
    tc.name AS '用例名称',
    e.status AS '状态',
    e.duration AS '耗时(ms)',
    CASE e.executed_by WHEN 1 THEN '用户1' WHEN 2 THEN '用户2' ELSE CONCAT('用户', e.executed_by) END AS '执行人',
    DATE_FORMAT(e.created_time, '%Y-%m-%d %H:%i:%s') AS '执行时间'
FROM test_case_executions e
LEFT JOIN test_cases tc ON e.test_case_id = tc.unique_id
ORDER BY e.created_time DESC;

SELECT '========== 按状态统计 ==========' AS info;
SELECT
    status AS '状态',
    COUNT(*) AS '记录数',
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM test_case_executions), 2) AS '占比(%)'
FROM test_case_executions
GROUP BY status
ORDER BY COUNT(*) DESC;

SELECT '========== 按用例统计 ==========' AS info;
SELECT
    tc.case_number AS '用例编号',
    tc.name AS '用例名称',
    COUNT(*) AS '执行次数',
    SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END) AS '成功',
    SUM(CASE WHEN e.status = 'FAILED' THEN 1 ELSE 0 END) AS '失败',
    SUM(CASE WHEN e.status = 'SKIPPED' THEN 1 ELSE 0 END) AS '跳过'
FROM test_case_executions e
LEFT JOIN test_cases tc ON e.test_case_id = tc.unique_id
GROUP BY tc.unique_id, tc.case_number, tc.name
ORDER BY COUNT(*) DESC;
