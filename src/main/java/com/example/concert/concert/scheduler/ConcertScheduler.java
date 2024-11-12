package com.example.concert.concert.scheduler;

import com.example.concert.concert.cache.ConcertCache;
import com.example.concert.concert.domain.Concert;
import com.example.concert.reservation.repository.ReservationRepository;
import com.example.concert.utils.TimeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConcertScheduler {

    private final ReservationRepository reservationRepository;
    private final TimeProvider timeProvider;
    private final ConcertCache concertCache;

    @Scheduled(fixedRate = 300000)
    public void updateTop30Concerts() throws JsonProcessingException {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        List<Concert> top30Concerts = reservationRepository.findTop30Concerts(threeDaysAgo);
        concertCache.saveTop30Concerts(top30Concerts);
    }
}
