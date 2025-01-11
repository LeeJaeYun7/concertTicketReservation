package concert.application.member.application;

import concert.application.member.application.dto.response.MemberResponse;
import concert.domain.member.application.MemberService;
import concert.domain.member.domain.vo.MemberVO;
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
}
