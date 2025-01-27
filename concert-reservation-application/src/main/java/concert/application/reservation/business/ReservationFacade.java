package concert.application.reservation.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.reservation.ReservationConst;
import concert.application.reservation.application.kafka.ReservationEventProducer;
import concert.application.reservation.event.PaymentConfirmedEvent;
import concert.application.reservation.event.PaymentRequestEvent;
import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.commons.utils.TimeProvider;
import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertService;
import concert.domain.concert.services.ConcertScheduleSeatService;
import concert.domain.concert.services.SeatGradeService;
import concert.domain.member.entities.Member;
import concert.domain.member.services.MemberService;
import concert.domain.reservation.command.PaymentConfirmedCommand;
import concert.domain.reservation.entities.Outbox;
import concert.domain.reservation.entities.dao.OutboxRepository;
import concert.domain.reservation.txservices.ReservationTxService;
import concert.domain.reservation.vo.ReservationVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationFacade {

  private final TimeProvider timeProvider;
  private final ReservationTxService reservationTxService;
  private final MemberService memberService;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final SeatGradeService seatGradeService;

  private final ConcertService concertService;
  private final ConcertScheduleService concertScheduleService;
  private final OutboxRepository outboxRepository;
  private final ReservationEventProducer reservationEventProducer;

  @Getter
  private final CompletableFuture<ReservationVO> reservationFuture = new CompletableFuture<>();

  @Transactional
  public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long concertHallSeatId) throws JsonProcessingException {
    ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, concertHallSeatId);

    long seatGradeId = concertScheduleSeat.getSeatGradeId();
    long price = seatGradeService.getSeatGradePrice(seatGradeId);

    validateSeatReservation(concertScheduleId, concertHallSeatId);
    checkBalanceOverPrice(uuid, price);

    ConcertScheduleEntity concertSchedule = getConcertSchedule(concertScheduleId);

    PaymentRequestEvent event = PaymentRequestEvent.builder()
            .concertId(concertSchedule.getConcertId())
            .concertScheduleId(concertSchedule.getId())
            .uuid(uuid)
            .price(price)
            .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String eventJson = objectMapper.writeValueAsString(event);

    Outbox outbox = Outbox.of("reservation", ReservationConst.PAYMENT_REQUEST_TOPIC, "PaymentRequest", eventJson, false);
    outboxRepository.save(outbox);

    return reservationFuture;
  }

  private void validateSeatReservation(long concertScheduleId, long concertHallSeatId) {
    ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeat(concertScheduleId, concertHallSeatId);

    if (isFiveMinutesPassed(concertScheduleSeat.getUpdatedAt())) {
      throw new CustomException(ErrorCode.SEAT_RESERVATION_EXPIRED, Loggable.ALWAYS);
    }
  }

  private void checkBalanceOverPrice(String uuid, long price) {
    long balance = getMember(uuid).getBalance();

    if (balance - price < 0) {
      throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE, Loggable.NEVER);
    }
  }

  public void handlePaymentConfirmed(PaymentConfirmedEvent event) {

    long concertId = event.getConcertId();
    long concertScheduleId = event.getConcertScheduleId();
    String uuid = event.getUuid();
    long seatNumber = event.getSeatNumber();
    long price = event.getPrice();

    PaymentConfirmedCommand command = PaymentConfirmedCommand.of(concertId, concertScheduleId, uuid, seatNumber, price);

    try {
      reservationTxService.handlePaymentConfirmed(command);
    } catch (Exception e) {
      reservationEventProducer.sendPaymentConfirmedEvent(event);
      throw new CustomException(ErrorCode.RESERVATION_FAILED, Loggable.ALWAYS);
    }
  }


  private ConcertEntity getConcert(long concertScheduleId) {
    ConcertScheduleEntity concertSchedule = getConcertSchedule(concertScheduleId);
    return concertService.getConcertById(concertSchedule.getConcertId());
  }

  private ConcertScheduleEntity getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private Member getMember(String uuid) {
    return memberService.getMemberByUuid(uuid);
  }


  private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
    LocalDateTime now = timeProvider.now();
    Duration duration = Duration.between(updatedAt, now);
    return duration.toMinutes() >= 5;
  }

}
