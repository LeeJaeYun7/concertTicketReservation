package com.example.concert.reservation.infrastructure.kafka.consumer;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.reservation.event.PaymentConfirmedEvent;
import com.example.concert.reservation.infrastructure.messaging.Outbox;
import com.example.concert.reservation.infrastructure.repository.OutboxRepository;
import com.example.concert.reservation.service.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
        reservationService.handlePaymentConfirmed(event);

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
