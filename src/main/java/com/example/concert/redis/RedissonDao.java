package com.example.concert.redis;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDao {

    private final RedissonClient redisson;

    private static final String CONCERT_SCHEDULES = "concertSchedules";

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcertSchedules")
    public void saveConcertSchedules(String concertSchedules) {
        redisson.getBucket(CONCERT_SCHEDULES).set(concertSchedules);
        log.info("Concert schedules saved into redis");
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcertSchedules")
    public String getConcertSchedules() {
        RBucket<String> bucket = redisson.getBucket(CONCERT_SCHEDULES);
        String value = bucket.get();

        if (value != null) {
            log.info("Retrieved concert schedules: {}", value);
        } else {
            log.warn("No concert schedules found.");
        }

        return value;
    }

    // Fallback 메소드 정의 (서킷 브레이커가 열렸을 때 호출됨)
    public void fallbackSaveConcertSchedules(String concertSchedules, Throwable t) {
        log.error("Failed to save concert schedules to Redis. Circuit breaker is open.", t);
    }

    public String fallbackGetConcertSchedules(Throwable t) {
        log.error("Failed to retrieve concert schedules from Redis. Circuit breaker is open.", t);
        return null;
    }
}
