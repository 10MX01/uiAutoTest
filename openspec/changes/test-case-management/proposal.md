# 提案：测试用例管理模块

## Why

当前的测试用例管理方式存在以下问题：
1. **缺乏统一管理**：测试用例散落在各处，无法集中管理和维护
2. **协作困难**：团队成员无法共享和协作编辑测试用例
3. **执行顺序混乱**：无法定义测试用例之间的前置依赖关系，导致测试执行顺序不明确
4. **数据孤岛**：测试用例与测试脚本、执行结果分离，无法形成闭环

现在是构建测试用例管理模块的最佳时机，因为：
- 项目处于初始阶段，可以从零设计最佳架构
- 需要为后续的 AI 脚本生成、测试执行模块提供数据基础
- 可以为整个测试自动化平台建立统一的数据规范

## What Changes

### 新增功能
- **测试用例CRUD**：创建、读取、更新、删除测试用例
- **项目管理**：支持按项目组织测试用例
- **依赖关系**：定义测试用例之间的前置依赖关系（支持前置和后置条件）
- **高级搜索**：支持按状态、优先级、创建人、关键词等多维度搜索
- **批量导入**：支持通过 Excel 文件批量导入测试用例

### 技术架构
- 后端：Spring Boot + JPA + MySQL
- 前端：Vue 3 + TypeScript + Element UI
- 数据库：MySQL 8.0（主数据）、Redis（缓存）

## Capabilities

### New Capabilities
- `test-case-crud`: 测试用例的增删改查功能，包括创建、查询、更新、删除、批量导入
- `test-case-organization`: 测试用例组织管理，包括项目关联和依赖关系定义
- `test-case-search`: 测试用例搜索和过滤，支持多维度查询和全文搜索

### Modified Capabilities
（无）

## Impact

### 代码影响
- 新增后端模块：`test-case-management`
- 新增通用模块：`common`（包含 BaseEntity、ApiResponse 等通用类）
- 新增启动模块：`pacakge`（包含应用启动类和配置文件）
- 修改主 pom.xml：添加模块化配置

### API影响
- 新增 RESTful API 端点：
  - `POST /test-cases` - 创建测试用例
  - `GET /test-cases/{uniqueId}` - 获取测试用例详情
  - `POST /test-cases/{uniqueId}` - 更新测试用例
  - `POST /test-cases/{uniqueId}/delete` - 删除测试用例
  - `GET /test-cases` - 查询所有测试用例
  - `GET /test-cases/by-status?status={status}` - 按状态查询
  - `GET /test-cases/by-priority?priority={priority}` - 按优先级查询
  - `GET /test-cases/by-creator?createdBy={userId}` - 按创建人查询
  - `GET /test-cases/search?keyword={keyword}` - 搜索测试用例
  - `POST /test-cases/import` - 批量导入测试用例（Excel）
  - `GET /test-cases/import/template` - 下载导入模板
  - `POST /test-cases/{uniqueId}/dependencies` - 添加依赖关系
  - `GET /test-cases/{uniqueId}/dependencies` - 查询依赖关系
  - `POST /test-cases/{uniqueId}/dependencies/remove` - 移除依赖关系

### 数据库影响
- 新增 4 张数据库表：
  - `test_cases` - 测试用例主表
  - `projects` - 项目表
  - `test_case_projects` - 测试用例-项目关联表
  - `test_case_dependencies` - 测试用例依赖关系表（支持前置和后置依赖）

### 依赖影响
- 新增 Spring Boot Starter 依赖：Web、Data JPA、Validation
- 新增 MySQL 驱动依赖
- 新增 Lombok 依赖

### 系统影响
- 为后续的 AI 脚本生成模块提供数据源
- 为测试执行模块提供用例数据
- 为测试报告模块提供统计基础