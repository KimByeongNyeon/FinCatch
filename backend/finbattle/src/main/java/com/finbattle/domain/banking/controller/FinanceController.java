package com.finbattle.domain.banking.controller;

import com.finbattle.domain.banking.dto.account.AccountDetailDto;
import com.finbattle.domain.banking.dto.account.AccountRequestDto;
import com.finbattle.domain.banking.dto.account.FindAllAccountResponseDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionRequestDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionResponseDto;
import com.finbattle.domain.banking.service.FinanceService;
import com.finbattle.domain.oauth.dto.AuthenticatedUser;
import com.finbattle.global.common.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController implements FinanceApi {

    private final FinanceService financeService;

    @PostMapping("/account/all")
    @Override
    // 1. 금융망 유저 정보 조회
    public ResponseEntity<BaseResponse<FindAllAccountResponseDto>> findAllAccount(
        @AuthenticationPrincipal AuthenticatedUser detail) {

        FindAllAccountResponseDto res = financeService.findAllAccount(detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>(res));
    }

    @PatchMapping("/account/all")
    @Override
    // 1. 금융망 유저 정보 조회
    public ResponseEntity<BaseResponse<FindAllAccountResponseDto>> updateAllAccount(
        @AuthenticationPrincipal AuthenticatedUser detail) {

        FindAllAccountResponseDto res = financeService.updateAllAccount(detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>(res));
    }

    @PostMapping("/account/detail")
    @Override
    public ResponseEntity<BaseResponse<AccountDetailDto>> findAccountDetail(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody AccountRequestDto requestDto) {

        return ResponseEntity.ok(
            new BaseResponse<>(financeService.findAccountByNo(detail.getMemberId(),
                requestDto.accountNo())));
    }

    @PatchMapping("/account/change")
    @Override
    public ResponseEntity<BaseResponse<String>> changeMainAccount(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody AccountRequestDto requestDto) {
        financeService.changeAccount(detail.getMemberId(), requestDto.accountNo());
        return ResponseEntity.ok(
            new BaseResponse<>("계좌 변경 성공!"));
    }

    @PostMapping("/account/transactions")
    @Override
    public ResponseEntity<BaseResponse<LoadAllTransactionResponseDto>> findAlltransactionByNo(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody LoadAllTransactionRequestDto requestDto) {

        return ResponseEntity.ok(new BaseResponse<>(
            financeService.loadAllTransaction(detail.getMemberId(), requestDto)));
    }
}
