package concert.concertscheduleseat;

import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concerthallseat.domain.ConcertHallSeatRepository;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.enums.Grade;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeatRepository;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
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
public class ConcertScheduleSeatIntegrationTest {

  @Autowired
  private ConcertScheduleSeatService sut;

  @Autowired
  private ConcertRepository concertRepository;

  @Autowired
  private ConcertHallRepository concertHallRepository;

  @Autowired
  private ConcertScheduleRepository concertScheduleRepository;
  @Autowired
  private ConcertHallSeatRepository seatRepository;
  @Autowired
  private ConcertScheduleSeatRepository concertScheduleSeatRepository;

  @Nested
  @DisplayName("예약 가능한 좌석을 조회할 때")
  class 예약_가능한_좌석을_조회할때 {

    @Test
    @DisplayName("concertScheduleId가 전달될 때, 예약 가능한 좌석이 조회된다")
    void concertScheduleId가_전달될때_예약_가능한_좌석이_조회된다() {
      LocalDate startAt = LocalDate.of(2024, 10, 16);
      LocalDate endAt = LocalDate.of(2024, 10, 18);

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

      Concert concert = Concert.of("박효신 콘서트", savedConcertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      concertRepository.save(concert);

      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);
      ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

      ConcertHallSeat seat11 = ConcertHallSeat.of(savedConcertHall.getId(), 11);
      seat11.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
      ConcertHallSeat seat22 = ConcertHallSeat.of(savedConcertHall.getId(), 22);
      seat22.setUpdatedAt(LocalDateTime.now().minusMinutes(10));

      seatRepository.save(seat11);
      seatRepository.save(seat22);

      SeatGrade allSeatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat vipConcertScheduleSeat = ConcertScheduleSeat.of(seat11.getId(), concertSchedule.getId(), allSeatGrade.getId(), SeatStatus.AVAILABLE);
      SeatGrade standingSeatGrade = SeatGrade.of(concert.getId(), Grade.STANDING, 80000);
      ConcertScheduleSeat RConcertScheduleSeat = ConcertScheduleSeat.of(seat22.getId(), concertSchedule.getId(), standingSeatGrade.getId(), SeatStatus.AVAILABLE);

      concertScheduleSeatRepository.save(vipConcertScheduleSeat);
      concertScheduleSeatRepository.save(RConcertScheduleSeat);

      List<ConcertScheduleSeat> result = sut.getAllAvailableConcertScheduleSeats(savedConcertSchedule.getId());
      assertEquals(result.get(0).getConcertHallSeatId(), seat11.getId());
      assertEquals(result.get(1).getConcertHallSeatId(), seat22.getId());
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

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      ConcertHall savedConcertHall = concertHallRepository.save(concertHall);

      Concert concert = Concert.of("박효신 콘서트", savedConcertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      concertRepository.save(concert);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);
      ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

      ConcertHallSeat seat = ConcertHallSeat.of(savedConcertHall.getId(), 1);
      seatRepository.save(seat);

      sut.changeUpdatedAtWithDistributedLock(savedConcertSchedule.getId(), 1);

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

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      ConcertHall savedConcertHall = concertHallRepository.save(concertHall);
      Concert concert = Concert.of("박효신 콘서트", savedConcertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      concertRepository.save(concert);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);
      ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

      ConcertHallSeat seat = ConcertHallSeat.of(savedConcertHall.getId(), 11);
      seatRepository.save(seat);

      SeatGrade allSeatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat allConcertScheduleSeat = ConcertScheduleSeat.of(seat.getId(), concertSchedule.getId(), allSeatGrade.getId(), SeatStatus.AVAILABLE);
      concertScheduleSeatRepository.save(allConcertScheduleSeat);

      sut.updateSeatStatus(savedConcertSchedule.getId(), 11, SeatStatus.RESERVED);

      assertEquals(allConcertScheduleSeat.getStatus(), SeatStatus.RESERVED);
    }
  }
}
