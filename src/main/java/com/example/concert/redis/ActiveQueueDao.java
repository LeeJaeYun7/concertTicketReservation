package com.example.concert.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActiveQueueDao {

    private final RedissonClient redisson;

    public ActiveQueueDao(RedissonClient redisson){
        this.redisson = redisson;
    }

    public String getToken(long concertId, String uuid) {
        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용
        return activeQueue.get(uuid);
    }

    public void deleteToken(long concertId, String uuid){
        RMapCache<String, String> activeQueue = redisson.getMapCache("activeQueue:" + concertId);  // RMapCache 사용
        activeQueue.remove(uuid);
    }
}
