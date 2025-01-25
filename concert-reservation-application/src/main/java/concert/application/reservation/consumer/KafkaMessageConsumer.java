package concert.application.reservation.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.reservation.ReservationConst;
import concert.domain.reservation.event.PaymentConfirmedEvent;
import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.domain.reservation.txservice.ReservationTxService;
import concert.domain.reservation.domain.Outbox;
import concert.domain.reservation.domain.OutboxRepository;
import concert.domain.reservation.command.PaymentConfirmedCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumer {

  private final ReservationTxService reservationTxService;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = ReservationConst.PAYMENT_CONFIRMED_TOPIC)
  public void receivePaymentConfirmedEvent(String message) throws JsonProcessingException {
    PaymentConfirmedEvent event = objectMapper.readValue(message, PaymentConfirmedEvent.class);

    long concertId = event.getConcertId();
    long concertScheduleId = event.getConcertScheduleId();
    String uuid = event.getUuid();
    long seatNumber = event.getSeatNumber();
    long price = event.getPrice();

    PaymentConfirmedCommand command = PaymentConfirmedCommand.of(concertId, concertScheduleId, uuid, seatNumber, price);

    reservationTxService.handlePaymentConfirmed(command);

    Optional<Outbox> outboxEvent = outboxRepository.findByMessage(message);

    if (outboxEvent.isPresent()) {
      Outbox outbox = outboxEvent.get();
      outbox.updateSent(true);
      outboxRepository.save(outbox);
    }
  }

  @KafkaListener(topics = ReservationConst.PAYMENT_FAILED_TOPIC)
  public void receivePaymentFailedEvent(String message) throws JsonProcessingException {
    throw new CustomException(ErrorCode.PAYMENT_FAILED, Loggable.NEVER);
  }
}
