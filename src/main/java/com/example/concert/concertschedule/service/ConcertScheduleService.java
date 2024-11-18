package com.example.concert.concertschedule.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.seatinfo.service.SeatInfoService;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertScheduleService {

    private final TimeProvider timeProvider;
    private final SeatInfoService seatInfoService;
    private final ConcertScheduleRepository concertScheduleRepository;

    public void createConcertSchedule(Concert concert, LocalDateTime dateTime)  {
        ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);
        concertScheduleRepository.save(concertSchedule);
    }

    public List<LocalDateTime> getAllAvailableDateTimes(long concertId) {
        LocalDateTime now = timeProvider.now();
        List<ConcertSchedule> allConcertSchedules = concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);

        return allConcertSchedules.stream()
                .filter(concertSchedule -> {
                    long concertScheduleId = concertSchedule.getId();
                    return !seatInfoService.getAllAvailableSeats(concertScheduleId).isEmpty();
                })
                .map(ConcertSchedule::getDateTime)
                .collect(Collectors.toList());
    }

    public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
        return concertScheduleRepository.findById(concertScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
    }
}
