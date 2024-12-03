package com.example.concert.concert.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.cache.ConcertCache;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.reservation.infrastructure.repository.ReservationRepository;
import com.example.concert.utils.TimeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final TimeProvider timeProvider;
    private final ConcertRepository concertRepository;
    private final ReservationRepository reservationRepository;
    private final ConcertCache concertCache;

    public Concert getConcertById(long concertId) {
        return concertRepository.findById(concertId)
                                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Concert getConcertByName(String concertName) {
        return concertRepository.findByName(concertName)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, Loggable.ALWAYS));
    }

    public List<Long> getAllConcertIds() {
        return concertRepository.findAll().stream().map(Concert::getId).collect(Collectors.toList());
    }

    public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        List<Concert> top30concerts = reservationRepository.findTop30Concerts(threeDaysAgo);
        concertCache.saveTop30Concerts(top30concerts);
    }

    public List<Concert> getTop30ConcertsFromDB() {
        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        return reservationRepository.findTop30Concerts(threeDaysAgo);
    }

    public List<Concert> getTop30Concerts() throws JsonProcessingException {

        if(concertCache.findTop30Concerts() != null){
            return concertCache.findTop30Concerts();
        }

        LocalDateTime now = timeProvider.now();
        LocalDateTime threeDaysAgo = now.minus(Duration.ofHours(72));

        return reservationRepository.findTop30Concerts(threeDaysAgo);
    }
}
