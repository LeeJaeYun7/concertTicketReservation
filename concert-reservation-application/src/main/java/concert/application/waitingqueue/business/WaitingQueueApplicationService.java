package concert.application.waitingqueue.business;

import concert.domain.member.services.MemberService;
import concert.domain.waitingqueue.entities.vo.TokenVO;
import concert.domain.waitingqueue.services.WaitingQueueService;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingQueueApplicationService {

  private final MemberService memberService;
  private final WaitingQueueService waitingQueueService;
  private static final String QUEUE_ACTIVE_KEY = "waiting_queue_active";
  private static final String QUEUE_ACTIVE_VALUE = "true";
  private final RedissonClient redissonClient;

  public void activateWaitingQueue() {
    RBucket<String> queueActiveBucket = redissonClient.getBucket(QUEUE_ACTIVE_KEY);
    if (QUEUE_ACTIVE_VALUE.equals(queueActiveBucket.get())) {
      log.info("[QUEUE] Waiting queue is already active.");
    } else {
      queueActiveBucket.set(QUEUE_ACTIVE_VALUE); // TTL 없이 저장
      log.info("[QUEUE] Waiting queue has been activated!");
    }
  }

  public void deactivateWaitingQueue() {
    RBucket<String> queueActiveBucket = redissonClient.getBucket(QUEUE_ACTIVE_KEY);
    if (queueActiveBucket.isExists()) {
      queueActiveBucket.delete();
      log.info("[QUEUE] Waiting queue has been deactivated!");
    } else {
      log.info("[QUEUE] Waiting queue is already inactive.");
    }
  }

  public boolean isQueueActive() {
    RBucket<String> queueActiveBucket = redissonClient.getBucket(QUEUE_ACTIVE_KEY);
    return QUEUE_ACTIVE_VALUE.equals(queueActiveBucket.get());
  }

  public TokenVO retrieveToken(String uuid) {
    memberService.getMemberByUuid(uuid);
    String token = waitingQueueService.retrieveToken(uuid);
    return TokenVO.of(token);
  }

  public WaitingRankVO retrieveWaitingRank(String uuid) {
    return waitingQueueService.retrieveWaitingRank(uuid);
  }
}
