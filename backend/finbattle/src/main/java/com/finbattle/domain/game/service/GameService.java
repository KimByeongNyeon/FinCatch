package com.finbattle.domain.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbattle.domain.ai.service.EssayAiScoringService;
import com.finbattle.domain.game.dto.EventMessage;
import com.finbattle.domain.game.dto.EventType;
import com.finbattle.domain.game.dto.GameMemberStatus;
import com.finbattle.domain.game.model.GameData;
import com.finbattle.domain.game.repository.RedisGameRepository;
import com.finbattle.domain.quiz.dto.EssayQuizDto;
import com.finbattle.domain.quiz.dto.MultipleChoiceQuizDto;
import com.finbattle.domain.quiz.dto.ShortAnswerQuizDto;
import com.finbattle.domain.quiz.model.QuizMode;
import com.finbattle.global.common.redis.RedisPublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final RedisGameRepository redisGameRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final QuizTimerService quizTimerService; // 타이머 서비스 주입
    private final EssayAiScoringService essayAiScoringService; // AI 채점 서비스 주입

    // 문제 제시
    public void publishNextQuiz(Long roomId) {
        // 1) GameData 가져오기
        GameData gameData = redisGameRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("해당 roomId의 GameData가 존재하지 않습니다."));

        Integer quizNum = gameData.getQuizNum();
        if (quizNum == null) {
            quizNum = 1; // 기본값
        }

        // 아직 출제되지 않은 퀴즈 번호 리스트 만들기 (1~9)
        List<Integer> remaining = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if ((quizNum & (1 << (i - 1))) == 0) {
                remaining.add(i);
            }
        }

        if (remaining.isEmpty()) {
            log.info("모든 퀴즈가 출제되었습니다.");
            return;
        }

        // 랜덤으로 하나 선택
        int selectedQuizNum = remaining.get((int) (Math.random() * remaining.size()));

        quizNum |= (1 << (selectedQuizNum));
        gameData.setQuizNum(quizNum);
        gameData.setCurrentQuizNum(selectedQuizNum);
        redisGameRepository.save(gameData);

        // 2) quizNum에 따라 문제를 꺼내기
        if (selectedQuizNum >= 1 && selectedQuizNum <= 5) {
            // 객관식 문제 처리
            MultipleChoiceQuizDto quiz = gameData.getMultipleChoiceQuizList()
                .get(selectedQuizNum - 1);
            EventMessage<Map<String, Object>> message = new EventMessage<>(
                EventType.MULTIPLE_QUIZ,
                roomId,
                Map.of(
                    "quizId", quiz.getQuizId(),
                    "question", quiz.getMultipleQuestion(),
                    "options", quiz.getQuizOptions()
                )
            );
            publishToRoom(roomId, message);
            quizTimerService.startQuizTimer(roomId, quiz.getQuizId(), QuizMode.MULTIPLE_CHOICE,
                quiz);

        } else if (selectedQuizNum >= 6 && selectedQuizNum <= 8) {
            // 단답형 문제 처리
            int index = selectedQuizNum - 6;
            ShortAnswerQuizDto quiz = gameData.getShortAnswerQuizList().get(index);
            EventMessage<Map<String, Object>> message = new EventMessage<>(
                EventType.SHORT_QUIZ,
                roomId,
                Map.of(
                    "quizId", quiz.getQuizId(),
                    "question", quiz.getShortQuestion()
                )
            );
            publishToRoom(roomId, message);
            quizTimerService.startQuizTimer(roomId, quiz.getQuizId(), QuizMode.SHORT_ANSWER, quiz);

        } else if (selectedQuizNum == 9) {
            // 서술형 문제 처리
            EssayQuizDto quiz = gameData.getEssayQuiz();
            EventMessage<Map<String, Object>> message = new EventMessage<>(
                EventType.ESSAY_QUIZ,
                roomId,
                Map.of(
                    "quizId", quiz.getQuizId(),
                    "question", quiz.getEssayQuestion()
                )
            );
            publishToRoom(roomId, message);
            quizTimerService.startQuizTimer(roomId, quiz.getQuizId(), QuizMode.ESSAY, quiz);
        } else {
            log.info("모든 퀴즈 진행 완료 (quizNum={})", quizNum);
        }
    }

    /**
     * 정답 체크 및 결과 발행 → "topic/game/{roomId}"
     */
    public void checkQuizAnswer(Long roomId, String userAnswer, Long memberId) {
        // GameData 조회
        GameData gameData = redisGameRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("해당 roomId의 GameData가 없습니다."));

        int quizNum = gameData.getCurrentQuizNum();
        boolean isCorrect = false;
        Long quizId = null;

        if (quizNum >= 1 && quizNum <= 5) {
            // 객관식 문제
            MultipleChoiceQuizDto quiz = gameData.getMultipleChoiceQuizList().get(quizNum - 1);
            quizId = quiz.getQuizId();
            Integer ans = Integer.parseInt(userAnswer);
            isCorrect = quiz.getQuizOptions().stream()
                .anyMatch(option -> option.getOptionNumber() == ans && option.isCorrect());

        } else if (quizNum >= 6 && quizNum <= 8) {
            // 단답형 문제
            int index = quizNum - 6;
            ShortAnswerQuizDto quiz = gameData.getShortAnswerQuizList().get(index);
            quizId = quiz.getQuizId();
            isCorrect = quiz.getShortAnswer().equalsIgnoreCase(userAnswer.trim());

        } else if (quizNum == 9) {
            // 서술형 문제: AI 채점을 호출하여 0~100점 사이의 점수를 획득
            EssayQuizDto quiz = gameData.getEssayQuiz();
            quizId = quiz.getQuizId();
            log.info("quizId={}", quizId);
            int score = essayAiScoringService.scoreEssayAnswer(quiz.getEssayQuestion(), userAnswer);
            // 예시 기준: 점수가 70 이상이면 정답으로 간주
            isCorrect = score >= 70;
            Map<String, Object> essayResult = Map.of(
                "quizId", quizId,
                "score", score,
                "memberId", memberId
            );
            EventMessage<Map<String, Object>> essayResultMessage = new EventMessage<>(
                EventType.QUIZ_RESULT,
                roomId,
                essayResult
            );
            publishToRoom(roomId, essayResultMessage);

            if (isCorrect) {
                quizTimerService.cancelQuizTasks(roomId);
                gameData.setQuizNum(quizNum + 1);
                redisGameRepository.save(gameData);
            }
            updateUserLives(gameData, memberId, isCorrect);
            return;
        }

        // 객관식 및 단답형 문제 결과 메시지 발행
        EventMessage<Map<String, Object>> resultMessage = new EventMessage<>(
            EventType.QUIZ_RESULT,
            roomId,
            Map.of(
                "quizId", quizId,
                "result", isCorrect ? "정답입니다" : "오답입니다",
                "memberId", memberId
            )
        );
        publishToRoom(roomId, resultMessage);

        if (isCorrect) {
            quizTimerService.cancelQuizTasks(roomId);
            gameData.setQuizNum(quizNum + 1);
            redisGameRepository.save(gameData);
        }
        updateUserLives(gameData, memberId, isCorrect);
    }

    /**
     * (4) 멤버들의 라이프 업데이트 → WebSocket으로 반영
     */
    private void updateUserLives(GameData gameData, Long targetMemberId, boolean isCorrect) {
        List<GameMemberStatus> userList = gameData.getGameMemberStatusList();
        if (userList == null) {
            return;
        }
        if (isCorrect) {
            // 정답자 외 모든 유저 라이프 1 감소 (null 안전 비교)
            for (GameMemberStatus ms : userList) {
                if (!Objects.equals(ms.getMemberId(), targetMemberId)) {
                    ms.setLife(ms.getLife() - 1);
                }
            }
        } else {
            // 오답인 경우 본인만 라이프 1 감소
            userList.stream()
                .filter(ms -> Objects.equals(ms.getMemberId(), targetMemberId))
                .forEach(ms -> ms.setLife(ms.getLife() - 1));
        }
        gameData.setGameMemberStatusList(userList);
        redisGameRepository.save(gameData);

        EventMessage<List<GameMemberStatus>> userStatusMessage = new EventMessage<>(
            EventType.USER_STATUS, gameData.getRoomId(), userList
        );
        publishToRoom(gameData.getRoomId(), userStatusMessage);
    }

    /**
     * (5) WebSocket + Redis를 통한 메시지 발행 → "topic/game/{roomId}"
     */
    private void publishToRoom(Long roomId, EventMessage<?> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisPublisher.publish("game:" + roomId, jsonMessage);
            log.info("🚀 Sent WebSocket message to room {}: {}", roomId, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 변환 실패: {}", e.getMessage());
        }
    }
}
