package concert.interfaces.member;

import concert.domain.member.entities.vo.MemberVO;
import concert.domain.member.services.MemberService;
import concert.interfaces.member.request.MemberRequest;
import concert.interfaces.member.response.MemberBalanceResponse;
import concert.interfaces.member.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberController {

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
    List<MemberVO> memberVOs = memberService.getMembers();
    List<MemberResponse> memberResponses = memberVOs.stream()
                                                    .map(MemberResponse::of)
                                                    .collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.CREATED).body(memberResponses);
  }
}
