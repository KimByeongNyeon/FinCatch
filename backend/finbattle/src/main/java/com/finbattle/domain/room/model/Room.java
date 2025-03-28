package com.finbattle.domain.room.model;

import com.finbattle.domain.member.model.Member;
import com.finbattle.domain.quiz.model.SubjectType;
import com.finbattle.domain.room.dto.RoomStatus;
import com.finbattle.domain.room.dto.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId; // 방 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status; // 방 상태 (예: OPEN, CLOSED)

    @Column(nullable = false)
    private String roomTitle; // 방 제목

    private String password; // 비밀번호 (NULL 가능)

    @Column(nullable = false)
    private int maxPlayer; // 최대 플레이어 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType; // 방 유형 (예: QUIZ, GENERAL)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    // --- 1:N 관계 구현을 위한 핵심 ---
    // "한 Member"가 "여러 Room"을 생성할 수 있으므로,
    // Room 입장에서는 @ManyToOne으로 Member를 참조.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")  // 칼럼명: member_id
    private Member hostMember;
}

