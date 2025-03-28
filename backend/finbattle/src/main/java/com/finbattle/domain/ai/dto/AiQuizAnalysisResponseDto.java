package com.finbattle.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 소비내역 기반 AI퀴즈 분석 결과를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class AiQuizAnalysisResponseDto {
    private String analysis;        // 문제 분석 내용
    private String weakness;        // 취약점 설명
    private String recommendation;  // 추천 학습 방향
}
