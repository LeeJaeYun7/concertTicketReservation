package com.example.concert.charge.service;

import com.example.concert.common.CustomException;
import com.example.concert.member.domain.Member;
import com.example.concert.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChargeFacade {

    private final MemberService memberService;
    private final ChargeService chargeService;

    public ChargeFacade(MemberService memberService, ChargeService chargeService){
        this.memberService = memberService;
        this.chargeService = chargeService;
    }

    @Transactional
    public long chargeBalance(String uuid, long amount) {
        try {
            System.out.println("전달된 uuid는? " + uuid);

            validateMember(uuid);
            System.out.println("validateMember 완료");
            Member member = memberService.getMemberByUuidWithLock(uuid);
            System.out.println("lock으로 멤버 가져오기 완료");
            long balance = member.getBalance();
            long updatedBalance = balance + amount;
            System.out.println("balance, updatedBalance는? " + balance + ", " + updatedBalance);

            member.updateBalance(updatedBalance);
            chargeService.createCharge(uuid, amount);

            return updatedBalance; // 충전 후 새로운 잔액 반환
        } catch (CustomException e) {
            System.err.println("회원 확인 실패: " + e.getMessage());
            return -1; // 회원이 존재하지 않을 경우 -1 반환
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("예외 발생: " + e.getMessage());
            return -1; // 일반적인 예외 발생 시 -1 반환
        }
    }

    public void validateMember(String uuid) {
        memberService.getMemberByUuid(uuid);
    }
}
