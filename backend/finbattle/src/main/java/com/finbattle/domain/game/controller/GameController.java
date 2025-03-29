package com.finbattle.domain.game.controller;

import com.finbattle.domain.chat.model.StompPrincipal;
import com.finbattle.domain.game.dto.AnswerRequest;
import com.finbattle.domain.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    //퀴즈 정보 가지고 오기
    @MessageMapping("/game/{roomId}/getQuiz")
    public void getQuiz(@DestinationVariable Long roomId) {
        log.info("Received quiz request for roomId: {}", roomId);
        gameService.publishNextQuiz(roomId);
    }

    //퀴즈 정답 체크
    @MessageMapping("/game/{roomId}/checkAnswer")
    public void checkAnswer(@DestinationVariable Long roomId, @Payload AnswerRequest request,
        @AuthenticationPrincipal StompPrincipal member) {
        Long memberId = member.getMemberId();
        log.info("Received quiz answer for roomId: {} with answer: {} from memberId: {}",
            roomId, request.getUserAnswer(), memberId);
        gameService.checkQuizAnswer(roomId, request.getUserAnswer(),
            memberId);
    }

}
