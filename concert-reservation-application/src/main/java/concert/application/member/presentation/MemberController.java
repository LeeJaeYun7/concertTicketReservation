package concert.application.member.presentation;

import concert.application.member.business.MemberFacade;
import concert.application.member.presentation.request.MemberRequest;
import concert.application.member.presentation.response.MemberBalanceResponse;
import concert.application.member.presentation.response.MemberResponse;
import concert.domain.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

  private final MemberFacade memberFacade;


  @PostMapping("/api/v1/member")
  public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest memberRequest) {
    String name = memberRequest.getName();
    MemberVO memberVO = memberFacade.createMember(name);
    MemberResponse memberResponse = MemberResponse.of(memberVO.getUuid(), memberVO.getName());

    return ResponseEntity.status(HttpStatus.CREATED).body(memberResponse);
  }

  @GetMapping("/api/v1/member/balance")
  public ResponseEntity<MemberBalanceResponse> getMemberBalance(@RequestParam(value = "uuid") String uuid) {
    long balance = memberFacade.getMemberBalance(uuid);
    MemberBalanceResponse memberBalanceResponse = MemberBalanceResponse.of(balance);

    return ResponseEntity.status(HttpStatus.CREATED).body(memberBalanceResponse);
  }

  @GetMapping("/api/v1/member/download")
  public ResponseEntity<List<MemberResponse>> getMembers() {
    List<MemberResponse> memberResponseList = memberFacade.getMembers();
    return ResponseEntity.status(HttpStatus.CREATED).body(memberResponseList);
  }
}
