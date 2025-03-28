package com.finbattle.domain.ai.service;

import com.finbattle.domain.ai.dto.AiQuizWrongNoteDto;
import com.finbattle.domain.ai.model.AiMultipleChoiceQuiz;
import com.finbattle.domain.ai.model.AiOption;
import com.finbattle.domain.ai.model.AiQuiz;
import com.finbattle.domain.ai.model.AiQuizLog;
import com.finbattle.domain.ai.repository.AiMultipleChoiceQuizRepository;
import com.finbattle.domain.ai.repository.AiOptionRepository;
import com.finbattle.domain.ai.repository.AiQuizLogRepository;
import com.finbattle.domain.ai.repository.AiQuizRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import com.finbattle.global.common.model.dto.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiQuizLogService {

    private final AiQuizRepository aiQuizRepository;
    private final AiMultipleChoiceQuizRepository multipleChoiceQuizRepository;
    private final AiOptionRepository aiOptionRepository;
    private final AiQuizLogRepository aiQuizLogRepository;

    /**
     * AI퀴즈 정답 제출 (객관식 기준)
     */
    public void submitAnswer(Long quizId, Long memberId, int selectedIndex) {
        AiQuiz quiz = aiQuizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
        AiMultipleChoiceQuiz multipleQuiz = multipleChoiceQuizRepository.findByAiQuiz(quiz)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
        List<AiOption> options = aiOptionRepository.findByMultipleChoiceQuiz(multipleQuiz);
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            throw new BusinessException(BaseResponseStatus.BAD_REQUEST);
        }
        AiOption selectedOption = options.get(selectedIndex);
        boolean isCorrect = selectedOption.isCorrect();
        AiQuizLog log = AiQuizLog.builder()
                .aiQuizId(quizId)
                .memberId(memberId)
                .userAnswer(selectedOption.getOptionText())
                .isCorrect(isCorrect)
                .build();
        aiQuizLogRepository.save(log);
    }

    /**
     * AI퀴즈 오답 노트 조회 (틀린 로그만 반환)
     */
    public List<AiQuizWrongNoteDto> getWrongQuizLogsByMember(Long memberId) {
        // ai_quiz_log 테이블에서 memberId가 일치하고 isCorrect가 false인 로그를 조회하는 메서드가 있다고 가정
        List<AiQuizLog> wrongLogs = aiQuizLogRepository.findByMemberIdAndIsCorrectFalse(memberId);
        return wrongLogs.stream().map(log -> {
            AiQuiz aiQuiz = aiQuizRepository.findById(log.getAiQuizId())
                    .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
            AiMultipleChoiceQuiz multiple = multipleChoiceQuizRepository.findByAiQuiz(aiQuiz)
                    .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
            List<AiOption> options = aiOptionRepository.findByMultipleChoiceQuiz(multiple);
            String correctAnswer = options.stream()
                    .filter(AiOption::isCorrect)
                    .map(AiOption::getOptionText)
                    .findFirst().orElse(null);
            return AiQuizWrongNoteDto.builder()
                    .quizId(log.getAiQuizId())
                    .question(multiple.getQuestion())
                    .correctAnswer(correctAnswer)
                    .userAnswer(log.getUserAnswer())
                    .createdAt(log.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}
