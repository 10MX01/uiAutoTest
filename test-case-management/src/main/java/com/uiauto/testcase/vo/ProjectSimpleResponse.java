package com.uiauto.testcase.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目简单信息响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSimpleResponse {

    /**
     * 唯一标识ID
     */
    private Long uniqueId;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目代码
     */
    private String code;
}
