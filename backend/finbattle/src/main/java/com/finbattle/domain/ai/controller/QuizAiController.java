package com.finbattle.domain.ai.controller;

import com.finbattle.domain.ai.dto.QuizAiRequestDto;
import com.finbattle.domain.ai.dto.QuizAiResponseDto;
import com.finbattle.domain.ai.service.QuizAnalysisService;
import com.finbattle.global.common.Util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class QuizAiController {

    private final QuizAnalysisService quizAnalysisService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping("/analyze")
    public ResponseEntity<QuizAiResponseDto> analyze(@RequestBody QuizAiRequestDto dto) {
        Long memberId = authenticationUtil.getMemberId();  // 🔐 JWT에서 사용자 ID 추출
        QuizAiResponseDto feedback = quizAnalysisService.analyze(memberId, dto);  // 🧠 구조화된 응답
        return ResponseEntity.ok(feedback);  // 📤 JSON 응답
    }
}
