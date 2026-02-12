package com.uiauto.common.model;

/**
 * 测试步骤操作类型枚举
 */
public enum TestStepAction {
    /**
     * 点击操作（可能导致页面跳转）
     */
    CLICK,

    /**
     * 导航操作（必然导致页面跳转）
     */
    NAVIGATE,

    /**
     * 表单提交（通常导致页面跳转）
     */
    SUBMIT,

    /**
     * 填写表单（不会跳转）
     */
    FILL,

    /**
     * 选择下拉框（不会跳转）
     */
    SELECT,

    /**
     * 等待操作（不会跳转）
     */
    WAIT,

    /**
     * 验证操作（不会跳转）
     */
    VERIFY,

    /**
     * 断言元素存在（不会跳转）
     */
    ASSERT,

    /**
     * 断言元素不存在（不会跳转）
     * 用于验证错误提示不出现，或某个元素被移除
     */
    ASSERT_NOT_EXISTS,

    /**
     * 断言文本存在（不会跳转）
     * 用于验证页面包含某个文本（如成功消息、错误提示、创建的数据）
     */
    ASSERT_TEXT,

    /**
     * 断言文本不存在（不会跳转）
     * 用于验证页面不包含某个文本
     */
    ASSERT_NOT_TEXT,

    /**
     * 断言元素可见（不会跳转）
     * 验证元素不仅存在，而且可见
     */
    ASSERT_VISIBLE,

    /**
     * 断言元素隐藏（不会跳转）
     * 验证元素虽然存在但不可见
     */
    ASSERT_HIDDEN,

    /**
     * URL断言（不会跳转）
     */
    ASSERT_URL;

    /**
     * 判断是否为可能导致页面跳转的操作
     */
    public boolean isNavigation() {
        return this == CLICK || this == NAVIGATE || this == SUBMIT;
    }

    /**
     * 判断是否为验证类操作（需要等待异步结果）
     */
    public boolean isAssertion() {
        return this == ASSERT || this == ASSERT_NOT_EXISTS ||
               this == ASSERT_TEXT || this == ASSERT_NOT_TEXT ||
               this == ASSERT_VISIBLE || this == ASSERT_HIDDEN ||
               this == ASSERT_URL;
    }

    /**
     * 从字符串转换为操作类型
     */
    public static TestStepAction fromString(String action) {
        if (action == null || action.trim().isEmpty()) {
            return null;
        }
        try {
            return TestStepAction.valueOf(action.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // 如果无法匹配枚举，返回null
            return null;
        }
    }
}