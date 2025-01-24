package concert.concertscheduleseat;

import concert.application.concertscheduleseat.application.facade.ConcertScheduleSeatFacade;
import concert.commons.common.CustomException;
import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.member.application.MemberService;
import concert.domain.member.domain.Member;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.enums.Grade;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ConcertScheduleSeatFacadeTest {
  @Mock
  private TimeProvider timeProvider;
  @Mock
  private MemberService memberService;
  @Mock
  private ConcertScheduleService concertScheduleService;
  @Mock
  private ConcertScheduleSeatService concertScheduleSeatService;
  @InjectMocks
  private ConcertScheduleSeatFacade sut;

  @Nested
  @DisplayName("좌석 예약을 할 때")
  class 좌석_예약을_할_때 {

    @Test
    @DisplayName("모든 유효성 검사를 통과하고, 좌석 예약이 6분전에 일어났으면 좌석 예약이 가능하다")
    @Disabled
    void 모든_유효성_검사를_통과하고_좌석_예약이_6분전에_일어났으면_좌석_예약이_가능하다() {

      Member member = Member.of("Tom Cruise");
      String uuid = member.getUuid().toString();

      long concertScheduleId = 1L;
      LocalDate startAt = LocalDate.of(2024, 10, 16);
      LocalDate endAt = LocalDate.of(2024, 10, 18);

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      Concert concert = Concert.of("박효신 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);

      long number = 1L;
      ConcertHallSeat seat = ConcertHallSeat.of(concertHall.getId(), 1);
      SeatGrade seatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat concertScheduleSeat = ConcertScheduleSeat.of(seat.getId(), concertSchedule.getId(), seatGrade.getId(), SeatStatus.AVAILABLE);

      given(memberService.getMemberByUuid(uuid)).willReturn(member);
      given(concertScheduleService.getConcertScheduleById(concertScheduleId)).willReturn(concertSchedule);
      given(concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, number)).willReturn(concertScheduleSeat);
      given(timeProvider.now()).willReturn(LocalDateTime.of(2024, 10, 18, 0, 0));
      seat.setUpdatedAt(timeProvider.now().minusMinutes(6));

      sut.createConcertScheduleSeatReservationWithDistributedLock(uuid, concertScheduleId, number);
    }

    @Test
    @DisplayName("좌석_예약이_4분전에_일어났으면_좌석_예약이_불가능하다")
    void 좌석_예약이_4분전에_일어났으면_좌석_예약이_불가능하다() {

      Member member = Member.of("Tom Cruise");
      String uuid = member.getUuid().toString();

      long concertScheduleId = 1L;
      LocalDate startAt = LocalDate.of(2024, 10, 16);
      LocalDate endAt = LocalDate.of(2024, 10, 18);

      ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      Concert concert = Concert.of("박효신 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);

      long number = 1L;
      ConcertHallSeat seat = ConcertHallSeat.of(concertHall.getId(), 1);
      SeatGrade seatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
      ConcertScheduleSeat concertScheduleSeat = ConcertScheduleSeat.of(seat.getId(), concertSchedule.getId(), seatGrade.getId(), SeatStatus.AVAILABLE);

      given(memberService.getMemberByUuid(uuid)).willReturn(member);
      given(concertScheduleService.getConcertScheduleById(concertScheduleId)).willReturn(concertSchedule);
      given(concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, number)).willReturn(concertScheduleSeat);
      given(timeProvider.now()).willReturn(LocalDateTime.of(2024, 10, 18, 0, 0));
      seat.setUpdatedAt(timeProvider.now().minusMinutes(4));

      assertThrows(CustomException.class, () -> sut.createConcertScheduleSeatReservationWithDistributedLock(uuid, concertScheduleId, number));
    }
  }
}
