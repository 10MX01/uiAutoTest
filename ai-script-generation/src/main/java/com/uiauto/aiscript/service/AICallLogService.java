package com.uiauto.aiscript.service;

import com.uiauto.aiscript.entity.AICallLogEntity;

import java.util.List;

/**
 * AI调用日志Service接口
 */
public interface AICallLogService {

    /**
     * 记录成功的调用
     */
    AICallLogEntity logSuccess(String callType, String inputText, String outputJson,
                               long durationMs, int tokensUsed, String modelUsed, Long serviceConfigId);

    /**
     * 记录失败的调用
     */
    AICallLogEntity logFailure(String callType, String inputText, String errorMessage,
                               long durationMs, Long serviceConfigId);

    /**
     * 记录AI调用
     */
    AICallLogEntity logCall(AICallLogEntity log);

    /**
     * 根据调用类型查询日志
     */
    List<AICallLogEntity> queryLogsByCallType(String callType);

    /**
     * 根据成功状态查询日志
     */
    List<AICallLogEntity> queryLogsBySuccess(boolean success);

    /**
     * 根据服务配置ID查询日志
     */
    List<AICallLogEntity> queryLogsByServiceConfigId(Long serviceConfigId);
}
