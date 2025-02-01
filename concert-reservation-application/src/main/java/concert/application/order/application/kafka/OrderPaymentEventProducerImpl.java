package concert.application.order.application.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.order.OrderConst;
import concert.application.order.event.OrderPaymentCompensationEvent;
import concert.application.order.event.OrderPaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentEventProducerImpl implements OrderPaymentEventProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void sendOrderPaymentRequestEvent(OrderPaymentRequestEvent event) {
    try {
      String eventJson = objectMapper.writeValueAsString(event);
      log.info("ORDER_PAYMENT_REQUEST_TOPIC에 이벤트 전달");
      kafkaTemplate.send(OrderConst.ORDER_PAYMENT_REQUEST_TOPIC, eventJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event to JSON", e);
    }
  }

  public void sendOrderPaymentCompensationEvent(OrderPaymentCompensationEvent event) {
    try {
      String eventJson = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(OrderConst.ORDER_PAYMENT_COMPENSATION_TOPIC, eventJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event to JSON", e);
    }
  }
}
