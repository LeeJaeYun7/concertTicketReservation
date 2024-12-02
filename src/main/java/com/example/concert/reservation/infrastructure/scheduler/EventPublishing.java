package com.example.concert.reservation.infrastructure.scheduler;

import com.example.concert.reservation.event.PaymentRequestEvent;
import com.example.concert.reservation.infrastructure.kafka.producer.KafkaMessageProducer;
import com.example.concert.reservation.infrastructure.messaging.Outbox;
import com.example.concert.reservation.infrastructure.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublishing {

    private final OutboxRepository outboxRepository;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final ObjectMapper objectMapper;
    @Scheduled(fixedRate = 10000)
    public void publishPaymentRequestEvents() throws JsonProcessingException {
        log.info("publishPaymentRequestEvent 실행");

        List<Outbox> events = outboxRepository.findTop10UnsentEvents();

        if(!events.isEmpty()) {

            for(Outbox event: events) {
                String eventJson = event.getMessage();
                PaymentRequestEvent paymentRequestEvent = objectMapper.readValue(eventJson, PaymentRequestEvent.class);

                kafkaMessageProducer.sendPaymentRequestEvent("payment-request-topic", paymentRequestEvent);
                log.info("PaymentEvent Sent");
            }
        }
    }
}
