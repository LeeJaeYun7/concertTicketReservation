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

- 위의 코드는 **2가지 상황에서 문제**가 발생할 수 있습니다. 

<br> 

**(1)** **Kafka 서버 문제로 인한 메시지 소실**  
- 위의 코드에서는, 좌석과 사용자 잔액에 대한 검증을 마친 후, <br>
 **Kafka를 통해 PaymentRequestEvent를 발행**합니다. <br>

- 하지만 Kafka 서버에 문제가 발생해 결제 요청 메시지 발송이 실패할 경우, <br>
  **재처리 로직이 없기 때문에 메시지가 소실**될 수 있다는 문제가 발생합니다. <br> 


**(2)** **발송 서버 문제로 인한 메시지 오발송**  
- 위의 코드에서 **kafkaTemplate.send**를 통해 성공적으로 메시지가 발송된 다음, <br> 
  발송 서버의 문제로 서버가 다운될 수 있습니다. <br>

- 이 경우, 트랜잭션이 롤백되면서 예약 정보는 취소되지만, <br>
  이미 발송된 메시지는 **트랜잭션과 별개로 이미 전송되었기 때문에 롤백이 불가능**합니다. <br>
  이로 인해 **예약과 결제 간에 불일치**가 발생할 수 있습니다. 
  
<br> 


#### 2) 작업(Task)

- 위의 문제를 해결하기 위해 **Transactional Outbox Pattern 도입**을 고려하게 되었습니다. <br> 
