package concert.domain.waitingQueue.service;

import concert.domain.waitingQueue.vo.WaitingRankVo;
import concert.domain.waitingQueue.domain.WaitingQueueDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueService {

  private final WaitingQueueDao waitingQueueDao;

  public String retrieveToken(long concertId, String uuid) {
    return waitingQueueDao.addToWaitingQueue(concertId, uuid);
  }

  public WaitingRankVo retrieveWaitingRank(long concertId, String uuid) {
    long rank = waitingQueueDao.getWaitingRank(concertId, uuid);

    if (rank == -1) {
      return WaitingRankVo.of(rank, "active");
    }

    return WaitingRankVo.of(rank, "waiting");
  }
}
