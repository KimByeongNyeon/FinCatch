package com.finbattle.domain.ai.repository;

import com.finbattle.domain.ai.model.AiQuizLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiQuizLogRepository extends JpaRepository<AiQuizLog, Long> {
    List<AiQuizLog> findByMemberIdAndIsCorrectFalse(Long memberId);
    AiQuizLog findTopByAiQuizIdAndMemberIdOrderByCreatedAtDesc(Long aiQuizId, Long memberId);
}
