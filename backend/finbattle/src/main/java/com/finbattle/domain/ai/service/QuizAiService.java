package com.finbattle.domain.ai.service;

import com.finbattle.domain.ai.dto.QuizAiRequestDto;
import com.finbattle.domain.ai.dto.QuizAiResponseDto;
import com.finbattle.domain.quiz.model.*;
import com.finbattle.domain.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAiService {

    private final QuizRepository quizRepository;
    private final ShortAnswerQuizRepository shortAnswerQuizRepository;
    private final EssayQuizRepository essayQuizRepository;
    private final MultipleChoiceQuizRepository multipleChoiceQuizRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizLogRepository quizLogRepository;
    private final AiGptService aiGptService;

    public QuizAiResponseDto analyze(Long memberId, QuizAiRequestDto dto) {
        Long quizId = dto.getQuizId();
        QuizLog quizLog = quizLogRepository
                .findTopByQuizIdAndMemberIdOrderByCreatedAtDesc(quizId, memberId)
                .orElseThrow(() -> new RuntimeException("해당 퀴즈에 대한 유저 답변이 존재하지 않습니다."));
        String userAnswer = quizLog.getUserAnswer();
        String prompt;
        Optional<ShortAnswerQuiz> shortOpt = shortAnswerQuizRepository.findById(quizId);
        if (shortOpt.isPresent()) {
            ShortAnswerQuiz q = shortOpt.get();
            prompt = getPrompt(q.getShortQuestion(), userAnswer, null);
            return callOpenAiAndParse(prompt);
        }
        Optional<EssayQuiz> essayOpt = essayQuizRepository.findById(quizId);
        if (essayOpt.isPresent()) {
            EssayQuiz q = essayOpt.get();
            prompt = getPrompt(q.getEssayQuestion(), userAnswer, null);
            return callOpenAiAndParse(prompt);
        }
        Optional<MultipleChoiceQuiz> multipleOpt = multipleChoiceQuizRepository.findById(quizId);
        if (multipleOpt.isPresent()) {
            MultipleChoiceQuiz q = multipleOpt.get();
            List<QuizOption> options = quizOptionRepository.findByQuizId(quizId);
            String optionsText = options.stream()
                    .map(o -> "- " + o.getOptionText())
                    .reduce("", (a, b) -> a + b + "\n");
            prompt = getPrompt(q.getMultipleQuestion(), userAnswer, optionsText);
            return callOpenAiAndParse(prompt);
        }
        throw new RuntimeException("해당 퀴즈 ID로 문제 유형을 찾을 수 없습니다.");
    }

    private String getPrompt(String question, String userAnswer, String optionsText) {
        return String.format(
                "문제: %s\n%s사용자 답변: %s\n\n[분석 내용]\n문제에서 다루는 주요 개념이나 사실을 정리해줘.\n\n" +
                        "[취약점]\n사용자의 선택이 왜 맞거나 틀렸는지 설명해줘.\n\n" +
                        "[추천 학습]\n부족한 부분을 보완할 수 있는 학습 방향을 제안해줘.",
                question,
                optionsText != null ? "보기:\n" + optionsText + "\n" : "",
                userAnswer
        );
    }

    private QuizAiResponseDto callOpenAiAndParse(String prompt) {
        String content = aiGptService.callOpenAi(prompt);
        String[] sections = content.split("\\[.*?\\]");
        List<String> parts = new ArrayList<>();
        for (String part : sections) {
            if (!part.trim().isEmpty()) {
                parts.add(part.trim());
            }
        }
        return new QuizAiResponseDto(
                parts.size() > 0 ? parts.get(0) : "",
                parts.size() > 1 ? parts.get(1) : "",
                parts.size() > 2 ? parts.get(2) : ""
        );
    }
}
