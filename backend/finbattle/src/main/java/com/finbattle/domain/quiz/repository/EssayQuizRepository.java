package com.finbattle.domain.quiz.repository;

import com.finbattle.domain.quiz.model.EssayQuiz;
import com.finbattle.domain.quiz.model.SubjectType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EssayQuizRepository extends JpaRepository<EssayQuiz, Long> {

    @Query("SELECT q FROM EssayQuiz q " +
        "WHERE q.subjectType = :subject " +
        "ORDER BY function('random')")
    List<EssayQuiz> findRandomBySubject(@Param("subject") SubjectType subjectType,
        Pageable pageable);
}