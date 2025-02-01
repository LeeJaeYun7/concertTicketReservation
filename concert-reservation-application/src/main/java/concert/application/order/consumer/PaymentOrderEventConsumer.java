package concert.application.order.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.order.OrderConst;
import concert.application.order.business.OrderApplicationService;
import concert.application.order.event.PaymentOrderConfirmedEvent;
import concert.domain.order.command.PaymentOrderConfirmedCommand;
import concert.domain.order.entities.OutboxEntity;
import concert.domain.order.exceptions.OrderException;
import concert.domain.order.exceptions.OrderExceptionType;
import concert.domain.order.txservices.OrderTxService;
import concert.domain.order.entities.dao.OutboxEntityDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentOrderEventConsumer {

  private final OrderApplicationService orderApplicationService;
  private final OutboxEntityDAO outboxEntityDAO;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = OrderConst.PAYMENT_ORDER_CONFIRMED_TOPIC)
  public void receivePaymentOrderConfirmedEvent(String message) throws JsonProcessingException, OrderException {

    log.info("receivePaymentOrderConfirmedEvent message?" + message);

    PaymentOrderConfirmedEvent event = objectMapper.readValue(message, PaymentOrderConfirmedEvent.class);
    log.info("changed Object: {}", event);
    orderApplicationService.handlePaymentOrderConfirmed(event);

    Optional<OutboxEntity> outboxEvent = outboxEntityDAO.findByMessage(message);

    if (outboxEvent.isPresent()) {
      OutboxEntity outbox = outboxEvent.get();
      outbox.updateSent(true);
      outboxEntityDAO.save(outbox);
    }
  }

  @KafkaListener(topics = OrderConst.PAYMENT_ORDER_FAILED_TOPIC)
  public void receivePaymentOrderFailedEvent(String message) throws OrderException {
    throw new OrderException(OrderExceptionType.PAYMENT_FAILED);
  }
}
