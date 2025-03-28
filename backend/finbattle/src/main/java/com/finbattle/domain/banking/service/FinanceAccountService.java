package com.finbattle.domain.banking.service;

import static com.finbattle.global.common.model.dto.BaseResponseStatus.ACCOUNT_NOT_FOUND;

import com.finbattle.domain.banking.dto.account.AccountDetailDto;
import com.finbattle.domain.banking.dto.account.AccountResponseDto;
import com.finbattle.domain.banking.dto.account.FindAllAccountApiResponseDto;
import com.finbattle.domain.banking.dto.account.FindByNoResponseDto;
import com.finbattle.domain.banking.model.Account;
import com.finbattle.domain.banking.model.CommonRequestHeader;
import com.finbattle.domain.banking.model.FinanceMember;
import com.finbattle.domain.banking.repository.FinanceAccountRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceAccountService {

    private final FinanceAccountRepository financeAccountRepository;
    private final FinanceApiClient financeApiClient;

    public List<AccountResponseDto> findAllAccount(String financeKey, FinanceMember member) {
        List<Account> accounts = financeAccountRepository.findByFinancemember_MemberId(
            member.getMemberId());
        if (accounts.isEmpty()) {
            accounts = findAccountsApi(financeKey, member);
        }
        List<AccountResponseDto> result = new ArrayList<>();
        for (Account account : accounts) {
            AccountResponseDto dto = new AccountResponseDto(account);
            result.add(dto);
        }
        return result;
    }

    public boolean validAccountNo(String accountNo, FinanceMember member) {
        List<Account> accounts = financeAccountRepository.findByFinancemember_MemberId(
            member.getMemberId());

        for (Account account : accounts) {
            if (account.getAccountNo().equals(accountNo)) {
                return true;
            }
        }
        return false;
    }

    public AccountDetailDto findAccountByNo(String accountNo, String financeKey,
        FinanceMember member) {
        AccountDetailDto account = findAccountApi(accountNo, financeKey, member).getREC();
        if (account.getAccountNo().isEmpty()) {
            throw new BusinessException(ACCOUNT_NOT_FOUND);
        }
        long balance = Long.parseLong(account.getAccountBalance());
        Account myaccount = financeAccountRepository.findByAccountNo(accountNo)
            .orElse(null);
        if (balance != Objects.requireNonNull(myaccount).getAccountBalance()) {
            myaccount.changeAccountBalance(balance);
            financeAccountRepository.save(myaccount);
        }
        return account;
    }

    private List<Account> findAccountsApi(String financeKey, FinanceMember member) {
        String apiPath = "inquireDemandDepositAccountList";
        CommonRequestHeader header = new CommonRequestHeader(apiPath, financeKey,
            member.getFinanceKey());
        Map<String, CommonRequestHeader> dto = new HashMap<>();
        dto.put("Header", header);
        FindAllAccountApiResponseDto res = financeApiClient.post("edu/demandDeposit/" + apiPath,
            dto, FindAllAccountApiResponseDto.class);

        List<Account> accounts = new ArrayList<>();
        for (AccountDetailDto detailDto : res.getREC()) {
            Account account = new Account(detailDto, member);
            accounts.add(account);
        }
        financeAccountRepository.saveAll(accounts);
        return accounts;
    }

    private FindByNoResponseDto findAccountApi(String accountNo,
        String financeKey, FinanceMember member) {
        String apiPath = "inquireDemandDepositAccount";
        CommonRequestHeader header = new CommonRequestHeader(apiPath, financeKey,
            member.getFinanceKey());
        Map<String, Object> dto = new HashMap<>();
        dto.put("Header", header);
        dto.put("accountNo", accountNo);
        return financeApiClient.post("edu/demandDeposit/" + apiPath,
            dto, FindByNoResponseDto.class);
    }
}
