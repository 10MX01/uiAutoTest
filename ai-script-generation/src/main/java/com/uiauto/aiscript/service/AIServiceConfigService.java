package com.uiauto.aiscript.service;

import com.uiauto.aiscript.dto.AIServiceConfigCreateRequest;
import com.uiauto.aiscript.dto.AIServiceConfigUpdateRequest;
import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import com.uiauto.aiscript.vo.ConnectionTestResponse;

import java.util.List;

/**
 * AI服务配置Service接口
 */
public interface AIServiceConfigService {

    /**
     * 创建AI服务配置
     */
    AIServiceConfigEntity createConfig(AIServiceConfigCreateRequest request, Long userId);

    /**
     * 更新AI服务配置
     */
    AIServiceConfigEntity updateConfig(AIServiceConfigUpdateRequest request, Long userId);

    /**
     * 删除AI服务配置
     */
    void deleteConfig(Long configId);

    /**
     * 根据ID查询配置
     */
    AIServiceConfigEntity getConfigById(Long configId);

    /**
     * 查询所有配置
     */
    List<AIServiceConfigEntity> listAllConfigs();

    /**
     * 查询启用的配置
     */
    List<AIServiceConfigEntity> listActiveConfigs();

    /**
     * 获取默认配置
     */
    AIServiceConfigEntity getDefaultConfig();

    /**
     * 测试连接
     */
    ConnectionTestResponse testConnection(Long configId);

    /**
     * 设置为默认配置
     */
    void setAsDefault(Long configId);

    /**
     * 脱敏显示API密钥
     */
    String maskApiKey(String apiKey);
}
