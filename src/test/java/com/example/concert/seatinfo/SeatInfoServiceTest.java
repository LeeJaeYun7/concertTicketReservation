package com.example.concert.seatinfo;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seatgrade.domain.SeatGrade;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.repository.SeatInfoRepository;
import com.example.concert.seatinfo.service.SeatInfoService;
import com.example.concert.utils.TimeProvider;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatInfoServiceTest {

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private SeatInfoRepository seatInfoRepository;

    @InjectMocks
    private SeatInfoService sut;

    @Nested
    @DisplayName("예약 가능한 좌석을 조회할 때")
    class 예약_가능한_좌석을_조회할때 {

        @Test
        @DisplayName("concertScheduleId가 전달될 때, 예약 가능한 좌석이 조회된다")
        void concertScheduleId가_전달될때_예약_가능한_좌석이_조회된다() {
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);

            when(timeProvider.now()).thenReturn(LocalDateTime.of(2024, 10, 18, 0, 0));
            LocalDateTime threshold = timeProvider.now().minusMinutes(5);

            Seat seat11 = Seat.of(concertHall, 11);
            Seat seat22 = Seat.of(concertHall, 22);

            SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo vipSeatInfo = SeatInfo.of(seat11, concertSchedule, vipSeatGrade, SeatStatus.AVAILABLE);
            SeatGrade RseatGrade = SeatGrade.of(concert, Grade.R, 80000);
            SeatInfo RSeatInfo = SeatInfo.of(seat22, concertSchedule, RseatGrade, SeatStatus.AVAILABLE);
            List<SeatInfo> availableSeatInfos = List.of(vipSeatInfo, RSeatInfo);

            given(seatInfoRepository.findAllAvailableSeats(1L, SeatStatus.AVAILABLE, threshold))
                    .willReturn(List.of(vipSeatInfo, RSeatInfo));

            List<SeatInfo> result = sut.getAllAvailableSeats(1L);
            assertEquals(result.get(0).getSeat().getNumber(), availableSeatInfos.get(0).getSeat().getNumber());
            assertEquals(result.get(1).getSeat().getNumber(), availableSeatInfos.get(1).getSeat().getNumber());
        }
    }

    @Nested
    @DisplayName("좌석의 업데이트 시각을 최신화할 때")
    class 업데이트_시각을_최신화할때 {

        @Test
        @DisplayName("업데이트 시각을 최신화할 때 성공한다")
        void 업데이트_시각을_최신화할때_성공한다() {
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);

            Seat seat = Seat.of(concertHall, 1);
            SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo vipSeatInfo = SeatInfo.of(seat, concertSchedule, vipSeatGrade, SeatStatus.AVAILABLE);

            when(seatInfoRepository.findSeatInfoWithPessimisticLock(1L, 1))
                    .thenReturn(Optional.of(vipSeatInfo));
            when(timeProvider.now()).thenReturn(LocalDateTime.of(2024, 10, 18, 0, 0));

            sut.changeUpdatedAtWithPessimisticLock(1L, 1);

            assertEquals(vipSeatInfo.getUpdatedAt(), LocalDateTime.of(2024, 10, 18, 0, 0));
        }
    }

    @Nested
    @DisplayName("좌석 상태를 업데이트할 때")
    class 좌석_상태를_업데이트할때 {
        @Test
        @DisplayName("좌석 상태를 업데이트할 때 성공한다")
        void 좌석_상태를_업데이트할_때_성공한다() {
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad",  120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);

            Seat seat = Seat.of(concertHall, 1);
            SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo vipSeatInfo = SeatInfo.of(seat, concertSchedule, vipSeatGrade, SeatStatus.AVAILABLE);

            when(seatInfoRepository.findSeatInfoWithPessimisticLock(1L, 1))
                    .thenReturn(Optional.of(vipSeatInfo));

            sut.updateSeatStatus(1L, 1, SeatStatus.RESERVED);

            assertEquals(vipSeatInfo.getStatus(), SeatStatus.RESERVED);
        }
    }
}