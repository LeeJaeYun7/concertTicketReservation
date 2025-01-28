package concert.application.member.business;

import concert.domain.member.services.MemberService;
import concert.domain.member.entities.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberFacade {

  private final MemberService memberService;

  public List<MemberVO> getMembers() {
      return memberService.getMembers();
  }

  public MemberVO createMember(String name) {
    return memberService.createMember(name);
  }

  public long getMemberBalance(String uuid) {
    return memberService.getMemberBalance(uuid);
  }
}
