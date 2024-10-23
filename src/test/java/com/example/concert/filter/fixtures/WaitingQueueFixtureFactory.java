package com.example.concert.filter.fixtures;

import com.example.concert.waitingQueue.domain.WaitingQueue;
import com.example.concert.waitingQueue.domain.WaitingQueueStatus;

import java.time.LocalDateTime;

import static org.springframework.test.util.ReflectionTestUtils.setField;

public class WaitingQueueFixtureFactory {

    public static WaitingQueue createWaitingQueue(){
        return new WaitingQueue();
    }

    public static WaitingQueue createWaitingQueueWithToken(String token){
        WaitingQueue waitingQueue = createWaitingQueue();
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        setField(waitingQueue, "token", token);
        setField(waitingQueue, "status", WaitingQueueStatus.ACTIVE);
        setField(waitingQueue, "updatedAt", fiveMinutesAgo);
        return waitingQueue;
    }
}
