# MSA 기반 서비스 분리 시, Transactional Outbox Pattern 적용 보고서 

## 개요

이 보고서는 크게 4가지 파트로 구성됩니다.
  
**1) 문제 정의 - 분산 시스템에서 데이터 일관성 확보의 어려움** <br>
**2) 문제 해결 - Transactional Outbox Pattern 도입** <br>
**3) 예약 서버-결제 서버 메시지 통신 시, Transctional Outbox Pattern 구현** <br> 
**4) 참고 자료** <br> 


<br> 


### 1) 문제 정의 - 분산 시스템에서 데이터 일관성 확보의 어려움

- MSA 기반 서비스 분리 시, **특정 서버에서 발생한 이벤트를 다른 서버로 발송**해야 합니다. <br> 
  이러한 경우, **데이터 일관성을 확보하는 것이 어렵습니다**. <br> 
  이 문제를 예시를 통해 좀 더 자세하게 살펴보겠습니다. <br> 

<br> 

#### (1) 발행되어야 하는 메시지가 발행되지 않는 경우
![image](https://github.com/user-attachments/assets/00e85476-f3cf-4dec-82d4-35cd8f17dbda)
![image](https://github.com/user-attachments/assets/51114dfa-b937-4663-850c-3c848aa97f75)
![image](https://github.com/user-attachments/assets/4549fa3e-332c-4c23-9fbc-fb80a2da9a9d)

- 위의 아키텍처는 **상품을 등록하는 Market 서버 및 DB**와 <br>
  상품을 검색하는 Search 서버와 DB가 분리된 구조 나타냅니다. <br> 
  이러한 경우 **새로운 상품이 Market DB에 등록되면**, <br>
  이 사실이 **Search 서버로 전달되어, Search DB에도 등록**되어야 합니다. <br>

- 이를 위해 서비스 클래스의 registerProduct 메소드에서 **Market DB에 상품을 등록**한 후, <br>
  **컨트롤러 클래스에서 메시지를 발행**하는 방식으로 구현할 수 있습니다. <br>

- 하지만 이와 같은 방식으로 구현할 경우, **메시지를 발행하는 도중에 예외가 발생**하면, <br>
  **Market DB에는 상품이 추가**되었지만 **Search DB에는 등록되지 않는 상황**이 발생할 수 있습니다. <br>
  즉, **상품 정보가 동기화되지 않는 문제**가 발생합니다. <br> 
  

<br> 


#### (2) 발행되지 않아야 하는 메시지가 발행되는 경우

![image](https://github.com/user-attachments/assets/de5a074e-f2bb-4f5e-aa2d-87cc7a185d5c)
![image](https://github.com/user-attachments/assets/156fef63-5f5f-4d30-8a60-b037e3d64e1d)

- 이번에는 **서비스 클래스에서 메시지 발송을 같이 처리**하는 방식으로 구현해보았습니다., <br>
  하지만 이와 같은 방식으로 구현할 경우, <br>
  **데이터베이스 지연 등으로 인해 트랜잭션 커밋이 실패**하는 상황이 발생할 수 있습니다. <br>
  **상품은 데이터베이스에 저장되지 않는데, 메시지는 발행된다는 문제**가 있습니다.

- 이 경우, **Search 서버는 상품이 등록된 것으로 잘못 판단**하여, <br>
  **Search DB에 새로운 상품을 등록**하게 됩니다. <br> 
  즉, **등록되지 않아야 하는 상품 정보가 등록된다는 문제**가 발생합니다. <br>  


<br> 


### 2) 문제 해결 - Transactional Outbox Pattern 도입 

- 위와 같은 문제는 Transactional Outbox Pattern을 도입함으로써 해결할 수 있습니다. <br>

**(1) Transactional Outbox Pattern 이란?** 

![image](https://github.com/user-attachments/assets/dedc0f33-efcd-49fa-9f25-21c5f8e5604a)


- **Transacitonal Outbox Pattern**은 트랜잭셔널 메시징(Transactional Message)의 대표적인 패턴입니다. <br> 
  트랜잭셔널 메시징(Transactional Messaging)은 결과적 일관성(Eventual Consistency)을 목표로 하여, <br> 
  비즈니스 로직 수행과 후속 이벤트 발행을 **원자적으로** 함께 처리하는 방식을 의미합니다. <br> 

- 이를 통해 시스템의 일관성을 유지하며, 메시지 발송 과정에서 발생할 수 있는 오류를 방지할 수 있습니다.


**(2) Transactional Outbox Pattern의 수행 과정** 

- **도메인 로직이 성공적으로 수행되면**, 이벤트 메시지를 **Outbox 테이블**이라는 <br> 
  별도의 테이블에 저장하여 **같이 Commit** 합니다. <br> 

- 즉, 동일한 트랜잭션 내에서 **이벤트 발행을 위한 Outbox 데이터 적재**까지 진행해 <br>
  **이벤트 발행에 대해 보장**합니다. 

- 이렇게 하면, 이벤트 발행 상태 또한 **Outbox 데이터**에 존재하므로, <br>
  배치 프로세스 등을 이용해 **미발행된 데이터에 대한 Fallback 처리**가 용이하다는 이점이 있습니다. <br>


<br> 


### 3)  예약 서버-결제 서버 메시지 통신 시, Transactional Outbox Pattern 구현 

<br> 

(1) **Outbox 엔티티 생성**
- Outbox 테이블을 생성하기 위해 **Outbox 엔티티를 정의**해서 생성했습니다. 

```
@NoArgsConstructor
@Entity
@Getter
@Table(name = "outbox")
public class Outbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender")
    @Schema(description = "발신자, domain name을 의미한다.")
    private String sender;

    @Column(name = "recipient")
    @Schema(description = "수신자, 메시지를 발송할 Kafka topic을 의미한다.")
    private String recipient;

    @Column(name = "subject")
    @Schema(description = "제목, Event Type을 의미한다.")
    private String subject;

    @Column(name = "message")
    @Schema(description = "메시지, 보낸 Event 내용을 의미한다.")
    private String message;

    @Column(name = "sent")
    @Schema(description = "메시지 발송 여부를 의미한다.")
    private boolean sent;

    @Builder
    public Outbox(String sender, String recipient, String subject, String message, boolean sent) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.sent = sent;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Outbox of(String sender, String recipient, String subject, String message, boolean sent){
        return Outbox.builder()
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .sent(sent)
                .build();
    }

    public void updateSent(boolean sent){
        this.sent = sent;
    }
}


```

<br> 


(2) **예약 서비스 수행 시, Outbox 테이블에 이벤트 메시지 저장**
- 현재는 **예약 서버와 결제 서버가 분리**되어 있습니다. <br> 
  따라서 예약 서비스 수행 시, **Outbox 테이블에 걸제 요청 이벤트를 저장**했습니다. <br>
  해당 이벤트는 **별도의 스케줄러를 통해 결제 서버로 전달**될 것입니다. <br> 

```
@Transactional
public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long seatNumber) throws JsonProcessingException {

        SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);
        long price = seatInfo.getSeatGrade().getPrice();

        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, price);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);

        PaymentRequestEvent event = PaymentRequestEvent.builder()
                                                       .concertId(concertSchedule.getConcert().getId())
                                                       .concertScheduleId(concertSchedule.getId())
                                                       .uuid(uuid)
                                                       .seatNumber(seatNumber)
                                                       .price(price)
                                                       .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String eventJson = objectMapper.writeValueAsString(event);

        Outbox outbox = Outbox.of("reservation", "payment-request-topic", "PaymentRequest", eventJson, false);
        outboxRepository.save(outbox);

        return reservationFuture;
}


```


<br> 



(3) **별도의 스케줄러를 통한 결제 요청 이벤트 메시지 발송**
- **별도의 스케줄러를 구현하여**, 10초마다 **발송되지 않은 결제 요청 이벤트 메시지**를<br>
  **최대 10개씩 발송**하도록 처리하였습니다.

```
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

```

(4) **결제 서버로부터 결제 확인 이벤트 수신 시, Outbox 발송 여부 업데이트 처리**
- 결제 서버에서 **결제가 완료되면, 결제 확인 이벤트**를 발송합니다. <br>
  이 때, **예약 서버는 Kafka Consumer 클래스**를 사용하여 결제 확인 이벤트를 **수신**합니다<br>
  그 후, 예약 서버는 **Outbox 이벤트의 발송 여부**를 **최종적으로 업데이트 처리** 합니다. <br>


```
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
```


### 4) 참고 자료
- 분산 시스템에서 메시지 안전하게 다루기(https://blog.gangnamunni.com/post/transactional-outbox/)
- Transactional Outbox 패턴으로 메시지 발행 보장하기(https://ridicorp.com/story/transactional-outbox-pattern-ridi/)
  

