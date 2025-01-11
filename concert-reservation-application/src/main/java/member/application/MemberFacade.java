package member.application;

import lombok.RequiredArgsConstructor;
import member.application.dto.response.MemberResponse;
import member.domain.Member;
import member.domain.vo.MemberVO;
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
