package com.finbattle.domain.quiz.dto;

import com.finbattle.domain.quiz.model.Quiz;
import com.finbattle.domain.quiz.model.QuizMode;
import com.finbattle.domain.quiz.model.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Quiz 엔티티의 공통 필드를 담는 추상 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class QuizDto {

    private Long quizId;
    private QuizMode quizMode;
    private SubjectType subjectType;
    private String createdAt;

    protected void mapCommonFields(Quiz entity) {
        this.quizId = entity.getQuizId();
        this.quizMode = entity.getQuizMode();
        this.subjectType = entity.getSubjectType();
        this.createdAt = entity.getCreatedAt();
    }
}
