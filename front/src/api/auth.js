import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/users/logout',
    method: 'post'
  })
}

/**
 * 获取所有用户列表
 */
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

/**
 * 获取执行人列表（用于下拉框）
 */
export function getExecutors() {
  return request({
    url: '/users/executors',
    method: 'get'
  })
}

/**
 * 根据ID获取用户
 */
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 */
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 启用用户
 */
export function enableUser(id) {
  return request({
    url: `/users/${id}/enable`,
    method: 'post'
  })
}

/**
 * 禁用用户
 */
export function disableUser(id) {
  return request({
    url: `/users/${id}/disable`,
    method: 'post'
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}
