package com.example.concert.waitingQueue.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.service.ConcertService;
import com.example.concert.redis.WaitingQueueDao;
import com.example.concert.utils.RandomStringGenerator;
import com.example.concert.waitingQueue.domain.WaitingQueue;
import com.example.concert.waitingQueue.vo.TokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.Math.max;

@Service
@RequiredArgsConstructor
public class WaitingQueueFacade {

    private final WaitingQueueDao waitingQueueDao;

    public TokenVO createConcertToken(long concertId, String uuid) {
        waitingQueueDao.addToWaitingQueue(concertId, uuid);
        long rank = waitingQueueDao.getWaitingRank(concertId, uuid);
        return TokenVO.of(rank);
    }

}
