package com.example.concert.reservation.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public <T> void sendPaymentEvent(String topic, T event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, eventJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event to JSON", e);
        }
    }
}
