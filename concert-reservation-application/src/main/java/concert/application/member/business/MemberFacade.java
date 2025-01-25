package concert.application.member.business;

import concert.application.member.presentation.response.MemberResponse;
import concert.domain.member.service.MemberService;
import concert.domain.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberFacade {

  private final MemberService memberService;

  public List<MemberResponse> getMembers() {
    List<MemberVO> members = memberService.getMembers();

    return members.stream()
            .map(memberVO -> new MemberResponse(memberVO.getUuid(), memberVO.getName()))
            .toList();
  }

  public MemberVO createMember(String name) {
    return memberService.createMember(name);
  }

  public long getMemberBalance(String uuid) {
    return memberService.getMemberBalance(uuid);
  }
}
