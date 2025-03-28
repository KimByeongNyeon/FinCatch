package com.finbattle.domain.ai.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ai_quiz_log")
public class AiQuizLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiQuizLogId;

    @Column(name = "ai_quiz_id", nullable = false)
    private Long aiQuizId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
