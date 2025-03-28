package com.finbattle.domain.banking.repository;

import com.finbattle.domain.banking.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceAccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByFinancemember_MemberId(Long memberId);

    Optional<Account> findByAccountNo(String accountNo);
}
