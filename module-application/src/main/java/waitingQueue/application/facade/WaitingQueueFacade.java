package waitingQueue.application.facade;

import lombok.RequiredArgsConstructor;
import member.application.MemberService;
import org.springframework.stereotype.Component;
import waitingQueue.application.WaitingQueueService;
import waitingQueue.application.dto.TokenResponse;

@Component
@RequiredArgsConstructor
public class WaitingQueueFacade {

    private final MemberService memberService;
    private final WaitingQueueService waitingQueueService;

    public TokenResponse retrieveToken(long concertId, String uuid) {
        memberService.getMemberByUuid(uuid);

        String token = waitingQueueService.retrieveToken(concertId, uuid);
        return TokenResponse.of(token);
    }
}
