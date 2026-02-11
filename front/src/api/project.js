import request from '@/utils/request'

/**
 * 获取所有项目
 */
export function getProjects(params) {
  return request({
    url: '/projects',
    method: 'get',
    params
  })
}

/**
 * 获取项目详情
 */
export function getProjectDetail(id) {
  return request({
    url: `/projects/${id}`,
    method: 'get'
  })
}

/**
 * 创建项目
 */
export function createProject(data) {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

/**
 * 更新项目
 */
export function updateProject(id, data) {
  return request({
    url: `/projects/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除项目
 */
export function deleteProject(id) {
  return request({
    url: `/projects/${id}/delete`,
    method: 'post'
  })
}

/**
 * 获取项目的测试用例列表
 */
export function getProjectTestCases(projectId) {
  return request({
    url: `/projects/${projectId}/test-cases`,
    method: 'get'
  })
}
