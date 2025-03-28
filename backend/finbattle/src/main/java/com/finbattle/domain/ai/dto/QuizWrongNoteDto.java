package com.finbattle.domain.ai.dto;

import com.finbattle.domain.quiz.model.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class QuizWrongNoteDto {
    private Long quizId;
    private String quizMode; // 혹은 QuizMode
    private SubjectType quizSubject; // 추가
    private String question;
    private String correctAnswer;
    private String userAnswer;
    private LocalDateTime createdAt;
}
