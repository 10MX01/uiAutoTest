package com.uiauto.aiscript.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.uiauto.aiscript.model.InteractiveElement;
import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.aiscript.service.PageSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 页面快照Service实现
 */
@Slf4j
@Service
public class PageSnapshotServiceImpl implements PageSnapshotService {

    private final Gson gson = new Gson();

    @Override
    public PageSnapshot captureSnapshot(String url) throws Exception {
        Playwright playwright = null;
        Browser browser = null;
        Page page = null;

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            page = browser.newPage();

            // 导航到目标页面
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            // 提取页面信息
            String pageTitle = page.title();
            String pageUrl = page.url();

            // 提取可交互元素
            List<InteractiveElement> elements = extractInteractiveElements(page);

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
            log.error("捕获页面快照失败: {}", url, e);
            throw new Exception("捕获页面快照失败: " + e.getMessage(), e);
        } finally {
            if (page != null) {
                try { page.close(); } catch (Exception e) { log.error("关闭页面失败", e); }
            }
            if (browser != null) {
                try { browser.close(); } catch (Exception e) { log.error("关闭浏览器失败", e); }
            }
            if (playwright != null) {
                try { playwright.close(); } catch (Exception e) { log.error("关闭Playwright失败", e); }
            }
        }
    }

    @Override
    public PageSnapshot fromJson(String snapshotJson) {
        try {
            return gson.fromJson(snapshotJson, PageSnapshot.class);
        } catch (Exception e) {
            log.error("从JSON构建快照失败", e);
            throw new RuntimeException("快照JSON格式错误", e);
        }
    }

    @Override
    public String toJson(PageSnapshot snapshot) {
        return gson.toJson(snapshot);
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

            log.info("提取到 {} 个可交互元素", elements.size());

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
