package com.example.concert.concertschedule;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.service.ConcertScheduleFacade;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatGrade;
import com.example.concert.seat.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ConcertScheduleFacadeTest {

    @Mock
    private ConcertScheduleService concertScheduleService;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private ConcertScheduleFacade sut;


    @Nested
    @DisplayName("예약 가능한 공연 날짜를 찾을 때")
    class 예약_가능한_공연_날짜를_찾을_때 {

        @Test
        @DisplayName("두 번의 공연 날짜에 대해서, 모두 예약 가능하다")
        void 두_번의_공연_날짜에_대해서_모두_예약_가능하다() {
            long concertId = 1L;
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad",  120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime1 = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule1 = ConcertSchedule.of(concert, dateTime1, 50000);
            Seat seat1 = Seat.of(concertHall, 1, 50000, SeatGrade.ALL);
            Seat seat2 = Seat.of(concertHall, 2, 50000, SeatGrade.ALL);

            LocalDateTime dateTime2 = LocalDateTime.of(2024, 10, 18, 22, 30);
            ConcertSchedule concertSchedule2 = ConcertSchedule.of(concert, dateTime2, 50000);
            Seat seat3 = Seat.of(concertHall, 1, 50000, SeatGrade.ALL);
            Seat seat4 = Seat.of(concertHall, 2, 50000, SeatGrade.ALL);

            given(concertScheduleService.getAllConcertSchedulesAfterNowByConcertId(concertId)).willReturn(List.of(concertSchedule1, concertSchedule2));
            given(seatService.getAllAvailableSeats(concertSchedule1.getId())).willReturn(List.of(seat1, seat2));
            given(seatService.getAllAvailableSeats(concertSchedule2.getId())).willReturn(List.of(seat3, seat4));

            List<LocalDateTime> availableDateTimes = sut.getAvailableDateTimes(1L);
            assertEquals(2, availableDateTimes.size());
        }

        @Test
        @DisplayName("두 번의 공연 날짜에_대해서 모두 예약이 불가능하다")
        void 두_번의_공연_날짜에_대해서_모두_예약이_불가능하다() {
            long concertId = 1L;
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime1 = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule1 = ConcertSchedule.of(concert, dateTime1, 50000);

            LocalDateTime dateTime2 = LocalDateTime.of(2024, 10, 18, 22, 30);
            ConcertSchedule concertSchedule2 = ConcertSchedule.of(concert, dateTime2, 50000);

            given(concertScheduleService.getAllConcertSchedulesAfterNowByConcertId(concertId)).willReturn(List.of(concertSchedule1, concertSchedule2));
            given(seatService.getAllAvailableSeats(concertSchedule1.getId())).willReturn(List.of());
            given(seatService.getAllAvailableSeats(concertSchedule2.getId())).willReturn(List.of());

            List<LocalDateTime> availableDateTimes = sut.getAvailableDateTimes(1L);
            assertEquals(0, availableDateTimes.size());
        }
    }
}
