package com.finbattle.domain.ai.repository;

import com.finbattle.domain.ai.model.AiQuiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiQuizRepository extends JpaRepository<AiQuiz, Long> {
    List<AiQuiz> findByMemberIdAndIsDeletedFalse(Long memberId, Pageable pageable);
}
