package com.uiauto.aiscript.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AI服务配置创建请求DTO
 */
@Data
public class AIServiceConfigCreateRequest {

    @NotBlank(message = "服务提供商不能为空")
    private String provider;

    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    private String apiEndpoint;

    private String customHeaders;

    private Boolean isDefault;

    private Integer maxTokens;

    private BigDecimal temperature;

    private Integer timeoutSeconds;
}
