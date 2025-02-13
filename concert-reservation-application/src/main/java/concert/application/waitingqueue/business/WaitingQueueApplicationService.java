package concert.application.waitingqueue.business;

import concert.domain.member.services.MemberService;
import concert.domain.waitingqueue.entities.vo.TokenVO;
import concert.domain.waitingqueue.services.WaitingQueueService;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
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
  private static final String WAITING_QUEUE_STATUS_ACTIVE = "active";
  private static final String WAITING_QUEUE_STATUS_INACTIVE = "inactive";

  private static final long activationTriggerTraffic = 1500L;
  private static final long deactivationTriggerTraffic = 300L;
  private static final long COOLDOWN_TIME = 180_000; // 3분 (밀리초)

  private final RedissonClient redissonClient;

  public void activateWaitingQueue(long totalTraffic) {
    RMap<String, String> waitingQueueStatusMap = redissonClient.getMap(WAITING_QUEUE_STATUS_KEY);
    long now = System.currentTimeMillis();

    String currentStatus = waitingQueueStatusMap.getOrDefault("status", WAITING_QUEUE_STATUS_INACTIVE);
    String lastChangedStr = waitingQueueStatusMap.get("lastChanged");
    long lastChanged = lastChangedStr != null ? Long.parseLong(lastChangedStr) : 0;

    if (WAITING_QUEUE_STATUS_ACTIVE.equals(currentStatus)) {
      log.info("[QUEUE] Waiting queue is already active.");
    } else if(
            (WAITING_QUEUE_STATUS_INACTIVE.equals(currentStatus) && (now - lastChanged > COOLDOWN_TIME))
          || (WAITING_QUEUE_STATUS_INACTIVE.equals(currentStatus) && (now - lastChanged < COOLDOWN_TIME) && totalTraffic >= activationTriggerTraffic)
    ){
      waitingQueueStatusMap.put("status", WAITING_QUEUE_STATUS_ACTIVE);
      waitingQueueStatusMap.put("lastChanged", String.valueOf(now));

      RTopic topic = redissonClient.getTopic(WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL);
      topic.publish(WAITING_QUEUE_STATUS_ACTIVE);

      log.info("[QUEUE] Waiting queue has been activated!");
    }
  }

  public void deactivateWaitingQueue(long totalTraffic) {
    RMap<String, String> waitingQueueStatusMap = redissonClient.getMap(WAITING_QUEUE_STATUS_KEY);
    long now = System.currentTimeMillis();

    String currentStatus = waitingQueueStatusMap.get("status");
    String lastChangedStr = waitingQueueStatusMap.get("lastChanged");
    long lastChanged = lastChangedStr != null ? Long.parseLong(lastChangedStr) : 0;

    if (WAITING_QUEUE_STATUS_INACTIVE.equals(currentStatus)) {
      log.info("[QUEUE] Waiting queue is already inactive.");
    } else if( (WAITING_QUEUE_STATUS_ACTIVE.equals(currentStatus) && (now - lastChanged > COOLDOWN_TIME))
            || (WAITING_QUEUE_STATUS_ACTIVE.equals(currentStatus) && (now - lastChanged < COOLDOWN_TIME) && (totalTraffic <= deactivationTriggerTraffic))
    ) {
      waitingQueueStatusMap.put("status", WAITING_QUEUE_STATUS_INACTIVE);
      waitingQueueStatusMap.put("lastChanged", String.valueOf(now));

      RTopic topic = redissonClient.getTopic(WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL);
      topic.publish(WAITING_QUEUE_STATUS_INACTIVE);

      waitingQueueService.clearAllQueues();

      log.info("[QUEUE] Waiting queue has been deactivated!");
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
