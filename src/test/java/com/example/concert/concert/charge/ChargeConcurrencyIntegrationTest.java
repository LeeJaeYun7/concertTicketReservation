package com.example.concert.concert.charge;

import com.example.concert.charge.service.ChargeFacade;
import com.example.concert.member.domain.Member;
import com.example.concert.member.repository.MemberRepository;
import com.example.concert.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Disabled
public class ChargeConcurrencyIntegrationTest {

    // Logger 선언
    private static final Logger log = LoggerFactory.getLogger(ChargeConcurrencyIntegrationTest.class);

    @Autowired
    private ChargeFacade chargeFacade;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private String memberUuid;

    @BeforeEach
    void setUp() {
        Member member = Member.of("Tom Cruise");
        Member savedMember = memberRepository.save(member);
        memberUuid = savedMember.getUuid();
    }

    @Nested
    @DisplayName("멤버가 같은 충전 요청을 여러 번 보낼 때")
    class 멤버가_같은_충전_요청을_여러_번_보낼때 {
        @Test
        @DisplayName("총 50번의 충전 요청 중 1번만 멤버 잔액에 반영된다")
        void 총_50번의_충전_요청_중_1번만_멤버_잔액에_반영된다() throws InterruptedException {
            int requestCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

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

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Total time taken for 50 requests: " + duration + " ms");

            Member updatedMember = memberRepository.findByUuid(memberUuid).orElseThrow();

            assertEquals(1, successCount.get());
            assertEquals(10000, updatedMember.getBalance());
        }
    }
}

