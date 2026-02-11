package com.uiauto.aiscript.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AI服务配置更新请求DTO
 */
@Data
public class AIServiceConfigUpdateRequest {

    @NotNull(message = "配置ID不能为空")
    private Long uniqueId;

    private String provider;

    private String modelName;

    private String apiKey;

    private String apiEndpoint;

    private String customHeaders;

    private Boolean isDefault;

    private String status;

    private Integer maxTokens;

    private BigDecimal temperature;

    private Integer timeoutSeconds;
}
