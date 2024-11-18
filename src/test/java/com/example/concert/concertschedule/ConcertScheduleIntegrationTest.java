package com.example.concert.concertschedule;


import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concerthall.repository.ConcertHallRepository;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ConcertScheduleIntegrationTest {

    @Autowired
    private ConcertScheduleService sut;
    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertHallRepository concertHallRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Nested
    @DisplayName("현재 이후의 모든 콘서트 스케줄을 가져올 때")
    class 현재_이후의_모든_콘서트_스케줄을_가져올때 {
        @Test
        @DisplayName("총 스케줄이_2개인 경우 2개를 가져온다.")
        void 총_스케줄이_2개인_경우_2개를_가져온다() {
            LocalDate startAt = LocalDate.of(2024, 11, 25);
            LocalDate endAt = LocalDate.of(2024, 11, 28);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

            Concert concert = Concert.of("김연우 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime1 = LocalDateTime.of(2024, 11, 25, 22, 30);
            ConcertSchedule concertSchedule1 = ConcertSchedule.of(concert, dateTime1);
            LocalDateTime dateTime2 = LocalDateTime.of(2024, 11, 28, 22, 30);
            ConcertSchedule concertSchedule2 = ConcertSchedule.of(concert, dateTime2);

            Concert savedConcert = concertRepository.save(concert);
            concertScheduleRepository.save(concertSchedule1);
            concertScheduleRepository.save(concertSchedule2);

            List<LocalDateTime> result = sut.getAllAvailableDateTimes(savedConcert.getId());

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("총 스케줄이_2개인 경우 해당하는 1개를 가져온다.")
        void 총_스케줄이_2개인_경우_해당하는_1개를_가져온다() {
            LocalDate startAt = LocalDate.of(2024, 11, 25);
            LocalDate endAt = LocalDate.of(2024, 11, 28);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

            Concert concert = Concert.of("김연우 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime1 = LocalDateTime.of(2024, 9, 25, 22, 30);
            ConcertSchedule concertSchedule1 = ConcertSchedule.of(concert, dateTime1);
            LocalDateTime dateTime2 = LocalDateTime.of(2024, 11, 28, 22, 30);
            ConcertSchedule concertSchedule2 = ConcertSchedule.of(concert, dateTime2);

            Concert saveConcert = concertRepository.save(concert);
            concertScheduleRepository.save(concertSchedule1);
            concertScheduleRepository.save(concertSchedule2);

            List<LocalDateTime> result = sut.getAllAvailableDateTimes(saveConcert.getId());

            assertEquals(1, result.size());
        }
    }
}
