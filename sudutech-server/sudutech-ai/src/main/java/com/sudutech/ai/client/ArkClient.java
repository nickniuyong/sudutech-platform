package com.sudutech.ai.client;

import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 火山方舟 Ark 客户端（OpenAI 兼容 chat/completions 协议）
 */
@Slf4j
@Component
public class ArkClient {

    @Value("${sudutech.ai.ark.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Value("${sudutech.ai.ark.api-key:}")
    private String apiKey;

    @Value("${sudutech.ai.ark.model:doubao-pro-32k}")
    private String model;

    @Value("${sudutech.ai.ark.timeout-seconds:60}")
    private int timeoutSeconds;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }

    /**
     * 调用大模型，返回 content 文本
     */
    @SuppressWarnings("unchecked")
    public String chat(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            throw new ServiceException("未配置火山方舟 API Key，请在环境变量 ARK_API_KEY 或配置文件 sudutech.ai.ark.api-key 中设置");
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.1
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(body)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Ark接口返回异常: {} {}", response.statusCode(), response.body());
                throw new ServiceException("大模型调用失败(HTTP " + response.statusCode() + "): " + response.body());
            }
            Map<String, Object> result = JsonUtils.parseMap(response.body());
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ServiceException("大模型返回为空");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return String.valueOf(message.get("content"));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("大模型调用异常: " + e.getMessage());
        }
    }
}
