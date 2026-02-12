package com.uiauto.aiscript.service.impl;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import com.uiauto.aiscript.model.InteractiveElement;
import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStepWithSelectors;
import com.uiauto.common.model.VariableResolver;
import com.uiauto.aiscript.service.PlaywrightExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Playwright执行器实现
 */
@Slf4j
@Service
public class PlaywrightExecutorImpl implements PlaywrightExecutor {

    /**
     * Session存储
     */
    private final Map<String, BrowserSession> sessions = new ConcurrentHashMap<>();

    /**
     * 浏览器会话
     */
    private static class BrowserSession {
        Playwright playwright;
        Browser browser;
        Page page;
        ExecutionConfig config;
        boolean isFirstNavigation = true;
        String baseUrl;  // 保存baseURL，用于处理相对路径
        VariableResolver variableResolver = new VariableResolver();  // 变量解析器
    }

    @Override
    public String createSession(ExecutionConfig config) {
        String sessionId = UUID.randomUUID().toString();

        try {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(config.isHeadless())
            );

            // 创建浏览器上下文，忽略HTTPS错误（支持自签名证书）
            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setIgnoreHTTPSErrors(true)
            );

            Page page = context.newPage();

            BrowserSession session = new BrowserSession();
            session.playwright = playwright;
            session.browser = browser;
            session.page = page;
            session.config = config;

            sessions.put(sessionId, session);
            log.info("创建浏览器会话: {} (忽略HTTPS错误)", sessionId);

            return sessionId;

        } catch (Exception e) {
            log.error("创建会话失败", e);
            throw new RuntimeException("创建浏览器会话失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ExecutionResult executeInSession(String sessionId, String url, List<TestStepWithSelectors> steps) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        long startTime = System.currentTimeMillis();
        List<StepExecutionResult> stepResults = new ArrayList<>();
        List<String> screenshots = new ArrayList<>();
        boolean overallSuccess = true;
        String errorMessage = null;

        try {
            // 第一次执行时导航到URL
            if (session.isFirstNavigation && url != null) {
                session.page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
                session.isFirstNavigation = false;

                // 保存baseUrl（用于处理相对路径）
                session.baseUrl = extractBaseUrl(url);
                log.info("导航到页面: {}, baseUrl: {}", url, session.baseUrl);
            }

            // 执行每个测试步骤
            for (TestStepWithSelectors step : steps) {
                StepExecutionResult stepResult = executeStep(session.page, step, session.config, session.baseUrl, session.variableResolver);
                stepResults.add(stepResult);

                if (!stepResult.isSuccess()) {
                    overallSuccess = false;
                    errorMessage = "步骤 " + step.getStepNumber() + " 失败: " + stepResult.getErrorMessage();

                    // 失败时截图
                    if (session.config.isScreenshotOnFailure()) {
                        String screenshot = captureScreenshot(session.page);
                        if (screenshot != null) {
                            screenshots.add(screenshot);
                        }
                    }

                    // 停止执行后续步骤
                    break;
                }

                // 成功后截图（如果配置了）
                if (session.config.isScreenshot()) {
                    String screenshot = captureScreenshot(session.page);
                    if (screenshot != null) {
                        screenshots.add(screenshot);
                    }
                }
            }

        } catch (Exception e) {
            log.error("执行测试脚本失败", e);
            overallSuccess = false;
            errorMessage = "执行异常: " + e.getMessage();

        }

        long duration = System.currentTimeMillis() - startTime;
        String status = overallSuccess ? "SUCCESS" : "FAILED";

        return new ExecutionResult(overallSuccess, status, duration, stepResults, screenshots, errorMessage);
    }

    @Override
    public String getCurrentUrl(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }
        return session.page.url();
    }

    @Override
    public String capturePageSnapshot(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }
        return session.page.content();
    }

    @Override
    public PageSnapshot capturePageSnapshotWithElements(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        try {
            Page page = session.page;

            log.info("捕获页面快照 - 当前URL: {}", page.url());

            // 等待页面完全加载
            log.debug("等待页面加载完成...");
            try {
                page.waitForLoadState();
            } catch (Exception e) {
                log.debug("waitForLoadState失败，继续: {}", e.getMessage());
            }

            // 等待网络空闲（等待所有网络请求完成）
            log.debug("等待网络空闲...");
            try {
                page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30000));
            } catch (Exception e) {
                log.debug("waitForLoadState NETWORKIDLE超时，继续: {}", e.getMessage());
            }

            // 额外等待1秒，确保JavaScript执行完成
            try {
                page.waitForTimeout(1000);
            } catch (Exception e) {
                // 忽略等待超时
            }

            // 提取页面信息
            String pageTitle = page.title();
            String pageUrl = page.url();

            log.info("当前页面: title={}, url={}", pageTitle, pageUrl);

            // 提取可交互元素
            List<InteractiveElement> elements = extractInteractiveElements(page);
            log.info("提取到 {} 个可交互元素", elements.size());

            // 生成哈希值
            String hash = generateSnapshotHash(pageUrl, elements);

            return PageSnapshot.builder()
                    .url(pageUrl)
                    .title(pageTitle)
                    .hash(hash)
                    .elements(elements)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("捕获页面快照失败: sessionId={}", sessionId, e);
            throw new RuntimeException("捕获页面快照失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导航到指定URL
     */
    public void navigate(String sessionId, String url) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        try {
            log.info("导航到URL: {}", url);
            session.page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
            session.isFirstNavigation = false;
            // 同时设置baseUrl，用于后续处理相对路径
            session.baseUrl = extractBaseUrl(url);
            log.info("设置baseUrl: {}", session.baseUrl);
        } catch (Exception e) {
            log.error("导航失败: url={}", url, e);
            throw new RuntimeException("导航失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void closeSession(String sessionId) {
        BrowserSession session = sessions.remove(sessionId);
        if (session != null) {
            try {
                if (session.page != null) {
                    session.page.close();
                }
            } catch (Exception e) {
                log.warn("关闭页面失败", e);
            }
            try {
                // 关闭浏览器上下文（page的所有者）
                if (session.page != null && session.page.context() != null) {
                    session.page.context().close();
                }
            } catch (Exception e) {
                log.warn("关闭浏览器上下文失败", e);
            }
            try {
                if (session.browser != null) {
                    session.browser.close();
                }
            } catch (Exception e) {
                log.warn("关闭浏览器失败", e);
            }
            try {
                if (session.playwright != null) {
                    session.playwright.close();
                }
            } catch (Exception e) {
                log.warn("关闭Playwright失败", e);
            }
            log.info("关闭浏览器会话: {}", sessionId);
        }
    }

    @Override
    public ExecutionResult execute(String url, List<TestStepWithSelectors> steps, ExecutionConfig config) {
        long startTime = System.currentTimeMillis();
        List<StepExecutionResult> stepResults = new ArrayList<>();
        List<String> screenshots = new ArrayList<>();
        boolean overallSuccess = true;
        String errorMessage = null;

        Playwright playwright = null;
        Browser browser = null;
        BrowserContext context = null;
        Page page = null;

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(config.isHeadless())
            );

            // 创建浏览器上下文，忽略HTTPS错误（支持自签名证书）
            context = browser.newContext(
                    new Browser.NewContextOptions().setIgnoreHTTPSErrors(true)
            );

            page = context.newPage();

            // 导航到目标页面
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
            log.info("导航到页面: {}", url);

            // 提取baseUrl（用于处理相对路径）
            String baseUrl = extractBaseUrl(url);

            // 执行每个测试步骤
            for (TestStepWithSelectors step : steps) {
                StepExecutionResult stepResult = executeStep(page, step, config, baseUrl);
                stepResults.add(stepResult);

                if (!stepResult.isSuccess()) {
                    overallSuccess = false;
                    errorMessage = "步骤 " + step.getStepNumber() + " 失败: " + stepResult.getErrorMessage();

                    // 失败时截图
                    if (config.isScreenshotOnFailure()) {
                        String screenshot = captureScreenshot(page);
                        if (screenshot != null) {
                            screenshots.add(screenshot);
                        }
                    }

                    // 停止执行后续步骤
                    break;
                }

                // 成功后截图（如果配置了）
                if (config.isScreenshot()) {
                    String screenshot = captureScreenshot(page);
                    if (screenshot != null) {
                        screenshots.add(screenshot);
                    }
                }
            }

        } catch (Exception e) {
            log.error("执行测试脚本失败", e);
            overallSuccess = false;
            errorMessage = "执行异常: " + e.getMessage();

        } finally {
            if (page != null) {
                try { page.close(); } catch (Exception e) { log.error("关闭页面失败", e); }
            }
            if (context != null) {
                try { context.close(); } catch (Exception e) { log.error("关闭浏览器上下文失败", e); }
            }
            if (browser != null) {
                try { browser.close(); } catch (Exception e) { log.error("关闭浏览器失败", e); }
            }
            if (playwright != null) {
                try { playwright.close(); } catch (Exception e) { log.error("关闭Playwright失败", e); }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        String status = overallSuccess ? "SUCCESS" : "FAILED";

        return new ExecutionResult(overallSuccess, status, duration, stepResults, screenshots, errorMessage);
    }

    /**
     * 执行单个步骤（带变量解析器）
     */
    private StepExecutionResult executeStep(Page page, TestStepWithSelectors step, ExecutionConfig config, String baseUrl, VariableResolver variableResolver) {
        long startTime = System.currentTimeMillis();

        try {
            String action = step.getAction();
            String selector = step.getSelector();
            String value = step.getValue();
            List<String> fallbackSelectors = step.getFallbackSelectors();

            // 解析变量（value 和 selector 中的 ${...} 占位符）
            String resolvedValue = variableResolver.resolve(value);
            String resolvedSelector = variableResolver.resolve(selector);

            log.info("【执行步骤】步骤{}: action={}, selector={}, value={}",
                    step.getStepNumber(), action, selector, value);
            if (!resolvedValue.equals(value)) {
                log.info("【变量解析】value: {} -> {}", value, resolvedValue);
            }
            if (!resolvedSelector.equals(selector)) {
                log.info("【变量解析】selector: {} -> {}", selector, resolvedSelector);
            }

            switch (action.toLowerCase()) {
                case "navigate":
                    // 处理相对路径：如果value不是完整URL，则拼接baseUrl
                    String targetUrl = resolvedValue;
                    if (resolvedValue != null && !resolvedValue.isEmpty() && !resolvedValue.startsWith("http://") && !resolvedValue.startsWith("https://")) {
                        // 如果没有传入baseUrl，尝试从当前页面URL提取
                        if (baseUrl == null || baseUrl.isEmpty()) {
                            String currentUrl = page.url();
                            if (currentUrl != null && !currentUrl.isEmpty()) {
                                baseUrl = extractBaseUrl(currentUrl);
                                log.info("从当前页面URL提取baseUrl: {} (currentUrl: {})", baseUrl, currentUrl);
                            }
                        }

                        if (baseUrl != null && !baseUrl.isEmpty()) {
                            targetUrl = baseUrl + (resolvedValue.startsWith("/") ? "" : "/") + resolvedValue;
                            log.info("转换相对路径: {} -> {}", resolvedValue, targetUrl);
                        } else {
                            throw new RuntimeException("无法导航到相对路径 '" + resolvedValue + "'，因为baseUrl未设置且无法从当前页面提取。请确保先导航到一个完整URL。");
                        }
                    }
                    log.info("【执行】正在导航到: {}", targetUrl);
                    page.navigate(targetUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
                    String finalUrl = page.url();
                    log.info("【执行】导航完成，当前页面: {}", finalUrl);
                    break;

                case "click":
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        log.info("【执行】正在点击元素: {}", resolvedSelector);
                        page.click(resolvedSelector, new Page.ClickOptions().setTimeout(config.getTimeout()));
                        log.info("【执行】点击成功");
                    } else {
                        throw new RuntimeException("点击操作缺少选择器");
                    }
                    break;

                case "fill":
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        log.info("【执行】正在填充输入框: selector={}, value={}", resolvedSelector, resolvedValue);
                        page.fill(resolvedSelector, resolvedValue);
                        log.info("【执行】填充成功");

                        // 变量提取
                        if (step.getExtractAs() != null && !step.getExtractAs().isEmpty()) {
                            variableResolver.extractVariable(step.getExtractAs(), resolvedValue);
                        }
                    } else {
                        throw new RuntimeException("填充操作缺少选择器");
                    }
                    break;

                case "assert":
                    log.info("【执行】正在断言元素存在: {}", resolvedSelector);
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        page.waitForSelector(resolvedSelector, new Page.WaitForSelectorOptions().setTimeout(config.getTimeout()));
                        log.info("【执行】断言成功，元素存在");
                    } else {
                        throw new RuntimeException("断言操作缺少选择器");
                    }
                    break;

                case "assert_not_exists":
                    log.info("【执行】正在断言元素不存在: {}", resolvedSelector);
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        try {
                            page.waitForSelector(resolvedSelector, new Page.WaitForSelectorOptions().setTimeout(3000));
                            throw new RuntimeException("元素不应该存在，但找到了: " + resolvedSelector);
                        } catch (Exception e) {
                            if (e.getMessage().contains("Timeout")) {
                                log.info("【执行】断言成功，元素不存在");
                            } else {
                                throw e;
                            }
                        }
                    } else {
                        throw new RuntimeException("断言操作缺少选择器");
                    }
                    break;

                case "assert_text":
                    log.info("【执行】正在断言页面包含文本: {}", resolvedValue);
                    if (resolvedValue != null && !resolvedValue.isEmpty()) {
                        // 等待一下让异步内容出现
                        try {
                            page.waitForTimeout(500);
                        } catch (Exception e) {
                            // 忽略
                        }

                        String pageContent = page.content();
                        if (!pageContent.contains(resolvedValue)) {
                            throw new RuntimeException("页面不包含文本: " + resolvedValue);
                        }
                        log.info("【执行】断言成功，页面包含文本");
                    } else {
                        throw new RuntimeException("断言操作缺少value");
                    }
                    break;

                case "assert_not_text":
                    log.info("【执行】正在断言页面不包含文本: {}", resolvedValue);
                    if (resolvedValue != null && !resolvedValue.isEmpty()) {
                        // 等待一下让异步内容出现
                        try {
                            page.waitForTimeout(500);
                        } catch (Exception e) {
                            // 忽略
                        }

                        String pageContent = page.content();
                        if (pageContent.contains(resolvedValue)) {
                            throw new RuntimeException("页面不应该包含文本，但找到了: " + resolvedValue);
                        }
                        log.info("【执行】断言成功，页面不包含文本");
                    } else {
                        throw new RuntimeException("断言操作缺少value");
                    }
                    break;

                case "assert_visible":
                    log.info("【执行】正在断言元素可见: {}", resolvedSelector);
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        page.waitForSelector(resolvedSelector, new Page.WaitForSelectorOptions().setTimeout(config.getTimeout()));
                        // 检查元素是否可见
                        Boolean isVisible = page.locator(resolvedSelector).isVisible();
                        if (!isVisible) {
                            throw new RuntimeException("元素存在但不可见: " + resolvedSelector);
                        }
                        log.info("【执行】断言成功，元素可见");
                    } else {
                        throw new RuntimeException("断言操作缺少选择器");
                    }
                    break;

                case "assert_hidden":
                    log.info("【执行】正在断言元素隐藏: {}", resolvedSelector);
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        page.waitForSelector(resolvedSelector, new Page.WaitForSelectorOptions().setTimeout(3000));
                        // 检查元素是否隐藏
                        Boolean isVisible = page.locator(resolvedSelector).isVisible();
                        if (isVisible) {
                            throw new RuntimeException("元素应该是隐藏的，但可见: " + resolvedSelector);
                        }
                        log.info("【执行】断言成功，元素隐藏");
                    } else {
                        throw new RuntimeException("断言操作缺少选择器");
                    }
                    break;

                case "assert_url":
                    log.info("【执行】正在断言URL包含: {}", resolvedValue);
                    if (resolvedValue != null && !resolvedValue.isEmpty()) {
                        String currentUrl = page.url();
                        log.info("【执行】当前URL: {}", currentUrl);
                        if (!currentUrl.contains(resolvedValue)) {
                            throw new RuntimeException("URL断言失败: 当前URL " + currentUrl + " 不包含 " + resolvedValue);
                        }
                        log.info("【执行】URL断言成功");
                    }
                    break;

                case "wait":
                    log.info("【执行】正在等待: {}ms", resolvedValue);
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        page.waitForSelector(resolvedSelector, new Page.WaitForSelectorOptions().setTimeout(config.getTimeout()));
                    } else {
                        long waitTime = resolvedValue != null ? Long.parseLong(resolvedValue) : 1000;
                        page.waitForTimeout(waitTime);
                        log.info("【执行】等待完成");
                    }
                    break;

                case "select":
                    log.info("【执行】正在选择下拉选项: selector={}, value={}", resolvedSelector, resolvedValue);
                    if (resolvedSelector != null && !resolvedSelector.isEmpty()) {
                        selectOption(page, resolvedSelector, resolvedValue, config.getTimeout());
                        log.info("【执行】选择成功");
                    } else {
                        throw new RuntimeException("选择操作缺少选择器");
                    }
                    break;

                default:
                    throw new RuntimeException("不支持的操作类型: " + action);
            }

            long duration = System.currentTimeMillis() - startTime;
            return new StepExecutionResult(step.getStepNumber(), true, "SUCCESS", duration, null, null);

        } catch (Exception e) {
            log.error("步骤执行失败: {}", step.getStepNumber(), e);
            long duration = System.currentTimeMillis() - startTime;
            return new StepExecutionResult(step.getStepNumber(), false, "FAILED", duration, null, e.getMessage());
        }
    }

    /**
     * 执行单个步骤（兼容旧版本，不使用变量解析器）
     */
    private StepExecutionResult executeStep(Page page, TestStepWithSelectors step, ExecutionConfig config, String baseUrl) {
        // 使用空的变量解析器
        return executeStep(page, step, config, baseUrl, new VariableResolver());
    }

    /**
     * 截图
     */
    private String captureScreenshot(Page page) {
        try {
            byte[] screenshot = page.screenshot();
            return Base64.getEncoder().encodeToString(screenshot);
        } catch (Exception e) {
            log.warn("截图失败", e);
            return null;
        }
    }

    /**
     * 提取可交互元素
     */
    private List<InteractiveElement> extractInteractiveElements(Page page) {
        List<InteractiveElement> elements = new ArrayList<>();

        try {
            // 提取表单元素
            elements.addAll(extractElements(page, "input"));
            elements.addAll(extractElements(page, "button"));
            elements.addAll(extractElements(page, "select"));
            elements.addAll(extractElements(page, "textarea"));

            // 提取链接和导航元素
            elements.addAll(extractElements(page, "a"));

            // 提取菜单相关元素（用于Element UI等框架）
            elements.addAll(extractElementsWithFilter(page, "li"));
            elements.addAll(extractElementsWithFilter(page, "div"));

            log.info("【页面快照】提取到 {} 个可交互元素", elements.size());

        } catch (Exception e) {
            log.error("提取可交互元素失败", e);
        }

        return elements;
    }

    /**
     * 提取指定标签的元素（带过滤）
     * 只提取有意义的元素（有类名、ID、data-*属性或role属性）
     */
    private List<InteractiveElement> extractElementsWithFilter(Page page, String tagName) {
        List<InteractiveElement> result = new ArrayList<>();

        try {
            List<ElementHandle> handles = page.querySelectorAll(tagName);

            for (ElementHandle handle : handles) {
                InteractiveElement element = extractElementInfo(handle);
                if (element != null && isMeaningfulElement(element)) {
                    result.add(element);
                }
            }

        } catch (Exception e) {
            log.warn("提取{}元素失败", tagName, e);
        }

        return result;
    }

    /**
     * 判断元素是否有意义（值得包含在快照中）
     */
    private boolean isMeaningfulElement(InteractiveElement element) {
        // 有ID
        if (element.getId() != null && !element.getId().isEmpty()) {
            return true;
        }

        // 有类名（排除纯数字或动态ID类名）
        if (element.getClassName() != null && !element.getClassName().isEmpty()) {
            String className = element.getClassName();
            // 排除动态ID类名（如 el-id-5537-2, ant-123）
            if (!className.matches(".*\\bel-id-\\d+.*") &&
                !className.matches(".*\\bant-\\d+.*") &&
                !className.matches("^\\d+$")) {
                return true;
            }
        }

        // 有data-*测试属性
        if (element.getDataTestId() != null || element.getDataTest() != null ||
            element.getDataAutomation() != null) {
            return true;
        }

        // 有role属性
        if (element.getRole() != null && !element.getRole().isEmpty()) {
            return true;
        }

        // 有aria-label
        if (element.getAriaLabel() != null && !element.getAriaLabel().isEmpty()) {
            return true;
        }

        // li元素通常是有意义的（菜单项）
        if ("li".equals(element.getTagName())) {
            return true;
        }

        return false;
    }

    /**
     * 提取指定标签的元素
     */
    private List<InteractiveElement> extractElements(Page page, String tagName) {
        List<InteractiveElement> result = new ArrayList<>();

        try {
            List<ElementHandle> handles = page.querySelectorAll(tagName);

            for (ElementHandle handle : handles) {
                InteractiveElement element = extractElementInfo(handle);
                if (element != null) {
                    result.add(element);
                }
            }

        } catch (Exception e) {
            log.warn("提取{}元素失败", tagName, e);
        }

        return result;
    }

    /**
     * 提取单个元素的信息
     */
    private InteractiveElement extractElementInfo(ElementHandle handle) {
        try {
            String tagName = handle.evaluate("el => el.tagName.toLowerCase()").toString();
            String id = getAttribute(handle, "id");
            String name = getAttribute(handle, "name");
            String className = getAttribute(handle, "class");
            String type = getAttribute(handle, "type");
            String placeholder = getAttribute(handle, "placeholder");
            String text = handle.evaluate("el => el.textContent").toString().trim();
            String dataTestId = getAttribute(handle, "data-testid");
            String dataTest = getAttribute(handle, "data-test");
            String dataAutomation = getAttribute(handle, "data-automation");
            String ariaLabel = getAttribute(handle, "aria-label");
            String role = getAttribute(handle, "role");

            // 生成选择器
            List<String> selectors = generateSelectors(
                    tagName, id, name, className, type, text, dataTestId, dataTest, dataAutomation, ariaLabel, role
            );

            return InteractiveElement.builder()
                    .tagName(tagName)
                    .id(id)
                    .name(name)
                    .className(className)
                    .type(type)
                    .placeholder(placeholder)
                    .text(text)
                    .dataTestId(dataTestId)
                    .dataTest(dataTest)
                    .dataAutomation(dataAutomation)
                    .ariaLabel(ariaLabel)
                    .role(role)
                    .selectors(selectors)
                    .build();

        } catch (Exception e) {
            log.warn("提取元素信息失败", e);
            return null;
        }
    }

    /**
     * 获取元素属性
     */
    private String getAttribute(ElementHandle handle, String attrName) {
        try {
            Object result = handle.getAttribute(attrName);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成选择器（按优先级排序）
     */
    private List<String> generateSelectors(String tagName, String id, String name, String className,
                                          String type, String text, String dataTestId, String dataTest,
                                          String dataAutomation, String ariaLabel, String role) {

        List<String> selectors = new ArrayList<>();

        // 优先级1: data-testid
        if (dataTestId != null && !dataTestId.isEmpty()) {
            selectors.add(String.format("[data-testid='%s']", dataTestId));
        }

        // 优先级2: id
        if (id != null && !id.isEmpty()) {
            selectors.add(String.format("#%s", id));
        }

        // 优先级3: name
        if (name != null && !name.isEmpty()) {
            selectors.add(String.format("[name='%s']", name));
        }

        // 优先级4: aria-label
        if (ariaLabel != null && !ariaLabel.isEmpty()) {
            selectors.add(String.format("[aria-label='%s']", ariaLabel));
        }

        // 优先级5: text (仅对button和a有效)
        if ((tagName.equals("button") || tagName.equals("a")) && text != null && !text.isEmpty()) {
            selectors.add(String.format("%s:has-text('%s')", tagName, escapeText(text)));
        }

        // 优先级6: data-test
        if (dataTest != null && !dataTest.isEmpty()) {
            selectors.add(String.format("[data-test='%s']", dataTest));
        }

        // 优先级7: data-automation
        if (dataAutomation != null && !dataAutomation.isEmpty()) {
            selectors.add(String.format("[data-automation='%s']", dataAutomation));
        }

        // 优先级8: CSS选择器
        StringBuilder css = new StringBuilder(tagName);
        if (id != null && !id.isEmpty()) {
            css.append("#").append(id);
        } else if (className != null && !className.isEmpty()) {
            String firstClass = className.split(" ")[0];
            css.append(".").append(firstClass);
        }
        if (type != null && !type.isEmpty()) {
            css.append("[type='").append(type).append("']");
        }
        selectors.add(css.toString());

        return selectors;
    }

    /**
     * 转义文本
     */
    private String escapeText(String text) {
        return text.replace("'", "\\'");
    }

    /**
     * 选择下拉选项（支持原生select和自定义下拉框）
     */
    private void selectOption(Page page, String selector, String value, int timeout) throws Exception {
        try {
            // 首先尝试使用原生的 selectOption 方法（针对原生 <select> 元素）
            page.selectOption(selector, value);
            log.info("【select】使用原生selectOption成功");
        } catch (Exception e) {
            // 如果失败，说明可能是自定义下拉框（如 Element UI 的 el-select）
            log.info("【select】元素不是原生select，尝试自定义下拉框操作: {}", e.getMessage());

            // 自定义下拉框操作流程：
            // 1. 点击下拉框打开选项列表
            // 2. 等待选项列表出现
            // 3. 点击要选择的选项

            // 1. 点击下拉框
            log.info("【select】步骤1: 点击下拉框打开选项列表");
            page.click(selector, new Page.ClickOptions().setTimeout(timeout));

            // 2. 等待一下让选项列表展开
            try {
                page.waitForTimeout(500);
            } catch (Exception ex) {
                // 忽略
            }

            // 3. 尝试多种方式查找并点击选项
            String targetValue = value;
            boolean needRandomSelection = (targetValue == null || targetValue.trim().isEmpty());

            // 如果value为空，随机选择一个选项
            if (needRandomSelection) {
                log.info("【select】value为空，将随机选择一个选项");
                // 获取所有可用的下拉选项
                try {
                    List<ElementHandle> options = page.querySelectorAll("li.el-select-dropdown__item");
                    if (options.isEmpty()) {
                        options = page.querySelectorAll(".el-select-dropdown li");
                    }
                    if (!options.isEmpty()) {
                        // 随机选择一个选项
                        java.util.Random random = new java.util.Random();
                        int randomIndex = random.nextInt(options.size());
                        ElementHandle selectedOption = options.get(randomIndex);
                        targetValue = selectedOption.innerText();
                        log.info("【select】随机选择选项 [{}/{}]: {}", randomIndex + 1, options.size(), targetValue);
                    } else {
                        log.warn("【select】未找到可用的下拉选项");
                        return;
                    }
                } catch (Exception ex) {
                    log.warn("【select】获取下拉选项失败: {}", ex.getMessage());
                    return;
                }
            }

            // 方式1: 查找 li 元素（Element UI 选项通常是 li）
            try {
                String optionSelector = String.format("li:has-text('%s')", targetValue);
                log.info("【select】步骤2: 尝试通过 li 查找选项: {}", optionSelector);
                page.waitForSelector(optionSelector, new Page.WaitForSelectorOptions().setTimeout(timeout));
                page.click(optionSelector, new Page.ClickOptions().setTimeout(timeout));
                log.info("【select】通过 li 元素选择成功");
                return;
            } catch (Exception ex) {
                log.info("【select】通过 li 元素选择失败: {}", ex.getMessage());
            }

            // 方式2: 查找 .el-select-dropdown__item 类（Element UI 特有）
            try {
                String optionSelector = String.format(".el-select-dropdown__item:has-text('%s')", targetValue);
                log.info("【select】步骤2: 尝试通过 Element UI 类名查找选项: {}", optionSelector);
                page.waitForSelector(optionSelector, new Page.WaitForSelectorOptions().setTimeout(timeout));
                page.click(optionSelector, new Page.ClickOptions().setTimeout(timeout));
                log.info("【select】通过 Element UI 类名选择成功");
                return;
            } catch (Exception ex) {
                log.info("【select】通过 Element UI 类名选择失败: {}", ex.getMessage());
            }

            // 方式3: 在下拉框的父级容器内查找选项
            try {
                // 假设下拉框后面跟着下拉面板
                String optionSelector = String.format(".el-select-dropdown li:has-text('%s')", targetValue);
                log.info("【select】步骤2: 尝试在下拉面板内查找选项: {}", optionSelector);
                page.waitForSelector(optionSelector, new Page.WaitForSelectorOptions().setTimeout(timeout));
                page.click(optionSelector, new Page.ClickOptions().setTimeout(timeout));
                log.info("【select】通过下拉面板选择成功");
                return;
            } catch (Exception ex) {
                log.info("【select】通过下拉面板选择失败: {}", ex.getMessage());
            }

            // 如果所有方式都失败，抛出异常
            throw new RuntimeException("无法选择下拉选项 '" + targetValue + "'，已尝试所有可能的选择方式");
        }
    }

    /**
     * 从URL中提取baseUrl
     * 例如：https://10.0.108.6:9028/login -> https://10.0.108.6:9028
     */
    private String extractBaseUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String baseUrl = urlObj.getProtocol() + "://" + urlObj.getHost();
            if (urlObj.getPort() != -1) {
                baseUrl += ":" + urlObj.getPort();
            }
            return baseUrl;
        } catch (Exception e) {
            log.warn("提取baseUrl失败: {}", url, e);
            // 如果解析失败，尝试简单处理：去掉路径部分
            int pathIndex = url.indexOf('/', 8); // 跳过 "https://"
            if (pathIndex > 0) {
                return url.substring(0, pathIndex);
            }
            return url;
        }
    }

    /**
     * 生成快照哈希值
     */
    private String generateSnapshotHash(String url, List<InteractiveElement> elements) {
        try {
            String data = url + elements.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().substring(0, 16);

        } catch (Exception e) {
            log.warn("生成哈希值失败", e);
            return String.valueOf(System.currentTimeMillis());
        }
    }
}
