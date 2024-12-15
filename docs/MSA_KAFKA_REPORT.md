# MSA 기반 서비스 분리 시, Kafka 도입 보고서 

## 개요

이 보고서는 크게 4가지 파트로 구성됩니다.
  
**1) 문제 정의 - 분산 시스템에서 동기적 메세지 전달 방식의 문제** <br>
**2) 문제 해결 - Message 브로커 도입** <br> 
**3) Message 브로커 비교 및 선택** <br> 
**4) Kafka 설정하기** <br> 
**5) 참고 자료** <br> 

<br> 

#### 1) 문제 정의 - 분산 시스템에서 동기적 메세지 전달 방식의 문제
![image](https://github.com/user-attachments/assets/3ee70a52-dbb1-490e-a17c-83ee5a0f214d)


- MSA 아키텍처와 같은 분산 시스템에서는 한 서버에서 발생한 이벤트를 다른 서버로 전달할 필요성이 있습니다. <br>
  예를 들어, 예약 서버와 결제 서버가 분리되어 있다면, <br>
  예약 이벤트가 완료되기 위해서 결제가 완료되어야 하므로 <br>
  예약 이벤트는 결제 서버로 발송되어야 합니다. <br>

- 그런데 이 때, MSA 아키텍처에서 동기적 방식으로 메시지를 발송하면 <br>
  다음과 같은 문제가 발생할 수 있습니다.


#### (1) 서비스 간의 높은 결합도(Strongly Coupled)   
- **동기 방식**에서는 보내는 서버가 **응답을 기다려야** 합니다. <br>
  이 때, 만약 **수신 서버에 장애나 지연이 발생**한다면 송신 서버는 멈추거나 대기하게 됩니다. <br>
  이로 인해 송신 서버에 지연이 발생할 수 있습니다. <br>
  이러한 문제는 송신 서버와 수신 서버가 **강한 결합**(Strongly Coupled)을 맺고 있는 결과로 볼 수 있습니다. <br>
  **강한 결합**은 서로의 상태에 영향을 주어 시스템의 유연성과 효율성을 저하시킬 수 있습니다. <br> 


#### (2) 흐름 제어(Flow Control)   
- **동기 방식**에서는 수신 서버가 일시적으로 과부하가 걸리거나 빠르게 처리할 수 없는 경우, <br>
   송신 서버가 이에 맞추어서 처리 속도를 조정하거나 흐름을 제어할 수 있는 여지가 제한적입니다 <br>
   이로 인해 유연성이 떨어지고, 비효율적인 지연이 발생할 수 있습니다. <br>    


#### (3) 재처리(Reprocessing)   
- 송신 서버에서 수신 서버로 메시지 발송이 실패하는 경우, 이에 대한 재처리(Reprocessing)가 필요합니다. <br> 
  그런데 동기 방식에서 재처리는 송신 서버에서 추가적인 대기 시간을 유발할 수 있습니다. <br>  
  그리고 이러한 대기 시간으로 인해 시스템의 효율성이나 성능이 저하될 수 있습니다. <br> 


<br> 


#### 2) 문제 해결 - 비동기 Message 브로커 도입

- 위와 같이 동기적 방식에서 발생하는 문제를 해결하기 위해 비동기 Message 브로커 도입을 고려하게 되었습니다.

#### (1) 왜 비동기 Message 브로커인가?
- 비동기 Message 브로커는 다음과 같은 장점을 갖고 있어서 동기적 방식의 문제를 해결할 수 있습니다. <br> 

(1) **시스템 간 느슨한 결합**
- 비동기 메시지 브로커는 애플리케이션이나 서비스 간의 결합도를 낮추는데 큰 도움이 됩니다. <br>
  시스템 간 직접적인 연결 없이 메시지를 주고 받을 수 있기 때문에, <br> 
  한 시스템이 다른 시스템의 동작을 기다리지 않고, 독립적으로 실행할 수 있습니다.

**(2) 비동기 처리를 통한 성능 향상**
- 비동기 메시지 브로커를 사용하면 응답 시간이 빠르며, 대기 시간이 길어지는 작업을 다른 시스템에 맡기고, <br> 
  즉시 응답을 돌려줄 수 있습니다. 이는 특히 높은 트래픽을 처리해야 하는 시스템에서 중요한 장점입니다. <br> 


**(3) 확장성과 유연성**
- 비동기 메시지 브로커는 확장성과 유연성을 높이는 데 도움을 줍니다. <br>
  예를 들어, 수많은 요청을 처리해야 하는 경우, 메시지 브로커는 요청을 큐에 쌓고 <br>
  순차적으로 처리함으로써 트래픽을 효과적으로 분산시킬 수 있습니다. <br>

  

#### 3) Message 브로커 비교 및 선택 

- Message 브로커는 대표적으로 **Redis Pub/Sub, RabbitMQ, Apache Kafka**가 있습니다. <br>
  각각에 대해 비교해서 살펴보겠습니다. <br>         

<img src="https://github.com/user-attachments/assets/c53f07d6-1aaa-49e8-ac2a-3e384b8d85c4" width="30%" style="margin-right: 10px;" /> 
<img src="https://github.com/user-attachments/assets/8b47c053-789a-47d7-bd90-abdad0747428" width="30%" style="margin-right: 10px;" />
<img src="https://github.com/user-attachments/assets/c45c5244-db7b-4e10-9b9d-1fd2f3aca215" width="30%" />


<br> 
<br> 


| **특징**                     | **Redis Pub/Sub**                                            | **RabbitMQ**                                              | **Apache Kafka**                                          |
|------------------------------|--------------------------------------------------------------|----------------------------------------------------------|----------------------------------------------------------|
| **기본 개념**                 | 메시지를 발행하고 구독하는 간단한 메시징 시스템            | 메시지를 큐에 저장하고 소비자가 이를 읽는 큐 기반 시스템 | 로그 기반의 분산 스트리밍 플랫폼, 메시지 큐와 비슷한 기능 수행 |
| **구성 요소**                 | Pub/Sub (Publisher/Subscriber) 모델                         | Producer, Consumer, Queue, Exchange                      | Producer, Consumer, Broker, Topic                        |
| **메시지 지속성**             | 메시지 영속화가 기본적으로 없으며, 메모리 기반 처리        | 메시지 영속화 가능, 디스크에 저장                         | 메시지 영속화, 디스크 기반으로 긴 시간동안 저장 가능   |
| **성능**                      | 매우 빠름, 낮은 레이턴시                                   | 상대적으로 빠름, 높은 처리량                              | 높은 처리량, 높은 확장성, 대규모 데이터 스트리밍에 적합   |
| **확장성**                    | 수평 확장 가능, 하지만 복잡한 구성에서는 제한적일 수 있음 | 높은 확장성, 클러스터링과 파티셔닝으로 확장 가능         | 매우 높은 확장성, 수많은 파티션을 통해 데이터 분산 가능  |
| **순서 보장**                 | 메시지 순서 보장하지 않음                                 | 큐 내 메시지 순서 보장 가능                               | 파티션 내에서만 메시지 순서 보장                         |
| **메시지 전달 방식**          | 한 번 전달된 메시지는 구독자가 처리하고 종료               | 메시지는 한 번 소비되면 큐에서 제거                       | 메시지는 Consumer가 오프셋을 관리하며 여러 번 읽을 수 있음 |
| **사용 사례**                 | 실시간 알림, 간단한 이벤트 전송 시스템                    | 주문 시스템, 채팅 시스템, 워크플로우 시스템 등           | 실시간 데이터 처리, 로그 수집, 이벤트 스트리밍            |
| **주요 장점**                 | 빠른 메시지 처리 속도, 클러스터 설정 간단, 메모리 기반으로 속도 좋음       | 다양한 기능, 안정성, AMQP 프로토콜 지원                  | 높은 처리량, 대규모 데이터 스트리밍, 내결함성             |
| **주요 단점**                 | 메시지 휘발성, 대규모 트래픽 하에서 성능 문제 가능        | 상대적으로 느리며, 메시지 복제와 큐 관리가 복잡할 수 있음 | 관리와 운영이 복잡, 높은 리소스 소모                      |

<br> 

- 콘서트 예약 서비스 프로젝트에서는 이러한 특징들을 고려할 때, <br>

  (1) **높은 처리량 및 확장성** <br>
  (2) **메시지 순서 보장** <br> 
  (3) **높은 신뢰성 및 성능** <br> 

  을 보장하는 **Apache Kafka를 선택**하기로 하였습니다. 


<br> 

#### 4) Kafka 설정하기   

**(1) Kafka Docker 설정**
- 기본적으로 Docker를 활용해서 Kafka 클러스터를 실행시켰습니다. <br>
  Kafka 클러스터는 Zookeeper를 통해 관리하며, 세 개의 브로커(kafka-1, kafka-2, kafka-3)로 운영되도록 하였습니다. <br> 
  또한, Kafka UI를 위한 kafka-ui도 포함시켰습니다. 


```
---
version: '3.8'
services:
  zookeeper-1:
    image: confluentinc/cp-zookeeper:5.5.1
    container_name: zookeeper-1
    ports:
      - '32181:32181'
    environment:
      ZOOKEEPER_CLIENT_PORT: 32181
      ZOOKEEPER_TICK_TIME: 2000

  kafka-1:
    image: confluentinc/cp-kafka:5.5.1
    container_name: kafka-1
    ports:
      - '9092:9092'
    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-1:29092,EXTERNAL://localhost:9092
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3

  kafka-2:
    image: confluentinc/cp-kafka:5.5.1
    container_name: kafka-2
    ports:
      - '9093:9093'
    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-2:29093,EXTERNAL://localhost:9093
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3

  kafka-3:
    image: confluentinc/cp-kafka:5.5.1
    container_name: kafka-3
    ports:
      - '9094:9094'
    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-1:32181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-3:29094,EXTERNAL://localhost:9094
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3

  kafka-ui:
    image: provectuslabs/kafka-ui
    container_name: kafka-ui
    ports:
      - "8989:8080"
    restart: always
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka-1:29092,kafka-2:29093,kafka-3:29094
      - KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper-1:22181


```

**(2) Kafka Producer & Consumer 설정**
- Kafka를 활용해 네트워크 통신을 하기 위해서는 <br>
  각각의 서버에 Producer, Consumer 설정이 필요합니다.
  Spring 기준으로, 이 설정은 application.yml에 추가해서 관리합니다.

```
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

    consumer:
      bootstrap-servers: localhost:9092
      group-id: my_group
      enable-auto-commit: false
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

<br>

- 각 설정을 정리하면 다음과 같습니다.

| **Type**     | **Property**           | **Description**                                                                                                                                                                                                                   |
|--------------|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Producer** | `bootstrap.servers`     | 카프카 클러스터와 첫 연결을 생성하기 위해 프로듀서가 사용할 브로커의 host:port입니다.                                                                                                                                               |
|              | `key.serializer`        | 카프카에 쓸 레코드의 키의 값을 직렬화하기 위해 사용하는 시리얼라이저 클래스의 이름입니다. 카프카 브로커는 메시지의 키값, 밸류값으로 바이트 배열을 받습니다.                                                                           |
|              | `value.serializer`      | 카프카에 쓸 레코드의 밸류값을 직렬화하기 위해 사용하는 시리얼라이저 클래스의 이름입니다.                                                                                                                                               |
| **Consumer** | `group.id`              | Consumer가 속하는 Consumer Group을 지정하는 속성입니다.                                                                                                                                                                           |
|              | `enable.auto-commit`    | Consumer가 자동으로 오프셋을 커밋할지의 여부를 결정하는 매개변수입니다. 기본값은 `true`이며, `false`로 설정하면 커밋 시점을 직접 제어할 수 있습니다.                                                                            |
|              | `auto.offset.reset`     | Consumer가 예전에 오프셋을 커밋한 적이 없거나, 커밋된 오프셋이 유효하지 않을 때, 파티션을 읽기 시작할 때의 작동을 정의합니다. 기본값은 `latest`로, 유효한 오프셋이 없을 경우 최신 레코드부터 읽기 시작합니다. `earliest`로 설정하면 파티션의 맨 처음부터 모든 데이터를 읽습니다. |


<br> 


#### 5) 참고 자료
-카프카 핵심 가이드(https://product.kyobobook.co.kr/detail/S000201464167) <br> 
-라이브 채팅 플랫폼 구현기 1탄(https://kakaoentertainment-tech.tistory.com/109) <br> 
-Kafka와 RabbitMQ의 차이점은 무엇인가요?(https://aws.amazon.com/ko/compare/the-difference-between-rabbitmq-and-kafka/)


