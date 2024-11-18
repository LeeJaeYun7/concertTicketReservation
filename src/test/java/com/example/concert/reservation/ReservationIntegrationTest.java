package com.example.concert.reservation;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concerthall.repository.ConcertHallRepository;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.reservation.domain.Reservation;
import com.example.concert.reservation.repository.ReservationRepository;
import com.example.concert.reservation.service.ReservationService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.repository.SeatRepository;
import com.example.concert.seatgrade.domain.SeatGrade;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.seatgrade.repository.SeatGradeRepository;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.repository.SeatInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ReservationIntegrationTest {

    @Autowired
    private ReservationService sut;
    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertHallRepository concertHallRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatGradeRepository seatGradeRepository;

    @Autowired
    private SeatInfoRepository seatInfoRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Nested
    @DisplayName("예약을 생성할 때")
    class 예약을_생성할때 {
        @Test
        @DisplayName("ConcertSchedule, uuid, Seat, price가 전달될 때, 예약이 생성된다")
        void ConcertSchedule_uuid_Seat_price가_전달될때_예약이_생성된다() {
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

            Concert concert = Concert.of("브루노 마스 콘서트", savedConcertHall, "장충체육관", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);

            concertRepository.save(concert);
            concertScheduleRepository.save(concertSchedule);

            String uuid = UUID.randomUUID().toString();
            Seat seat = Seat.of(savedConcertHall, 1);
            SeatGrade seatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo seatInfo = SeatInfo.of(seat, concertSchedule, seatGrade, SeatStatus.AVAILABLE);

            seatRepository.save(seat);
            seatGradeRepository.save(seatGrade);
            seatInfoRepository.save(seatInfo);

            Reservation reservation = Reservation.of(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, 50000);
            reservationRepository.save(reservation);

            Reservation savedReservation = sut.createReservation(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, 50000);

            assertEquals("브루노 마스 콘서트", savedReservation.getConcertSchedule().getConcert().getName());
            assertEquals(50000, savedReservation.getPrice());
            assertEquals(1, savedReservation.getSeatInfo().getSeat().getNumber());
        }
    }
}
