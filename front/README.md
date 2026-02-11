# Vue3 前端项目

这是基于 Vue3 + Vite 的前端项目，使用方案二集成到 Spring Boot。

## 目录结构

```
front/
├── src/
│   ├── api/          # API 接口封装
│   ├── components/   # 公共组件
│   ├── views/        # 页面组件
│   ├── router/       # 路由配置
│   ├── store/        # 状态管理（Pinia）
│   ├── utils/        # 工具函数
│   ├── App.vue       # 根组件
│   └── main.js       # 入口文件
├── public/           # 静态资源
├── scripts/          # 构建脚本
├── dist/             # 打包输出目录（自动生成）
├── package.json
└── vite.config.js    # Vite 配置

后端：
pacakge/src/main/resources/static/  # Spring Boot 静态资源目录
```

## 开发流程

### 1. 开发模式（前后端分离）

```bash
# 终端1：启动后端
cd pacakge
mvn spring-boot:run

# 终端2：启动前端（支持热更新）
cd front
npm run dev
```

访问：http://localhost:5173

### 2. 集成模式（打包到 Spring Boot）

```bash
# 1. 打包并复制到 Spring Boot
cd front
npm run deploy

# 2. 启动 Spring Boot
cd ../pacakge
mvn spring-boot:run
```

访问：http://localhost:8080/api/

## 配置说明

### vite.config.js

- `base: '/api/'` - 基础路径与 Spring Boot context-path 一致
- `server.proxy` - 开发时代理 API 请求到后端
- `build.outDir` - 打包输出目录

### Spring Boot 配置

- `IndexController.java` - 处理前端路由转发
- `WebConfig.java` - 配置静态资源和跨域

## 前端路由

如果使用 Vue Router 的 history 模式，前端路由路径已在 `IndexController` 中配置转发：

- `/ui` - UI 管理
- `/script` - 脚本管理
- `/test` - 测试用例
- `/settings` - 设置

**注意**：前端路由路径不要与后端 API 路径冲突。

## API 请求示例

```javascript
// axios 配置
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',  // 与 Spring Boot 的 context-path 一致
  timeout: 10000
})

// API 调用
api.get('/test-cases/list')
  .then(response => {
    console.log(response.data)
  })
```

## 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 开发模式
npm run build        # 打包
npm run preview      # 预览打包结果
npm run deploy       # 打包并复制到 Spring Boot
```

## 后续开发建议

1. 安装常用依赖：
```bash
npm install vue-router@4 pinia axios element-plus
```

2. 在 `src/router/` 中配置路由
3. 在 `src/api/` 中封装 API 接口
4. 在 `src/views/` 中开发页面组件
5. 在 `src/components/` 中创建公共组件

