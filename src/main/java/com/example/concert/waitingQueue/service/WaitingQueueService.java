package com.example.concert.waitingQueue.service;

import com.example.concert.redis.WaitingQueueDao;
import com.example.concert.waitingQueue.dto.response.TokenResponse;
import com.example.concert.waitingQueue.dto.response.WaitingRankResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueService {

    private final WaitingQueueDao waitingQueueDao;

    public TokenResponse retrieveToken(long concertId, String uuid) {
        String token = waitingQueueDao.addToWaitingQueue(concertId, uuid);
        return TokenResponse.of(token);
    }

    public WaitingRankResponse retrieveWaitingRank(long concertId, String uuid) {
        long rank = waitingQueueDao.getWaitingRank(concertId, uuid);
        return WaitingRankResponse.of(rank);
    }
}
