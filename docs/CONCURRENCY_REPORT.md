

# '콘서트 좌석 5분간 선점 예약' 동시성 제어 보고서 

## 개요

이 보고서는 크게 5가지 파트로 구성됩니다.
  
**1) 락 도입 이유** <br>
**2) 락 도입 과정** <br>
**3) 락 구현** <br>
**4) 락 테스트 코드 구현** <br>
**5) 락 도입을 통해 개선된 점** <br> 


### 1) 락 도입 이유 
![image](https://github.com/user-attachments/assets/bba5abe8-9d67-4930-94fa-b2e30b8519d4)

- 콘서트 예약 서비스에서 사용자는 **콘서트 선택 - 콘서트 스케줄 선택 - 좌석 선택 - 좌석 결제**의 flow로 서비스를 이용합니다. <br>
  그런데 이 때, 2명 이상의 사용자가 동시에 특정 좌석을 선택해서 예약을 진행하는 경우, <br> 
  좌석 선택 후, 좌석을 결제하기 전에 다른 사용자가 먼저 좌석을 결제해서 <br>
  좌석 결제가 불가능한 상황이 발생하고, 이는 사용자 경험에 악영향을 미치게 됩니다. <br>

- 따라서, 특정 사용자가 좌석을 선택하면 해당 사용자에게 **'5분'의 좌석 선점*** 을 제공함으로써 <br>
  사용자 경험을 개선하고자 했습니다. <br> 

- 그런데 이 때, '좌석 선점'을 하는 과정에서 **여러 명의 사용자가 '동시에' 좌석 선점을 시도**하는 상황이 발생할 수 있습니다. <br>
  따라서 이런 상황을 대비해 좌석 선점에 락을 도입함으로써 동시성을 제어해야 한다고 판단했습니다. <br>
  

### 2) 락 도입 과정 

- 해당 서비스가 분산 환경에서 제공됨을 고려할 때, 동시성 제어를 위해 크게 3가지 락을 고려했습니다. <br> 
  그것은 **비관적 락 , 낙관적 락, 레디스 분산 락**입니다.<br> 
  각각의 장단점은 아래와 같습니다. 


| 락 종류             | 장점                                                         | 단점                                                         |
|---------------------|--------------------------------------------------------------|--------------------------------------------------------------|
| **비관적 락 (Pessimistic Lock)**  | - 충돌을 방지하기 위해 항상 락을 사용. <br> - 데이터 무결성을 보장. | - 성능 저하 (락 대기 시간 증가). <br> - 동시성 낮음. <br> - 데드락 발생 가능성. |
| **낙관적 락 (Optimistic Lock)**   | - 충돌이 적을 때 성능이 뛰어남. <br> - 락을 최소화하여 높은 동시성 제공. | - 충돌 발생 시 재시도 비용 발생. <br> - 충돌이 잦으면 성능 저하. <br> - 복잡한 구현 필요. |
| **레디스 분산 락 (Redis Distributed Lock)** | - 분산 환경에서 락을 관리할 수 있음. <br> - 빠르고 가벼운 락 제공. <br> - 여러 서버 간 동기화 용이. | - 락이 만료되면 다른 클라이언트가 락을 획득할 수 있음. <br> - 네트워크 지연 및 장애 발생 시 문제. <br> - 분산 시스템에서의 일관성 문제. |


### 2) 락 도입 과정 


### 2) 좌석 예약 요청 
**(1) 비관적 락(Pessimistic Lock) <br>**
- 좌석 선점 예약 시, 우선적으로 **DB에서 해당 좌석이 선점되었는지 조회가 필요한데 DB 조회시 비관적 락**을 걸어주었습니다. <br> 
-> 비관적 락은 좌석 선점 예약이 업데이트 될 때, **JPA의 Dirty-Checking에 의해 DB에 커밋되면서 해제**됩니다.   
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
- 좌석 선점 예약 시, 우선적으로 **DB에서 해당 좌석이 선점되었는지 조회가 필요한데 DB 조회시 낙관적 락**을 걸어주었습니다. <br> 
-> **낙관적 락은 좌석 Entity에 Version 필드를 추가해서 관리**됩니다. <br> 
-> **여러 스레드가 경합하는 상황에서**, 한 스레드에 의해 버전 정보가 변동되었다면, **다른 스레드는 정보 업데이트가 불가능**합니다. <br> 
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
- 좌석 선점 예약 시, **concertScheduleId와 좌석 번호를 결합한 정보를 Key로 Redis 분산 락을 생성**하였습니다. <br> 
-> 즉, **한 스레드가 Redis 분산 락을 획득 하면**, 다른 스레드는 그 락이 해제될 때까지 **해당 자원에 접근하지 못하게 됩니다**. <br>
-> Redis 분산 락 구현 시, **코드의 유지보수성을 고려해 AOP로 구현**하였습니다. <br>      
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


<br>

## 3. 동시성 테스트


### 1) 잔액 충전
**(1) 비관적 락(Pessimistic Lock) 동시성 테스트 <br>** 
```
@Test
@DisplayName("총 50번의 충전 요청 중 1번만 멤버 잔액에 반영된다")
void 총_50번의_충전_요청_중_1번만_멤버_잔액에_반영된다() throws InterruptedException {
            int requestCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> {
                    try {
                        chargeFacade.chargeBalance(memberUuid, 10000);
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            Member updatedMember = memberRepository.findByUuid(memberUuid).orElseThrow();

            assertEquals(1, successCount.get());
            assertEquals(10000, updatedMember.getBalance());
        }
```
![image](https://github.com/user-attachments/assets/f266cc2c-485d-4d8e-9eda-84799249bc49)

<br>

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
![image](https://github.com/user-attachments/assets/84d6042a-2787-4732-9214-d65e3fa4df0f)


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
![image](https://github.com/user-attachments/assets/e6f0a9d8-1f12-484c-adb7-7e330ea6d937)


## 3. 결론

### 1. 구현의 복잡도
- 비관적 락 == 낙관적 락 <<< Redis 분산락 <br> 
- 비관적 락과 낙관적 락은 JPA에서 제공하는 어노테이션으로 비교적 쉽게 구현이 가능하다 <br>
- 반면, Redis 분산락은 Redisson 라이브러리를 사용하는 경우, waitTime(대기 시간), leaseTime(락 유지 시간) 등을 지정해줘야 한다 <br>
-> 따라서, 내 비즈니스 요구사항에 맞춰서 해당 정책에 대한 기준을 수립해야 한다 <br>
-> 또한, Redis 분산락 같은 경우 AOP를 사용하여 구현하였기 때문에, 구현의 복잡도가 다소 높아졌다 <br>

### 2. 성능
- 좌석 예약 테스트(1000명) 기준으로 설명하면 <br> 
-> 낙관적 락 >= 비관적 락 >>> Redis 분산락 <br>
- 낙관적 락은 비관적 락과는 다르게 락이 해제되기를 기다리지 않아도 된다는 점 때문인지, 약간 성능이 우수하게 나왔다 <br>
- Redis 분산락 같은 경우는, waitTime 60ms로 설정했을 때, 낙관적 락, 비관적 락의 2배가 넘는 시간이 소요되었다. <br>
-> 그리고 waitTime을 늘릴수록 테스트 수행 시간도 더 길어졌다 <br>
-> 어느 정도의 waitTime, leaseTime을 설정하는 것이 적절한지에 대한 고민을 갖게 되었다 <br>


## 4. 앞으로 고민할 포인트
#### 1) Redis 분산락
(1) 비관적 락, 낙관적 락과 비교할 때 Redis 분산락이 어떤 이점이 있는가? <br>
-> 테스트 결과만을 봤을 때는, Redis 분산락이 갖는 경쟁우위가 없어 보인다. <br>

(2) Redis 분산락을 실무에서 적용할 때, waitTime, leaseTime 등에 대해서 어떻게 고민해야 하는가? <br>

(3) Redis 분산락이 Redis에서 잘 생성되었다가, 생성되지 않았다가 하는 문제가 반복되었다. <br>
-> 왜 이런 문제가 발생했는가? <br>
  
(4) Redis 분산락을 생성하면 key 형태로 Redis에 생성이 된다 <br> 
-> 실제로는 Redis 분산락 이외에 Redis 캐시 등 다양한 Key들이 존재할텐데, Key들은 어떤 정책으로 관리되는가? <br> 

#### 2) 트랜잭션 
(1) Redis 분산락 생성 시, 자식 트랜잭션을 독립적으로 생성해주었다 <br>
-> 이 부분에 대해서 명확하게 이해했는가? <br> 
  
(2) 트랜잭션 범위를 조정하기 위한 여러 시도들을 했는데(@Transactional 위치 변경, 자식 트랜잭션 생성 등) <br>
    테스트가 제대로 되지 않는 문제가 발생해서, 결론적으로 철회했다. <br> 
-> 왜 그런 문제들이 발생했는가? 왜 트랜잭션 범위를 조정하는 것이 왜 잘 되지 않았는가? <br>  


#### 3) 테스트 
(1) 예약 하위에서 결제가 발생하고 있는데, 이에 대한 테스트를 분리해야 하는가? 아니면 예약에 대한 테스트만으로 충분한가? <br> 

(2) 데이터베이스 클렌징에 대해 제안해주신 코드 적용해보기  






