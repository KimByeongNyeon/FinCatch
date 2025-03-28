package com.finbattle.domain.member.dto;

import com.finbattle.domain.banking.model.FinanceMember;
import com.finbattle.domain.cat.entity.Cat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Schema(description = "사용자 정보 DTO")
public class MyInfoDto {

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 닉네임", example = "고양이사랑해")
    private String nickname;

    @Schema(description = "사용자 대표 캐릭터", example = "default")
    private String mainCat;

    @Schema(description = "사용자가 보유한 고양이 목록")
    private List<CatDto> cats;

    @Schema(description = "사용자의 경험치", example = "1500")
    private Long exp; // 경험치 (기본값: 0)

    @Schema(description = "사용자의 포인트", example = "500")
    private Long point; // 포인트 (기본값: 0)

    private String main_account;

    public MyInfoDto(String email, String nickname, List<Cat> Cats, String mainCat, Long exp,
        Long point, FinanceMember financeMember) {
        this.email = email;
        this.nickname = nickname;
        this.cats = Cats.stream()
            .map(CatDto::new)
            .collect(Collectors.toList());
        this.mainCat = mainCat;
        this.exp = exp;
        this.point = point;
        if (financeMember == null) {
            this.main_account = null;
        } else {
            this.main_account = financeMember.getMainaccount();
        }
    }
}
