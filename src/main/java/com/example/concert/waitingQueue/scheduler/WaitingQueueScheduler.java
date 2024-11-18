package com.example.concert.waitingQueue.scheduler;

import com.example.concert.common.CustomException;
import com.example.concert.concert.service.ConcertService;
import com.example.concert.redis.WaitingQueueDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueScheduler {

    private final ConcertService concertService;
    private final WaitingQueueDao waitingQueueDao;

    // @Scheduled(fixedRate = 10000)
    public void processWaitingQueue() {
        List<Long> concertIds = concertService.getAllConcertIds();

        for (long concertId: concertIds){
            try{
                waitingQueueDao.getAndRemoveTop250FromQueue(concertId);
            }catch(CustomException e){
                log.error("대기열을 처리하는 중 에러 발생: " + e.getMessage());
            }
        }
    }
}
