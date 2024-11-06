package com.example.concert.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class WaitingQueueDao {

    private final RedissonClient redisson;
    private RSortedSet<String> globalWaitingQueue;

    public WaitingQueueDao(RedissonClient redisson){
        this.redisson = redisson;
        globalWaitingQueue = redisson.getSortedSet("waitingQueue");
        log.info("global waiting queue initialized");
    }

    public void addToWaitingQueue(long concertId, String uuid){
        String timestamp = Long.toString(System.currentTimeMillis());
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        waitingQueue.add(timestamp + ":" + uuid);
    }

    public long getWaitingRank(long concertId, String uuid){
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        Collection<String> waitingQueueList = waitingQueue.readAll();
        long rank = 1L;

        for(String token: waitingQueueList){
            String[] splits = token.split(":");
            if(splits[1].equals(uuid)){
                break;
            }
            rank += 1;
        }
        return rank;
    }

}
