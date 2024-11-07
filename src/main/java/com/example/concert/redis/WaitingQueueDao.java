package com.example.concert.redis;

import com.example.concert.utils.RandomStringGenerator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WaitingQueueDao {

    private final RedissonClient redisson;

    public WaitingQueueDao(RedissonClient redisson){
        this.redisson = redisson;
        log.info("global waiting queue initialized");
    }

    public void addToWaitingQueue(long concertId, String uuid){
        String timestamp = Long.toString(System.currentTimeMillis());

        String queueEntry = new StringBuilder(timestamp).append(":").append(uuid).toString();
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        waitingQueue.add(queueEntry);

        log.info("Added to waiting queue: {}", queueEntry);
    }

    public long getWaitingRank(long concertId, String uuid){
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        Collection<String> waitingQueueList = waitingQueue.readAll();

        long rank = 1L;

        // rank 계산
        for(String queueEntry: waitingQueueList){
            String[] splits = queueEntry.split(":");
            if(splits[1].equals(uuid)){
                break;
            }
            rank += 1;
        }

        // uuid가 대기열에 없음 -> -1을 반환
        if(rank > waitingQueueList.size()){
            return -1;
        }

        return rank;
    }

    public String getActiveQueueToken(long concertId, String uuid){
        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용

        return activeQueue.get(uuid);
    }

    // 각 콘서트 대기열 마다
    // 250개의 queueEntry를 10초마다 활성화열로 이동
    public void getAndRemoveTop250FromQueue(long concertId) {
        RSortedSet<String> waitingQueue = redisson.getSortedSet("waitingQueue:" + concertId);
        Collection<String> total = waitingQueue.readAll();

        if (total.isEmpty()) {
             return;
        }

        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용
        Collection<String> top250 = total.stream()
                                         .limit(250)
                                         .toList();

        top250.forEach(entry -> {
            String[] parts = entry.split(":");
            String uuid = parts[1];
            String token = RandomStringGenerator.generateRandomString(16);

            activeQueue.putIfAbsent(uuid, token, 300, TimeUnit.SECONDS);
        });

        top250.forEach(waitingQueue::remove);
    }
}
