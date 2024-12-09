# MSA 기반 서비스 분리 시, Saga 패턴을 활용한 분산 트랜잭션 도입 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 모놀리식 -> MSA 전환 배경** <br>
**2) 기존 예약 기능** <br>
**3) 2PC 방식 vs Saga 패턴** <br>
**4) MSA로 분리된 예약&결제 기능** <br>
**5) MSA 도입을 통해 개선된 점** <br> 

<br> 

### 1) 모놀리식 -> MSA 전환 배경

- 기존의 콘서트 티켓 서비스는 **모놀리식 아키텍처**로 개발되었습니다. <br>
  하지만 모놀리식 아키텍처는 다음과 같은 단점이 있습니다:

  (1) **특정 기능** 또는 **DB 장애**가 발생할 경우, 장애가 전체 서비스에 영향을 미칠 수 있습니다 <br>
  (2) 서비스 규모가 커질수록, 작은 변경에도 **전체 서비스**를 재배포해야 하는 부담이 있습니다 <br> 
  
   
- 이러한 문제점을 해결하기 위해, <br>
  콘서트 티켓 서비스는 우선적으로 **'결제' 기능**을 독립된 서비스로 분리하는 방안을 채택하였습니다.

<br> 


### 2) 기존 예약 기능 

- 기존 예약 기능은 다음과 같은 코드로 구현되어 있었습니다. <br>

```

    @Transactional
    public ReservationVO createReservation(String token, String uuid, long concertScheduleId, long seatNumber) {
        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, concertScheduleId);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
        Seat seat = seatService.getSeatByConcertHallIdAndNumberWithPessimisticLock(concertScheduleId, seatNumber);
        long price = getConcertSchedule(concertScheduleId).getPrice();

        reservationService.createReservation(concertSchedule.getConcert(), concertSchedule, uuid, seat, price);
        paymentService.createPayment(concertSchedule.getConcert(), concertSchedule, uuid, price);
        memberService.decreaseBalance(uuid, price);

        updateStatus(token, concertScheduleId, seatNumber);

        String name = getMember(uuid).getName();
        String concertName = getConcert(concertScheduleId).getName();
        LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

        return ReservationVO.of(name, concertName, dateTime, price);
    }
```

- 하나의 트랜잭션 내에서 **예약, 결제, 멤버 서비스**에 대한 조회가 동시에 이루어집니다. <br>
  즉, **예약, 결제, 멤버 서비스**가 모두 완료되어야만 트랜잭션이 종료되는 구조입니다. <br> 


<br> 
<br> 



### 3) 2PC 방식 vs Saga 패턴 <br>

- **MSA** 환경에서는 **각 서비스가 독립적인 데이터베이스**를 사용합니다. <br>
  따라서 **모놀리식 환경**에서처럼 단일 트랜잭션의 **커밋과 롤백**을 처리하는 방식과는 달리, <br>
  여러 서비스 간의 **트랜잭션 원자성**(Atomicity)을 보장하기 위한 **추가적인 접근**이 필요합니다. <br> 


<br> 


#### (1) Two-Phase Commit(2PC) <br> 
<br> 
<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/9b559a3c-1bd5-4fbd-a7d5-d78be3e5c1b3" alt="Image 1" width="800"></td>
    <td><img src="https://github.com/user-attachments/assets/9a14741f-fd6a-408d-aefd-11f7351da2df" alt="Image 2" width="800"></td>
  </tr>
</table>


- **Two-Phase Commit**은 코디네이터가 존재하며, 이름 그대로 **2단계**로 나눠서 커밋을 진행하는 접근법입니다. <br>
  첫 번째 단계는 **투표 단계**인데, 코디네이터가 각 트랜잭션 참가자들에게 **커밋 가능 여부**를 질의합니다. <br>
  각 트랜잭션 참여자들은 트랜잭션을 열고, **커밋 가능 여부**를 답하게 됩니다. <br>   


- 두 번째 단계는 **커밋 단계**입니다. 모든 참여자들이 **트랜잭션 커밋 가능**이라고 응답했을 경우에 <br>
  **코디네이터**가 커밋 요청을 보내, 트랜잭션을 **성공적으로 종료**합니다. <br>
  만약 **단 하나의 서비스**라도, **트랜잭션 커밋 불가능**이라고 답했을 경우에는, <br>
  코디네이터가 **롤백 요청**을 보내 **트랜잭션을 실패로 처리**합니다. <br>


<br>

- **2PC 방식**은 다음과 같은 **장점과 단점**을 갖고 있습니다.
 

| 항목   | 내용                                                                 |
|--------|----------------------------------------------------------------------|
| 장점   | 트랜잭션의 원자성(All or nothing)을 보장합니다. |
|        | 데이터 일관성을 보장합니다.                                           |
| 단점   | 2PC는 코디네이터에 의존적이어서, 코디네이터 장애 시 모든 트랜잭션 작업이 중단됩니다. |
|        | 2PC는 참여자가 많아질수록 복잡도가 증가합니다.                         |
|        | 2PC는 블로킹 방식으로 동작하므로, 참여자 중 하나가 응답을 하지 않으면 전체 트랜잭션이 블로킹됩니다. |
|        | NoSQL 등 일부 DBMS가 지원하지 않으면 사용할 수 없습니다.                   |

- 결론적으로, 2PC 방식은 **높은 일관성**을 보장하지만, 그 대가로 **낮은 가용성**과 **낮은 확장성**을 가진 방식입니다.


<br>   


#### (2) Saga 패턴 <br> 
<br>
<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/4215561b-5c6f-404d-afc2-08c9ee783af0" alt="Image 1" width="800"></td>
    <td><img src="https://github.com/user-attachments/assets/c686d098-26b5-4ea2-a428-d2afc5d89634" alt="Image 2" width="800"></td>
  </tr>
</table>


- Saga 패턴은 2PC와 달리 각 서비스가 **작은 로컬 트랜잭션**을 실행하면서 진행됩니다. <br> 
  특정 단계에서 실패하면 이전에 커밋된 트랜잭션들에게 **보상 트랜잭션**을 보내 롤백합니다. <br> 

- Saga 패턴의 특징은 '**최종 일관성**(Eventual Consistency)을 보장한다는 점입니다. <br> 
  이는 2PC가 제공하는 '**강력한 일관성**(Strong Consistency)'과 차이를 보입니다. <br> 
  최종 일관성은 시스템이 결과적으로 일관성을 맞추는 모델로, 즉시 일관성을 요구하지 않고, <br>
  점진적으로 일관된 상태로 수렴하는 방식입니다. <br> 

- Saga 패턴은 크게 **코레오그래피 방식**과 **오케스트레이션 방식**이 존재합니다. <br>
  각각에 대해 살펴보고 **콘서트 예약 서비스를 기준으로 도식화**해보겠습니다. <br>

<br>


#### (2-1) 코레오그래피(Choreography) 방식

<br> 

![hello drawio](https://github.com/user-attachments/assets/9c1fba82-7327-4423-adbf-1511ca2f5258)










#### (2-2) 오케스트레이션(Orchestration) 방식

<br>

![hello drawio (1)](https://github.com/user-attachments/assets/45afa4db-4e1f-453b-967c-68eba7754f94)




<br> 




#### (3) 2PC 방식과 Saga 패턴 비교 및 선택 <br> 
<br> 

![image](https://github.com/user-attachments/assets/be82919c-486a-464f-b9cf-4d34a76239ae)

- **2PC 방식**은 모든 참여자들이 트랜잭션을 열고, **가장 느린 참여자**의 투표까지 기다려야 한다는 점에서 <br>
  **낮은 가용성**과 **확장성**을 갖는다는 문제가 있습니다. <br>

- 반면, **Saga 패턴**의 경우 각 서비스들의 **로컬 트랜잭션**을 진행한다는 점에서 <br>
  **높은 가용성**과 **확장성**을 갖지만, <br>
  일부 트랜잭션들만 **커밋된 중간 상태**가 노출되며, **보상 트랜잭션**을 직접 구현해야 한다는 **단점**이 있습니다. <br> 

- 저는 **예약 서비스**가 **높은 트래픽**을 견뎌야 하고, <br> 
  추후에 **다른 트랜잭션 참여자들**이 추가될 수 있다는 점을 고려하여 **Saga 패턴**을 채택하였습니다. <br> 


<br> 
<br> 

### 4) MSA로 분리된 예약&결제 기능  


(1) **새로운 예약 기능**
- 새로운 예약 기능은 사용자 **좌석 예약**과 **잔액 조회** 후, <br>
  결제 서버로 **결제 요청 이벤트**를 전송하는 방식으로 작동합니다. <br>
  결제 요청 이벤트는 메시지 큐인 **Kafka**를 사용하여 전달됩니다. <br> 


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
}
```


<br> 


(2) **MSA로 분리된 결제 기능** 
- 결제 기능은 **Kafka**를 통해 전달된 이벤트를 **리스닝**한 후 결제 작업을 처리합니다. <br>
  결제가 성공하면 **예약 서버**로 **성공 이벤트**를 Kafka를 통해 예약 서버로 전달하고, <br>
  실패하면 **실패 이벤트**를 전송합니다. <br> 

- 이 과정에서는 외부 결제 시스템이 연동된다고 가정합니다. <br> 

```
@Transactional
public void createPayment(PaymentRequestEvent paymentRequestEvent){

        long concertId = paymentRequestEvent.getConcertId();
        long concertScheduleId = paymentRequestEvent.getConcertScheduleId();
        String uuid = paymentRequestEvent.getUuid();
        long seatNumber = paymentRequestEvent.getSeatNumber();
        long price = paymentRequestEvent.getPrice();

        try {
            boolean paymentSuccess = externalPaymentSystemCall(uuid, price);

            if (!paymentSuccess) {
                kafkaMessageProducer.sendPaymentFailedEvent("payment-failed-topic", new PaymentFailedEvent(
                        concertId, concertScheduleId, uuid, seatNumber, price, "Payment system error"
                ));
                return;
            }

            Payment payment = Payment.of(concertId, concertScheduleId, uuid, price);
            paymentRepository.save(payment);

            kafkaMessageProducer.sendPaymentConfirmedEvent("payment-confirmed-topic", new PaymentConfirmedEvent(
                    concertId, concertScheduleId, uuid, seatNumber, price));

        } catch (Exception e) {
            kafkaMessageProducer.sendPaymentFailedEvent("payment-failed-topic", new PaymentFailedEvent(
                    concertId, concertScheduleId, uuid, seatNumber, price, "System error"
            ));
        }
    }
```

<br> 


(3) **결제 완료 시 추가적으로 실행되는 예약 기능**
- 결제 완료 후, 해당 **이벤트**는 **Kafka**를 통해 전달되어 추가적인 예약 작업이 실행됩니다. <br>
  이 과정에서 예약 중 **예외**가 발생하면, **이전 결제 트랜잭션**을 취소해야 하므로, <br>
  **결제 서버**에 **보상 트랜잭션**을 실행할 이벤트를 전달합니다.
  
```
@Transactional
public void handlePaymentConfirmed(PaymentConfirmedEvent event) {

        long concertScheduleId = event.getConcertScheduleId();
        String uuid = event.getUuid();
        long seatNumber = event.getSeatNumber();
        long price = event.getPrice();

        try {
            ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
            SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);

            memberService.decreaseBalance(uuid, price);
            updateStatus(concertScheduleId, seatNumber);

            createReservation(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, price);

            String name = getMember(uuid).getName();
            String concertName = getConcert(concertScheduleId).getName();
            LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

            ReservationVO reservationVO = ReservationVO.of(name, concertName, dateTime, price);

            reservationFacade.getReservationFuture().complete(reservationVO);
        } catch (Exception ex) {
            kafkaMessageProducer.sendPaymentConfirmedEvent("payment-compensation-topic", event);
            throw new CustomException(ErrorCode.RESERVATION_FAILED, Loggable.ALWAYS);
        }
    }
```

<br> 

(4) **예약 실패 시, 결제 서버에서 발생하는 보상 트랜잭션**

- **결제 서버**는 **보상 트랜잭션 요청 이벤트**를 전달 받으면, <br>
  해당 요청 따라 **보상 트랜잭션**을 실시합니다. <br>
  이를 통해 이전에 **성공한 결제 트랜잭션은 취소**가 됩니다. <br> 

```
@Transactional
public void handleCompensationEvent(PaymentRequestEvent paymentRequestEvent) {
        long concertId = paymentRequestEvent.getConcertId();
        long concertScheduleId = paymentRequestEvent.getConcertScheduleId();
        String uuid = paymentRequestEvent.getUuid();
        long seatNumber = paymentRequestEvent.getSeatNumber();
        long price = paymentRequestEvent.getPrice();

        try {
            Payment payment = paymentRepository.findByConcertIdAndConcertScheduleIdAndUuid(concertId, concertScheduleId, uuid)
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, Loggable.ALWAYS));

            paymentRepository.delete(payment);

            kafkaMessageProducer.sendPaymentCompensationSuccessEvent("payment-compensation-success-topic", new PaymentCompensationSuccessEvent(
                    concertId, concertScheduleId, uuid, seatNumber, price, "Payment canceled successfully"
            ));
        } catch (Exception e) {
            kafkaMessageProducer.sendPaymentCompensationFailedEvent("payment-compensation-failed-topic", new PaymentCompensationFailedEvent(
                    concertId, concertScheduleId, uuid, seatNumber, price, "Compensation failed"
            ));
        }
}
```



**5) MSA 도입을 통해 개선된 점** <br> 

- MSA 도입을 통해 **개선된 점**은 다음과 같습니다.

**(1) 독립적 배포 가능** <br>
- 각 서비스별로 독립적으로 배포가 가능하고, 빌드 파일의 크기도 줄어드므로, 빌드 시간도 단축됩니다. <br> 

**(2) 각 서비스별 장애 격리** <br> 
- 하나의 서비스에서 발생한 장애가 다른 서비스로 전파되지 않고, 각 서비스별 장애가 격리됩니다.

**(3) 서비스별 기술 선택의 유연성** <br>
- 각 서비스가 독립적인 기술 스택을 선택할 수 있어, 기술 선택에 있어 더 큰 유연성을 제공합니다. <br> 




