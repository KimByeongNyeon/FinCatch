package com.finbattle.domain.banking.dto.account;

import com.finbattle.domain.banking.model.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AccountResponseDto {

    @Schema(description = "계좌 번호", example = "3333026965506")
    private final String accountNo;

    @Schema(description = "은행 코드", example = "090")
    private final String bankCode;

    @Schema(description = "계좌 이름", example = "수시입출금")
    private final String accountName;

    @Schema(description = "계좌 잔액", example = "0")
    private final Long accountBalance;

    public AccountResponseDto(Account account) {
        this.accountNo = account.getAccountNo();
        this.bankCode = account.getBankCode();
        this.accountName = account.getAccountName();
        this.accountBalance = account.getAccountBalance();
    }
}
