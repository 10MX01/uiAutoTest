import request from '@/utils/request'

/**
 * 获取所有测试用例
 */
export function getTestCases(params) {
  return request({
    url: '/test-cases',
    method: 'get',
    params
  })
}

/**
 * 获取测试用例详情
 */
export function getTestCaseDetail(id) {
  return request({
    url: `/test-cases/${id}`,
    method: 'get'
  })
}

/**
 * 创建测试用例
 */
export function createTestCase(data) {
  return request({
    url: '/test-cases',
    method: 'post',
    data
  })
}

/**
 * 更新测试用例
 */
export function updateTestCase(id, data) {
  return request({
    url: `/test-cases/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除测试用例
 */
export function deleteTestCase(id) {
  return request({
    url: `/test-cases/${id}/delete`,
    method: 'post'
  })
}

/**
 * 执行测试用例
 */
export function executeTestCase(id, overrideUrl) {
  return request({
    url: '/executions/execute',
    method: 'post',
    data: {
      testCaseId: id,
      overrideUrl: overrideUrl || null
    }
  })
}

/**
 * 执行全部测试用例
 */
export function executeAllTestCases() {
  return request({
    url: '/test-cases/execute-all',
    method: 'post'
  })
}

/**
 * 导入测试用例（Excel文件）
 */
export function importTestCases(file, projectId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('projectId', projectId)
  return request({
    url: '/file/import/test-cases',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000 // 导入接口单独设置5分钟超时
  })
}

/**
 * 获取按项目分组的测试用例列表
 * 项目按最后更新时间排序，最近更新的项目在前
 */
export function getTestCasesGroupedByProject() {
  return request({
    url: '/test-cases/grouped-by-project',
    method: 'get'
  })
}
