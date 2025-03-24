package com.finbattle.domain.quiz.repository;

import com.finbattle.domain.quiz.model.ShortAnswerQuiz;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShortAnswerQuizRepository extends JpaRepository<ShortAnswerQuiz, Long> {

    // PostgreSQL의 random() 함수를 사용하여 랜덤 정렬 후 첫 개의 퀴즈를 가져옵니다.
    @Query("SELECT q FROM ShortAnswerQuiz q ORDER BY function('random')")
    List<ShortAnswerQuiz> findRandomQuiz(Pageable pageable);
}
