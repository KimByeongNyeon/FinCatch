package com.finbattle.domain.room.model;

import com.finbattle.domain.quiz.model.SubjectType;
import com.finbattle.domain.room.dto.RedisRoomMember;
import com.finbattle.domain.room.dto.RoomStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash("room")
public class RedisRoom {

    @Id
    private Long roomId;

    private int maxPeople;
    private SubjectType subjectType;
    private RoomStatus status;    // OPEN, IN_PROGRESS, CLOSED, DELETED
    private RedisRoomMember host; // 방장 정보
    private List<RedisRoomMember> members = new ArrayList<>();
}
