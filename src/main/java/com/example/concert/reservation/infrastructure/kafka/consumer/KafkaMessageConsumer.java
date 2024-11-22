package com.example.concert.reservation.infrastructure.kafka.consumer;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.reservation.event.PaymentConfirmedEvent;
import com.example.concert.reservation.service.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumer {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-confirmed-topic")
    public void receivePaymentConfirmedEvent(String message) throws JsonProcessingException {
        log.info("message 출력!");
        PaymentConfirmedEvent event = objectMapper.readValue(message, PaymentConfirmedEvent.class);
        log.info("역직렬화 완료");
        reservationService.handlePaymentConfirmed(event);
    }

    @KafkaListener(topics = "payment-failed-topic")
    public void receivePaymentFailedEvent(String message) throws JsonProcessingException {
        throw new CustomException(ErrorCode.PAYMENT_FAILED, Loggable.NEVER);
    }
}
