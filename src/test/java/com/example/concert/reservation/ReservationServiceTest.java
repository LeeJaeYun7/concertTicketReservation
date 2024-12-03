package com.example.concert.reservation;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.reservation.domain.Reservation;
import com.example.concert.reservation.infrastructure.repository.ReservationRepository;
import com.example.concert.reservation.service.ReservationService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seatgrade.domain.SeatGrade;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService sut;

    @Nested
    @DisplayName("예약을 생성할 때")
    class 예약을_생성할때 {
        @Test
        @DisplayName("ConcertSchedule, uuid, Seat, price가 전달될 때, 예약이 생성된다")
        void ConcertSchedule_uuid_Seat_price가_전달될때_예약이_생성된다() {
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);


            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);

            String uuid = UUID.randomUUID().toString();
            Seat seat = Seat.of(concertHall, 1);
            SeatGrade seatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo seatInfo = SeatInfo.of(seat, concertSchedule, seatGrade, SeatStatus.AVAILABLE);

            Reservation reservation = Reservation.of(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, 50000);

            given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

            sut.createReservation(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, 50000);

            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }
    }
}
