package com.example.concert.concertschedule.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertScheduleService {

    private final TimeProvider timeProvider;
    private final ConcertScheduleRepository concertScheduleRepository;

    public void createConcertSchedule(Concert concert, LocalDateTime dateTime, long price)  {
        ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime, price);
        concertScheduleRepository.save(concertSchedule);
    }

    public List<ConcertSchedule> getAllConcertSchedulesAfterNowByConcertId(long concertId){
        LocalDateTime now = timeProvider.now();
        return concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);
    }

    public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
        return concertScheduleRepository.findById(concertScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
    }
}
