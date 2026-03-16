package com.smartforum.controller;

import com.smartforum.common.Result;
import com.smartforum.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * AI 智能问答
     */
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Result.error("问题不能为空");
        }
        String answer = aiService.chat(question);
        return Result.success(answer);
    }
}
