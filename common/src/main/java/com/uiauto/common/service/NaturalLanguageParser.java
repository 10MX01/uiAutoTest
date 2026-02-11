package com.uiauto.common.service;

import com.uiauto.common.model.TestStep;

import java.util.List;

/**
 * 自然语言解析Service接口
 */
public interface NaturalLanguageParser {

    /**
     * 解析自然语言测试步骤
     *
     * @param stepsText 自然语言测试步骤
     * @return 结构化的测试步骤列表
     */
    List<TestStep> parseSteps(String stepsText) throws Exception;

    /**
     * 批量解析测试步骤
     *
     * @param stepsTextList 多个自然语言测试步骤
     * @return 解析结果列表
     */
    List<ParseResult> parseBatch(List<String> stepsTextList);

    /**
     * 解析结果
     */
    class ParseResult {
        private boolean success;
        private List<TestStep> steps;
        private String errorMessage;

        public ParseResult(boolean success, List<TestStep> steps, String errorMessage) {
            this.success = success;
            this.steps = steps;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public List<TestStep> getSteps() {
            return steps;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
