package concert.domain.waitingqueue.entities;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
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

  private static final String ACTIVATED_TOKENS_KEY = "activatedTokens";
  private static final String TOKEN_SESSION_ID_MAP = "tokenSessionId";


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

  public boolean isTokenExistsInWaitingQueue(String token){
      RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);
      return waitingQueue.contains(token);
  }

  public void deleteWaitingQueueToken(String token){
      RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);
      waitingQueue.remove(token);
  }

  public void deleteWaitQueueTokens(Collection<WaitingDTO> tokens) {
    RSortedSet<String> waitingQueue = redisson.getSortedSet(WAITING_QUEUE_KEY);

    tokens.forEach(waitingDTO -> {
      String token = waitingDTO.getToken();
      waitingQueue.remove(token);
    });
  }

  public boolean isTokenExistsInActiveQueue(WaitingDTO waitingDTO){
    RMapCache<String, String> activeQueue = redisson.getMapCache(ACTIVE_QUEUE_KEY);  // RMapCache 사용
    String uuid = waitingDTO.getUuid();
    return activeQueue.containsKey(uuid);
  }

  public void deleteActiveQueueToken(String uuid) {
    RMapCache<String, String> activeQueue = redisson.getMapCache(ACTIVE_QUEUE_KEY);  // RMapCache 사용
    activeQueue.remove(uuid);
  }

  public boolean isActivatedTokenExists(String token){
    RSet<String> activatedTokensSet = redisson.getSet(ACTIVATED_TOKENS_KEY);
    return activatedTokensSet.contains(token);
  }

  public void removeActivatedToken(String token){
    RSet<String> activatedTokensSet = redisson.getSet(ACTIVATED_TOKENS_KEY);
    activatedTokensSet.remove(token);
  }

  public boolean isSessionExists(String token){
      RMap<String, String> sessionsMap = redisson.getMap(TOKEN_SESSION_ID_MAP);
      return sessionsMap.containsKey(token);
  }

  public void removeSession(String token){
    RMap<String, String> sessionsMap = redisson.getMap(TOKEN_SESSION_ID_MAP);
    sessionsMap.remove(token);  // 해당 토큰에 대한 세션 정보 삭제
  }
}

