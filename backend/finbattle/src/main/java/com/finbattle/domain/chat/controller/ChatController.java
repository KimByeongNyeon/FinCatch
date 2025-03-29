package com.finbattle.domain.chat.controller;

import com.finbattle.domain.chat.dto.ChatMessage;
import com.finbattle.domain.chat.model.StompPrincipal;
import com.finbattle.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 클라이언트 → 서버: /app/chat/{roomId} 메시지 수신 후 DB 저장 및 Redis Pub/Sub 발행
     */
    @MessageMapping("/chat/{roomId}")
    public void processChatMessage(ChatMessage message,
        @AuthenticationPrincipal StompPrincipal member) {
        Long memberId = member.getMemberId();
        log.info("Received chat message: {}, sender: {}", message, memberId);
        chatService.processChatMessage(message, memberId);
    }
}
