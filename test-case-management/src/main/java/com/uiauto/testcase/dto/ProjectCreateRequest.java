package com.uiauto.testcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建项目请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 255, message = "项目名称长度不能超过255个字符")
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目代码（唯一）
     */
    @NotBlank(message = "项目代码不能为空")
    @Size(max = 50, message = "项目代码长度不能超过50个字符")
    private String code;

    /**
     * 目标URL（测试环境地址）
     */
    @Size(max = 500, message = "目标URL长度不能超过500个字符")
    private String targetUrl;

    /**
     * 基础URL（可选）
     */
    @Size(max = 500, message = "基础URL长度不能超过500个字符")
    private String baseUrl;
}
