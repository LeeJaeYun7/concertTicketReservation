package concert.domain.waitingqueue.entities;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WaitingQueueDao {

  private final RedissonClient redisson;

  public WaitingQueueDao(RedissonClient redisson) {
    this.redisson = redisson;
  }

  public String addToWaitingQueue(long concertId, WaitingDTO waitingDTO) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(getWaitingQueueKey(concertId));
    String token = waitingDTO.getToken();
    waitingQueue.add(token);
    return token;
  }

  public Collection<WaitingDTO> getAllWaitingTokens(long concertId) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(getWaitingQueueKey(concertId));
    Collection<String> tokenList = waitingQueue.readAll();

    return tokenList.stream().map(WaitingDTO::parse).collect(Collectors.toList());
  }

  public Collection<WaitingDTO> getAllWaitingTokens(long concertId, long limit) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(getWaitingQueueKey(concertId));
    Collection<String> tokenList = waitingQueue.readAll();

    Collection<String> limitedList = tokenList.stream()
            .limit(limit)
            .toList();

    return limitedList.stream().map(WaitingDTO::parse).collect(Collectors.toList());
  }


  public String getActiveQueueToken(long concertId, String uuid) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(getActiveQueueKey(concertId));  // RMapCache 사용

    return activeQueue.get(uuid);
  }

  public void deleteActiveQueueToken(long concertId, String uuid) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(getActiveQueueKey(concertId));  // RMapCache 사용
    activeQueue.remove(uuid);
  }

  /**
   * @param concertId
   * @param tokens    <uuid, token>
   */
  public void putActiveQueueToken(long concertId, Collection<WaitingDTO> tokens) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(getActiveQueueKey(concertId));  // RMapCache 사용

    for (WaitingDTO waitingDTO : tokens) {
      String uuid = waitingDTO.getUuid();
      String token = waitingDTO.getToken();
      activeQueue.putIfAbsent(uuid, token, 5, TimeUnit.SECONDS);
    }
  }

  public void deleteWaitQueueTokens(long concertId, Collection<WaitingDTO> tokens) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(getWaitingQueueKey(concertId));

    tokens.forEach(waitingDTO -> {
      String token = waitingDTO.getToken();
      waitingQueue.remove(token);
    });
  }

  private static String getWaitingQueueKey(long concertId) {
    return "waitingQueue:" + concertId;
  }

  private static String getActiveQueueKey(long concertId) {
    return "activeQueue:" + concertId;
  }


}
