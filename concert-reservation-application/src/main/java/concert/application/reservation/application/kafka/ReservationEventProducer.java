package concert.application.reservation.application.kafka;

import concert.application.reservation.event.PaymentConfirmedEvent;
import concert.application.reservation.event.PaymentRequestEvent;

public interface ReservationEventProducer {
    void sendPaymentRequestEvent(PaymentRequestEvent event);
    void sendPaymentConfirmedEvent(PaymentConfirmedEvent event);
}
