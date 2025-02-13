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
    }
    // 대기열 활성화 조건으로
    // 1. 이전 대기열 상태 변경 시점 기준 3분이 지나야만 가능하거나
    // 2. 트래픽이 1500이상이 되면 즉시 가능하도록 하였습니다.
    // 1번의 조건은 트래픽 변동폭이 큰 시점에서 대기열 On/Off가 너무 자주 발생하지 않도록 3분의 Delay time을 적용하였습니다
    // 2번의 조건은, 그럼에도 불구하고 Peak 트래픽이 발생하는 경우, 이에 대응하여 대기열 발동이 필요하다고 판단하여, Peak 트래픽의 기준을 1500으로 잡고, 대기열 활성화를 추가하였습니다.
    else if(
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
    }
    // 대기열 비활성화 조건으로
    // 1. 마찬가지로 이전 대기열 상태 변경 시점 기준 3분이 지나야만 가능하거나
    // 2. 트래픽이 300이하가 되면 즉시 비활성화하도록 하였습니다.
    // 1번의 조건은 트래픽 변동폭이 큰 시점에서 대기열 On/Off가 너무 자주 발생하지 않도록 3분의 Delay time을 적용하였습니다
    // 2번의 조건은, 트래픽이 매우 낮은 경우, 대기열이 필요하지 않으므로, 즉시 대기열 비활성화를 하도록 하였습니다.

    else if( (WAITING_QUEUE_STATUS_ACTIVE.equals(currentStatus) && (now - lastChanged > COOLDOWN_TIME))
            || (WAITING_QUEUE_STATUS_ACTIVE.equals(currentStatus) && (now - lastChanged < COOLDOWN_TIME) && (totalTraffic <= deactivationTriggerTraffic))
    ) {
      waitingQueueStatusMap.put("status", WAITING_QUEUE_STATUS_INACTIVE);
      waitingQueueStatusMap.put("lastChanged", String.valueOf(now));

      RTopic topic = redissonClient.getTopic(WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL);
      topic.publish(WAITING_QUEUE_STATUS_INACTIVE);

      // 대기열이 비활성화되면, 대기열, 활성화열, 그리고 활성화토큰 정보를 모두 삭제처리하였습니다
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
