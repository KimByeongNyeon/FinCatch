package com.finbattle.domain.room.dto;

import com.finbattle.domain.quiz.model.SubjectType;
import com.finbattle.domain.room.model.Room;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RoomResponse {

    private Long roomId;
    private String roomTitle;
    private RoomStatus status;
    private RoomType roomType;
    private SubjectType subjectType;
    private int maxPlayer;
    private Long memberId;
    private LocalDateTime createdAt;

    // ✅ fromEntity() 추가
    public static RoomResponse fromEntity(Room room) {
        RoomResponse response = new RoomResponse();
        response.roomId = room.getRoomId();
        response.roomTitle = room.getRoomTitle();
        response.status = room.getStatus();
        response.roomType = room.getRoomType();
        response.subjectType = room.getSubjectType();
        response.maxPlayer = room.getMaxPlayer();
        response.memberId =
            room.getHostMember() != null ? room.getHostMember().getMemberId() : null;
        response.createdAt = room.getCreatedAt();
        return response;
    }
}
