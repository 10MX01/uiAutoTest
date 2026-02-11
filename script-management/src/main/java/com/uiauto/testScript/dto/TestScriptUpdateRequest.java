package com.uiauto.testScript.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新测试脚本请求DTO（重构版）
 *
 * 核心变更：
 * 1. 移除脚本内容编辑功能（scriptContent、changeSummary）
 * 2. 仅允许更新基本信息：scriptName、scriptDescription、category
 * 3. 不允许修改关联关系（已强制关联测试用例）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScriptUpdateRequest {

    /**
     * 脚本唯一ID
     */
    @NotNull(message = "脚本ID不能为空")
    private Long uniqueId;

    /**
     * 脚本名称（可选）
     */
    private String scriptName;

    /**
     * 脚本描述（可选）
     */
    private String scriptDescription;

    /**
     * 脚本分类（可选）
     */
    private String category;

    // ⚠️ 注意：不允许修改以下字段
    // - scriptContent：不支持编辑脚本内容
    // - testCaseId：已强制关联，不可修改
    // - generationMethod：生成方式不可修改
    // - enabled：使用专门的updateEnabledStatus方法
    // - AI相关字段：不允许手动修改
}
