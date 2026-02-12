import axios from 'axios'
import { Auth } from './auth'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',  // 与 Spring Boot 的 context-path 一致
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 Token 到请求头
    const token = Auth.getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 后端返回的是 ApiResponse 包装的数据结构
    // 需要提取 data 字段
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      console.error('业务错误:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)

    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除认证信息
      Auth.clear()
      // 跳转到登录页
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    // 可以在这里统一处理错误
    return Promise.reject(error)
  }
)

export default request
