package concert.application.order.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.order.application.kafka.OrderPaymentEventProducer;
import concert.application.order.event.OrderPaymentRequestEvent;
import concert.domain.order.entities.OutboxEntity;
import concert.domain.order.entities.dao.OutboxEntityDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentEventScheduler {

  private final OutboxEntityDAO outboxEntityDAO;
  private final OrderPaymentEventProducer orderPaymentEventProducer;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedRate = 10000)
  public void publishOrderPaymentRequestEvents() throws JsonProcessingException {
    log.info("publishOrderPaymentRequestEvent 실행");

    List<OutboxEntity> events = outboxEntityDAO.findTop10UnsentEvents();

    if (events.isEmpty()) {
      return;
    }

    for (OutboxEntity event : events) {
      String eventJson = event.getMessage();
      OrderPaymentRequestEvent orderPaymentRequestEvent = objectMapper.readValue(eventJson, OrderPaymentRequestEvent.class);

      orderPaymentEventProducer.sendOrderPaymentRequestEvent(orderPaymentRequestEvent);
      log.info("OrderPaymentEvent Sent");
    }
  }
}
