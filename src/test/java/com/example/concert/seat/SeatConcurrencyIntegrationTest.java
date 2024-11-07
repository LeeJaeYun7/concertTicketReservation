package com.example.concert.seat;

import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.seat.enums.SeatGrade;
import com.example.concert.seat.service.SeatFacade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.member.domain.Member;
import com.example.concert.member.repository.MemberRepository;
import com.example.concert.member.service.MemberService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.repository.SeatRepository;

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
@Slf4j
public class SeatConcurrencyIntegrationTest {
    @Autowired
    private SeatFacade seatFacade;

    @Autowired
    private MemberService memberService;

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SeatRepository seatRepository;

    private String token;
    private List<Member> savedMembers;
    private List<String> memberUuids;

    private Concert savedConcert;

    private ConcertSchedule savedConcertSchedule;

    private Seat savedSeat;

    @BeforeEach
    void setUp() {

        savedMembers = new ArrayList<>();
        memberUuids = new ArrayList<>();

        for(int i = 0; i < 1000; i++){
            Member member = Member.of("Member" + i);
            member.updateBalance(100000);
            savedMembers.add(memberRepository.save(member));
            memberUuids.add(savedMembers.get(i).getUuid());
        }

        LocalDate startAt = LocalDate.of(2024, 10, 16);
        LocalDate endAt = LocalDate.of(2024, 10, 18);

        Concert concert = Concert.of("박효신 콘서트", "ballad", "장충체육관", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

        savedConcert = concertRepository.save(concert);

        LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
        ConcertSchedule concertSchedule = ConcertSchedule.of(savedConcert, dateTime, 50000);
        savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

        Seat seat = Seat.of(savedConcertSchedule, 1, 50000, SeatGrade.ALL);
        seat.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
        savedSeat = seatRepository.save(seat);
    }

    @AfterEach
    void databaseCleansing() {
        seatRepository.deleteById(savedSeat.getId());
        concertScheduleRepository.deleteById(savedConcertSchedule.getId());
        concertRepository.deleteById(savedConcert.getId());

        for(int i = 0; i < 1000; i++){
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
    }
}

