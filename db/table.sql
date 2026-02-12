-- =====================================================
-- UiAutoTest 数据库建表脚本
-- AI驱动的Web自动化测试平台
-- =====================================================
--
-- 数据库规范：
-- 所有表必须包含以下5个标准字段：
-- 1. unique_id: 唯一标识ID（自增主键）
-- 2. created_by: 创建人ID
-- 3. updated_by: 更新人ID
-- 4. created_time: 创建时间
-- 5. updated_time: 更新时间
--

-- 创建数据库
CREATE DATABASE IF NOT EXISTS uiaut_test
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE uiaut_test;

-- =====================================================

-- =====================================================
-- 1. 项目表 (projects)
-- =====================================================
-- 说明：管理测试项目，一个测试用例可以属于多个项目
CREATE TABLE IF NOT EXISTS projects (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间（ON UPDATE CURRENT_TIMESTAMP）',

    -- 业务字段
    name             VARCHAR(255) NOT NULL                      COMMENT '项目名称',
    description      TEXT                                         COMMENT '项目描述',
    code             VARCHAR(50)                                  COMMENT '项目代码（唯一）',
    target_url       VARCHAR(500)                                 COMMENT '目标URL（测试环境地址）',
    base_url         VARCHAR(500)                                 COMMENT '基础URL（可选）',

    UNIQUE KEY uk_code (code),
    INDEX idx_name (name)
) COMMENT='项目表';

-- =====================================================
-- 注：标签系统、版本控制、软删除功能已从需求中移除
-- 多对多项目关系已改为多对一关系（test_cases.project_id）
-- =====================================================
-- 2. 测试用例主表 (test_cases)
-- =====================================================
-- 说明：存储测试用例的核心信息
CREATE TABLE IF NOT EXISTS test_cases (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    case_number      VARCHAR(50)                                  COMMENT '用例编号',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间（ON UPDATE CURRENT_TIMESTAMP）',

    -- 业务字段
    name             VARCHAR(255) NOT NULL                      COMMENT '测试用例名称',
    description      TEXT                                         COMMENT '测试用例描述',
    project_id       BIGINT                                       COMMENT '关联项目ID',

    -- 测试步骤：双字段存储
    steps_text       TEXT         NOT NULL                      COMMENT '测试步骤（自然语言描述）',
    steps_json       JSON                                         COMMENT '测试步骤（AI结构化后的JSON，不含选择器）',
    is_ai_generated  BOOLEAN      DEFAULT FALSE                  COMMENT '是否由AI生成',

    expected_result  TEXT                                         COMMENT '预期结果（自然语言）',

    -- 分类和状态
    priority         VARCHAR(10)  DEFAULT 'P2'                   COMMENT '优先级：P0/P1/P2/P3',
    status           VARCHAR(20)  DEFAULT 'NOT_EXECUTED'         COMMENT '执行状态：NOT_EXECUTED-未执行, PASSED-通过, FAILED-未通过',
    automation_status VARCHAR(20) DEFAULT 'MANUAL'               COMMENT '自动化状态：MANUAL/AUTOMATED/PARTIAL',
    executed_by      BIGINT                                    COMMENT '执行人ID',
    execution_time   DATETIME                                   COMMENT '最后执行时间',

    INDEX idx_name (name),
    INDEX idx_case_number (case_number),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_created_by (created_by),
    INDEX idx_project_id (project_id),

    FOREIGN KEY (project_id) REFERENCES projects(unique_id) ON DELETE SET NULL
) COMMENT='测试用例主表';

-- =====================================================
-- 3. 测试用例依赖关系表 (test_case_dependencies)
-- =====================================================
-- 说明：管理测试用例之间的前置依赖关系
CREATE TABLE IF NOT EXISTS test_case_dependencies (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间（ON UPDATE CURRENT_TIMESTAMP）',

    -- 业务字段
    test_case_id     BIGINT       NOT NULL                      COMMENT '当前测试用表的unique_id',
    prerequisite_id  BIGINT       NOT NULL                      COMMENT '前置测试用表的unique_id',
    dependency_type  VARCHAR(20)  DEFAULT 'HARD'                COMMENT '依赖类型：HARD-强依赖/SOFT-弱依赖',

    FOREIGN KEY (test_case_id) REFERENCES test_cases(unique_id) ON DELETE CASCADE,
    FOREIGN KEY (prerequisite_id) REFERENCES test_cases(unique_id) ON DELETE CASCADE,
    UNIQUE KEY uk_dependency (test_case_id, prerequisite_id),
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_prerequisite_id (prerequisite_id)
) COMMENT='测试用例依赖关系表';

-- =====================================================
-- 4. 测试脚本表 (test_scripts)
-- =====================================================
-- 说明：存储测试脚本的基本信息和内容，支持启用/禁用状态管理
-- 核心变更：移除版本控制，添加启用状态，强制关联测试用例，支持AI生成失败重试
CREATE TABLE IF NOT EXISTS test_scripts (
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID（系统生成时为0）',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间（ON UPDATE CURRENT_TIMESTAMP）',

    script_name      VARCHAR(255) NOT NULL                      COMMENT '脚本名称',
    script_description TEXT                                       COMMENT '脚本描述',
    script_content   LONGTEXT     NOT NULL                      COMMENT '脚本内容（TypeScript/JavaScript代码）',
    language         VARCHAR(20)  DEFAULT 'typescript'          COMMENT '脚本语言：typescript/javascript',
    generation_method VARCHAR(50)  NOT NULL                      COMMENT '生成方式：EXCEL_IMPORT/AI_GENERATED',
    enabled          BOOLEAN      DEFAULT TRUE                   COMMENT '是否启用：同一测试用例只能有一个启用',
    ai_generation_status VARCHAR(20) DEFAULT 'SUCCESS'           COMMENT 'AI生成状态：SUCCESS/FAILED/PENDING',
    ai_retry_count   INT          DEFAULT 0                     COMMENT 'AI重试次数（最多3次）',
    ai_error_message TEXT                                        COMMENT 'AI生成失败错误信息',
    ai_model_used    VARCHAR(100)                              COMMENT '使用的AI模型（如gpt-4）',
    ai_generation_time TIMESTAMP NULL                            COMMENT 'AI生成时间',
    test_case_id     BIGINT       NOT NULL                      COMMENT '关联的测试用例ID',
    category         VARCHAR(100)                              COMMENT '脚本分类',
    execution_count  INT          DEFAULT 0                     COMMENT '执行次数',
    last_execution_time TIMESTAMP NULL                           COMMENT '最后执行时间',
    last_execution_result VARCHAR(20)                            COMMENT '最后执行结果：SUCCESS/FAILED/SKIPPED',

    INDEX idx_script_name (script_name),
    INDEX idx_generation_method (generation_method),
    INDEX idx_enabled (enabled),
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_ai_generation_status (ai_generation_status),
    INDEX idx_category (category),
    INDEX idx_created_by (created_by),

    UNIQUE KEY uk_test_case_enabled (test_case_id, enabled)     COMMENT '确保同一测试用例只有一个启用脚本',
    FOREIGN KEY (test_case_id) REFERENCES test_cases(unique_id) ON DELETE CASCADE
) COMMENT='测试脚本表（重构版：移除版本控制和软删除，添加启用状态管理）';
-- =====================================================
-- 5. AI服务配置表 (ai_service_configs)
-- =====================================================
-- 说明：存储AI服务的配置信息，支持多种AI服务接入
CREATE TABLE IF NOT EXISTS ai_service_configs (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    -- 业务字段
    provider         VARCHAR(50)  NOT NULL                      COMMENT 'AI服务提供商：openai/anthropic/custom',
    model_name       VARCHAR(100) NOT NULL                      COMMENT '模型名称：gpt-4/gpt-3.5-turbo/claude-3等',
    api_key          VARCHAR(500) NOT NULL                      COMMENT 'API密钥（加密存储）',
    api_endpoint     VARCHAR(500)                               COMMENT 'API端点（自定义服务时使用）',
    custom_headers   JSON                                        COMMENT '自定义请求头（JSON格式）',
    is_default       BOOLEAN      DEFAULT FALSE                  COMMENT '是否为默认服务',
    status           VARCHAR(20)  DEFAULT 'ACTIVE'               COMMENT '状态：ACTIVE/INACTIVE',
    max_tokens       INT          DEFAULT 2000                   COMMENT '最大token数',
    temperature      DECIMAL(3,2) DEFAULT 0.70                   COMMENT '温度参数（0.0-1.0）',
    timeout_seconds  INT          DEFAULT 30                     COMMENT '超时时间（秒）',

    INDEX idx_provider (provider),
    INDEX idx_is_default (is_default),
    INDEX idx_status (status)
) COMMENT='AI服务配置表';

-- =====================================================
-- 6. Prompt模板表 (prompt_templates)
-- =====================================================
-- 说明：存储AI调用的Prompt模板
CREATE TABLE IF NOT EXISTS prompt_templates (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    -- 业务字段
    template_name    VARCHAR(100) NOT NULL                      COMMENT '模板名称',
    template_type    VARCHAR(50)  NOT NULL                      COMMENT '模板类型：natural_language_parse/script_generation/script_generation_from_json',
    prompt_content   TEXT         NOT NULL                      COMMENT 'Prompt内容',
    version          VARCHAR(20)  DEFAULT '1.0'                 COMMENT '模板版本',
    is_active        BOOLEAN      DEFAULT TRUE                  COMMENT '是否启用',
    description      TEXT                                        COMMENT '模板描述',

    UNIQUE KEY uk_template_name_version (template_name, version),
    INDEX idx_template_type (template_type),
    INDEX idx_is_active (is_active)
) COMMENT='Prompt模板表';

-- =====================================================
-- 7. AI调用日志表 (ai_call_logs)
-- =====================================================
-- 说明：记录所有AI调用的日志，用于追溯和优化
CREATE TABLE IF NOT EXISTS ai_call_logs (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    -- 业务字段
    call_type        VARCHAR(50)  NOT NULL                      COMMENT '调用类型：natural_language_parse/script_generation',
    input_text       TEXT         NOT NULL                      COMMENT '输入文本',
    output_json      JSON                                        COMMENT '输出JSON结果',
    success          BOOLEAN      NOT NULL                      COMMENT '是否成功',
    error_message    TEXT                                        COMMENT '错误信息',
    duration_ms      BIGINT                                       COMMENT '耗时（毫秒）',
    tokens_used      INT                                         COMMENT '使用的token数',
    model_used       VARCHAR(100)                               COMMENT '使用的AI模型',
    service_config_id BIGINT                                     COMMENT '使用的AI服务配置ID',

    FOREIGN KEY (service_config_id) REFERENCES ai_service_configs(unique_id) ON DELETE SET NULL,
    INDEX idx_call_type (call_type),
    INDEX idx_success (success),
    INDEX idx_created_time (created_time)
) COMMENT='AI调用日志表';

-- =====================================================
-- 8. 测试用例执行记录表 (test_case_executions)
-- =====================================================
-- 说明：记录测试用例的执行历史和结果
CREATE TABLE IF NOT EXISTS test_case_executions (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT                                    COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    -- 业务字段
    test_case_id     BIGINT       NOT NULL                      COMMENT '测试用例ID',
    project_id     BIGINT       NOT NULL                      COMMENT '关联项目ID',
    execution_url    VARCHAR(500) NOT NULL                      COMMENT '执行时的URL',
    status           VARCHAR(20)  NOT NULL                      COMMENT '执行状态：SUCCESS/FAILED/SKIPPED',
    duration         BIGINT                                       COMMENT '执行耗时（毫秒）',
    generated_script JSON                                        COMMENT '生成的脚本（包含选择器）',
    steps_result     JSON                                        COMMENT '每个步骤的执行结果',
    screenshots      JSON                                        COMMENT '截图数组（Base64或路径）',
    error_message    TEXT                                        COMMENT '错误信息',
    executed_by      BIGINT       NOT NULL                      COMMENT '执行人ID',

    FOREIGN KEY (test_case_id) REFERENCES test_cases(unique_id) ON DELETE CASCADE,
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) COMMENT='测试用例执行记录表';

CREATE TABLE IF NOT EXISTS file_management (
    unique_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件唯一ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(100) NOT NULL COMMENT '文件类型（MIME类型）',
    file_extension VARCHAR(20) NOT NULL COMMENT '文件扩展名',
    file_md5 VARCHAR(32) COMMENT '文件MD5值，用于去重',
    business_type VARCHAR(50) COMMENT '业务类型（TEST_CASE_IMPORT、TEST_DATA等）',
    business_id BIGINT COMMENT '关联业务ID',
    uploader_id BIGINT NOT NULL COMMENT '上传人ID',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_business (business_type, business_id),
    INDEX idx_uploader (uploader_id),
    INDEX idx_md5 (file_md5),
    INDEX idx_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理表';

-- =====================================================
-- 9. 用户表 (users)
-- =====================================================
-- 说明：管理系统用户，支持登录认证和权限管理
CREATE TABLE IF NOT EXISTS users (
    -- 标准字段（所有表必须包含）
    unique_id        BIGINT       PRIMARY KEY  AUTO_INCREMENT  COMMENT '唯一标识ID（自增主键）',
    created_by       BIGINT       NOT NULL                      COMMENT '创建人ID',
    updated_by       BIGINT                                    COMMENT '更新人ID',
    created_time     DATETIME     NOT NULL                      COMMENT '创建时间',
    updated_time     DATETIME     NOT NULL                      COMMENT '更新时间',

    -- 业务字段
    username         VARCHAR(50)  NOT NULL                      COMMENT '用户名（登录名）',
    password         VARCHAR(255) NOT NULL                      COMMENT '密码（BCrypt加密）',
    real_name        VARCHAR(100) NOT NULL                      COMMENT '真实姓名',
    email            VARCHAR(100)                               COMMENT '邮箱',
    phone            VARCHAR(20)                               COMMENT '手机号',
    role             VARCHAR(20)  NOT NULL DEFAULT 'USER'       COMMENT '角色：ADMIN-管理员, USER-普通用户',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'      COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
    last_login_time  DATETIME                                    COMMENT '最后登录时间',

    UNIQUE KEY uk_username (username),
    INDEX idx_real_name (real_name),
    INDEX idx_role (role),
    INDEX idx_status (status)
) COMMENT='用户表';

-- 初始化默认管理员（密码：admin123）
INSERT INTO users (unique_id, created_by, username, password, real_name, role, status, created_time, updated_time)
VALUES (1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_time = NOW();
