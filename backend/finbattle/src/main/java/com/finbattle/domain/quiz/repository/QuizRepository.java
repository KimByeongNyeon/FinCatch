package com.finbattle.domain.quiz.repository;

import com.finbattle.domain.quiz.model.Quiz;
import com.finbattle.domain.quiz.model.QuizMode;
import com.finbattle.global.common.model.enums.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 퀴즈 타입으로 조회
    List<Quiz> findByQuizMode(QuizMode quizMode);

    // 주제로 조회
    List<Quiz> findBySubjectType(SubjectType subjectType);

    // 퀴즈 타입 + 주제 조합으로 조회
    List<Quiz> findByQuizModeAndSubjectType(QuizMode quizMode, SubjectType subjectType);
}
