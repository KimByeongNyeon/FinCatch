package com.finbattle.domain.ai.service;

import com.finbattle.domain.ai.dto.QuizWrongNoteDto;
import com.finbattle.domain.quiz.model.EssayQuiz;
import com.finbattle.domain.quiz.model.MultipleChoiceQuiz;
import com.finbattle.domain.quiz.model.Quiz;
import com.finbattle.domain.quiz.model.QuizLog;
import com.finbattle.domain.quiz.model.ShortAnswerQuiz;
import com.finbattle.domain.quiz.model.QuizMode;
import com.finbattle.domain.quiz.repository.EssayQuizRepository;
import com.finbattle.domain.quiz.repository.MultipleChoiceQuizRepository;
import com.finbattle.domain.quiz.repository.QuizLogRepository;
import com.finbattle.domain.quiz.repository.QuizOptionRepository;
import com.finbattle.domain.quiz.repository.QuizRepository;
import com.finbattle.domain.quiz.repository.ShortAnswerQuizRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import com.finbattle.global.common.model.dto.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizWrongNoteService {

    private final QuizLogRepository quizLogRepository;
    private final QuizRepository quizRepository;
    private final ShortAnswerQuizRepository shortAnswerQuizRepository;
    private final EssayQuizRepository essayQuizRepository;
    private final MultipleChoiceQuizRepository multipleChoiceQuizRepository;
    private final QuizOptionRepository quizOptionRepository;

    public List<QuizWrongNoteDto> getWrongNotesByMember(Long memberId) {
        List<QuizLog> wrongLogs = quizLogRepository.findByMemberIdAndIsCorrectFalse(memberId);
        return wrongLogs.stream().map(log -> {
            Quiz quiz = quizRepository.findById(log.getQuizId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + log.getQuizId()));
            String question = "";
            String correctAnswer = "";
            // 타입별로 Quiz 엔티티에서 필요한 정보를 추출
            switch (quiz.getQuizMode()) {
                case SHORT_ANSWER:
                    ShortAnswerQuiz saQuiz = shortAnswerQuizRepository.findById(quiz.getQuizId())
                            .orElseThrow(() -> new RuntimeException("ShortAnswerQuiz not found with id: " + quiz.getQuizId()));
                    question = saQuiz.getShortQuestion();
                    correctAnswer = saQuiz.getShortAnswer();
                    break;
                case ESSAY:
                    EssayQuiz essayQuiz = essayQuizRepository.findById(quiz.getQuizId())
                            .orElseThrow(() -> new RuntimeException("EssayQuiz not found with id: " + quiz.getQuizId()));
                    question = essayQuiz.getEssayQuestion();
                    // 서술형은 정답이 없으므로 필요에 따라 처리
                    correctAnswer = "서술형 문제는 정답이 없습니다.";
                    break;
                case MULTIPLE_CHOICE:
                    MultipleChoiceQuiz mcQuiz = multipleChoiceQuizRepository.findById(quiz.getQuizId())
                            .orElseThrow(() -> new RuntimeException("MultipleChoiceQuiz not found with id: " + quiz.getQuizId()));
                    question = mcQuiz.getMultipleQuestion();
                    List<?> options = quizOptionRepository.findByQuizId(quiz.getQuizId());
                    correctAnswer = options.stream()
                            .filter(opt -> {
                                // QuizOption의 isCorrect 메서드 사용 (타입 캐스팅)
                                return ((Boolean) ((com.finbattle.domain.quiz.model.QuizOption) opt).isCorrect());
                            })
                            .map(opt -> ((com.finbattle.domain.quiz.model.QuizOption) opt).getOptionText())
                            .findFirst()
                            .orElse("정답 정보 없음");
                    break;
                default:
                    throw new BusinessException(BaseResponseStatus.INVALID_QUIZ_TYPE);
            }
            return QuizWrongNoteDto.builder()
                    .quizId(quiz.getQuizId())
                    .quizMode(quiz.getQuizMode().toString())
                    .question(question)
                    .correctAnswer(correctAnswer)
                    .userAnswer(log.getUserAnswer())
                    .createdAt(log.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}
