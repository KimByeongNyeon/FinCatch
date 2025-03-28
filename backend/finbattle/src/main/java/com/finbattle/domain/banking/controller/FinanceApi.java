package com.finbattle.domain.banking.controller;

import com.finbattle.domain.banking.dto.account.AccountDetailDto;
import com.finbattle.domain.banking.dto.account.AccountRequestDto;
import com.finbattle.domain.banking.dto.account.FindAllAccountResponseDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionRequestDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionResponseDto;
import com.finbattle.domain.oauth.dto.AuthenticatedUser;
import com.finbattle.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "금융망 API", description = "금융망의 유저 금융 정보를 담당하는 API입니다.")
public interface FinanceApi {

    @Operation(
        summary = "모든 금융 계좌 조회",
        description = "로그인한 사용자의 금융망 계좌 정보를 모두 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계좌 조회 성공")
    })
    ResponseEntity<BaseResponse<FindAllAccountResponseDto>> findAllAccount(
        @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser detail);

    @Operation(
        summary = "모든 금융 계좌 갱신",
        description = "로그인한 사용자의 금융망 계좌 정보를 모두 최신화합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계좌 동기화 성공")
    })
    ResponseEntity<BaseResponse<FindAllAccountResponseDto>> updateAllAccount(
        @AuthenticationPrincipal AuthenticatedUser detail);

    @Operation(
        summary = "금융 계좌 상세 조회",
        description = "로그인한 사용자의 금융망 특정 계좌의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계좌 조회 성공")
    })
    ResponseEntity<BaseResponse<AccountDetailDto>> findAccountDetail(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody AccountRequestDto requestDto);

    @Operation(
        summary = "주 거래 계좌 변경",
        description = "로그인한 사용자의 주 거래 계좌를 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계좌 변경 성공")
    })
    ResponseEntity<BaseResponse<String>> changeMainAccount(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody AccountRequestDto requestDto);

    @Operation(
        summary = "금융 계좌 거래 내역 조회",
        description = "로그인한 사용자의 금융망 특정 계좌의 거래 내역 리스트를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계좌 조회 성공")
    })
    ResponseEntity<BaseResponse<LoadAllTransactionResponseDto>> findAlltransactionByNo(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody LoadAllTransactionRequestDto requestDto);
}
