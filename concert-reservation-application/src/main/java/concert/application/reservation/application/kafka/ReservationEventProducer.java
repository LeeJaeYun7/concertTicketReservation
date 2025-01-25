package concert.application.reservation.application.kafka;

import concert.domain.reservation.event.PaymentConfirmedEvent;
import concert.domain.reservation.event.PaymentRequestEvent;

public interface ReservationEventProducer {
    void sendPaymentRequestEvent(String topic, PaymentRequestEvent event);
    void sendPaymentConfirmedEvent(String topic, PaymentConfirmedEvent event);
}
