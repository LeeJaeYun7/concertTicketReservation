

# 동시성 제어 보고서 

## 1. 콘서트 대기열 시스템에서 동시성 문제가 발생할 수 있는 로직 

### 1) 잔액 충전

**(1) 발생 원인**<br>
- 한 명의 사용자가 잔액을 충전할 때, 같은 요청을 여러 번 호출 할 수 있음<br> 
-> 이러한 경우, 1번의 요청만 승인되도록 해야 하며, 이를 멱등성(idempotent) 처리라고 합니다. 

**(2) 목표 결과**<br>
- 한 명의 사용자가 여러 번 충전 요청을 보내더라도, 잔액은 한 번만 증가해야 한다
- 충전 금액은 사용자 계정에 정확하게 반영되어야 한다.

<br> 

### 2) 좌석 예약 요청

**(1) 발생 원인**<br>
- 동시에 여러 명의 사용자가 하나의 좌석에 대해 예약 요청을 할 수 있음  

**(2) 목표 결과**<br>
- 특정 좌석에 대해 한 명의 사용자만 예약 요청이 성공해야 한다
- 동시에 요청한 나머지 사용자들은 예약 요청이 실패해야 한다   

<br> 

### 3) 결제 요청

**(1) 발생 원인**<br>
- 한 명의 사용자가 결제를 할 때, 같은 요청을 여러 번 호출할 수 있음<br>
-> 이러한 경우, 1번의 요청만 승인되도록 해야 하며, 이를 멱등성(idempotent) 처리라고 합니다. 

**(2) 목표 결과**<br>
- 한 명의 사용자가 여러 번 결제 요청을 보내더라도, 결제는 한 번만 되어야 한다.
- 결제 금액의 차감은 사용자 계정에 정확하게 반영되어야 한다.

<br> 

## 2. 동시성 제어 코드


### 1) 잔액 충전

**(1) 비관적 락(Pessimistic Lock) <br>**

```
public ChargeResponse chargeBalance(UUID uuid, long amount) throws Exception {
        validateMember(uuid);

        Member member = memberService.getMemberByUuidWithLock(uuid);
        long balance = member.getBalance();
        long updatedBalance = balance + amount;
        member.updateBalance(updatedBalance);

        chargeService.createCharge(uuid, amount);

        return ChargeResponse.of(updatedBalance);
}
```
```
public Member getMemberByUuidWithLock(UUID uuid) throws Exception {
        return memberRepository.findByUuidWithLock(uuid).orElseThrow(Exception::new);
}
```
```
@Lock(LockModeType.PESSIMISTIC_READ)
@Query("SELECT m from Member m WHERE m.uuid = :uuid")
Optional<Member> findByUuidWithLock(@Param("uuid") UUID uuid);
```

<br> 

### 2) 좌석 예약 요청 
**(1) 비관적 락(Pessimistic Lock) <br>**

```
public Seat getSeatByConcertScheduleIdAndNumberWithPessimisticLock(long concertScheduleId, long number) throws Exception {
        return seatRepository.findByConcertScheduleIdAndNumberWithPessimisticLock(concertScheduleId, number)
                             .orElseThrow(Exception::new);
}
```
```
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.concertSchedule.id = :concertScheduleId AND s.number = :number")
Optional<Seat> findByConcertScheduleIdAndNumberWithPessimisticLock(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);
```

<br> 

**(2) 낙관적 락(Optimistic Lock) <br>**

```

@Getter
@Entity
@Table(name = "seat")
@NoArgsConstructor
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_schedule_id")
    private ConcertSchedule concertSchedule;
    private long number;
    private long price;

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    ...
}
```

```
public Seat getSeatByConcertScheduleIdAndNumberWithOptimisticLock(long concertScheduleId, long number) {
        return seatRepository.findByConcertScheduleIdAndNumberWithOptimisticLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
}
```

```
@Lock(LockModeType.OPTIMISTIC)
@Query("SELECT s FROM Seat s WHERE s.concertSchedule.id = :concertScheduleId AND s.number = :number")
Optional<Seat> findByConcertScheduleIdAndNumberWithOptimisticLock(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);
```

<br>

**(3) 레디스 분산 락(Redis Distributed Lock) <br>**

```
@DistributedLock(key = "#concertScheduleId + '_' + #number", waitTime = 500, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
public Seat getSeatByConcertScheduleIdAndNumberWithDistributedLock(String lockName, long concertScheduleId, long number) {
        return seatRepository.findByConcertScheduleIdAndNumberWithDistributedLock(concertScheduleId, number)
                             .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
}
```

```
@Query("SELECT s FROM Seat s WHERE s.concertSchedule.id = :concertScheduleId AND s.number = :number")
Optional<Seat> findByConcertScheduleIdAndNumberWithDistributedLock(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);
```

<br> 


### 3) 결제 요청 
**(1) 비관적 락(Pessimistic Lock) <br>**

```
@Transactional
public ReservationResponse createReservation(String token, UUID uuid, long concertScheduleId, long seatNumber) throws Exception {
        validateToken(token);
        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, concertScheduleId);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
        Seat seat = seatService.getSeatByConcertScheduleIdAndNumberWithLock(concertScheduleId, seatNumber);
        long price = getConcertSchedule(concertScheduleId).getPrice();

        reservationService.createReservation(concertSchedule, uuid, seat, price);
        paymentService.createPayment(concertSchedule, uuid, price);
        memberService.decreaseBalance(uuid, price);

        updateStatus(token, concertScheduleId, seatNumber);

        String name = getMember(uuid).getName();
        String concertName = getConcert(concertScheduleId).getName();
        LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

        return ReservationResponse.of(name, concertName, dateTime, price);
}
```
```
public void decreaseBalance(UUID uuid, long price) throws Exception {
        Member member = getMemberByUuidWithLock(uuid);
        member.updateBalance(member.getBalance()-price);
}
```
```
 public Member getMemberByUuidWithLock(UUID uuid) throws Exception {
        return memberRepository.findByUuidWithLock(uuid).orElseThrow(Exception::new);
}
```
```
@Lock(LockModeType.PESSIMISTIC_READ)
@Query("SELECT m from Member m WHERE m.uuid = :uuid")
Optional<Member> findByUuidWithLock(@Param("uuid") UUID uuid);
```

<br>

## 3. 동시성 테스트


### 2) 좌석 예약 요청 
**(1) 비관적 락(Pessimistic Lock) 동시성 테스트 <br>**

```
@Test
@DisplayName("비관적 락을 활용해 1000번의 좌석 예약 요청 중 1번만 성공한다")
void 비관적_락을_활용해_1000번의_좌석_예약_요청_중_1번만_성공한다() throws InterruptedException {

            int requestCount = 1000;
            ExecutorService executorService = Executors.newFixedThreadPool(50);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < requestCount; i++) {
                int finalI = i;

                executorService.submit(() -> {
                    try {
                        seatFacade.createSeatReservationWithPessimisticLock(savedMembers.get(finalI).getUuid(), savedConcertSchedule.getId(), savedSeat.getNumber());
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Total time taken for 1000 requests: " + duration + " ms");

            assertEquals(1, successCount.get());
}

```
![image](https://github.com/user-attachments/assets/d27c0a06-bc8a-44e2-895b-0c15e78b04be)


<br> 

**(2) 낙관적 락(Optimistic Lock) 동시성 테스트 <br>**

```
@Test
@DisplayName("낙관적 락을 활용해 1000번의 좌석 예약 요청 중 1번만 성공한다")
void 낙관적_락을_활용해_1000번의_좌석_예약_요청_중_1번만_성공한다() throws InterruptedException {

            int requestCount = 1000;
            ExecutorService executorService = Executors.newFixedThreadPool(50);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < requestCount; i++) {
                int finalI = i;

                executorService.submit(() -> {
                    try {
                        seatFacade.createSeatReservationWithOptimisticLock(savedMembers.get(finalI).getUuid(), savedConcertSchedule.getId(), savedSeat.getNumber());
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Total time taken for 1000 requests: " + duration + " ms");

            assertEquals(1, successCount.get());
}
```
![image](https://github.com/user-attachments/assets/7fb8b0f1-99bc-4167-9ce3-319a9fbc45ad)

<br> 

**(3) 레디스 분산 락(Redis Distributed Lock) 동시성 테스트 <br>**

```
@Test
@DisplayName("분산 락을 활용해 1000번의 좌석 예약 요청 중 1번만 성공한다")
void 분산_락을_활용해_1000번의_좌석_예약_요청_중_1번만_성공한다() throws InterruptedException {

            int requestCount = 1000;
            ExecutorService executorService = Executors.newFixedThreadPool(50);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < requestCount; i++) {
                int finalI = i;
                
                executorService.submit(() -> {
                    try {
                        seatFacade.createSeatReservationWithDistributedLock(savedMembers.get(finalI).getUuid(), savedConcertSchedule.getId(), savedSeat.getNumber());
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Total time taken for 1000 requests: " + duration + " ms");

            assertEquals(1, successCount.get());
        }
```
![image](https://github.com/user-attachments/assets/39c35e14-d257-4c5e-a09c-df7079f582ce)


<br> 

### 3) 결제 요청 
**(1) 비관적 락(Pessimistic Lock) 동시성 테스트 <br>**
```
@Test
@DisplayName("총 50번의 예약 요청 중 1번만 성공한다")
public void 총_50번의_예약_요청_중_1번만_성공한다() throws InterruptedException {
            int requestCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> {
                    try {
                        reservationFacade.createReservation(token, memberUuid, savedConcertSchedule.getId(), savedSeat.getNumber());
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Total time taken for 50 requests: " + duration + " ms");

            assertEquals(1, successCount.get());
        }
```


