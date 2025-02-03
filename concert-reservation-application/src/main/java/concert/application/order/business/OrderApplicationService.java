package concert.application.order.business;

import concert.application.order.OrderConst;
import concert.application.order.application.kafka.OrderEventProducer;
import concert.application.order.event.OrderCompensationEvent;
import concert.application.order.event.PaymentConfirmedEvent;
import concert.application.order.event.OrderRequestEvent;
import concert.application.shared.utils.ApplicationJsonConverter;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertScheduleSeatService;
import concert.domain.concert.services.ConcertSeatGradeService;
import concert.domain.order.command.PaymentConfirmedCommand;
import concert.domain.order.entities.OutboxEntity;
import concert.domain.order.entities.dao.OutboxEntityDAO;
import concert.domain.order.exceptions.OrderException;
import concert.domain.order.exceptions.OrderExceptionType;
import concert.domain.order.txservices.OrderTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

  private final ApplicationJsonConverter applicationJsonConverter;
  private final OrderTxService orderTxService;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final ConcertSeatGradeService concertSeatGradeService;

  private final ConcertScheduleService concertScheduleService;
  private final OutboxEntityDAO outboxEntityDAO;
  private final OrderEventProducer orderEventProducer;

  @Transactional
  public void createOrder(String uuid, long concertScheduleId, List<Long> concertScheduleSeatIds) {

    long totalPrice = 0;

    for(long concertScheduleSeatId: concertScheduleSeatIds){
      ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeat(concertScheduleSeatId);
      long concertSeatGradeId = concertScheduleSeat.getConcertSeatGradeId();
      long price = concertSeatGradeService.getConcertSeatGradePrice(concertSeatGradeId);
      totalPrice += price;
    }

    ConcertScheduleEntity concertSchedule = getConcertSchedule(concertScheduleId);

    OrderRequestEvent event = OrderRequestEvent.builder()
                                                             .concertId(concertSchedule.getConcertId())
                                                             .concertScheduleId(concertSchedule.getId())
                                                             .uuid(uuid)
                                                             .concertScheduleSeatIds(concertScheduleSeatIds)
                                                             .totalPrice(totalPrice)
                                                             .build();

    String eventJson = applicationJsonConverter.convertToJson(event);

    OutboxEntity outbox = OutboxEntity.of("order", OrderConst.ORDER_PAYMENT_REQUEST_TOPIC, "OrderPaymentRequest", eventJson, false);
    outboxEntityDAO.save(outbox);
  }

  public void handlePaymentConfirmed(PaymentConfirmedEvent event) throws OrderException {

    long concertId = event.getConcertId();
    long concertScheduleId = event.getConcertScheduleId();
    String uuid = event.getUuid();
    List<Long> concertScheduleSeatIds = event.getConcertScheduleSeatIds();
    long totalPrice = event.getTotalPrice();

    PaymentConfirmedCommand command = PaymentConfirmedCommand.of(concertId, concertScheduleId, uuid, concertScheduleSeatIds, totalPrice);

    try {
      orderTxService.handlePaymentConfirmed(command);
    } catch (Exception e) {
      OrderCompensationEvent compensationEvent = getOrderCompensationEvent(event);
      orderEventProducer.sendOrderCompensationEvent(compensationEvent);
      throw new OrderException(OrderExceptionType.ORDER_FAILED);
    }
  }

  private ConcertScheduleEntity getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private OrderCompensationEvent getOrderCompensationEvent(PaymentConfirmedEvent event){
    return OrderCompensationEvent.builder()
                                        .concertId(event.getConcertId())
                                        .concertScheduleId(event.getConcertScheduleId())
                                        .uuid(event.getUuid())
                                        .concertScheduleSeatIds(event.getConcertScheduleSeatIds())
                                        .totalPrice(event.getTotalPrice())
                                        .build();
  }
}
