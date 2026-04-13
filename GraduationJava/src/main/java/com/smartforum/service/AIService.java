package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartforum.entity.Post;
import com.smartforum.mapper.PostMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    @Value("${siliconflow.api-url}")
    private String sfApiUrl;

    @Value("${siliconflow.api-key}")
    private String sfApiKey;

    @Autowired
    private PostMapper postMapper;

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
     * 基于帖子内容的多轮问答
     *
     * @param postTitle   帖子标题
     * @param postContent 帖子正文
     * @param history     对话历史
     */
    public String postChat(String postTitle, String postContent, List<Map<String, String>> history) {
        String systemPrompt = "你是一个专业的阅读助手。用户正在阅读一篇帖子，请根据帖子内容回答用户提出的问题。" +
                "回答应简洁、准确、友好，且仅基于帖子内容作答。如果帖子内容中没有相关信息，请如实告知用户。\n\n" +
                "=== 帖子标题 ===\n" + postTitle + "\n\n" +
                "=== 帖子内容 ===\n" + postContent;
        return callAIWithHistory(systemPrompt, history);
    }

    /**
     * 多轮对话智能问答（支持上下文历史 + 热门帖子推荐）
     *
     * @param history 对话历史，每条格式 {"role": "user"/"assistant", "content": "..."}
     */
    public String chat(List<Map<String, String>> history) {
        String hotPostsInfo = buildHotPostsInfo();
        String systemPrompt = "你是「智能论坛」的 AI 助手，博学多才，擅长回答各类问题。" +
                "请用清晰、专业、友好的语言回答用户的问题。如果问题涉及代码，请使用 Markdown 格式展示代码块。\n\n" +
                "以下是论坛当前的热门帖子（按热度排序），当用户询问推荐、热门帖子等相关问题时，请结合这些信息回答：\n" +
                hotPostsInfo;
        return callAIWithHistory(systemPrompt, history);
    }

    /**
     * 查询热门帖子并格式化为文本
     */
    private String buildHotPostsInfo() {
        try {
            LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Post::getViewCount)
                    .orderByDesc(Post::getCreatedAt)
                    .last("LIMIT 10");
            List<Post> hotPosts = postMapper.selectList(wrapper);
            if (hotPosts == null || hotPosts.isEmpty()) {
                return "（暂无热门帖子数据）";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hotPosts.size(); i++) {
                Post p = hotPosts.get(i);
                sb.append(i + 1).append(". 《").append(p.getTitle()).append("》");
                if (p.getSummary() != null && !p.getSummary().isBlank()) {
                    sb.append(" - ").append(p.getSummary());
                }
                sb.append("（浏览量：").append(p.getViewCount()).append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "（热门帖子数据暂时不可用）";
        }
    }

    /**
     * 根据帖子内容和已有评论，生成评论建议（MiniMax）
     */
    public String suggestComment(String postTitle, String postContent, String existingComments) {
        String systemPrompt = "你是一个论坛评论助手。请根据帖子标题、内容以及已有评论，生成一条有价值、友好的评论建议。" +
                "要求：简洁自然，不超过100字，直接返回评论文本，不要包含任何前缀说明。";
        String userMsg = "帖子标题：" + postTitle + "\n\n帖子内容摘要：" +
                postContent.substring(0, Math.min(500, postContent.length())) +
                (existingComments == null || existingComments.isBlank() ? "" : "\n\n已有评论：\n" + existingComments);
        return callAI(systemPrompt, userMsg);
    }

    /**
     * 语义搜索帖子（GLM-5，返回帖子ID列表）
     */
    public List<Long> semanticSearch(String query) {
        try {
            LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Post::getCreatedAt).last("LIMIT 100");
            List<Post> posts = postMapper.selectList(wrapper);
            if (posts.isEmpty()) return List.of();

            StringBuilder sb = new StringBuilder();
            for (Post p : posts) {
                sb.append("ID:").append(p.getId())
                  .append(" 标题:《").append(p.getTitle()).append("》");
                if (p.getSummary() != null && !p.getSummary().isBlank()) {
                    sb.append(" 摘要:").append(p.getSummary());
                }
                sb.append("\n");
            }

            String systemPrompt = "你是一个帖子语义搜索引擎。根据用户的搜索意图，从帖子列表中找出最相关的帖子。" +
                    "只返回帖子ID，用英文逗号分隔，最多返回5个，按相关度从高到低排列。不要返回任何其他内容。" +
                    "如果没有相关帖子，返回空字符串。\n\n帖子列表：\n" + sb;
            String result = callSiliconFlow(systemPrompt,
                    List.of(Map.of("role", "user", "content", "搜索：" + query)),
                    "Pro/zai-org/GLM-5");
            if (result == null || result.isBlank()) return List.of();
            return Arrays.stream(result.split(","))
                    .map(String::trim)
                    .filter(s -> s.matches("\\d+"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 硅基流动多轮对话（支持指定模型）
     */
    public String chatWithModel(List<Map<String, String>> history, String modelId) {
        String hotPostsInfo = buildHotPostsInfo();
        String systemPrompt = "你是「智能论坛」的 AI 助手，博学多才，擅长回答各类问题。" +
                "请用清晰、专业、友好的语言回答用户的问题。如果问题涉及代码，请使用 Markdown 格式展示代码块。\n\n" +
                "以下是论坛当前的热门帖子（按热度排序），当用户询问推荐、热门帖子等相关问题时，请结合这些信息回答：\n" +
                hotPostsInfo;
        return callSiliconFlow(systemPrompt, history, modelId);
    }

    /**
     * 调用硅基流动 API（标准 OpenAI 格式）
     */
    private String callSiliconFlow(String systemPrompt, List<Map<String, String>> history, String modelId) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelId);

            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            for (Map<String, String> msg : history) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", "assistant".equals(msg.get("role")) ? "assistant" : "user");
                msgNode.put("content", msg.get("content"));
                messages.add(msgNode);
            }

            requestBody.set("messages", messages);
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.7);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(sfApiUrl)
                    .addHeader("Authorization", "Bearer " + sfApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("硅基流动 API 调用失败，状态码: " + response.code());
                }
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        return message.get("content").asText();
                    }
                }
                throw new RuntimeException("硅基流动 API 返回格式异常");
            }
        } catch (IOException e) {
            throw new RuntimeException("硅基流动 API 调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 MiniMax API（单轮，用于摘要/标题/润色）
     */
    private String callAI(String systemPrompt, String userMessage) {
        return callAIWithHistory(systemPrompt, List.of(Map.of("role", "user", "content", userMessage)));
    }

    /**
     * 调用 MiniMax API（多轮，支持完整对话历史）
     */
    private String callAIWithHistory(String systemPrompt, List<Map<String, String>> history) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);

            ArrayNode messages = objectMapper.createArrayNode();

            // system 消息
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("name", "AI助手");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            // 历史对话消息
            for (Map<String, String> msg : history) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                String role = msg.get("role");
                msgNode.put("role", "assistant".equals(role) ? "assistant" : "user");
                msgNode.put("name", "assistant".equals(role) ? "AI助手" : "用户");
                msgNode.put("content", msg.get("content"));
                messages.add(msgNode);
            }

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
