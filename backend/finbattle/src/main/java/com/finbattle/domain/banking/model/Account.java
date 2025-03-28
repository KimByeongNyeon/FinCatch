package com.finbattle.domain.banking.model;

import com.finbattle.domain.banking.dto.account.AccountDetailDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

@Entity
@NoArgsConstructor
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private FinanceMember financemember;

    @Column(name = "account_no", nullable = false, unique = true)
    private String accountNo;

    @Column(name = "bank_code", nullable = false)
    private Integer bankCode;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_balance", nullable = false)
    private Long accountBalance;

    @Column(name = "account_type", nullable = false)
    private int accountType;

    public Account(AccountDetailDto dto, FinanceMember member) {
        this.financemember = member;
        this.accountNo = dto.getAccountNo();
        this.bankCode = Integer.parseInt(dto.getBankCode());
        this.accountName = dto.getAccountName();
        this.accountBalance = Long.parseLong(dto.getAccountBalance());
        this.accountType = Integer.parseInt(dto.getAccountTypeCode());
    }

    public void changeAccountBalance(Long balance) {
        this.accountBalance = balance;
    }
}
