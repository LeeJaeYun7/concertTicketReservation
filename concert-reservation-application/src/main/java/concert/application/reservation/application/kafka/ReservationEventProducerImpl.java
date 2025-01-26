package concert.application.reservation.application.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.reservation.ReservationConst;
import concert.application.reservation.event.PaymentConfirmedEvent;
import concert.application.reservation.event.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationEventProducerImpl implements ReservationEventProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void sendPaymentRequestEvent(PaymentRequestEvent event) {
    try {
      String eventJson = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(ReservationConst.PAYMENT_REQUEST_TOPIC, eventJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event to JSON", e);
    }
  }

  public void sendPaymentConfirmedEvent(PaymentConfirmedEvent event) {
    try {
      String eventJson = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(ReservationConst.PAYMENT_COMPENSATION_TOPIC, eventJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event to JSON", e);
    }
  }
}
