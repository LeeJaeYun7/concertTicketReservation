package concert.domain.waitingqueue.services;

import concert.domain.waitingqueue.entities.WaitingDTO;
import concert.domain.waitingqueue.entities.WaitingQueueDao;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueService {

  private final WaitingQueueDao waitingQueueDao;

  private final RedissonClient redissonClient;  // RedissonClient 추가
  private static final int MAX_TRANSFER_COUNT = 250;
  private static final String TOKEN_PUB_SUB_CHANNEL = "tokenChannel";  // Pub/Sub 채널 이름
  private static final String WAITING_QUEUE_STATUS_KEY = "waitingQueueStatusKey";
  private static final String WAITING_QUEUE_STATUS_ACTIVE = "active";
  private static final String ACTIVE_QUEUE_LOCK_KEY = "activeQueueLock";  // 분산 락을 위한 키

  public String retrieveToken(String uuid) {
    WaitingDTO waitingDTO = WaitingDTO.of(uuid);

    // 대기열 활성화 정보를 관리하는 Redis 버킷
    RBucket<String> waitingQueueStatusBucket = redissonClient.getBucket(WAITING_QUEUE_STATUS_KEY);

    // 대기열이 비활성화일 때(트래픽 800이하), 대기열에 토큰을 저장하지 않고, 반환만 한다
    if (!WAITING_QUEUE_STATUS_ACTIVE.equals(waitingQueueStatusBucket.get())) {
      return waitingDTO.getToken();
    }

    // 대기열이 활성화일 때(트래픽 1200 이상), 대기열에 토큰을 저장한다
    return waitingQueueDao.addToWaitingQueue(waitingDTO);
  }

  public WaitingRankVO retrieveWaitingRank(String uuid) {
    Collection<WaitingDTO> tokenList = waitingQueueDao.getAllWaitingTokens();
    long rank = 1L;

    // rank 계산
    for (WaitingDTO token : tokenList) {
      if (token.isUuidEquals(uuid)) {
        break;
      }
      rank += 1;
    }

    // uuid가 대기열에 없음 -> -1을 반환
    if (rank > tokenList.size()) {
      rank = -1;
    }

    if (rank == -1) {
      return WaitingRankVO.of(rank, "active");
    }

    return WaitingRankVO.of(rank, "waiting");
  }

  public void migrateFromWaitingToActiveQueue() {
    // 분산 락을 사용하여 동시에 한 서버만 작업을 하도록 함
    RLock lock = redissonClient.getLock(ACTIVE_QUEUE_LOCK_KEY);
    try {
      // 락을 얻을 때까지 대기, 최대 5초 대기
      if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
        try {
          long activeQueueSize = waitingQueueDao.getActiveQueueSize();
          long transferCount = Math.min(5000 - activeQueueSize, MAX_TRANSFER_COUNT);

          if(transferCount == 0){
             return;
          }

          Collection<WaitingDTO> tokenList = waitingQueueDao.getAllWaitingTokens(transferCount);

          if (tokenList.isEmpty()) {
            return;
          }

          // 토큰 목록을 활성화열로 이동시킨다.
          waitingQueueDao.putActiveQueueToken(tokenList);

          // 토큰 목록을 Redis Pub/Sub을 통해 발행한다
          // 발행한 토큰 목록은 WebSocket 서버에서 Redis Pub/Sub을 통해 구독한다
          // 토큰 Pub/Sub의 목적은, 토큰이 활성화되었을 때, 웹소켓 클라이언트에게 알림을 주기 위함이다.
          RTopic topic = redissonClient.getTopic(TOKEN_PUB_SUB_CHANNEL);
          topic.publish(tokenList.stream().map(WaitingDTO::getToken).collect(Collectors.toList()));  // tokenList를 발행

          // 토큰 목록을 대기열에서 삭제처리한다
          waitingQueueDao.deleteWaitQueueTokens(tokenList);
        } finally {
          lock.unlock();  // 작업 완료 후 락을 해제
        }
      } else {
        log.warn("Unable to acquire lock for migrating waiting queue.");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Error while attempting to acquire lock: {}", e.getMessage());
    }
  }

  public void removeUserFromQueues(String token) {
    boolean waitingQueueExists = waitingQueueDao.isTokenExistsInWaitingQueue(token);

    if(waitingQueueExists){
      waitingQueueDao.deleteWaitingQueueToken(token);
      return;
    }

    WaitingDTO waitingDTO = WaitingDTO.parse(token);
    boolean activeQueueExists = waitingQueueDao.isTokenExistsInActiveQueue(waitingDTO);

    if(activeQueueExists){
      waitingQueueDao.deleteActiveQueueToken(token);
    }
  }

  public void removeActivatedToken(String token){
    boolean activatedTokenExists = waitingQueueDao.isActivatedTokenExists(token);
    if(activatedTokenExists){
      waitingQueueDao.removeActivatedToken(token);
    }
  }

  public void removeSession(String token){
    boolean sessionExists = waitingQueueDao.isSessionExists(token);
    if(sessionExists){
      waitingQueueDao.removeSession(token);
    }
  }
  public void clearAllQueues(){
     waitingQueueDao.clearAllQueues();
  }
}
