package com.smartforum.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class AIService {

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成文章摘要
     */
    public String generateSummary(String content) {
        String systemPrompt = "你是一个专业的文本摘要助手。请对用户提供的文章内容进行精炼总结，提取核心观点和关键信息，生成一段不超过200字的摘要。要求语言简洁、准确、通顺。";
        return callAI(systemPrompt, "请为以下文章生成摘要：\n\n" + content);
    }

    /**
     * AI 生成标题
     */
    public String generateTitle(String content) {
        String systemPrompt = "你是一个专业的标题创作助手。请根据用户提供的文章内容，生成一个吸引人且准确概括文章主题的标题。标题应简洁有力，不超过30个字。只需返回标题文本，不要包含任何额外说明。";
        return callAI(systemPrompt, "请为以下文章生成标题：\n\n" + content);
    }

    /**
     * AI 润色内容
     */
    public String polishContent(String content) {
        String systemPrompt = "你是一个专业的文章润色助手。请对用户提供的文本进行语言润色和优化，使其更加通顺、优美、专业。保持原文的核心意思不变，只进行语言层面的改进。直接返回润色后的文本，不要包含任何额外说明。";
        return callAI(systemPrompt, "请润色以下内容：\n\n" + content);
    }

    /**
     * 开放式智能问答
     */
    public String chat(String question) {
        String systemPrompt = "你是智能论坛的AI助手，博学多才，擅长回答各类问题。请用清晰、专业、友好的语言回答用户的问题。如果问题涉及代码，请使用Markdown格式展示代码块。";
        return callAI(systemPrompt, question);
    }

    /**
     * 调用 MiniMax API
     */
    private String callAI(String systemPrompt, String userMessage) {
        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);

            ArrayNode messages = objectMapper.createArrayNode();

            // system 消息
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("name", "AI助手");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            // user 消息
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("name", "用户");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.set("messages", messages);
            requestBody.put("max_completion_tokens", 2048);
            requestBody.put("temperature", 0.7);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("AI API 调用失败，状态码: " + response.code());
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // 检查 base_resp 状态
                JsonNode baseResp = jsonNode.get("base_resp");
                if (baseResp != null && baseResp.get("status_code").asInt() != 0) {
                    throw new RuntimeException("AI API 错误: " + baseResp.get("status_msg").asText());
                }

                // 提取回复内容
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        return message.get("content").asText();
                    }
                }

                throw new RuntimeException("AI API 返回格式异常");
            }
        } catch (IOException e) {
            throw new RuntimeException("AI API 调用异常: " + e.getMessage(), e);
        }
    }
}
