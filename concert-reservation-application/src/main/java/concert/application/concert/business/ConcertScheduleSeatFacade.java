package concert.application.concert.business;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.commons.utils.TimeProvider;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.entities.enums.ConcertScheduleSeatStatus;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertService;
import concert.domain.concerthall.services.ConcertHallSeatService;
import concert.domain.concerthall.entities.ConcertHallSeatEntity;
import concert.domain.member.services.MemberService;
import concert.domain.concert.services.ConcertScheduleSeatService;
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
    List<ConcertHallSeatEntity> concertHallSeatEntities = concertHallSeatService.getConcertHallSeatsByConcertHallId(concertHallId);

    return concertScheduleSeatService.getAllAvailableConcertScheduleSeatNumbers(concertScheduleId, concertHallSeatEntities);
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

    ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, concertHallSeatNumber);
    return concertScheduleSeat.getStatus().equals(ConcertScheduleSeatStatus.AVAILABLE) && isFiveMinutesPassed(concertScheduleSeat.getUpdatedAt());
  }

  private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
    LocalDateTime now = timeProvider.now();
    Duration duration = Duration.between(updatedAt, now);
    return duration.toMinutes() >= 5;
  }
}
