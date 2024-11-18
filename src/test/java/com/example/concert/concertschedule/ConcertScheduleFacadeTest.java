package com.example.concert.concertschedule;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.service.ConcertScheduleFacade;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seatgrade.domain.SeatGrade;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.service.SeatInfoService;
import org.junit.jupiter.api.BeforeEach;
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
    private SeatInfoService seatInfoService;

    @InjectMocks
    private ConcertScheduleFacade sut;

    private long concertId;
    private Concert concert;
    private ConcertHall concertHall;

    private Seat seatNumber1;

    private Seat seatNumber100;
    private ConcertSchedule firstSchedule;
    private ConcertSchedule secondSchedule;
    private SeatInfo firstVIPSeatInfo;
    private SeatInfo firstRSeatInfo;
    private SeatInfo secondVIPSeatInfo;
    private SeatInfo secondRSeatInfo;

    @BeforeEach
    void setUp() {
        concertId = 1L;
        LocalDate startAt = LocalDate.of(2024, 10, 16);
        LocalDate endAt = LocalDate.of(2024, 10, 18);
        concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
        concert = Concert.of("박효신 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

        seatNumber1 = Seat.of(concertHall, 1L);
        seatNumber100 = Seat.of(concertHall, 100L);

        SeatGrade vipGrade = SeatGrade.of(concert, Grade.VIP, 100000);
        SeatGrade rGrade = SeatGrade.of(concert, Grade.R, 80000);

        LocalDateTime firstDateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
        firstSchedule = ConcertSchedule.of(concert, firstDateTime);
        firstVIPSeatInfo = SeatInfo.of(seatNumber1, firstSchedule, vipGrade, SeatStatus.AVAILABLE);
        firstRSeatInfo = SeatInfo.of(seatNumber100, firstSchedule, rGrade, SeatStatus.AVAILABLE);

        LocalDateTime secondDateTime = LocalDateTime.of(2024, 10, 18, 22, 30);
        secondSchedule = ConcertSchedule.of(concert, secondDateTime);
        secondVIPSeatInfo = SeatInfo.of(seatNumber1, secondSchedule, vipGrade, SeatStatus.AVAILABLE);
        secondRSeatInfo = SeatInfo.of(seatNumber100, secondSchedule, rGrade, SeatStatus.AVAILABLE);
    }

    @Nested
    @DisplayName("예약 가능한 공연 날짜를 찾을 때")
    class 예약_가능한_공연_날짜를_찾을_때 {

        @Test
        @DisplayName("두 번의 공연 날짜에 대해서, 모두 예약 가능하다")
        void 두_번의_공연_날짜에_대해서_모두_예약_가능하다() {
            // given
            given(concertScheduleService.getAllAvailableDateTimes(concertId))
                    .willReturn(List.of(firstSchedule.getDateTime(), secondSchedule.getDateTime()));

            given(seatInfoService.getAllAvailableSeats(firstSchedule.getId()))
                    .willReturn(List.of(firstVIPSeatInfo, firstRSeatInfo));

            given(seatInfoService.getAllAvailableSeats(secondSchedule.getId()))
                    .willReturn(List.of(secondVIPSeatInfo, secondRSeatInfo));

            // when
            List<LocalDateTime> availableDateTimes = sut.getAvailableDateTimes(concertId);

            // then
            assertEquals(2, availableDateTimes.size());
        }

        @Test
        @DisplayName("두 번의 공연 날짜에 대해서, 모두 예약이 불가능하다")
        void 두_번의_공연_날짜에_대해서_모두_예약이_불가능하다() {
            // given
            given(concertScheduleService.getAllAvailableDateTimes(concertId))
                    .willReturn(List.of());

            given(seatInfoService.getAllAvailableSeats(firstSchedule.getId()))
                    .willReturn(List.of());
            given(seatInfoService.getAllAvailableSeats(secondSchedule.getId()))
                    .willReturn(List.of());

            // when
            List<LocalDateTime> availableDateTimes = sut.getAvailableDateTimes(concertId);

            // then
            assertEquals(0, availableDateTimes.size());
        }
    }
}
