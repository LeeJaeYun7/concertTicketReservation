package concert.application.waitingqueue.business;

import concert.domain.member.services.MemberService;
import concert.domain.waitingqueue.entities.vo.TokenVO;
import concert.domain.waitingqueue.services.WaitingQueueService;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingQueueApplicationService {

  private final MemberService memberService;
  private final WaitingQueueService waitingQueueService;

  public TokenVO retrieveToken(long concertId, String uuid) {
    memberService.getMemberByUuid(uuid);
    String token = waitingQueueService.retrieveToken(concertId, uuid);
    return TokenVO.of(token);
  }

  public WaitingRankVO retrieveWaitingRank(long concertId, String uuid) {
    return waitingQueueService.retrieveWaitingRank(concertId, uuid);
  }
}
