package com.uiauto.aiscript.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 页面可交互元素
 */
@Data
@Builder
public class InteractiveElement {

    /**
     * 元素标签名
     */
    private String tagName;

    /**
     * id属性
     */
    private String id;

    /**
     * name属性
     */
    private String name;

    /**
     * class属性
     */
    private String className;

    /**
     * type属性（用于input）
     */
    private String type;

    /**
     * placeholder属性
     */
    private String placeholder;

    /**
     * text内容
     */
    private String text;

    /**
     * data-testid属性
     */
    private String dataTestId;

    /**
     * data-test属性
     */
    private String dataTest;

    /**
     * data-automation属性
     */
    private String dataAutomation;

    /**
     * aria-label属性
     */
    private String ariaLabel;

    /**
     * role属性
     */
    private String role;

    /**
     * 选择器列表（按优先级排序）
     */
    private List<String> selectors;
}
