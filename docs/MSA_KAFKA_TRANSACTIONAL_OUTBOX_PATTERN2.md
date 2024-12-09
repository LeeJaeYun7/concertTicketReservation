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
  그러나 Kafka 서버에 문제가 발생해 **결제 요청 메시지 발송이 실패**할 경우, <br>
  **해당 메시지가 소실**될 수 있다는 문제가 발생했습니다. <br>  
  이 문제를 코드로 좀 더 자세히 살펴보겠습니다. <br>


```
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

- 위의 코드를 살펴보면, 좌석과 사용자 잔액에 대한 검증을 마친 후, <br>
  Kafka를 통해 PaymentRequestEvent를 발행합니다. <br>
  하지만 앞서 언급한대로, Kafka 서버에 문제가 발생해 결제 요청 메시지 발송이 실패할 경우, <br>
  해당 메시지가 소실될 수 있다는 문제가 발생합니다. <br> 

  
