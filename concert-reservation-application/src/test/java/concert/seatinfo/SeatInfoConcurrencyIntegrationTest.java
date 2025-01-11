package concert.seatinfo;

import concert.application.seatinfo.application.facade.SeatInfoFacade;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.member.application.MemberService;
import concert.domain.member.domain.Member;
import concert.domain.member.domain.MemberRepository;
import concert.domain.seat.domain.Seat;
import concert.domain.seat.domain.SeatRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Disabled
public class SeatInfoConcurrencyIntegrationTest {

  // Logger 선언
  private static final Logger log = LoggerFactory.getLogger(SeatInfoConcurrencyIntegrationTest.class);
  @Autowired
  ConcertRepository concertRepository;
  @Autowired
  ConcertHallRepository concertHallRepository;
  @Autowired
  ConcertScheduleRepository concertScheduleRepository;
  @Autowired
  private SeatInfoFacade seatInfoFacade;
  @Autowired
  private MemberService memberService;
  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private SeatRepository seatRepository;

  private String token;
  private List<Member> savedMembers;
  private List<String> memberUuids;

  private Concert savedConcert;

  private ConcertHall savedConcertHall;

  private ConcertSchedule savedConcertSchedule;

  private Seat savedSeat;

  @BeforeEach
  void setUp() {

    savedMembers = new ArrayList<>();
    memberUuids = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      Member member = Member.of("Member" + i);
      member.updateBalance(100000);
      savedMembers.add(memberRepository.save(member));
      memberUuids.add(savedMembers.get(i).getUuid());
    }

    LocalDate startAt = LocalDate.of(2024, 10, 16);
    LocalDate endAt = LocalDate.of(2024, 10, 18);


    ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
    savedConcertHall = concertHallRepository.save(concertHall);
    Concert concert = Concert.of("박효신 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

    savedConcert = concertRepository.save(concert);

    LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
    ConcertSchedule concertSchedule = ConcertSchedule.of(savedConcert, dateTime);
    savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

    Seat seat = Seat.of(savedConcertHall, 1);
    seat.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
    savedSeat = seatRepository.save(seat);
  }

  @AfterEach
  void databaseCleansing() {
    seatRepository.deleteById(savedSeat.getId());
    concertScheduleRepository.deleteById(savedConcertSchedule.getId());
    concertRepository.deleteById(savedConcert.getId());

    for (int i = 0; i < 1000; i++) {
      memberRepository.deleteById(savedMembers.get(i).getId());
    }
  }

  @Nested
  @DisplayName("1000명의 멤버가 같은 예약 요청을 한 번씩 보낼 때")
  class 천명의_멤버가_같은_좌석_예약_요청을_한_번씩_보낼때 {
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
            seatInfoFacade.createSeatInfoReservationWithPessimisticLock(savedMembers.get(finalI).getUuid(), savedConcertSchedule.getId(), savedSeat.getNumber());
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
            seatInfoFacade.createSeatInfoReservationWithOptimisticLock(savedMembers.get(finalI).getUuid(), savedConcertSchedule.getId(), savedSeat.getNumber());
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
            seatInfoFacade.createSeatInfoReservationWithDistributedLock(savedMembers.get(finalI).getUuid(), savedConcertSchedule.getId(), savedSeat.getNumber());
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
  }
}

