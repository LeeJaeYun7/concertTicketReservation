package concert.application.waitingQueue.business;

import concert.application.waitingQueue.presentation.response.TokenResponse;
import concert.domain.member.service.MemberService;
import concert.domain.waitingQueue.service.WaitingQueueService;
import concert.domain.waitingQueue.vo.WaitingRankVo;
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
