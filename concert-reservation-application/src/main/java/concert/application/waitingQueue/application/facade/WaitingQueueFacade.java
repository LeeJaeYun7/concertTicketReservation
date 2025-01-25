package concert.application.waitingQueue.application.facade;

import concert.application.waitingQueue.application.dto.TokenResponse;
import concert.domain.member.service.MemberService;
import concert.domain.waitingQueue.application.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
