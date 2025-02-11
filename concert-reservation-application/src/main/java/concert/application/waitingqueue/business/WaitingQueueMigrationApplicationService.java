package concert.application.waitingqueue.business;

import concert.domain.waitingqueue.services.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueMigrationApplicationService {

  private final WaitingQueueService waitingQueueService;

  public void migrateFromWaitingToActiveQueue() {
    waitingQueueService.migrateFromWaitingToActiveQueue();
    // 슬랙 알람을 보낸다거나, 다른 작업으로 noti 를 한다거나, 이벤트를 보낸다거나. 등등.
  }
}
