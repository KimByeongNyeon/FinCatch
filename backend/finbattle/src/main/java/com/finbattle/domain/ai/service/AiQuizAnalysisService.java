package com.finbattle.domain.ai.service;

import com.finbattle.domain.ai.dto.AiQuizAnalysisResponseDto;
import com.finbattle.domain.ai.model.AiMultipleChoiceQuiz;
import com.finbattle.domain.ai.model.AiQuiz;
import com.finbattle.domain.ai.model.AiQuizLog;
import com.finbattle.domain.ai.repository.AiMultipleChoiceQuizRepository;
import com.finbattle.domain.ai.repository.AiQuizLogRepository;
import com.finbattle.domain.ai.repository.AiQuizRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import com.finbattle.global.common.model.dto.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuizAnalysisService {

    private final AiQuizRepository aiQuizRepository;
    private final AiMultipleChoiceQuizRepository multipleChoiceQuizRepository;
    private final AiQuizLogRepository aiQuizLogRepository;
    private final AiGptService aiGptService;

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    public AiQuizAnalysisResponseDto analyzeAiQuiz(Long quizId, Long memberId) {
        // ai_quiz_log 테이블에서 최신 로그 조회
        AiQuizLog log = aiQuizLogRepository
                .findTopByAiQuizIdAndMemberIdOrderByCreatedAtDesc(quizId, memberId);
        if (log == null) {
            throw new BusinessException(BaseResponseStatus.QUIZ_LOG_NOT_FOUND);
        }
        // ai_quiz 테이블에서 문제 정보 조회
        AiQuiz aiQuiz = aiQuizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
        AiMultipleChoiceQuiz multiple = multipleChoiceQuizRepository.findByAiQuiz(aiQuiz)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
        // 프롬프트 구성 – 보기가 필요하면 추가 가능
        String prompt = String.format(
                "문제: %s\n사용자 답변: %s\n\n[분석 내용]\n문제의 핵심 개념 정리\n\n" +
                        "[취약점]\n사용자가 왜 틀렸는지 설명\n\n[추천 학습]\n보완할 학습 방향 제안",
                multiple.getQuestion(), log.getUserAnswer());
        String gptResponse = aiGptService.callOpenAi(prompt);
        String[] sections = gptResponse.split("\\[.*?\\]");
        List<String> parts = Arrays.stream(sections)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return new AiQuizAnalysisResponseDto(
                parts.size() > 0 ? parts.get(0) : "",
                parts.size() > 1 ? parts.get(1) : "",
                parts.size() > 2 ? parts.get(2) : ""
        );
    }
}
