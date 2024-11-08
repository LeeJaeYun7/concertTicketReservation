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
public class ConcertDao {

    private final RedissonClient redisson;

    private static final String CONCERTS = "concerts";

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcertSchedules")
    public void saveConcerts(String concerts) {
        redisson.getBucket(CONCERTS).set(concerts);
        log.info("Concerts saved into redis");
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcertSchedules")
    public String getConcerts() {
        RBucket<String> bucket = redisson.getBucket(CONCERTS);
        String value = bucket.get();

        if (value != null) {
            log.info("Retrieved concerts: {}", value);
        } else {
            log.warn("No concerts found.");
        }

        return value;
    }

    // Fallback 메소드 정의 (서킷 브레이커가 열렸을 때 호출됨)
    public void fallbackSaveConcerts(String concerts, Throwable t) {
        log.error("Failed to save concerts to Redis. Circuit breaker is open.", t);
    }

    public String fallbackGetConcerts(Throwable t) {
        log.error("Failed to retrieve concerts from Redis. Circuit breaker is open.", t);
        return null;
    }
}
