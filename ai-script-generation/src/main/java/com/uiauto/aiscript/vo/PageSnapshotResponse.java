package com.uiauto.aiscript.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 页面快照响应VO
 */
@Data
@Builder
public class PageSnapshotResponse {

    private String url;
    private String title;
    private String hash;
    private long timestamp;
    private int elementCount;
    private List<Object> elements;
}
