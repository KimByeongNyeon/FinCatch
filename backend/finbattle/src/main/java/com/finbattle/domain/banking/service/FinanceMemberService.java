package com.finbattle.domain.banking.service;

import static com.finbattle.global.common.model.dto.BaseResponseStatus.MEMBER_NOT_FOUND;

import com.finbattle.domain.banking.dto.financemember.FinanceMemberRequestDto;
import com.finbattle.domain.banking.dto.financemember.FinanceMemberResponseDto;
import com.finbattle.domain.banking.exception.MemberNotFoundException;
import com.finbattle.domain.banking.model.FinanceMember;
import com.finbattle.domain.banking.repository.FinanceMemberRepository;
import com.finbattle.domain.member.model.Member;
import com.finbattle.domain.member.repository.MemberRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceMemberService {

    private final FinanceMemberRepository financeMemberRepository;
    private final MemberRepository memberRepository;
    private final FinanceApiClient financeApiClient;

    @Transactional
    public FinanceMember loadOrRegister(Long memberId, String financeKey) {
        return financeMemberRepository.findById(memberId)
            .orElseGet(() -> {
                try {
                    return searchmember(memberId, financeKey);
                } catch (MemberNotFoundException e) {
                    log.info("금융망 ID 생성");
                    return register(memberId, financeKey);
                }
            });
    }

    public void changeMainAccount(FinanceMember member, String accountNo) {
        member.changeMainAccount(accountNo);
        financeMemberRepository.save(member);
    }

    private FinanceMember register(Long memberId, String financeKey) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
        FinanceMemberRequestDto dto = new FinanceMemberRequestDto(financeKey,
            member.getEmail());

        FinanceMemberResponseDto res = financeApiClient.post("member/", dto,
            FinanceMemberResponseDto.class);
        FinanceMember fmember = new FinanceMember(res, member);
        financeMemberRepository.save(fmember);
        return fmember;
    }

    private FinanceMember searchmember(Long memberId, String financeKey) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
        ;
        FinanceMemberRequestDto dto = new FinanceMemberRequestDto(financeKey,
            member.getEmail());

        FinanceMemberResponseDto res = financeApiClient.post(
            "member/search", dto, FinanceMemberResponseDto.class,
            () -> new MemberNotFoundException("존재하지 않는 금융회원입니다.")
        );

        FinanceMember fmember = new FinanceMember(res, member);
        financeMemberRepository.save(fmember);
        return fmember;
    }

}
