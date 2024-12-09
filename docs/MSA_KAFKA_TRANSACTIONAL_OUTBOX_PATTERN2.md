# MSA 기반 서비스 분리 시, Transactional Outbox Pattern 적용 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.

<br> 
  
**1) 상황(Situation)** <br>
**2) 작업(Task)** <br>
**3) 행동(Action)** <br>
**4) 결과(Result)** <br>
**5) 참고 자료** <br> 


<br> 


#### 1) 상황(Situation) 

- 콘서트 예약 서비스에서는 **결제 서버를 별도로 분리**하여, <br>
  사용자가 예약을 요청할 때, Kafka를 통해 결제 요청 메시지를 발행하도록 구현하였습니다. <br>
  이 요구 사항을 처음에는 **아래와 같은 코드로 구현**하였습니다. <br>


```
@Transactional
public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long seatNumber) {
        SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);
        long price = seatInfo.getSeatGrade().getPrice();

        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, price);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
        kafkaTemplate.send("payment-request-topic", new PaymentRequestEvent(concertSchedule.getConcert().getId(), concertScheduleId, uuid, seatNumber, price));

        return reservationFuture;
}
```

<br> 

- 하지만 위의 코드는 **2가지 상황에서 문제**가 발생할 수 있습니다. 

<br> 

**(1)** **Kafka 서버 문제로 인한 메시지 소실**  
- 위의 코드에서는, 좌석과 사용자 잔액에 대한 검증을 마친 후, <br>
 **Kafka를 통해 PaymentRequestEvent를 발행**합니다. <br>

- 하지만 Kafka 서버에 문제가 발생해 결제 요청 메시지 발송이 실패할 경우, <br>
  **재처리 로직이 없기 때문에 메시지가 소실**될 수 있다는 문제가 발생합니다. <br> 

<br> 

**(2)** **발송 서버 문제로 인한 메시지 오발송**  
- 위의 코드에서 **kafkaTemplate.send**를 통해 성공적으로 메시지가 발송된 다음, <br> 
  발송 서버의 문제로 서버가 다운될 수 있습니다. <br>

- 이 경우, 트랜잭션이 롤백되면서 예약 정보는 취소되지만, <br>
  이미 발송된 메시지는 **트랜잭션과 별개로 이미 전송되었기 때문에 롤백이 불가능**합니다. <br>
  이로 인해 **예약과 결제 간에 불일치**가 발생할 수 있습니다. 
  
<br> 


#### 2) 작업(Task)

- 위의 문제를 해결하기 위해 **Transactional Outbox Pattern 도입**을 고려하게 되었습니다. <br>


**(1) Transactional Outbox Pattern 이란?** 

![image](https://github.com/user-attachments/assets/dedc0f33-efcd-49fa-9f25-21c5f8e5604a)


- **Transacitonal Outbox Pattern**은 트랜잭셔널 메시징(Transactional Message)의 대표적인 패턴입니다. <br> 
  트랜잭셔널 메시징(Transactional Messaging)은 **결과적 일관성**(Eventual Consistency)을 목표로 하여, <br> 
  비즈니스 로직 수행과 후속 이벤트 발행을 **원자적으로** 함께 처리하는 방식을 의미합니다. <br> 

- 이를 통해 시스템의 일관성을 유지하며, 메시지 발송 과정에서 발생할 수 있는 오류를 방지할 수 있습니다.


**(2) Transactional Outbox Pattern의 수행 과정** 

- **도메인 로직이 성공적으로 수행되면**, 이벤트 메시지를 **Outbox 테이블**이라는 <br> 
  별도의 테이블에 저장하여 **같이 Commit** 합니다. <br> 

- 즉, 동일한 트랜잭션 내에서 **이벤트 발행을 위한 Outbox 데이터 적재**까지 진행해 <br>
  **이벤트 발행에 대해 보장**합니다. 

- 이렇게 하면, 이벤트 발행 상태 또한 **Outbox 데이터**에 존재하므로, <br>
  배치 프로세스 등을 이용해 **미발행된 데이터에 대한 재처리**가 용이하다는 이점이 있습니다. <br>


<br> 

