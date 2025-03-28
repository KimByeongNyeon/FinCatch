package com.finbattle.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class AiQuizWrongNoteDto {
    private Long quizId;
    private String question;
    private String correctAnswer;
    private String userAnswer;
    private LocalDateTime createdAt;
}
