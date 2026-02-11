package com.uiauto.aiscript.service;

import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStepWithSelectors;

import java.util.List;

/**
 * Playwright执行器接口
 */
public interface PlaywrightExecutor {

    /**
     * 创建执行会话（保持浏览器状态用于连续执行）
     *
     * @param config 执行配置
     * @return 会话ID
     */
    String createSession(ExecutionConfig config);

    /**
     * 在会话中执行测试脚本（保持浏览器状态）
     *
     * @param sessionId 会话ID
     * @param url 目标URL（第一次执行时导航到该URL，后续执行时忽略）
     * @param steps 测试步骤
     * @return 执行结果
     */
    ExecutionResult executeInSession(String sessionId, String url, List<TestStepWithSelectors> steps);

    /**
     * 获取会话当前页面的URL
     *
     * @param sessionId 会话ID
     * @return 当前URL
     */
    String getCurrentUrl(String sessionId);

    /**
     * 获取会话当前页面的快照
     *
     * @param sessionId 会话ID
     * @return 页面快照HTML
     */
    String capturePageSnapshot(String sessionId);

    /**
     * 获取会话当前页面的快照（包含元素信息）
     *
     * @param sessionId 会话ID
     * @return 页面快照对象
     */
    PageSnapshot capturePageSnapshotWithElements(String sessionId);

    /**
     * 导航到指定URL
     *
     * @param sessionId 会话ID
     * @param url 目标URL
     */
    void navigate(String sessionId, String url);

    /**
     * 关闭会话并释放资源
     *
     * @param sessionId 会话ID
     */
    void closeSession(String sessionId);

    /**
     * 执行测试脚本（独立执行，不保持状态）
     *
     * @param url 目标URL
     * @param steps 测试步骤
     * @param config 执行配置
     * @return 执行结果
     */
    ExecutionResult execute(String url, List<TestStepWithSelectors> steps, ExecutionConfig config);

    /**
     * 执行配置
     */
    class ExecutionConfig {
        private boolean headless = true;
        private boolean screenshot = true;
        private int timeout = 30000;
        private boolean screenshotOnFailure = true;

        public ExecutionConfig() {}

        public ExecutionConfig(boolean headless, boolean screenshot, int timeout, boolean screenshotOnFailure) {
            this.headless = headless;
            this.screenshot = screenshot;
            this.timeout = timeout;
            this.screenshotOnFailure = screenshotOnFailure;
        }

        public boolean isHeadless() {
            return headless;
        }

        public boolean isScreenshot() {
            return screenshot;
        }

        public int getTimeout() {
            return timeout;
        }

        public boolean isScreenshotOnFailure() {
            return screenshotOnFailure;
        }
    }

    /**
     * 执行结果
     */
    class ExecutionResult {
        private boolean success;
        private String status;
        private long duration;
        private List<StepExecutionResult> stepResults;
        private List<String> screenshots;
        private String errorMessage;

        public ExecutionResult(boolean success, String status, long duration,
                               List<StepExecutionResult> stepResults,
                               List<String> screenshots, String errorMessage) {
            this.success = success;
            this.status = status;
            this.duration = duration;
            this.stepResults = stepResults;
            this.screenshots = screenshots;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getStatus() {
            return status;
        }

        public long getDuration() {
            return duration;
        }

        public List<StepExecutionResult> getStepResults() {
            return stepResults;
        }

        public List<String> getScreenshots() {
            return screenshots;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 步骤执行结果
     */
    class StepExecutionResult {
        private int stepNumber;
        private boolean success;
        private String status;
        private long duration;
        private String screenshot;
        private String errorMessage;

        public StepExecutionResult(int stepNumber, boolean success, String status,
                                  long duration, String screenshot, String errorMessage) {
            this.stepNumber = stepNumber;
            this.success = success;
            this.status = status;
            this.duration = duration;
            this.screenshot = screenshot;
            this.errorMessage = errorMessage;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getStatus() {
            return status;
        }

        public long getDuration() {
            return duration;
        }

        public String getScreenshot() {
            return screenshot;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
