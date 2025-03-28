package com.finbattle.domain.banking.service;

import com.finbattle.domain.banking.dto.transaction.AllTransactionApiRequestDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionRequestDto;
import com.finbattle.domain.banking.dto.transaction.LoadAllTransactionResponseDto;
import com.finbattle.domain.banking.model.CommonRequestHeader;
import com.finbattle.domain.banking.model.FinanceMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceTransactionService {

    private final FinanceApiClient financeApiClient;

    public LoadAllTransactionResponseDto loadAllTransaction(LoadAllTransactionRequestDto dto,
        String financeKey, FinanceMember member) {

        String apiPath = "inquireTransactionHistoryList";
        CommonRequestHeader header = new CommonRequestHeader(apiPath, financeKey,
            member.getFinanceKey());

        AllTransactionApiRequestDto requestbody = toApiRequest(dto, header);
        log.info("Request Data: {}", requestbody.toString());
        return financeApiClient.post("edu/demandDeposit/" + apiPath,
            requestbody, LoadAllTransactionResponseDto.class);
    }

    private AllTransactionApiRequestDto toApiRequest(LoadAllTransactionRequestDto req,
        CommonRequestHeader header) {
        return AllTransactionApiRequestDto.builder()
            .Header(header)
            .accountNo(req.getAccountNo())
            .startDate(req.getStartDate())
            .endDate(req.getEndDate())
            .transactionType(req.getTransactionType())
            .orderByType("DESC") // 기본 정렬 설정
            .build();
    }


}
