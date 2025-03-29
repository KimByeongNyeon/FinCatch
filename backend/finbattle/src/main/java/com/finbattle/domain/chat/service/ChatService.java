package com.finbattle.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbattle.domain.chat.dto.ChatMessage;
import com.finbattle.domain.chat.model.ChatLog;
import com.finbattle.domain.chat.repository.ChatLogRepository;
import com.finbattle.global.common.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLogRepository chatLogRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 채팅 메시지를 DB에 저장하고, Redis 채널("chat")에 발행
     */
    public void processChatMessage(ChatMessage message, Long memberId) {
        // DB 저장
        ChatLog log = new ChatLog(message.getRoomId(), memberId, message.getContent());
        chatLogRepository.save(log);
        // Redis에 JSON 형태로 발행
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisPublisher.publish("chat", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
