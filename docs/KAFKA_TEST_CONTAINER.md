# Kafka 통합 테스트 시, Testcontainer 적용 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.

<br> 
  
**1) 상황(Situation)** <br>
**2) 작업(Task)** <br>
**3) 행동(Action)** <br> 
**4) 결과(Result)** <br>
**5) 참고 자료** <br>

<br> 
<br> 


**1) 상황(Situation)** <br> 
![image](https://github.com/user-attachments/assets/ec7f68d7-064a-47e6-9917-fbdacf61df03)

- 콘서트 예약 서비스 프로젝트에서, 결제 서버로 메시지를 발송하는 과정에서<br> 
  **Apache Kafka**를 사용하여 메시지를 전달하고 있었습니다. <br> 
  이 때, 작성한 애플리케이션 코드가 **Kafka를 통해 정상적으로 동작하는지 테스트**해야 했습니다. <br>

- 하지만, 실제 Prod 환경에 배포될 Kafka를 그대로 테스트에 활용하게 되면, <br> 
  실제 서비스에 사용될 **Offset 오염**, **메시지 Cleaning**  등의 문제가 발생할 수 있습니다. <br>
  따라서 **실제 Prod 환경과 격리된 환경**에서 Kafka 통합 테스트를 진행해야 했습니다. <br>


<br> 

**2) 작업(Task)** <br>

![image](https://github.com/user-attachments/assets/98077e89-2c1d-42b3-80ec-3ff38924f5d5)

- 위의 문제를 해결하기 위해 **'Test Container' 도입**을 선택했습니다. <br> 
  Test Container 도입을 **선택한 이유**는 다음과 같습니다.

  **(1) 격리된 테스트 환경 제공**
  - Test Container는 테스트가 실행될 때마다, **독립적인 환경**에서 진행됩니다. <br>
    따라서 다른 환경과 격리되어 원하는 테스트를 수행할 수 있습니다. <br>

  **(2) 데이터 Cleaning 불필요 및 멱등성 보장**
  - 테스트가 끝나면 Test Container는 자동으로 종료되고 제거됩니다. <br>
    이로 인해 별도의 데이터 Cleaning 작업이 필요하지 않으며, <br>
    각 테스트는 독립적으로 실행되어 다른 테스트에 영향을 주지 않기 때문에 <br>
    **멱등성을 보장**할 수 있습니다. <br> 


<br> 


**3) 행동(Action)** <br>

- TestContainer를 활용한 테스트를 작성하였습니다.

```
// KafkaPaymentRequestIntegrationTest.java

package com.example.concert.reservation;

import com.example.concert.reservation.event.PaymentRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
public class KafkaPaymentRequestIntegrationTest {

    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
                                                                          .waitingFor(Wait.forListeningPort());

    static {
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String kafkaBootstrapServer = kafkaContainer.getBootstrapServers(); 

        // Producer 관련 설정 추가
        registry.add("spring.kafka.producer.bootstrap-servers", () -> kafkaBootstrapServer);
        registry.add("spring.kafka.producer.key-serializer", () -> StringSerializer.class.getName());
        registry.add("spring.kafka.producer.value-serializer", () -> StringSerializer.class.getName());
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;
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

        // Consumer 관련 설정 추가 
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





```



**4) 결과(Result)**

- 격리된 TestContainer 환경에서 테스트가 성공적으로 수행되었습니다. 
![image](https://github.com/user-attachments/assets/ff1a032c-7e64-4eaf-b770-7e0e79c799d7)



**5) 참고 자료**
- Testcontainers로 통합테스트 만들기(https://dev.gmarket.com/76)
- Testcontainers(https://testcontainers.com/)



