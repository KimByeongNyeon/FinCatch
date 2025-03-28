package com.finbattle.domain.banking.service;

import static com.finbattle.global.common.model.dto.BaseResponseStatus.ACCOUNT_NOT_VALID;

import com.finbattle.domain.banking.dto.account.AccountDetailDto;
import com.finbattle.domain.banking.dto.account.AccountResponseDto;
import com.finbattle.domain.banking.dto.account.FindAllAccountResponseDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionRequestDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionResponseDto;
import com.finbattle.domain.banking.model.FinanceMember;
import com.finbattle.global.common.exception.exception.BusinessException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinanceFacadeService implements FinanceService {

    private final FinanceMemberService financeMemberService;
    private final FinanceAccountService financeAccountService;
    private final FinanceTransactionService financeTransactionService;

    @Value("${app.financeKey}")
    private String financeKey;

    @Override
    public FindAllAccountResponseDto findAllAccount(Long memberId) {
        // ✅ 1. 회원을 "무조건 확보" (없으면 금융망 등록까지 완료)
        FinanceMember member = financeMemberService.loadOrRegister(memberId, financeKey);

        log.info("✅ 금융회원 확보 완료: {}", member.getFinanceKey());

        List<AccountResponseDto> lists = financeAccountService.findAllAccount(financeKey, member);
        FindAllAccountResponseDto res = new FindAllAccountResponseDto();

        // ✅ 3. 메인 계좌 설정
        if (member.getMainaccount().isEmpty() && !lists.isEmpty()) {
            member.changeMainAccount(lists.get(0).getAccountNo());
        }

        res.setMainAccount(member.getMainaccount());
        res.setAccounts(lists);
        return res;
    }

    @Override
    public AccountDetailDto findAccountByNo(Long memberId, String accountNo) {
        FinanceMember member = financeMemberService.loadOrRegister(memberId, financeKey);
        return financeAccountService.findAccountByNo(accountNo, financeKey, member);
    }

    @Override
    public void changeAccount(Long memberId, String accountNo) {
        FinanceMember member = financeMemberService.loadOrRegister(memberId, financeKey);
        if (financeAccountService.validAccountNo(accountNo, member)) {
            financeMemberService.changeMainAccount(member, accountNo);
        } else {
            throw new BusinessException(ACCOUNT_NOT_VALID);
        }
    }

    @Override
    public LoadAllTransactionResponseDto loadAllTransaction(Long memberId,
        LoadAllTransactionRequestDto dto) {
        FinanceMember member = financeMemberService.loadOrRegister(memberId, financeKey);
        return financeTransactionService.loadAllTransaction(dto, financeKey, member);
    }
}
