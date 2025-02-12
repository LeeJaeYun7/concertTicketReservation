package concert.domain.waitingqueue.services;

import concert.domain.waitingqueue.entities.WaitingDTO;
import concert.domain.waitingqueue.entities.WaitingQueueDao;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private static final String PUB_SUB_CHANNEL = "tokenChannel";  // Pub/Sub 채널 이름

  private static final String LOCK_KEY = "activeQueueLock";  // 분산 락을 위한 키

  public String retrieveToken(String uuid) {
    WaitingDTO waitingDTO = WaitingDTO.of(uuid);
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
    RLock lock = redissonClient.getLock(LOCK_KEY);
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

          // activeQueue 로 push 함.
          waitingQueueDao.putActiveQueueToken(tokenList);

          RTopic topic = redissonClient.getTopic(PUB_SUB_CHANNEL);
          topic.publish(tokenList.stream().map(WaitingDTO::getToken).collect(Collectors.toList()));  // tokenList를 발행

          // waitingQueue 에서 삭제처리
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
}
