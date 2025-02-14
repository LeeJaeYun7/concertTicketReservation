package concert.application.waitingqueue.scheduler;

import concert.domain.waitingqueue.services.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
@RequiredArgsConstructor
@Slf4j
public class InactiveUserCleanerScheduler {

    private final WaitingQueueService waitingQueueService;
    private final RedissonClient redissonClient;
    private static final String HEARTBEAT_HASH_KEY = "userHeartbeat";
    private static final long INACTIVITY_THRESHOLD = 30 * 1000; // 30초 (밀리초 단위)

    @Scheduled(fixedRate = 10000)  // 10초마다 실행
    public void detectInactiveUsers() {
        RMap<String, String> heartbeatMap = redissonClient.getMap(HEARTBEAT_HASH_KEY);

        // 현재 시간을 밀리초 단위로 구함
        long currentTime = System.currentTimeMillis();

        // Map을 순회하면서 이탈한 사용자 감지
        Iterator<String> iterator = heartbeatMap.keySet().iterator();

        while (iterator.hasNext()) {
            String token = iterator.next();
            String timestampStr = heartbeatMap.get(token);

            if (timestampStr != null) {
                try {
                    // 타임스탬프를 밀리초로 변환
                    long timestamp = Long.parseLong(timestampStr);

                    // 만약 30초 이상 경과한 사용자라면 이탈한 사용자로 간주하고 삭제
                    if (currentTime - timestamp > INACTIVITY_THRESHOLD) {
                        // 이탈한 사용자 처리 (예: 대기열에서 제거)
                        iterator.remove();  // 해당 토큰 삭제
                        waitingQueueService.removeUserFromQueues(token);
                        waitingQueueService.removeActivatedToken(token);
                        waitingQueueService.removeSession(token);
                        log.info("User with token " + token + " has become inactive and was removed from the queue.");
                    }
                } catch (NumberFormatException e) {
                    log.error("Wrong timestamp number format", e);
                }
            }
        }
    }
}


