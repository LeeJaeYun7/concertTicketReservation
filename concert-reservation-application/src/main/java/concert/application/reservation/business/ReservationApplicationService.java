package concert.application.reservation.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.reservation.ReservationConst;
import concert.application.reservation.application.kafka.ReservationEventProducer;
import concert.application.reservation.event.PaymentConfirmedEvent;
import concert.application.reservation.event.PaymentRequestEvent;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.exceptions.ConcertException;
import concert.domain.concert.exceptions.ConcertExceptionType;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertScheduleSeatService;
import concert.domain.concert.services.ConcertSeatGradeService;
import concert.domain.reservation.command.PaymentConfirmedCommand;
import concert.domain.reservation.entities.Outbox;
import concert.domain.reservation.entities.dao.OutboxRepository;
import concert.domain.reservation.exceptions.ReservationException;
import concert.domain.reservation.exceptions.ReservationExceptionType;
import concert.domain.reservation.txservices.ReservationTxService;
import concert.domain.reservation.vo.ReservationVO;
import concert.domain.shared.utils.TimeProvider;
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
public class ReservationApplicationService {

  private final TimeProvider timeProvider;
  private final ReservationTxService reservationTxService;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final ConcertSeatGradeService concertSeatGradeService;

  private final ConcertScheduleService concertScheduleService;
  private final OutboxRepository outboxRepository;
  private final ReservationEventProducer reservationEventProducer;

  @Getter
  private final CompletableFuture<ReservationVO> reservationFuture = new CompletableFuture<>();

  @Transactional
  public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long concertHallSeatId) throws JsonProcessingException {
    ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, concertHallSeatId);

    long concertSeatGradeId = concertScheduleSeat.getSeatGradeId();
    long price = concertSeatGradeService.getConcertSeatGradePrice(concertSeatGradeId);

    validateConcertScheduleSeatReservation(concertScheduleId, concertHallSeatId);

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

  private void validateConcertScheduleSeatReservation(long concertScheduleId, long concertHallSeatId) {
    ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeat(concertScheduleId, concertHallSeatId);

    if (isFiveMinutesPassed(concertScheduleSeat.getUpdatedAt())) {
      throw new ConcertException(ConcertExceptionType.SEAT_RESERVATION_EXPIRED);
    }
  }

  public void handlePaymentConfirmed(PaymentConfirmedEvent event) throws ReservationException {

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
      throw new ReservationException(ReservationExceptionType.RESERVATION_FAILED);
    }
  }


  private ConcertScheduleEntity getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
    LocalDateTime now = timeProvider.now();
    Duration duration = Duration.between(updatedAt, now);
    return duration.toMinutes() >= 5;
  }

}
