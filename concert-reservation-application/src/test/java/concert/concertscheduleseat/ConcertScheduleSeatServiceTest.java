package concert.concertscheduleseat;

import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.enums.Grade;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeatRepository;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConcertScheduleSeatServiceTest {

  @Mock
  private TimeProvider timeProvider;

  @Mock
  private ConcertScheduleSeatRepository concertScheduleSeatRepository;

  @InjectMocks
  private ConcertScheduleSeatService sut;

  @Nested
  @DisplayName("예약 가능한 좌석을 조회할 때")
  class 예약_가능한_좌석을_조회할때 {

    @Test
    @DisplayName("concertScheduleId가 전달될 때, 예약 가능한 좌석이 조회된다")
    void concertScheduleId가_전달될때_예약_가능한_좌석이_조회된다() {
      LocalDate startAt = LocalDate.of(2024, 10, 16);
      LocalDate endAt = LocalDate.of(2024, 10, 18);

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      Concert concert = Concert.of("박효신 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);

      when(timeProvider.now()).thenReturn(LocalDateTime.of(2024, 10, 18, 0, 0));
      LocalDateTime threshold = timeProvider.now().minusMinutes(5);

      ConcertHallSeat seat11 = ConcertHallSeat.of(concertHall.getId(), 11);
      ConcertHallSeat seat22 = ConcertHallSeat.of(concertHall.getId(), 22);

      SeatGrade allSeatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat vipConcertScheduleSeat = ConcertScheduleSeat.of(seat11.getId(), concertSchedule.getId(), allSeatGrade.getId(), SeatStatus.AVAILABLE);

      SeatGrade standingSeatGrade = SeatGrade.of(concert.getId(), Grade.STANDING, 80000);
      ConcertScheduleSeat RConcertScheduleSeat = ConcertScheduleSeat.of(seat22.getId(), concertSchedule.getId(), standingSeatGrade.getId(), SeatStatus.AVAILABLE);

      List<ConcertScheduleSeat> availableConcertScheduleSeats = List.of(vipConcertScheduleSeat, RConcertScheduleSeat);

      given(concertScheduleSeatRepository.findAllAvailableConcertScheduleSeats(1L, SeatStatus.AVAILABLE, threshold))
              .willReturn(List.of(vipConcertScheduleSeat, RConcertScheduleSeat));

      List<ConcertScheduleSeat> result = sut.getAllAvailableConcertScheduleSeats(1L);

      assertEquals(result.get(0).getConcertHallSeatId(), availableConcertScheduleSeats.get(0).getConcertHallSeatId());
      assertEquals(result.get(1).getConcertHallSeatId(), availableConcertScheduleSeats.get(1).getConcertHallSeatId());
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
      Concert concert = Concert.of("박효신 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);

      ConcertHallSeat seat = ConcertHallSeat.of(concertHall.getId(), 1);
      SeatGrade allSeatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat allConcertScheduleSeat = ConcertScheduleSeat.of(seat.getId(), concertSchedule.getId(), allSeatGrade.getId(), SeatStatus.AVAILABLE);

      when(concertScheduleSeatRepository.findConcertScheduleSeatWithDistributedLock(1L, 1))
              .thenReturn(Optional.of(allConcertScheduleSeat));
      when(timeProvider.now()).thenReturn(LocalDateTime.of(2024, 10, 18, 0, 0));

      sut.changeUpdatedAtWithDistributedLock(1L, 1);

      assertEquals(allConcertScheduleSeat.getUpdatedAt(), LocalDateTime.of(2024, 10, 18, 0, 0));
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
      Concert concert = Concert.of("박효신 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);

      ConcertHallSeat seat = ConcertHallSeat.of(concertHall.getId(), 1);
      SeatGrade allSeatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat allConcertScheduleSeat = ConcertScheduleSeat.of(seat.getId(), concertSchedule.getId(), allSeatGrade.getId(), SeatStatus.AVAILABLE);

      when(concertScheduleSeatRepository.findConcertScheduleSeatWithDistributedLock(1L, 1))
              .thenReturn(Optional.of(allConcertScheduleSeat));

      sut.updateSeatStatus(1L, 1, SeatStatus.RESERVED);

      assertEquals(allConcertScheduleSeat.getStatus(), SeatStatus.RESERVED);
    }
  }
}