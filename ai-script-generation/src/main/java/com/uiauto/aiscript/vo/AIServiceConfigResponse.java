package com.uiauto.aiscript.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI服务配置响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIServiceConfigResponse {

    private Long uniqueId;
    private String provider;
    private String modelName;
    private String apiKey; // 脱敏后的密钥
    private String apiEndpoint;
    private String customHeaders;
    private Boolean isDefault;
    private String status;
    private Integer maxTokens;
    private BigDecimal temperature;
    private Integer timeoutSeconds;
    private Long createdBy;
    private Long updatedBy;
    private String createdTime;
    private String updatedTime;
}
