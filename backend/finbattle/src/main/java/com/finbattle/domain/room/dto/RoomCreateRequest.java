package com.finbattle.domain.room.dto;

import com.finbattle.global.common.model.enums.SubjectType;
import lombok.Data;

@Data
public class RoomCreateRequest {

    private Long memberId;      // 방을 생성하는 사용자 ID
    private String roomTitle;   // 방 제목
    private String password;    // 방 비밀번호
    private int maxPlayer;      // 최대 인원수
    private RoomType roomType;  // ENUM (QUIZ, GENERAL, PRIVATE)
    private SubjectType subjectType;
}