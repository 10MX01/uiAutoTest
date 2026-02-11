package com.uiauto.aiscript.service;

import com.uiauto.aiscript.model.PageSnapshot;

/**
 * 页面快照Service接口
 */
public interface PageSnapshotService {

    /**
     * 捕获页面快照
     *
     * @param url 页面URL
     * @return 页面快照
     */
    PageSnapshot captureSnapshot(String url) throws Exception;

    /**
     * 从JSON对象构建页面快照
     *
     * @param snapshotJson 快照JSON字符串
     * @return 页面快照对象
     */
    PageSnapshot fromJson(String snapshotJson);

    /**
     * 将页面快照转换为JSON
     *
     * @param snapshot 页面快照
     * @return JSON字符串
     */
    String toJson(PageSnapshot snapshot);
}
