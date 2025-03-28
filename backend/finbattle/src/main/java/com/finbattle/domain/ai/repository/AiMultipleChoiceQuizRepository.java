package com.finbattle.domain.ai.repository;

import com.finbattle.domain.ai.model.AiMultipleChoiceQuiz;
import com.finbattle.domain.ai.model.AiQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiMultipleChoiceQuizRepository extends JpaRepository<AiMultipleChoiceQuiz, Long> {
    Optional<AiMultipleChoiceQuiz> findByAiQuiz(AiQuiz aiQuiz);
}
