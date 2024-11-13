

# '콘서트 좌석 5분간 선점 예약' 동시성 제어 보고서 

## 개요

이 보고서는 크게 6가지 파트로 구성됩니다.
  
**1) 락 도입 이유** <br>
**2) 락 도입 과정** <br>
**3) 3가지 락 구현** <br>
**4) 3가지 락 동시성 통합 테스트 구현** <br>
**5) 결론** <br> 
**6) 앞으로 고민할 포인트** <br> 


### 1) 락 도입 이유 
![image](https://github.com/user-attachments/assets/bba5abe8-9d67-4930-94fa-b2e30b8519d4)

**(1) 문제 상황**
- 콘서트 예약 서비스에서 사용자는 **콘서트 선택 → 콘서트 스케줄 선택 → 좌석 선택 → 좌석 결제** 순으로 예약을 진행합니다. <br>
  하지만 두 명 이상의 사용자가 동시에 동일한 좌석을 선택하여 예약을 진행하는 경우, <br>
  **좌석 선택 후 결제 전**에 다른 사용자가 먼저 결제함으로써 예약이 불가능한 상황이 발생할 수 있습니다. <br>
  이로 인해 사용자 경험에 부정적인 영향을 미치게 됩니다. <br>

**(2) 해결 방안**
- 이 문제를 해결하기 위해, **사용자가 특정 좌석을 선택하면 해당 좌석에 대해 5분 간의 선점**을 부여하는 방식으로 <br>
  사용자 경험을 개선하고자 했습니다. <br> 
  하지만 이 과정에서도 **여러 명의 사용자가 동시에 좌석 선점을 시도**하는 상황이 발생할 수 있기 때문에, <br>
  **좌석 선점 과정에 락을 도입하여 동시성을 제어**하는 것이 필요하다고 판단했습니다. <br> 
  

### 2) 락 도입 과정 

- 해당 서비스가 분산 환경에서 제공되는 점을 고려하여, <br>
  동시성 제어를 위해 **비관적 락, 낙관적 락, 레디스 분산 락** 이라는 세 가지 방법을 검토했습니다. <br> 
  각각의 장단점은 아래와 같습니다.


| 락 종류             | 장점                                                         | 단점                                                         |
|---------------------|--------------------------------------------------------------|--------------------------------------------------------------|
| **비관적 락 (Pessimistic Lock)**  | - 충돌을 방지하기 위해 항상 락을 사용. <br> - 데이터 무결성을 보장. | - 성능 저하 (락 대기 시간 증가). <br> - 동시성 낮음. <br> |
| **낙관적 락 (Optimistic Lock)**   | - 충돌이 적을 때 성능이 뛰어남. <br> - 락을 최소화하여 높은 동시성 제공. | - 충돌 발생 시 재시도 비용 발생. <br> - 충돌이 잦으면 성능 저하. <br> - 재시도 로직 구현 필요. |
| **레디스 분산 락 (Redis Distributed Lock)** | - 분산 환경에서 락을 관리할 수 있음. <br> - 빠르고 가벼운 락 제공. <br> - 여러 서버 간 동기화 용이. |  - 네트워크 지연 및 장애 발생 시 문제. <br> - 락 관리의 복잡성 |


- 저는 3가지 락을 비교하기 위해, 3가지 락을 모두 구현한 후 **동시성 통합 테스트를 진행**하기로 결정했습니다.


<br> 


### 3) 3가지 락 구현

**(1) 비관적 락(Pessimistic Lock) <br>**

- 좌석 선점 예약 시, 우선적으로 **DB에서 해당 좌석이 이미 선점되었는지 조회**해야 합니다. <br>
  이 때 **비관적 락**을 사용하여 DB 조회 시 다른 사용자의 접근을 차단합니다. <br>
  
- 비관적 락은 좌석 선점 예약이 **JPA의 Dirty-Checking**에 의해 업데이트되고, <br>
  그 결과가 DB에 커밋되면서 락이 해제됩니다.

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

- 좌석 선점 예약 시, **DB에서 해당 좌석이 선점되었는지 조회**해야 하는데, 이 때 **낙관적 락**을 적용하여 경합을 방지합니다. <br> 
- **낙관적 락은 좌석 엔티티에 Version 필드를 추가하여 관리**됩니다.<br> 
- **여러 스레드가 경합하는 상황**에서, 한 스레드가 해당 엔티티의 버전 정보를 변경하면, **다른 스레드는 해당 엔티티를 업데이트할 수 없습니다**. <br> 

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

**(3) 레디스 분산 락 (Redis Distributed Lock)**

- 좌석 선점 예약 시, **concertScheduleId와 좌석 번호를 결합한 정보를 키(Key)로 Redis 분산 락을 생성**하였습니다. <br> 
- 즉, **한 스레드가 Redis 분산 락을 획득**하면, 다른 스레드는 락이 해제될 때까지 **해당 자원에 접근할 수 없습니다**. <br> 
- Redis 분산 락 구현 시, **코드의 유지보수성을 고려하여 AOP를 사용해 구현**하였습니다. <br>  

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

### 4) 3가지 락 동시성 테스트 구현

- 테스트 시나리오는 **1000명의 사용자가 동시에 좌석 선점 예약을 시도할 때, 오직 1명만 성공**하는 방식으로 구성하였습니다.

<br> 

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


### 5) 결론 <br> 

#### 1. 구현의 복잡도
- **비관적 락**과 **낙관적 락**은 JPA에서 제공하는 어노테이션을 사용해 비교적 쉽게 구현할 수 있습니다. <br> 
- 반면, **Redis 분산 락**은 **Redisson 라이브러리**를 사용해야 하며, <br>
  waitTime(대기 시간)과 leaseTime(락 유지 시간) 등을 설정해야 하므로, 비즈니스 요구사항에 맞춰 정책을 정의해야 합니다. <br>
- 또한, **Redis 분산 락**은 **AOP**를 사용하여 구현되었기 때문에, 구현의 복잡도가 상대적으로 높습니다.
  
#### 2. 성능
- **1000명**을 기준으로 성능을 비교한 결과, <br> 
  **낙관적 락 ≥ 비관적 락 >>> Redis 분산 락** <br>
- **낙관적 락**은 락 해제를 기다리지 않아도 되기 때문에 성능이 약간 더 우수한 결과를 보였습니다. <br> 
  **Redis 분산 락**은 waitTime을 60ms로 설정했을 때, 낙관적 락과 비관적 락의 **2배 이상의 시간**이 소요되었습니다. <br> 
  waitTime을 늘리면 테스트 시간도 더 길어졌고, <br>
  적절한 waitTime과 leaseTime을 설정하는 데 대한 고민이 필요했습니다. <br> 


### 6) 앞으로 고민할 포인트
**(1) Redis 분산 락**

**(1-1) Redis 분산 락의 경쟁 우위** <br> 
- 비관적 락과 낙관적 락과 비교할 때, 테스트 결과만으로는 Redis 분산락이 특별한 이점을 가지지 않는 것으로 보입니다. <br>
  어떤 시나리오에서 Redis 분산 락이 경쟁 우위를 가질지에 대한 이해가 필요합니다. <br>
  
**(1-2) Redis 분산락의 waitTime, leaseTime 설정** <br> 
- 실무에서 Redis 분산락을 적용할 때, waitTime과 leaseTime을 어떻게 설정해야 할지에 대한 고민이 필요합니다. <br>
  
**(1-3) Redis Key 관리 정책** <br> 
- Redis 분산락은 특정 key 형태로 생성됩니다. <br> 
  Redis에는 분산락 외에도 다양한 캐시 key가 존재하므로, 이들에 대한 관리 정책에 대한 고민이 필요합니다. <br>

**(2) 트랜잭션** <br> 

**(2-1) 트랜잭션 범위 조정 문제** <br> 
- 트랜잭션 범위를 조정하기 위한 여러 시도(@Transactional 위치 변경, 자식 트랜잭션 생성 등)를 했지만,<br>
  테스트가 제대로 되지 않아서 철회하였습니다. <br>
  해당 케이스에 대한 대한 조사가 필요합니다. <br>

**(3) 테스트**

**(3-1) 예약과 결제 테스트 분리 여부** <br> 
- 예약 하위에서 결제가 발생하는데, 이를 별도로 테스트해야 할지 고민이 필요합니다. <br>

**(3-2) 데이터베이스 클렌징 코드 적용**<br>
- 제안해주신 데이터베이스 클렌징 코드를 적용해본 후 결과를 점검해야 합니다. <br> 






