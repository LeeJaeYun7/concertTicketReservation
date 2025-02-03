package concert.application.waitingqueue.business;

import concert.application.shared.utils.TokenParser;
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
  private final TokenParser tokenParser;

  public TokenVO retrieveToken(long concertId, String uuid) {
    memberService.getMemberByUuid(uuid);
    String token = waitingQueueService.retrieveToken(concertId, uuid);
    return TokenVO.of(token);
  }

  public WaitingRankVO retrieveWaitingRank(long concertId, String token) {
    String uuid = tokenParser.getUuidFromToken(token);
    return waitingQueueService.retrieveWaitingRank(concertId, uuid);
  }
}
