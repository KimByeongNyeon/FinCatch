package com.finbattle.domain.quiz.repository;

import com.finbattle.domain.quiz.model.MultipleChoiceQuiz;
import com.finbattle.domain.quiz.model.SubjectType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MultipleChoiceQuizRepository extends JpaRepository<MultipleChoiceQuiz, Long> {

    @Query("SELECT q FROM MultipleChoiceQuiz q " +
        "WHERE q.subjectType = :subject " +
        "ORDER BY function('random')")
    List<MultipleChoiceQuiz> findRandomBySubject(@Param("subject") SubjectType subjectType,
        Pageable pageable);
}
