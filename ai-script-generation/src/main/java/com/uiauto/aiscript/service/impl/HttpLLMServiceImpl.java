package com.uiauto.aiscript.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import com.uiauto.aiscript.service.LLMService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 基于HTTP的LLM服务实现
 * 支持OpenAI兼容格式的HTTP接口调用
 */
@Slf4j
@Service
public class HttpLLMServiceImpl implements LLMService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_ENCRYPTION_KEY = "uiaut-test-2024-ai";

    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;

    public HttpLLMServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String callLLM(AIServiceConfigEntity config, String prompt) throws Exception {
        return callLLM(config, prompt, config.getTimeoutSeconds() * 1000L);
    }

    @Override
    public String callLLM(AIServiceConfigEntity config, String prompt, long timeoutMs) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // 解密API密钥
            String decryptedKey = decrypt(config.getApiKey());

            // 构建请求体
            JsonObject requestBody = buildRequestBody(config, prompt);

            // 构建HTTP请求
            Request request = buildHttpRequest(config, decryptedKey, requestBody, timeoutMs);

            // 发送请求
            OkHttpClient clientWithTimeout = httpClient.newBuilder()
                    .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .build();

            Response response = clientWithTimeout.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("LLM调用失败: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("LLM响应: {}", responseBody);

            // 解析响应
            return extractContentFromResponse(responseBody);

        } catch (Exception e) {
            log.error("LLM调用异常", e);
            throw new Exception("LLM服务调用失败: " + e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("LLM调用耗时: {}ms", duration);
        }
    }

    @Override
    public boolean testConnection(AIServiceConfigEntity config) {
        try {
            String result = callLLM(config, "Hello", 5000L);
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            log.error("连接测试失败", e);
            return false;
        }
    }

    /**
     * 构建请求体（OpenAI兼容格式）
     */
    private JsonObject buildRequestBody(AIServiceConfigEntity config, String prompt) {
        JsonObject requestBody = new JsonObject();

        // 模型名称
        requestBody.addProperty("model", config.getModelName());

        // 构建消息数组
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);
        requestBody.add("messages", messages);

        // 其他参数
        if (config.getMaxTokens() != null) {
            requestBody.addProperty("max_tokens", config.getMaxTokens());
        }

        if (config.getTemperature() != null) {
            requestBody.addProperty("temperature", config.getTemperature());
        }

        return requestBody;
    }

    /**
     * 构建HTTP请求
     */
    private Request buildHttpRequest(AIServiceConfigEntity config, String apiKey, JsonObject requestBody, long timeoutMs) {
        String endpoint = config.getApiEndpoint();
        if (endpoint == null || endpoint.isEmpty()) {
            // 使用默认的OpenAI端点
            endpoint = "https://api.openai.com/v1/chat/completions";
        }

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(endpoint)
                .post(body)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json");

        // 添加自定义请求头
        if (config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
            try {
                JsonObject customHeaders = JsonParser.parseString(config.getCustomHeaders()).getAsJsonObject();
                for (String key : customHeaders.keySet()) {
                    String value = customHeaders.get(key).getAsString();
                    // 支持变量替换
                    value = value.replace("${api_key}", apiKey);
                    requestBuilder.header(key, value);
                }
            } catch (Exception e) {
                log.warn("解析自定义请求头失败", e);
            }
        }

        return requestBuilder.build();
    }

    /**
     * 从响应中提取内容
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();

            // OpenAI格式: choices[0].message.content
            if (response.has("choices")) {
                JsonArray choices = response.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        if (message.has("content")) {
                            return message.get("content").getAsString();
                        }
                    }
                }
            }

            // 其他格式直接返回
            return responseBody;

        } catch (Exception e) {
            log.error("解析LLM响应失败", e);
            return responseBody;
        }
    }

    /**
     * 解密API密钥
     */
    private String decrypt(String encryptedKey) throws Exception {
        if (encryptedKey == null || encryptedKey.isEmpty()) {
            return "";
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    DEFAULT_ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedKey);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("API Key未加密或解密失败，使用原始密钥");
            return encryptedKey;
        }
    }

    /**
     * 加密API密钥
     */
    public String encrypt(String plainKey) throws Exception {
        if (plainKey == null || plainKey.isEmpty()) {
            return "";
        }

        SecretKeySpec secretKey = new SecretKeySpec(
                DEFAULT_ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8),
                ALGORITHM
        );
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(plainKey.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
