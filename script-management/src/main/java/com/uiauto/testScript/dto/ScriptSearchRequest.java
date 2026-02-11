package com.uiauto.testScript.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 脚本搜索请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptSearchRequest {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 脚本分类
     */
    private String category;

    /**
     * 脚本状态
     */
    private String status;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortField = "createdTime";

    /**
     * 排序方向：ASC/DESC
     */
    @Builder.Default
    private String sortOrder = "DESC";
}
