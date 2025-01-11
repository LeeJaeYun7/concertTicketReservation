package concert.application.member.presentation;

import concert.application.member.application.MemberFacade;
import concert.application.member.application.dto.request.MemberRequest;
import concert.application.member.application.dto.response.MemberBalanceResponse;
import concert.application.member.application.dto.response.MemberResponse;
import concert.domain.member.application.MemberService;
import concert.domain.member.domain.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

  private final MemberFacade memberFacade;
  private final MemberService memberService;

  @PostMapping("/api/v1/member")
  public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest memberRequest) {
    String name = memberRequest.getName();
    MemberVO memberVO = memberService.createMember(name);
    MemberResponse memberResponse = MemberResponse.of(memberVO.getUuid(), memberVO.getName());

    return ResponseEntity.status(HttpStatus.CREATED).body(memberResponse);
  }

  @GetMapping("/api/v1/member/balance")
  public ResponseEntity<MemberBalanceResponse> getMemberBalance(@RequestParam(value = "uuid") String uuid) {
    long balance = memberService.getMemberBalance(uuid);
    MemberBalanceResponse memberBalanceResponse = MemberBalanceResponse.of(balance);

    return ResponseEntity.status(HttpStatus.CREATED).body(memberBalanceResponse);
  }

  @GetMapping("/api/v1/member/download")
  public ResponseEntity<List<MemberResponse>> getMembers() {
    List<MemberResponse> memberResponseList = memberFacade.getMembers();
    return ResponseEntity.status(HttpStatus.CREATED).body(memberResponseList);
  }
}
