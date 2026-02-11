import request from '@/utils/request'

/**
 * 获取所有脚本
 */
export function getScripts(params) {
  return request({
    url: '/scripts/all',
    method: 'get',
    params
  })
}

/**
 * 获取脚本详情
 */
export function getScriptDetail(id) {
  return request({
    url: `/scripts/${id}`,
    method: 'get'
  })
}

/**
 * 创建脚本
 */
export function createScript(data) {
  return request({
    url: '/scripts',
    method: 'post',
    data
  })
}

/**
 * 更新脚本基本信息
 */
export function updateScript(id, data) {
  return request({
    url: '/scripts/update-basic-info',
    method: 'post',
    params: { scriptId: id },
    data
  })
}

/**
 * 删除脚本
 */
export function deleteScript(id) {
  return request({
    url: '/scripts/delete',
    method: 'post',
    params: { uniqueId: id }
  })
}

/**
 * 切换脚本启用状态
 */
export function toggleScriptEnabled(id) {
  return request({
    url: '/scripts/toggle-enabled',
    method: 'post',
    params: { scriptId: id }
  })
}

/**
 * 执行脚本
 */
export function executeScript(id) {
  return request({
    url: `/scripts/${id}/execute`,
    method: 'post'
  })
}

/**
 * AI 生成脚本
 */
export function generateScriptByAI(testCaseId) {
  return request({
    url: `/scripts/ai-generate/${testCaseId}`,
    method: 'post'
  })
}

/**
 * 重新生成脚本（AI失败重试）
 */
export function regenerateScript(id) {
  return request({
    url: `/scripts/${id}/regenerate`,
    method: 'post'
  })
}
