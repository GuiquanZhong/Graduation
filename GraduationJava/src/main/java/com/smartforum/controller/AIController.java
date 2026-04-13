package com.smartforum.controller;

import com.smartforum.common.Result;
import com.smartforum.entity.Post;
import com.smartforum.service.AIService;
import com.smartforum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private PostService postService;

    /**
     * AI 生成摘要
     */
    @PostMapping("/summary")
    public Result<?> generateSummary(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }
        String summary = aiService.generateSummary(content);
        return Result.success(summary);
    }

    /**
     * AI 生成标题
     */
    @PostMapping("/title")
    public Result<?> generateTitle(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }
        String title = aiService.generateTitle(content);
        return Result.success(title);
    }

    /**
     * AI 润色内容
     */
    @PostMapping("/polish")
    public Result<?> polishContent(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }
        String polished = aiService.polishContent(content);
        return Result.success(polished);
    }

    /**
     * AI 智能问答（支持多轮对话历史）
     * 请求体格式：{ "history": [{"role": "user", "content": "..."}, {"role": "assistant", "content": "..."}] }
     */
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
        if (history == null || history.isEmpty()) {
            return Result.error("消息历史不能为空");
        }
        String model = (String) params.get("model");
        String answer;
        if (model != null && !model.isBlank()) {
            answer = aiService.chatWithModel(history, model);
        } else {
            answer = aiService.chat(history);
        }
        return Result.success(answer);
    }

    /**
     * 基于帖子内容的多轮问答
     * 请求体格式：{ "postTitle": "...", "postContent": "...", "history": [...] }
     */
    @PostMapping("/post-chat")
    public Result<?> postChat(@RequestBody Map<String, Object> params) {
        String postTitle = (String) params.get("postTitle");
        String postContent = (String) params.get("postContent");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
        if (postContent == null || postContent.trim().isEmpty()) {
            return Result.error("帖子内容不能为空");
        }
        if (history == null || history.isEmpty()) {
            return Result.error("消息历史不能为空");
        }
        String answer = aiService.postChat(postTitle == null ? "" : postTitle, postContent, history);
        return Result.success(answer);
    }

    @PostMapping("/comment-suggest")
    public Result<?> commentSuggest(@RequestBody Map<String, String> params) {
        String postTitle = params.getOrDefault("postTitle", "");
        String postContent = params.getOrDefault("postContent", "");
        String existingComments = params.getOrDefault("existingComments", "");
        if (postContent.trim().isEmpty()) {
            return Result.error("帖子内容不能为空");
        }
        String suggestion = aiService.suggestComment(postTitle, postContent, existingComments);
        return Result.success(suggestion);
    }

    @GetMapping("/search")
    public Result<?> semanticSearch(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return Result.error("搜索内容不能为空");
        }
        List<Long> ids = aiService.semanticSearch(query.trim());
        if (ids.isEmpty()) {
            return Result.success(java.util.List.of());
        }
        List<Post> posts = ids.stream()
                .map(id -> {
                    try { return postService.getPostDetail(id); }
                    catch (Exception e) { return null; }
                })
                .filter(p -> p != null)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(posts);
    }
}
