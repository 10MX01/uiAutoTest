package com.uiauto.aiscript.service.impl;

import com.uiauto.aiscript.dto.AIServiceConfigCreateRequest;
import com.uiauto.aiscript.dto.AIServiceConfigUpdateRequest;
import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import com.uiauto.aiscript.repository.AIServiceConfigRepository;
import com.uiauto.aiscript.service.AIServiceConfigService;
import com.uiauto.aiscript.service.LLMService;
import com.uiauto.aiscript.vo.ConnectionTestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI服务配置Service实现
 */
@Slf4j
@Service
public class AIServiceConfigServiceImpl implements AIServiceConfigService {

    @Autowired
    private AIServiceConfigRepository configRepository;

    @Autowired
    private LLMService llmService;

    @Override
    @Transactional
    public AIServiceConfigEntity createConfig(AIServiceConfigCreateRequest request, Long userId) {
        // 检查是否设置为默认
        if (request.getIsDefault() != null && request.getIsDefault()) {
            // 取消其他默认配置
            configRepository.findByIsDefaultTrue().ifPresent(config -> {
                config.setIsDefault(false);
                configRepository.save(config);
            });
        }

        AIServiceConfigEntity entity = AIServiceConfigEntity.builder()
                .provider(request.getProvider())
                .modelName(request.getModelName())
                .apiKey(request.getApiKey())
                .apiEndpoint(request.getApiEndpoint())
                .customHeaders(request.getCustomHeaders())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .status("ACTIVE")
                .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 2000)
                .temperature(request.getTemperature())
                .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 30)
                .build();

        // 设置父类字段
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        return configRepository.save(entity);
    }

    @Override
    @Transactional
    public AIServiceConfigEntity updateConfig(AIServiceConfigUpdateRequest request, Long userId) {
        AIServiceConfigEntity entity = getConfigById(request.getUniqueId());

        // 如果设置为默认，取消其他默认配置
        if (request.getIsDefault() != null && request.getIsDefault() && !entity.getIsDefault()) {
            configRepository.findByIsDefaultTrue().ifPresent(config -> {
                config.setIsDefault(false);
                configRepository.save(config);
            });
        }

        if (request.getProvider() != null) {
            entity.setProvider(request.getProvider());
        }
        if (request.getModelName() != null) {
            entity.setModelName(request.getModelName());
        }
        if (request.getApiKey() != null) {
            entity.setApiKey(request.getApiKey());
        }
        if (request.getApiEndpoint() != null) {
            entity.setApiEndpoint(request.getApiEndpoint());
        }
        if (request.getCustomHeaders() != null) {
            entity.setCustomHeaders(request.getCustomHeaders());
        }
        if (request.getIsDefault() != null) {
            entity.setIsDefault(request.getIsDefault());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getMaxTokens() != null) {
            entity.setMaxTokens(request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            entity.setTemperature(request.getTemperature());
        }
        if (request.getTimeoutSeconds() != null) {
            entity.setTimeoutSeconds(request.getTimeoutSeconds());
        }

        entity.setUpdatedBy(userId);
        entity.setUpdatedTime(LocalDateTime.now());

        return configRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteConfig(Long configId) {
        configRepository.deleteById(configId);
    }

    @Override
    public AIServiceConfigEntity getConfigById(Long configId) {
        return configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("AI服务配置不存在: " + configId));
    }

    @Override
    public List<AIServiceConfigEntity> listAllConfigs() {
        return configRepository.findAll();
    }

    @Override
    public List<AIServiceConfigEntity> listActiveConfigs() {
        return configRepository.findByStatus("ACTIVE");
    }

    @Override
    public AIServiceConfigEntity getDefaultConfig() {
        return configRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new RuntimeException("未配置默认AI服务"));
    }

    @Override
    public ConnectionTestResponse testConnection(Long configId) {
        AIServiceConfigEntity config = getConfigById(configId);
        long startTime = System.currentTimeMillis();

        try {
            boolean success = llmService.testConnection(config);
            long duration = System.currentTimeMillis() - startTime;

            return ConnectionTestResponse.builder()
                    .success(success)
                    .message(success ? "连接成功" : "连接失败")
                    .duration(duration)
                    .modelName(config.getModelName())
                    .provider(config.getProvider())
                    .build();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return ConnectionTestResponse.builder()
                    .success(false)
                    .message("连接异常: " + e.getMessage())
                    .duration(duration)
                    .modelName(config.getModelName())
                    .provider(config.getProvider())
                    .build();
        }
    }

    @Override
    @Transactional
    public void setAsDefault(Long configId) {
        // 取消其他默认配置
        configRepository.findByIsDefaultTrue().ifPresent(config -> {
            config.setIsDefault(false);
            configRepository.save(config);
        });

        // 设置新的默认配置
        AIServiceConfigEntity entity = getConfigById(configId);
        entity.setIsDefault(true);
        configRepository.save(entity);
    }

    @Override
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
