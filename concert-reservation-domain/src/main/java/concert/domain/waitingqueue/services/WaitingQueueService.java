package concert.domain.waitingqueue.services;

import concert.domain.waitingqueue.entities.WaitingDTO;
import concert.domain.waitingqueue.entities.WaitingQueueDao;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueService {

  private final WaitingQueueDao waitingQueueDao;

  public String retrieveToken(long concertId, String uuid) {
    WaitingDTO waitingDTO = WaitingDTO.of(uuid);

    return waitingQueueDao.addToWaitingQueue(concertId, waitingDTO);
  }

  public WaitingRankVO retrieveWaitingRank(long concertId, String uuid) {
    Collection<WaitingDTO> tokenList = waitingQueueDao.getAllWaitingTokens(concertId);

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

  public void migrateFromWaitingToActiveQueue(long concertId) {
    // waitingQueue 에서 최근 333개 목록 가져옴.
    Collection<WaitingDTO> tokenList = waitingQueueDao.getAllWaitingTokens(concertId, 333);
    if (tokenList.isEmpty()) {
      return;
    }

    // activeQueue 로 push 함.
    waitingQueueDao.putActiveQueueToken(concertId, tokenList);

    // waitingQueue 에서 삭제처리
    waitingQueueDao.deleteWaitQueueTokens(concertId, tokenList);

  }
}
