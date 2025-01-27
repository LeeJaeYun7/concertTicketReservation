package concert.application.waitingqueue.business;

import concert.application.waitingqueue.presentation.response.TokenResponse;
import concert.domain.member.services.MemberService;
import concert.domain.waitingqueue.services.WaitingQueueService;
import concert.domain.waitingqueue.entities.vo.WaitingRankVo;
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

  public WaitingRankVo retrieveWaitingRank(long concertId, String uuid) {
    return waitingQueueService.retrieveWaitingRank(concertId, uuid);
  }
}
