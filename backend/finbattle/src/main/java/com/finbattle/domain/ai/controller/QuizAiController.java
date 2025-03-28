package com.finbattle.domain.ai.controller;

import com.finbattle.domain.ai.dto.QuizAiRequestDto;
import com.finbattle.domain.ai.dto.QuizAiResponseDto;
import com.finbattle.domain.ai.dto.QuizWrongNoteDto;
import com.finbattle.domain.ai.service.QuizAiService;
import com.finbattle.domain.ai.service.QuizWrongNoteService;
import com.finbattle.global.common.Util.AuthenticationUtil;
import com.finbattle.global.common.model.dto.BaseResponse;
import com.finbattle.global.common.model.dto.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ai/analysis/regular")
@RequiredArgsConstructor
@Tag(name = "일반 퀴즈 분석 API", description = "quiz_log 기반 일반 퀴즈 분석 및 오답 노트를 제공합니다.")
public class QuizAiController {

    private final QuizAiService quizAiService;
    private final QuizWrongNoteService quizWrongNoteService;
    private final AuthenticationUtil authenticationUtil;

    @Operation(summary = "일반 퀴즈 분석", description = "사용자가 푼 일반 퀴즈의 ID를 기반으로 AI 피드백(분석, 취약점, 추천 학습)을 분석합니다.")
    @PostMapping("/analyze")
    public ResponseEntity<BaseResponse<QuizAiResponseDto>> analyze(@RequestBody QuizAiRequestDto dto) {
        try {
            Long memberId = authenticationUtil.getMemberId();
            QuizAiResponseDto feedback = quizAiService.analyze(memberId, dto);
            return ResponseEntity.ok(new BaseResponse<>(feedback));
        } catch (RuntimeException e) {
            return ResponseEntity.status(BaseResponseStatus.QUIZ_LOG_NOT_FOUND.getHttpStatus())
                    .body(new BaseResponse<>(BaseResponseStatus.QUIZ_LOG_NOT_FOUND));
        } catch (Exception e) {
            return ResponseEntity.status(BaseResponseStatus.AI_ANALYSIS_FAILED.getHttpStatus())
                    .body(new BaseResponse<>(BaseResponseStatus.AI_ANALYSIS_FAILED));
        }
    }

    @Operation(summary = "일반 퀴즈 오답 노트 조회", description = "현재 로그인한 사용자의 quiz_log 기반 오답 노트를 조회합니다.")
    @GetMapping("/wrong")
    public ResponseEntity<BaseResponse<List<QuizWrongNoteDto>>> getWrongNotes() {
        Long memberId = authenticationUtil.getMemberId();
        List<QuizWrongNoteDto> wrongNotes = quizWrongNoteService.getWrongNotesByMember(memberId);
        return ResponseEntity.ok(new BaseResponse<>(wrongNotes));
    }
}
