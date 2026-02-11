package com.uiauto.aiscript.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 连接测试响应VO
 */
@Data
@Builder
public class ConnectionTestResponse {

    private boolean success;
    private String message;
    private long duration; // 毫秒
    private String modelName;
    private String provider;
}
