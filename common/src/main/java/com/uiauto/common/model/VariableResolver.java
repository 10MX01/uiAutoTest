package com.uiauto.common.model;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量解析器
 * 支持运行时变量函数的解析和变量提取
 */
@Slf4j
public class VariableResolver {

    private final Map<String, String> variables = new HashMap<>();
    private final Random random = new Random();

    /**
     * 解析字符串中的变量引用和函数
     * 支持：
     * - @{variableName} - 引用变量
     * - @{random:alphabet:8} - 随机字母
     * - @{random:numeric:6} - 随机数字
     * - @{random:alphanumeric:10} - 随机字母数字
     * - @{random:password} - 随机密码(8-15位，含字母数字特殊字符)
     * - @{random:uuid} - UUID
     * - @{timestamp} - 时间戳
     * - @{date:yyyy-MM-dd} - 格式化日期
     *
     * @param input 输入字符串
     * @return 解析后的字符串
     */
    public String resolve(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 检查是否包含变量占位符
        if (!input.contains("@{")) {
            return input;
        }

        // 匹配 @{...} 格式
        Pattern pattern = Pattern.compile("@\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String expression = matcher.group(1);
            String value = evaluateExpression(expression);
            log.debug("【变量解析】@{{{}}} -> {}", expression, value);
            matcher.appendReplacement(result, value);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 计算表达式值
     */
    private String evaluateExpression(String expression) {
        // 1. 检查是否是已定义的变量引用
        if (variables.containsKey(expression)) {
            return variables.get(expression);
        }

        // 2. 解析函数调用 @{function:params}
        if (expression.contains(":")) {
            String[] parts = expression.split(":", 3);
            String function = parts[0];

            switch (function) {
                case "random":
                    return evaluateRandomFunction(parts);

                case "timestamp":
                    return String.valueOf(System.currentTimeMillis());

                case "date":
                    return evaluateDateFunction(parts);

                default:
                    log.warn("【变量解析】未知函数: {}", function);
                    return "@{" + expression + "}";
            }
        }

        // 3. 未知的表达式，原样返回
        log.warn("【变量解析】无法解析表达式: {}", expression);
        return "@{" + expression + "}";
    }

    /**
     * 解析随机函数
     * @{random:alphabet:8} - 随机字母
     * @{random:numeric:6} - 随机数字
     * @{random:alphanumeric:10} - 随机字母数字
     * @{random:password} - 随机密码(8-15位，含字母数字特殊字符)
     */
    private String evaluateRandomFunction(String[] parts) {
        String type = parts[1];

        // password类型不需要长度参数，内部随机8-15位
        if ("password".equals(type)) {
            return generateRandomPassword();
        }

        if (parts.length < 3) {
            log.warn("【变量解析】random函数参数不足");
            return "";
        }

        int length;
        try {
            length = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            log.warn("【变量解析】random函数长度参数无效: {}", parts[2]);
            return "";
        }

        switch (type) {
            case "alphabet":
                return generateRandomAlphabet(length);

            case "numeric":
                return generateRandomNumeric(length);

            case "alphanumeric":
                return generateRandomAlphanumeric(length);

            default:
                log.warn("【变量解析】未知的random类型: {}", type);
                return "";
        }
    }

    /**
     * 解析日期函数
     * @{date:yyyy-MM-dd}
     */
    private String evaluateDateFunction(String[] parts) {
        String pattern = parts.length > 1 ? parts[1] : "yyyy-MM-dd";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
            return sdf.format(new java.util.Date());
        } catch (Exception e) {
            log.warn("【变量解析】日期格式无效: {}", pattern);
            return "";
        }
    }

    /**
     * 生成随机字母字符串（大小写混合）
     */
    private String generateRandomAlphabet(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    /**
     * 生成随机数字字符串
     */
    private String generateRandomNumeric(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }

    /**
     * 生成随机字母数字字符串（大小写混合）
     */
    private String generateRandomAlphanumeric(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    /**
     * 生成随机密码
     * 长度8-15位，必须包含字母、数字、特殊字符(@%*.)
     */
    private String generateRandomPassword() {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String specials = "@%*.";

        // 随机长度 8-15
        int length = random.nextInt(8) + 8; // 8到15之间

        // 确保至少包含1个字母、1个数字、1个特殊字符
        StringBuilder password = new StringBuilder();
        password.append(letters.charAt(random.nextInt(letters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specials.charAt(random.nextInt(specials.length())));

        // 剩余长度从所有字符中随机选择
        String allChars = letters + numbers + specials;
        for (int i = 3; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱顺序
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * 提取变量
     * 将步骤中生成的值保存为变量，供后续步骤使用
     *
     * @param name  变量名
     * @param value 变量值
     */
    public void extractVariable(String name, String value) {
        if (name != null && !name.trim().isEmpty()) {
            variables.put(name, value);
            log.info("【变量提取】{} = {}", name, value);
        }
    }

    /**
     * 获取变量值
     */
    public String getVariable(String name) {
        return variables.get(name);
    }

    /**
     * 检查变量是否存在
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * 获取所有变量
     */
    public Map<String, String> getAllVariables() {
        return new HashMap<>(variables);
    }

    /**
     * 清空所有变量
     */
    public void clear() {
        variables.clear();
        log.info("【变量】清空所有变量");
    }
}