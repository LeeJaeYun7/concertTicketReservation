package concert.concertschedule;

import concert.application.concertschedule.application.facade.ConcertScheduleFacade;
import concert.domain.concert.application.ConcertService;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.enums.Grade;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
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
  private ConcertService concertService;

  @Mock
  private ConcertScheduleService concertScheduleService;

  @InjectMocks
  private ConcertScheduleFacade sut;

  private long concertId;
  private Concert concert;
  private ConcertHall concertHall;

  private ConcertHallSeat seatNumber1;

  private ConcertHallSeat seatNumber100;
  private ConcertSchedule firstSchedule;
  private ConcertSchedule secondSchedule;
  private ConcertScheduleSeat firstALLConcertScheduleSeat;
  private ConcertScheduleSeat firstSTANDINGConcertScheduleSeat;
  private ConcertScheduleSeat secondALLConcertScheduleSeat;
  private ConcertScheduleSeat secondSTANDINGConcertScheduleSeat;

  @BeforeEach
  void setUp() {
    concertId = 1L;
    LocalDate startAt = LocalDate.of(2024, 10, 16);
    LocalDate endAt = LocalDate.of(2024, 10, 18);
    concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
    concert = Concert.of("박효신 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

    seatNumber1 = ConcertHallSeat.of(concertHall.getId(), 1L);
    seatNumber100 = ConcertHallSeat.of(concertHall.getId(), 100L);

    SeatGrade allGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
    SeatGrade standingGrade = SeatGrade.of(concert.getId(), Grade.STANDING, 80000);

    LocalDateTime firstDateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
    firstSchedule = ConcertSchedule.of(concert.getId(), firstDateTime);

    firstALLConcertScheduleSeat = ConcertScheduleSeat.of(seatNumber1.getId(), firstSchedule.getId(), allGrade.getId(), SeatStatus.AVAILABLE);
    firstSTANDINGConcertScheduleSeat = ConcertScheduleSeat.of(seatNumber100.getId(), firstSchedule.getId(), standingGrade.getId(), SeatStatus.AVAILABLE);

    LocalDateTime secondDateTime = LocalDateTime.of(2024, 10, 18, 22, 30);
    secondSchedule = ConcertSchedule.of(concert.getId(), secondDateTime);

    secondALLConcertScheduleSeat = ConcertScheduleSeat.of(seatNumber1.getId(), secondSchedule.getId(), allGrade.getId(), SeatStatus.AVAILABLE);
    secondSTANDINGConcertScheduleSeat = ConcertScheduleSeat.of(seatNumber100.getId(), secondSchedule.getId(), allGrade.getId(), SeatStatus.AVAILABLE);
  }

  @Nested
  @DisplayName("예약 가능한 공연 날짜를 찾을 때")
  class 예약_가능한_공연_날짜를_찾을_때 {

    @Test
    @DisplayName("두 번의 공연 날짜에 대해서, 모두 예약 가능하다")
    void 두_번의_공연_날짜에_대해서_모두_예약_가능하다() {
      // given
      given(concertService.getConcertById(concertId))
              .willReturn(concert);

      given(concertScheduleService.getAllAvailableDateTimes(concertId))
              .willReturn(List.of(firstSchedule.getDateTime(), secondSchedule.getDateTime()));

      // when
      List<LocalDateTime> availableDateTimes = sut.getAvailableDateTimes(concertId);

      // then
      assertEquals(2, availableDateTimes.size());
    }

    @Test
    @DisplayName("두 번의 공연 날짜에 대해서, 모두 예약이 불가능하다")
    void 두_번의_공연_날짜에_대해서_모두_예약이_불가능하다() {
      // given
      given(concertService.getConcertById(concertId))
              .willReturn(concert);

      given(concertScheduleService.getAllAvailableDateTimes(concertId))
              .willReturn(List.of());

      // when
      List<LocalDateTime> availableDateTimes = sut.getAvailableDateTimes(concertId);

      // then
      assertEquals(0, availableDateTimes.size());
    }
  }
}
