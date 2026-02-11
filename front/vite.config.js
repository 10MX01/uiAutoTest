import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],

  // 路径别名配置
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },

  // 基础路径：开发环境使用 /，生产环境使用 /api/
  base: process.env.NODE_ENV === 'production' ? '/api/' : '/',

  // 开发服务器配置
  server: {
    port: 5173,
    proxy: {
      // API 代理到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },

  // 构建配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    // 打包后的文件将复制到 Spring Boot 的 static 目录
    emptyOutDir: true
  }
})
