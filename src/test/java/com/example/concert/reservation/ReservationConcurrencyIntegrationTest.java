package com.example.concert.reservation;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.concert.repository.ConcertRepository;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.repository.ConcertScheduleRepository;
import com.example.concert.member.domain.Member;
import com.example.concert.member.repository.MemberRepository;
import com.example.concert.member.service.MemberService;
import com.example.concert.reservation.repository.ReservationRepository;
import com.example.concert.reservation.service.ReservationFacade;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatGrade;
import com.example.concert.seat.enums.SeatStatus;
import com.example.concert.seat.repository.SeatRepository;
import com.example.concert.utils.RandomStringGenerator;
import com.example.concert.waitingQueue.domain.WaitingQueue;
import com.example.concert.waitingQueue.domain.WaitingQueueStatus;
import com.example.concert.waitingQueue.repository.WaitingQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
public class ReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private MemberService memberService;

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    WaitingQueueRepository waitingQueueRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    private String token;
    private String memberUuid;

    private Member savedMember;

    private Concert savedConcert;
    private ConcertSchedule savedConcertSchedule;

    private Seat savedSeat;

    private WaitingQueue savedWaitingQueue;
    @BeforeEach
    void setUp() {
        Member member = Member.of("Tom Cruise");
        member.updateBalance(100000);
        savedMember = memberRepository.save(member);
        token = RandomStringGenerator.generateRandomString(16);
        memberUuid = savedMember.getUuid();

        LocalDate startAt = LocalDate.of(2024, 11, 25);
        LocalDate endAt = LocalDate.of(2024, 11, 28);
        Concert concert = Concert.of("김연우 콘서트", "ballad", "장충체육관", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

        savedConcert = concertRepository.save(concert);

        LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
        ConcertSchedule concertSchedule = ConcertSchedule.of(savedConcert, dateTime, 50000);
        savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

        Seat seat = Seat.of(savedConcertSchedule, 1, 50000, SeatGrade.ALL);
        seat.setUpdatedAt(LocalDateTime.now());
        savedSeat = seatRepository.save(seat);

        WaitingQueue waitingQueue = WaitingQueue.of(savedConcert, memberUuid, token, 0);
        waitingQueue.updateWaitingQueueStatus(WaitingQueueStatus.ACTIVE);
        savedWaitingQueue = waitingQueueRepository.save(waitingQueue);
    }

    @Nested
    @DisplayName("멤버가 같은 예약 요청을 여러 번 보낼 때")
    class 멤버가_같은_예약_요청을_여러_번_보낼때 {
        @Test
        @DisplayName("비관적 락을 활용하면 총 50번의 예약 요청 중 1번만 성공한다")
        public void 비관적_락을_활용하면_총_50번의_예약_요청_중_1번만_성공한다() throws InterruptedException {
            int requestCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> {
                    try {
                        reservationFacade.createReservationWithPessimisticLock(token, memberUuid, savedConcertSchedule.getId(), savedSeat.getNumber());
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

        @Test
        @DisplayName("낙관적 락을 활용하면 총 50번의 예약 요청 중 1번만 성공한다")
        public void 낙관적_락을_활용하면_총_50번의_예약_요청_중_1번만_성공한다() throws InterruptedException {
            int requestCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> {
                    try {
                        reservationFacade.createReservationWithOptimisticLock(token, memberUuid, savedConcertSchedule.getId(), savedSeat.getNumber());
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
    }
}

