
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

- 기존의 콘서트 티켓 서비스는 모놀리식 서버로 개발되어 있었습니다. <br>
  하지만 이러한 모놀리식 서버의 단점은 <br> 
  (1) 특정 기능 혹은 DB 장애 시 장애가 전체 서비스로 전파될 수 있음 <br>
  (2) 서비스가 커질수록, 작은 변경 시 전체 서비스를 재배포해야 함 <br> 
  과 같은 단점이 존재합니다. <br> 

- 따라서, 이러한 단점을 보완하고자 <br>
  콘서트 티켓 서비스에서 우선적으로 '결제'기능을 독립된 서비스로 분리하는 것을 결정하였습니다. <br>
  

<br> 


### 2) 기존 예약 기능 

- 기존 예약 기능은 다음과 같은 코드로 개발되어 있었습니다. <br>

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

- 하나의 트랜잭션 하에서 예약, 결제, 멤버 서비스에 대한 조회가 같이 일어나고 있습니다. <br> 
  즉, 예약, 결제, 멤버 서비스가 모두 완료되어야만, 트랜잭션이 종료된다는 특징을 갖고 있습니다. <br>  


<br> 


**3) 분산 트랜잭션 및 Saga 패턴** <br>
![image](https://github.com/user-attachments/assets/934ce4e4-aa13-431c-a6d6-80c7f4c9fcc4)![image](https://github.com/user-attachments/assets/91b26c4c-13ca-4343-9f1a-413d505f8722)
![image](https://github.com/user-attachments/assets/58c632a2-a3ff-433c-91c0-cac931921faf)

- 
```
