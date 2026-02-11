import request from '@/utils/request'

/**
 * 获取测试记录列表（分页）
 */
export function getTestRecords(params) {
  return request({
    url: '/test-records',
    method: 'get',
    params
  })
}

/**
 * 获取测试记录详情
 */
export function getTestRecordDetail(id) {
  return request({
    url: `/test-records/${id}`,
    method: 'get'
  })
}

/**
 * 删除测试记录
 */
export function deleteTestRecord(id) {
  return request({
    url: `/test-records/${id}`,
    method: 'delete'
  })
}

/**
 * 获取测试记录统计数据
 */
export function getTestRecordStatistics() {
  return request({
    url: '/test-records/statistics/summary',
    method: 'get'
  })
}
