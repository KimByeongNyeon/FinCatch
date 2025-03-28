package com.finbattle.domain.banking.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finbattle.domain.banking.model.CommonRequestHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AllTransactionApiRequestDto {

    @JsonProperty("Header")
    private CommonRequestHeader Header;

    private String accountNo;      // 계좌번호
    private String startDate;      // 조회 시작일 (yyyyMMdd)
    private String endDate;        // 조회 종료일 (yyyyMMdd)
    private String transactionType; // 거래 구분 (A: 전체, D: 출금, C: 입금 등)
    private String orderByType;    // 정렬 방식 (ASC: 오름차순, DESC: 내림차순)
}
