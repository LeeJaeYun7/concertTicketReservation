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
  private static final String WAITING_QUEUE_KEY = "waitingQueue";
  private static final String ACTIVE_QUEUE_KEY = "activeQueue";

  public WaitingQueueDao(RedissonClient redisson) {
    this.redisson = redisson;
  }

  public String addToWaitingQueue(WaitingDTO waitingDTO) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);
    String token = waitingDTO.getToken();
    waitingQueue.add(token);
    return token;
  }

  public Collection<WaitingDTO> getAllWaitingTokens() {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);
    Collection<String> tokenList = waitingQueue.readAll();

    return tokenList.stream().map(WaitingDTO::parse).collect(Collectors.toList());
  }

  public Collection<WaitingDTO> getAllWaitingTokens(long transferCount) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);
    Collection<String> tokenList = waitingQueue.readAll();

    Collection<String> limitedList = tokenList.stream()
                                              .limit(transferCount)
                                              .toList();

    return limitedList.stream().map(WaitingDTO::parse).collect(Collectors.toList());
  }


  public String getActiveQueueToken(String uuid) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(ACTIVE_QUEUE_KEY);  // RMapCache 사용

    return activeQueue.get(uuid);
  }

  public void deleteActiveQueueToken(String uuid) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(ACTIVE_QUEUE_KEY);  // RMapCache 사용
    activeQueue.remove(uuid);
  }

  /**
   * @param tokens    <uuid, token>
   */
  public void putActiveQueueToken(Collection<WaitingDTO> tokens) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(ACTIVE_QUEUE_KEY);  // RMapCache 사용

    for (WaitingDTO waitingDTO : tokens) {
      String uuid = waitingDTO.getUuid();
      String token = waitingDTO.getToken();
      activeQueue.putIfAbsent(uuid, token, 300, TimeUnit.SECONDS);
    }
  }

  public int getActiveQueueSize() {
    RMapCache<String, String> activeQueue = redisson.getMapCache(ACTIVE_QUEUE_KEY);
    return activeQueue.size();
  }

  public void deleteWaitQueueTokens(Collection<WaitingDTO> tokens) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);

    tokens.forEach(waitingDTO -> {
      String token = waitingDTO.getToken();
      waitingQueue.remove(token);
    });
  }
}
