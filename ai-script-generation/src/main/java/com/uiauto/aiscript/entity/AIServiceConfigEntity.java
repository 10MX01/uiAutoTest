package com.uiauto.aiscript.entity;

import com.uiauto.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * AI服务配置实体
 * 存储AI服务的配置信息，支持多种AI服务接入
 */
@Entity
@Table(name = "ai_service_configs")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIServiceConfigEntity extends BaseEntity {

    /**
     * AI服务提供商：openai/anthropic/custom
     */
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    /**
     * 模型名称：gpt-4/gpt-3.5-turbo/claude-3等
     */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    /**
     * API密钥（加密存储）
     */
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    /**
     * API端点（自定义服务时使用）
     */
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    /**
     * 自定义请求头（JSON格式）
     */
    @Column(name = "custom_headers", columnDefinition = "JSON")
    private String customHeaders;

    /**
     * 是否为默认服务
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * 状态：ACTIVE/INACTIVE
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 最大token数
     */
    @Column(name = "max_tokens")
    @Builder.Default
    private Integer maxTokens = 2000;

    /**
     * 温度参数（0.0-1.0）
     */
    @Column(name = "temperature", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal temperature = new BigDecimal("0.70");

    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    @Builder.Default
    private Integer timeoutSeconds = 30;
}
