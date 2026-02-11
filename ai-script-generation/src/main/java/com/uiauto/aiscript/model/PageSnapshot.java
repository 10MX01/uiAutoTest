package com.uiauto.aiscript.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 页面快照
 */
@Data
@Builder
public class PageSnapshot {

    /**
     * 页面URL
     */
    private String url;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 快照哈希值
     */
    private String hash;

    /**
     * 可交互元素列表
     */
    private List<InteractiveElement> elements;

    /**
     * 截图时间
     */
    private long timestamp;
}
