package concert.application.reservation.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.domain.reservation.event.PaymentRequestEvent;
import concert.application.reservation.application.kafka.ReservationEventProducer;
import concert.domain.reservation.domain.Outbox;
import concert.domain.reservation.domain.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventPublishing {

  private final OutboxRepository outboxRepository;
  private final ReservationEventProducer reservationEventProducer;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedRate = 10000)
  public void publishPaymentRequestEvents() throws JsonProcessingException {
    log.info("publishPaymentRequestEvent 실행");

    List<Outbox> events = outboxRepository.findTop10UnsentEvents();

    if (!events.isEmpty()) {

      for (Outbox event : events) {
        String eventJson = event.getMessage();
        PaymentRequestEvent paymentRequestEvent = objectMapper.readValue(eventJson, PaymentRequestEvent.class);

        reservationEventProducer.sendPaymentRequestEvent("payment-request-topic", paymentRequestEvent);
        log.info("PaymentEvent Sent");
      }
    }
  }
}
