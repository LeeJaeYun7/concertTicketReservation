package member.presentation;

import lombok.RequiredArgsConstructor;
import member.application.MemberFacade;
import member.application.MemberService;
import member.application.dto.request.MemberRequest;
import member.application.dto.response.MemberBalanceResponse;
import member.application.dto.response.MemberResponse;
import member.domain.vo.MemberVO;
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
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest memberRequest){
        String name = memberRequest.getName();
        MemberVO memberVO = memberService.createMember(name);
        MemberResponse memberResponse = MemberResponse.of(memberVO.getUuid(), memberVO.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(memberResponse);
    }

    @GetMapping("/api/v1/member/balance")
    public ResponseEntity<MemberBalanceResponse> getMemberBalance(@RequestParam(value="uuid") String uuid) {
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
