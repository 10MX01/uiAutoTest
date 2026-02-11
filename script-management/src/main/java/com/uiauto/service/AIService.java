package com.uiauto.service;

import com.uiauto.testcase.entity.TestCaseEntity;

/**
 * AI服务接口（用于生成测试脚本）
 */
public interface AIService {

    /**
     * 根据测试用例生成测试脚本
     *
     * @param testCase 测试用例实体
     * @return 生成的脚本内容
     */
    String generateScript(TestCaseEntity testCase);
}
