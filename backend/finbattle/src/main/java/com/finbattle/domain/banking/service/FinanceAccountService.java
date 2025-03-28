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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            accounts = findAccountsApi(financeKey, member, accounts);
        }

        return convertToDto(accounts);
    }

    @Transactional
    public List<AccountResponseDto> updateAccountList(String financeKey, FinanceMember member) {
        List<Account> accounts = financeAccountRepository.findByFinancemember_MemberId(
            member.getMemberId());
        accounts = findAccountsApi(financeKey, member, accounts);

        return convertToDto(accounts);
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
        Account myaccount = financeAccountRepository.findByAccountNo(accountNo).orElse(null);
        if (balance != Objects.requireNonNull(myaccount).getAccountBalance()) {
            myaccount.changeAccountBalance(balance);
            financeAccountRepository.save(myaccount);
        }
        return account;
    }

    private List<Account> findAccountsApi(String financeKey, FinanceMember member,
        List<Account> dbAccounts) {
        String apiPath = "inquireDemandDepositAccountList";
        CommonRequestHeader header = new CommonRequestHeader(apiPath, financeKey,
            member.getFinanceKey());
        Map<String, CommonRequestHeader> dto = Map.of("Header", header);
        FindAllAccountApiResponseDto res = financeApiClient.post("edu/demandDeposit/" + apiPath,
            dto, FindAllAccountApiResponseDto.class);

        // 동기화 처리 로직 분리
        return synchronizeAccounts(res.getREC(), dbAccounts, member);
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

    private List<Account> synchronizeAccounts(List<AccountDetailDto> apiAccountDtos,
        List<Account> existingAccounts,
        FinanceMember member) {
        Map<String, Account> existingMap = existingAccounts.stream()
            .collect(Collectors.toMap(Account::getAccountNo, a -> a));

        List<Account> accountsToSave = new ArrayList<>();
        List<Account> resultAccounts = new ArrayList<>();

        for (AccountDetailDto detailDto : apiAccountDtos) {
            Account apiAccount = new Account(detailDto, member);
            String accNum = apiAccount.getAccountNo();

            if (existingMap.containsKey(accNum)) {
                Account existing = existingMap.get(accNum);
                Long balance = Long.parseLong(detailDto.getAccountBalance());
                if (!existing.getAccountBalance()
                    .equals(balance)) {
                    existing.changeAccountBalance(balance);
                    accountsToSave.add(existing);
                }
                resultAccounts.add(existing);
            } else {
                accountsToSave.add(apiAccount);
                resultAccounts.add(apiAccount);
            }
        }
        log.info("변경할 계좌 리스트: {}", accountsToSave.toString());
        financeAccountRepository.saveAll(accountsToSave);

        return resultAccounts;
    }

    private List<AccountResponseDto> convertToDto(List<Account> accounts) {
        return accounts.stream()
            .map(AccountResponseDto::new)
            .collect(Collectors.toList());
    }
}
