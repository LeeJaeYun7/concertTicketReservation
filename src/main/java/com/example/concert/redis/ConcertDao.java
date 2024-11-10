package com.example.concert.redis;

import com.example.concert.concert.domain.Concert;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertDao {

    private final RedissonClient redisson;

    private static final String CONCERTS = "concerts";

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcerts")
    public void saveConcerts(List<Concert> concerts) {

        RMap<String, Concert> concertMap = redisson.getMap(CONCERTS);

        for(Concert concert: concerts){
            String concertId = String.valueOf(concert.getId());
            concertMap.put(concertId, concert);
        }

        log.info("Concerts saved into redis");
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "fallbackSaveConcerts")
    public List<Concert> getConcerts() {

        RMap<String, Concert> concertMap = redisson.getMap(CONCERTS);

        if (!concertMap.isEmpty()) {
            log.info("Retrieved concerts from redis.");
            return List.copyOf(concertMap.values());  // RMap에서 모든 값 가져오기
        } else {
            log.warn("No concerts found.");
            return null;
        }
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
