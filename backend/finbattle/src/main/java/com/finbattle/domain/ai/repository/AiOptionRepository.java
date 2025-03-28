package com.finbattle.domain.ai.repository;

import com.finbattle.domain.ai.model.AiMultipleChoiceQuiz;
import com.finbattle.domain.ai.model.AiOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiOptionRepository extends JpaRepository<AiOption, Long> {
    List<AiOption> findByMultipleChoiceQuiz(AiMultipleChoiceQuiz multipleChoiceQuiz);
}
