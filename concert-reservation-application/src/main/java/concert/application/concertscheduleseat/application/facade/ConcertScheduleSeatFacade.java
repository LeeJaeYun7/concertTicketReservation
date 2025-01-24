package concert.application.concertscheduleseat.application.facade;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.commons.utils.TimeProvider;
import concert.domain.concert.application.ConcertService;
import concert.domain.concerthallseat.application.ConcertHallSeatService;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.member.application.MemberService;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ConcertScheduleSeatFacade {

  private final TimeProvider timeProvider;
  private final MemberService memberService;
  private final ConcertService concertService;
  private final ConcertHallSeatService concertHallSeatService;
  private final ConcertScheduleService concertScheduleService;
  private final ConcertScheduleSeatService concertScheduleSeatService;

  public List<Long> getAvailableConcertScheduleSeatNumbers(long concertScheduleId) {
    concertScheduleService.getConcertScheduleById(concertScheduleId);

    long concertId = concertScheduleService.getConcertScheduleById(concertScheduleId).getConcertId();
    long concertHallId = concertService.getConcertById(concertId).getConcertHallId();
    List<ConcertHallSeat> concertHallSeats = concertHallSeatService.getConcertHallSeatsByConcertHallId(concertHallId);

    return concertScheduleSeatService.getAllAvailableConcertScheduleSeatNumbers(concertScheduleId, concertHallSeats);
  }

  @Transactional
  public void createConcertScheduleSeatReservationWithDistributedLock(String uuid, long concertScheduleId, long concertHallSeatNumber) {
    validateMember(uuid);
    validateConcertSchedule(concertScheduleId);

    boolean isReservable = validateSeatWithDistributedLock(concertScheduleId, concertHallSeatNumber);

    if (!isReservable) {
      throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
    }

    concertScheduleSeatService.changeUpdatedAtWithDistributedLock(concertScheduleId, concertHallSeatNumber);
  }


  private void validateMember(String uuid) {
    memberService.getMemberByUuid(uuid);
  }

  private void validateConcertSchedule(long concertScheduleId) {
    concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private boolean validateSeatWithDistributedLock(long concertScheduleId, long concertHallSeatNumber) {
    String lockName = "SEAT_RESERVATION:" + concertScheduleId + ":" + concertHallSeatNumber;

    ConcertScheduleSeat concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, concertHallSeatNumber);
    return concertScheduleSeat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(concertScheduleSeat.getUpdatedAt());
  }

  private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
    LocalDateTime now = timeProvider.now();
    Duration duration = Duration.between(updatedAt, now);
    return duration.toMinutes() >= 5;
  }
}
