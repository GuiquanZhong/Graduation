package com.smartforum.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartforum.common.Result;
import com.smartforum.entity.AiChatSession;
import com.smartforum.service.AiChatSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/sessions")
public class AiChatSessionController {

    @Autowired
    private AiChatSessionService sessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public Result<?> listSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(sessionService.listSessions(userId));
    }

    @GetMapping("/{id}")
    public Result<?> getSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AiChatSession session = sessionService.getSession(id, userId);
        if (session == null) return Result.error("会话不存在");
        return Result.success(session);
    }

    @PostMapping
    public Result<?> createSession(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String title = (String) body.getOrDefault("title", "新对话");
        List<Map<String, String>> messages = parseMessages(body.get("messages"));
        AiChatSession session = sessionService.createSession(userId, title, messages);
        return Result.success(session);
    }

    @PutMapping("/{id}")
    public Result<?> updateSession(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String title = (String) body.get("title");
        List<Map<String, String>> messages = parseMessages(body.get("messages"));
        sessionService.updateSession(id, userId, title, messages);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        sessionService.deleteSession(id, userId);
        return Result.success(null);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseMessages(Object raw) {
        if (raw == null) return List.of();
        try {
            String json = objectMapper.writeValueAsString(raw);
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
