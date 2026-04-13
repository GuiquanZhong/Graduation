package com.smartforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartforum.entity.AiChatSession;
import com.smartforum.mapper.AiChatSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiChatSessionService {

    @Autowired
    private AiChatSessionMapper sessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AiChatSession> listSessions(Long userId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, userId)
                .orderByDesc(AiChatSession::getUpdatedAt);
        List<AiChatSession> sessions = sessionMapper.selectList(wrapper);
        for (AiChatSession s : sessions) {
            try {
                List<?> msgs = objectMapper.readValue(s.getMessages(), List.class);
                s.setMessageCount(msgs.size());
            } catch (Exception e) {
                s.setMessageCount(0);
            }
            s.setMessages(null);
        }
        return sessions;
    }

    public AiChatSession getSession(Long id, Long userId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getId, id)
                .eq(AiChatSession::getUserId, userId);
        return sessionMapper.selectOne(wrapper);
    }

    public AiChatSession createSession(Long userId, String title, List<Map<String, String>> messages) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(title);
        try {
            session.setMessages(objectMapper.writeValueAsString(messages));
        } catch (JsonProcessingException e) {
            session.setMessages("[]");
        }
        sessionMapper.insert(session);
        return session;
    }

    public void updateSession(Long id, Long userId, String title, List<Map<String, String>> messages) {
        AiChatSession session = getSession(id, userId);
        if (session == null) return;
        if (title != null) session.setTitle(title);
        try {
            session.setMessages(objectMapper.writeValueAsString(messages));
        } catch (JsonProcessingException e) {
            session.setMessages("[]");
        }
        sessionMapper.updateById(session);
    }

    public void deleteSession(Long id, Long userId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getId, id)
                .eq(AiChatSession::getUserId, userId);
        sessionMapper.delete(wrapper);
    }
}
