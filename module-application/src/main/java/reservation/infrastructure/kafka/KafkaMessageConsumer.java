package reservation.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.CustomException;
import common.ErrorCode;
import common.Loggable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reservation.application.ReservationService;
import reservation.application.event.PaymentConfirmedEvent;
import reservation.domain.Outbox;
import reservation.domain.OutboxRepository;
import reservation.domain.vo.PaymentConfirmedVO;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumer {

    private final ReservationService reservationService;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-confirmed-topic")
    public void receivePaymentConfirmedEvent(String message) throws JsonProcessingException {
        PaymentConfirmedEvent event = objectMapper.readValue(message, PaymentConfirmedEvent.class);

        long concertId = event.getConcertId();
        long concertScheduleId = event.getConcertScheduleId();
        String uuid = event.getUuid();
        long seatNumber = event.getSeatNumber();
        long price = event.getPrice();

        PaymentConfirmedVO vo = PaymentConfirmedVO.of(concertId, concertScheduleId, uuid, seatNumber, price);

        reservationService.handlePaymentConfirmed(vo);

        Optional<Outbox> outboxEvent = outboxRepository.findByMessage(message);

        if(outboxEvent.isPresent()){
            Outbox outbox = outboxEvent.get();
            outbox.updateSent(true);
            outboxRepository.save(outbox);
        }
    }

    @KafkaListener(topics = "payment-failed-topic")
    public void receivePaymentFailedEvent(String message) throws JsonProcessingException {
        throw new CustomException(ErrorCode.PAYMENT_FAILED, Loggable.NEVER);
    }
}
