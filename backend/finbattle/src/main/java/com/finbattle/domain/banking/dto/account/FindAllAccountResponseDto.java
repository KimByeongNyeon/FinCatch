package com.finbattle.domain.banking.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자의 전체 계좌 목록과 대표 계좌 정보를 담는 DTO")
public class FindAllAccountResponseDto {

    @Schema(
        description = "대표 계좌 번호",
        example = "3333026965506"
    )
    private String mainAccount;

    @Schema(
        description = "사용자의 전체 계좌 리스트"
    )
    private List<AccountResponseDto> accounts;
}
