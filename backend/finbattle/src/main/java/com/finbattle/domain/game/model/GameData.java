package com.finbattle.domain.game.model;

import com.finbattle.domain.game.dto.GameMemberStatus;
import com.finbattle.domain.quiz.dto.EssayQuizDto;
import com.finbattle.domain.quiz.dto.MultipleChoiceQuizDto;
import com.finbattle.domain.quiz.dto.ShortAnswerQuizDto;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash("game")
public class GameData {

    @Id
    Long roomId;

    //퀴즈 번호
    Integer quizNum;
    Integer currentQuizNum;

    //game 이용자들
    List<GameMemberStatus> gameMemberStatusList;

    //문제 정보들
    List<MultipleChoiceQuizDto> multipleChoiceQuizList;
    List<ShortAnswerQuizDto> shortAnswerQuizList;
    EssayQuizDto essayQuiz;
}
