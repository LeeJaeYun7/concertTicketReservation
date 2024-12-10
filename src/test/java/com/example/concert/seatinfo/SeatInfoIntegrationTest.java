package com.example.concert.seatinfo;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concerthall.repository.ConcertHallRepository;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.repository.SeatRepository;
import com.example.concert.seatgrade.domain.SeatGrade;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.repository.SeatInfoRepository;
import com.example.concert.seatinfo.service.SeatInfoService;
import org.junit.jupiter.api.Disabled;
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
@Disabled
public class SeatInfoIntegrationTest {

    @Autowired
    private SeatInfoService sut;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertHallRepository concertHallRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatInfoRepository seatInfoRepository;

    @Nested
    @DisplayName("예약 가능한 좌석을 조회할 때")
    class 예약_가능한_좌석을_조회할때 {

        @Test
        @DisplayName("concertScheduleId가 전달될 때, 예약 가능한 좌석이 조회된다")
        void concertScheduleId가_전달될때_예약_가능한_좌석이_조회된다() {
            LocalDate startAt = LocalDate.of(2024, 10, 16);
            LocalDate endAt = LocalDate.of(2024, 10, 18);

            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
            ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

            Concert concert = Concert.of("박효신 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            concertRepository.save(concert);

            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);
            ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

            Seat seat11 = Seat.of(savedConcertHall, 11);
            seat11.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
            Seat seat22 = Seat.of(savedConcertHall, 22);
            seat22.setUpdatedAt(LocalDateTime.now().minusMinutes(10));

            seatRepository.save(seat11);
            seatRepository.save(seat22);

            SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo vipSeatInfo = SeatInfo.of(seat11, concertSchedule, vipSeatGrade, SeatStatus.AVAILABLE);
            SeatGrade RseatGrade = SeatGrade.of(concert, Grade.R, 80000);
            SeatInfo RSeatInfo = SeatInfo.of(seat22, concertSchedule, RseatGrade, SeatStatus.AVAILABLE);

            seatInfoRepository.save(vipSeatInfo);
            seatInfoRepository.save(RSeatInfo);

            List<SeatInfo> result = sut.getAllAvailableSeats(savedConcertSchedule.getId());
            assertEquals(result.get(0).getSeat().getNumber(), seat11.getNumber());
            assertEquals(result.get(1).getSeat().getNumber(), seat22.getNumber());
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
            ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

            Concert concert = Concert.of("박효신 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            concertRepository.save(concert);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);
            ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

            Seat seat = Seat.of(savedConcertHall, 1);
            seatRepository.save(seat);

            sut.changeUpdatedAtWithPessimisticLock(savedConcertSchedule.getId(), 1);

            assertEquals(seat.getUpdatedAt(), LocalDateTime.now());
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
            ConcertHall savedConcertHall = concertHallRepository.save(concertHall);
            Concert concert = Concert.of("박효신 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

            concertRepository.save(concert);

            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);
            ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

            Seat seat = Seat.of(savedConcertHall, 11);
            seatRepository.save(seat);

            SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
            SeatInfo vipSeatInfo = SeatInfo.of(seat, concertSchedule, vipSeatGrade, SeatStatus.AVAILABLE);
            seatInfoRepository.save(vipSeatInfo);

            sut.updateSeatStatus(savedConcertSchedule.getId(), 11, SeatStatus.RESERVED);

            assertEquals(vipSeatInfo.getStatus(), SeatStatus.RESERVED);
        }
    }
}
