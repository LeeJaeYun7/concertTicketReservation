package charge.application;

import lombok.RequiredArgsConstructor;
import member.application.MemberService;
import member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChargeFacade {

    private final MemberService memberService;
    private final ChargeService chargeService;

    @Transactional
    public long chargeBalance(String uuid, long amount) {

        validateMember(uuid);
        chargeService.getChargeByUuid(uuid);

        Member member = memberService.getMemberByUuidWithLock(uuid);
        long balance = member.getBalance();
        long updatedBalance = balance + amount;

        member.updateBalance(updatedBalance);
        chargeService.createCharge(uuid, amount);

        return updatedBalance;
    }

    public void validateMember(String uuid) {
        memberService.getMemberByUuid(uuid);
    }
}
