package com.uiauto.aiscript.service;

import com.uiauto.aiscript.entity.AIServiceConfigEntity;

/**
 * LLM服务接口
 * 支持多种大语言模型的统一调用接口
 */
public interface LLMService {

    /**
     * 调用LLM服务
     *
     * @param config AI服务配置
     * @param prompt 输入的Prompt
     * @return AI返回的文本内容
     * @throws Exception 调用失败时抛出异常
     */
    String callLLM(AIServiceConfigEntity config, String prompt) throws Exception;

    /**
     * 调用LLM服务（带超时）
     *
     * @param config AI服务配置
     * @param prompt 输入的Prompt
     * @param timeoutMs 超时时间（毫秒）
     * @return AI返回的文本内容
     * @throws Exception 调用失败或超时时抛出异常
     */
    String callLLM(AIServiceConfigEntity config, String prompt, long timeoutMs) throws Exception;

    /**
     * 测试连接
     *
     * @param config AI服务配置
     * @return 是否连接成功
     */
    boolean testConnection(AIServiceConfigEntity config);
}
