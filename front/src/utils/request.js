import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',  // 与 Spring Boot 的 context-path 一致
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 可以在这里添加 token 等认证信息
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
    // 可以在这里统一处理错误
    return Promise.reject(error)
  }
)

export default request
