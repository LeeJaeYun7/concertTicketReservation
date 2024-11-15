
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


(1) **Two-Phace Commit**(2PC) <br> 
<br> 
![image](https://github.com/user-attachments/assets/934ce4e4-aa13-431c-a6d6-80c7f4c9fcc4)




(2) **Saga 패턴** <br> 
<br> 
![image](https://github.com/user-attachments/assets/9eaadd1d-b181-4a6c-9e6a-0d2ab4d14ed2) <br> 
![image](https://github.com/user-attachments/assets/91b26c4c-13ca-4343-9f1a-413d505f8722) <br> 





(3) **두 가지 접근 법 비교 및 선택** <br> 
![image](https://github.com/user-attachments/assets/58c632a2-a3ff-433c-91c0-cac931921faf)

- 
```
