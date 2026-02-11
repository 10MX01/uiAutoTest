package com.uiauto.aiscript.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 两阶段生成请求DTO
 */
@Data
public class TwoPhaseGenerateRequest {

    @NotBlank(message = "自然语言描述不能为空")
    private String naturalLanguage;

    @NotBlank(message = "目标URL不能为空")
    private String url;
}
