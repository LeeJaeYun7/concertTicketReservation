package concert.application.waitingqueue.business;

import concert.domain.member.services.MemberService;
import concert.domain.waitingqueue.entities.vo.TokenVO;
import concert.domain.waitingqueue.services.WaitingQueueService;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingQueueApplicationService {

  private final MemberService memberService;
  private final WaitingQueueService waitingQueueService;
  private static final String WAITING_QUEUE_STATUS_KEY = "waitingQueueStatusKey";
  private static final String WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL = "waitingQueueStatusChannel";
  private static final String WAITING_QUEUE_STATUS_VALUE = "active";

  private final RedissonClient redissonClient;

  public void activateWaitingQueue() {
    RBucket<String> queueActiveBucket = redissonClient.getBucket(WAITING_QUEUE_STATUS_KEY);
    if (WAITING_QUEUE_STATUS_VALUE.equals(queueActiveBucket.get())) {
      log.info("[QUEUE] Waiting queue is already active.");
    } else {
      queueActiveBucket.set(WAITING_QUEUE_STATUS_VALUE); // TTL 없이 저장
      RTopic topic = redissonClient.getTopic(WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL);
      topic.publish("active");
      log.info("[QUEUE] Waiting queue has been activated!");
    }
  }

  public void deactivateWaitingQueue() {
    RBucket<String> queueActiveBucket = redissonClient.getBucket(WAITING_QUEUE_STATUS_KEY);
    if (queueActiveBucket.isExists()) {
      queueActiveBucket.delete();
      RTopic topic = redissonClient.getTopic(WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL);
      topic.publish("inactive");
      log.info("[QUEUE] Waiting queue has been deactivated!");
    } else {
      log.info("[QUEUE] Waiting queue is already inactive.");
    }
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
