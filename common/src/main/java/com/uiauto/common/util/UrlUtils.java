package com.uiauto.common.util;

/**
 * URL解析工具类
 */
public class UrlUtils {

    /**
     * 解析最终URL
     * 规则：
     * 1. 如果testCaseUrl以http开头，直接使用
     * 2. 如果testCaseUrl以/开头，拼接projectTargetUrl + testCaseUrl
     * 3. 如果testCaseUrl为空，使用projectTargetUrl
     *
     * @param projectTargetUrl 项目目标URL
     * @param testCaseUrl 测试用例URL（可为空）
     * @param overrideUrl 覆盖URL（执行时传入，可选）
     * @return 最终URL
     */
    public static String resolveUrl(String projectTargetUrl, String testCaseUrl, String overrideUrl) {
        // 优先使用覆盖URL
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            return overrideUrl;
        }

        // 如果测试用例指定了绝对URL
        if (testCaseUrl != null && !testCaseUrl.isEmpty() && isAbsoluteUrl(testCaseUrl)) {
            return testCaseUrl;
        }

        // 如果测试用例指定了相对路径
        if (testCaseUrl != null && !testCaseUrl.isEmpty() && testCaseUrl.startsWith("/")) {
            if (projectTargetUrl != null && !projectTargetUrl.isEmpty()) {
                return removeTrailingSlash(projectTargetUrl) + testCaseUrl;
            }
            return testCaseUrl;
        }

        // 使用项目URL
        if (projectTargetUrl != null && !projectTargetUrl.isEmpty()) {
            return projectTargetUrl;
        }

        // 使用测试用例URL（如果存在）
        if (testCaseUrl != null && !testCaseUrl.isEmpty()) {
            return testCaseUrl;
        }

        throw new IllegalArgumentException("无法解析URL：项目URL和测试用例URL都为空");
    }

    /**
     * 判断是否为绝对URL
     */
    private static boolean isAbsoluteUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * 移除URL末尾的斜杠
     */
    private static String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * 验证URL格式
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
