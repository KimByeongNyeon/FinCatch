package com.finbattle.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 클라이언트와 주고받을 채팅 메시지 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage { // 메시지 보낸 사람

    private String content;  // 메시지 내용
    private String roomId;   // 채팅방 ID
}
