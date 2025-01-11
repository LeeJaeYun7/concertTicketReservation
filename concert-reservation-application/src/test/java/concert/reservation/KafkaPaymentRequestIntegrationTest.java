package concert.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import concert.application.reservation.application.event.PaymentRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Slf4j
@Disabled
public class KafkaPaymentRequestIntegrationTest {

  @Container
  static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
          .waitingFor(Wait.forListeningPort());

  static {
    kafkaContainer.start();
  }

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    String kafkaBootstrapServer = "localhost:" + kafkaContainer.getMappedPort(9093);
    registry.add("spring.kafka.bootstrap-servers", () -> kafkaBootstrapServer);
    registry.add("spring.kafka.consumer.group-id", () -> "test-group");
    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    registry.add("spring.kafka.consumer.key-deserializer", () -> StringDeserializer.class.getName());
    registry.add("spring.kafka.consumer.value-deserializer", () -> StringDeserializer.class.getName());
  }

  @Test
  @DisplayName("PaymentRequestEvent를 Kafka에 발행 후 구독한다")
  void PaymentRequestEvent를_Kafka에_발행_후_구독한다() throws JsonProcessingException {
    PaymentRequestEvent event = new PaymentRequestEvent(11L, 11L, "abcd", 10, 50000);

    String eventJson = objectMapper.writeValueAsString(event);
    kafkaTemplate.send("payment-request-event", eventJson);
    String consumedMessage = pollMessageFromKafka("payment-request-event");

    PaymentRequestEvent consumedEvent = objectMapper.readValue(consumedMessage, PaymentRequestEvent.class);

    assertEquals(event.getConcertId(), consumedEvent.getConcertId());
    assertEquals(event.getConcertScheduleId(), consumedEvent.getConcertScheduleId());
    assertEquals(event.getUuid(), consumedEvent.getUuid());
  }

  private String pollMessageFromKafka(String topic) {
    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
    consumerProps.put("group.id", "test-group");
    consumerProps.put("key.deserializer", StringDeserializer.class);
    consumerProps.put("value.deserializer", StringDeserializer.class);
    consumerProps.put("auto.offset.reset", "earliest");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
    consumer.subscribe(java.util.Collections.singletonList(topic));

    var records = consumer.poll(Duration.ofSeconds(5));

    if (!records.isEmpty()) {
      return records.iterator().next().value();
    }

    return "";
  }
}
