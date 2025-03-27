package com.finbattle.domain.banking.service;

import com.finbattle.domain.banking.dto.account.AccountDetailDto;
import com.finbattle.domain.banking.dto.account.FindAllAccountResponseDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionRequestDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionResponseDto;

public interface FinanceService {

    FindAllAccountResponseDto findAllAccount(Long memberId);

    AccountDetailDto findAccountByNo(Long memberId, Long accountNo);

    LoadAllTransactionResponseDto loadAllTransaction(Long memberId,
        LoadAllTransactionRequestDto dto);
}
