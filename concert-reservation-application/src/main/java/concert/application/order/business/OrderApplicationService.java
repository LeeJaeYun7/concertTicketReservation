package concert.application.order.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.order.OrderConst;
import concert.application.order.application.kafka.OrderPaymentEventProducer;
import concert.application.order.event.OrderPaymentCompensationEvent;
import concert.application.order.event.PaymentOrderConfirmedEvent;
import concert.application.order.event.OrderPaymentRequestEvent;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertScheduleSeatService;
import concert.domain.concert.services.ConcertSeatGradeService;
import concert.domain.order.command.PaymentOrderConfirmedCommand;
import concert.domain.order.entities.OutboxEntity;
import concert.domain.order.entities.dao.OutboxEntityDAO;
import concert.domain.order.exceptions.OrderException;
import concert.domain.order.exceptions.OrderExceptionType;
import concert.domain.order.txservices.OrderTxService;
import concert.domain.order.vo.OrderVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

  private final OrderTxService orderTxService;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final ConcertSeatGradeService concertSeatGradeService;

  private final ConcertScheduleService concertScheduleService;
  private final OutboxEntityDAO outboxEntityDAO;
  private final OrderPaymentEventProducer orderPaymentEventProducer;

  private ConcurrentMap<String, CompletableFuture<OrderVO>> orderFutures = new ConcurrentHashMap<>();


  @Transactional
  public CompletableFuture<OrderVO> createOrder(String uuid, long concertScheduleId, List<Long> concertScheduleSeatIds) throws JsonProcessingException {

    long totalPrice = 0;

    for(long concertScheduleSeatId: concertScheduleSeatIds){
      ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeat(concertScheduleSeatId);
      long concertSeatGradeId = concertScheduleSeat.getConcertSeatGradeId();
      long price = concertSeatGradeService.getConcertSeatGradePrice(concertSeatGradeId);
      totalPrice += price;
    }

    ConcertScheduleEntity concertSchedule = getConcertSchedule(concertScheduleId);

    OrderPaymentRequestEvent event = OrderPaymentRequestEvent.builder()
                                                             .concertId(concertSchedule.getConcertId())
                                                             .concertScheduleId(concertSchedule.getId())
                                                             .uuid(uuid)
                                                             .concertScheduleSeatIds(concertScheduleSeatIds)
                                                             .totalPrice(totalPrice)
                                                             .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String eventJson = objectMapper.writeValueAsString(event);

    OutboxEntity outbox = OutboxEntity.of("order", OrderConst.ORDER_PAYMENT_REQUEST_TOPIC, "OrderPaymentRequest", eventJson, false);
    outboxEntityDAO.save(outbox);

    CompletableFuture<OrderVO> future = new CompletableFuture<>();
    orderFutures.put(uuid, future);

    return future;
  }

  public void handlePaymentOrderConfirmed(PaymentOrderConfirmedEvent event) throws OrderException {

    long concertId = event.getConcertId();
    long concertScheduleId = event.getConcertScheduleId();
    String uuid = event.getUuid();
    List<Long> concertScheduleSeatIds = event.getConcertScheduleSeatIds();
    long totalPrice = event.getTotalPrice();

    PaymentOrderConfirmedCommand command = PaymentOrderConfirmedCommand.of(concertId, concertScheduleId, uuid, concertScheduleSeatIds, totalPrice);

    try {
      OrderVO orderVO = orderTxService.handlePaymentOrderConfirmed(command);

      CompletableFuture<OrderVO> future = orderFutures.remove(uuid);
      if (future != null) {
        future.complete(orderVO);
        log.info("OrderFuture completed for uuid: " + uuid);
      } else {
        log.error("No pending order found for uuid: " + uuid);
      }

    } catch (Exception e) {
      OrderPaymentCompensationEvent compensationEvent = getOrderPaymentCompensationEvent(event);
      orderPaymentEventProducer.sendOrderPaymentCompensationEvent(compensationEvent);
      throw new OrderException(OrderExceptionType.ORDER_FAILED);
    }
  }

  private ConcertScheduleEntity getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private OrderPaymentCompensationEvent getOrderPaymentCompensationEvent(PaymentOrderConfirmedEvent event){
    return OrderPaymentCompensationEvent.builder()
                                        .concertId(event.getConcertId())
                                        .concertScheduleId(event.getConcertScheduleId())
                                        .uuid(event.getUuid())
                                        .concertScheduleSeatIds(event.getConcertScheduleSeatIds())
                                        .totalPrice(event.getTotalPrice())
                                        .build();
  }
}
