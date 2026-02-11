package com.uiauto.aiscript.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 基于JSON和快照生成脚本请求DTO
 */
@Data
public class GenerateScriptFromJsonRequest {

    @NotBlank(message = "步骤JSON不能为空")
    private String stepsJson;

    @NotBlank(message = "页面快照不能为空")
    private String pageSnapshot;
}
