package com.finbattle.domain.quiz.dto;

import com.finbattle.domain.quiz.model.QuizMode;
import com.finbattle.domain.quiz.model.SubjectType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongQuizLogDto {

    private Long quizId;
    private QuizMode quizMode;         // 문제 유형 (서술형/객관식 등)
    private SubjectType quizSubject;   // 문제 주제 (정책/금융지식 등)
    private String question;           // 문제 본문
    private String correctAnswer;      // 정답
    private String userAnswer;         // 사용자가 쓴 답
    private LocalDateTime createdAt;   // 푼 날짜
}
