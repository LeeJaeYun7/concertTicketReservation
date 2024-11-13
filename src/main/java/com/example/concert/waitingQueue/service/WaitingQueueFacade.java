package com.example.concert.waitingQueue.service;

import com.example.concert.redis.WaitingQueueDao;
import com.example.concert.waitingQueue.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingQueueFacade {

    private final WaitingQueueDao waitingQueueDao;

    // uuid가 대기열 -> 대기 번호 리턴
    // uuid가 활성화열 -> 토큰 리턴
    public TokenResponse retrieveWaitingRankOrToken(long concertId, String uuid) {
        long rank = waitingQueueDao.getWaitingRank(concertId, uuid);
        String token = waitingQueueDao.getActiveQueueToken(concertId, uuid);

        // 대기열, 활성화열 둘 다에 없는 경우
        if(rank == -1 && token == null){
            waitingQueueDao.addToWaitingQueue(concertId, uuid);
            rank = waitingQueueDao.getWaitingRank(concertId, uuid);
        }

        return TokenResponse.of(rank, token);
    }
}
