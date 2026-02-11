package com.uiauto.aiscript.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 解析步骤请求DTO
 */
@Data
public class ParseStepsRequest {

    @NotBlank(message = "测试步骤不能为空")
    private String stepsText;
}
