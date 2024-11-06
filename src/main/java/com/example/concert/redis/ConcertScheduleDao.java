package com.example.concert.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertScheduleDao {

    private final RedissonClient redisson;

    private static final String CONCERT_SCHEDULES = "concertSchedules";

    public void saveConcertSchedules(String concertSchedules) {
        redisson.getBucket(CONCERT_SCHEDULES).set(concertSchedules);
        log.info("Concert schedules saved into redis");
    }

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
}
