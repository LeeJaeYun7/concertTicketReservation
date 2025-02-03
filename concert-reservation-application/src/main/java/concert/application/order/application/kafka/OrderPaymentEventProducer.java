package concert.application.order.application.kafka;

import concert.application.order.event.OrderPaymentCompensationEvent;
import concert.application.order.event.OrderPaymentRequestEvent;

public interface OrderPaymentEventProducer {
    void sendOrderPaymentRequestEvent(OrderPaymentRequestEvent event);
    void sendOrderPaymentCompensationEvent(OrderPaymentCompensationEvent event);
}
