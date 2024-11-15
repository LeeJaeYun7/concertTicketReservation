# MSA 기반 서비스 분리에 따른 트랜잭션 처리 한계 및 해결 방안 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 모놀리식 -> MSA 전환 배경** <br>
**2) 기존 예약 기능** <br>
**3) 분산 트랜잭션 및 Saga 패턴** <br>
**4) MSA로 분리된 예약&결제 기능** <br>
**5) MSA 도입을 통해 개선된 점** <br> 

<br> 

### 1) 모놀리식 -> MSA 전환 배경

- 기존의 콘서트 티켓 서비스는 모놀리식 아키텍처로 개발되었습니다. <br>
  하지만 모놀리식 구조는 다음과 같은 단점이 있습니다:

  (1) **특정 기능** 또는 **DB 장애**가 발생할 경우, 장애가 전체 서비스에 영향을 미칠 수 있음 <br>
  (2) 서비스 규모가 커질수록, 작은 변경에도 **전체 서비스**를 재배포해야 하는 부담이 있음 <br> 
   
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



**3) 분산 트랜잭션 및 Saga 패턴** <br>

- **MSA** 환경에서는 **각 서비스가 독립적인 데이터베이스**를 사용합니다. <br>
  따라서 **모놀리식 환경**에서처럼 단일 트랜잭션의 **커밋과 롤백**을 처리하는 방식과는 달리, <br>
  여러 서비스 간의 **트랜잭션 원자성**(Atomicity)을 보장하기 위한 **추가적인 접근**이 필요합니다. <br> 


<br> 


(1) **Two-Phase Commit**(2PC) <br> 
<br> 
![image](https://github.com/user-attachments/assets/9b559a3c-1bd5-4fbd-a7d5-d78be3e5c1b3)


- **Two-Phase Commit**은 코디네이터가 존재하며, 이름 그대로 **2단계**로 나눠서 커밋을 진행하는 접근법입니다. <br>
  첫 번째 단계는 **투표 단계**인데, 코디네이터가 각 트랜잭션 참가자들에게 **커밋 가능 여부**를 질의합니다. <br>
  각 트랜잭션 참여자들은 트랜잭션을 열고, **커밋 가능 여부**를 답하게 됩니다. <br>   

<br> 

![image](https://github.com/user-attachments/assets/9a14741f-fd6a-408d-aefd-11f7351da2df)

- 두 번째 단계는 **커밋 단계**입니다. 모든 참여자들이 **트랜잭션 커밋 가능**이라고 응답했을 경우에 <br>
  **코디네이터**가 커밋 요청을 보내, 트랜잭션을 **성공적으로 종료**합니다. <br>
  만약 **단 하나의 서비스**라도, **트랜잭션 커밋 불가능**이라고 답했을 경우에는, <br>
  코디네이터가 **롤백 요청**을 보내 **트랜잭션을 실패로 처리**합니다. <br>


<br>   

  

(2) **Saga 패턴** <br> 
<br> 
![image](https://github.com/user-attachments/assets/91b26c4c-13ca-4343-9f1a-413d505f8722) <br> 
![image](https://github.com/user-attachments/assets/c686d098-26b5-4ea2-a428-d2afc5d89634) <br> 


- **Saga 패턴**은 각 서비스들이 **작은 로컬 트랜잭션**을 실행하면서 진행하다가 <br>
  특정 단계에서 **실패**하면 **이전에 커밋된 트랜잭션**들에게 **보상 트랜잭션**을 보내 **롤백**하는 방식입니다. <br> 


<br> 



(3) **두 가지 접근 법 비교 및 선택** <br> 
<br> 
![image](https://github.com/user-attachments/assets/58c632a2-a3ff-433c-91c0-cac931921faf) <br> 

- **2PC**는 모든 참여자들이 트랜잭션을 열고, **가장 느린 참여자**의 투표까지 기다려야 한다는 점에서 <br>
  **낮은 가용성**과 **확장성**을 갖는다는 문제가 있습니다. <br>

- 반면, **Saga 패턴**의 경우 각 서비스들의 **로컬 트랜잭션**을 진행한다는 점에서 <br>
  **높은 가용성**과 **확장성**을 갖지만, <br>
  일부 트랜잭션들만 **커밋된 중간 상태**가 노출되며, **보상 트랜잭션**을 직접 구현해야 한다는 **단점**이 있습니다. <br> 

- 저는 **예약 서비스**가 **높은 트래픽**을 견뎌야 하고, <br> 
  추후에 **다른 트랜잭션 참여자들**이 추가될 수 있다는 점을 고려하여 **Saga 패턴**을 채택하였습니다. <br> 


<br> 

### 4) MSA로 분리된 예약&결제 기능  


(1) **새로운 예약 기능**

```
@Transactional
public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long seatNumber) {
      validateSeatReservation(concertScheduleId, seatNumber);
      checkBalanceOverPrice(uuid, concertScheduleId);

      ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
      long price = getConcertSchedule(concertScheduleId).getPrice();

      kafkaTemplate.send("payment-request-topic", new PaymentRequestEvent(concertSchedule.getConcert().getId(), concertScheduleId, uuid, seatNumber, price));

      return reservationFuture;
}
```

<br> 


(2) **MSA로 분리된 결제 기능** 

```
@Transactional
@KafkaListener(topics = "payment-request-topic", groupId = "payment-service")
public void createPayment(PaymentRequestEvent paymentRequestEvent){
        long concertId = paymentRequestEvent.getConcertId();
        long concertScheduleId = paymentRequestEvent.getConcertScheduleId();
        String uuid = paymentRequestEvent.getUuid();
        long seatNumber = paymentRequestEvent.getSeatNumber();
        long price = paymentRequestEvent.getPrice();

        try {
            boolean paymentSuccess = externalPaymentSystemCall(uuid, price);

            if (!paymentSuccess) {
                kafkaTemplate.send("payment-failed-topic", new PaymentFailedEvent(
                        concertId, concertScheduleId, uuid, seatNumber, price, "Payment system error"
                ));
                return;
            }

            Payment payment = Payment.of(concertId, concertScheduleId, uuid, price);
            paymentRepository.save(payment);

            kafkaTemplate.send("payment-confirmed-topic", new PaymentConfirmedEvent(
                    concertId, concertScheduleId, uuid, seatNumber, price));

        } catch (Exception e) {
            kafkaTemplate.send("payment-failed-topic", new PaymentFailedEvent(
                    concertId, concertScheduleId, uuid, seatNumber, price, "System error"
            ));
        }
}

@Retryable(value = {CustomException.class},
           maxAttempts = 5,
           backoff = @Backoff(delay = 1000, multiplier = 3))
private boolean externalPaymentSystemCall(String uuid, long price) {
        return false;
}
```
