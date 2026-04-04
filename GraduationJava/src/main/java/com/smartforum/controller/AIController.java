package com.smartforum.controller;

import com.smartforum.common.Result;
import com.smartforum.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

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
        String answer = aiService.chat(history);
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
}
