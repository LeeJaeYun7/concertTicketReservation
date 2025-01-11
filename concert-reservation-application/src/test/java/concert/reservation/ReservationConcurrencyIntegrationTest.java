package concert.reservation;

/*
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
    ConcertHallRepository concertHallRepository;
    @Autowired
    ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatInfoRepository seatInfoRepository;


    @Autowired
    private SeatGradeRepository seatGradeRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    private String token;
    private String memberUuid;

    private Member savedMember;

    private Concert savedConcert;

    private ConcertHall savedConcertHall;
    private ConcertSchedule savedConcertSchedule;

    private Seat savedSeat;

    private SeatGrade savedSeatGrade;

    private SeatInfo savedSeatInfo;

    @BeforeEach
    void setUp() {
        Member member = Member.of("Tom Cruise");
        member.updateBalance(100000);
        savedMember = memberRepository.save(member);
        token = RandomStringGenerator.generateRandomString(16);
        memberUuid = savedMember.getUuid();

        LocalDate startAt = LocalDate.of(2024, 11, 25);
        LocalDate endAt = LocalDate.of(2024, 11, 28);

        ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
        savedConcertHall = concertHallRepository.save(concertHall);

        Concert concert = Concert.of("김연우 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

        savedConcert = concertRepository.save(concert);

        LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
        ConcertSchedule concertSchedule = ConcertSchedule.of(savedConcert, dateTime);
        savedConcertSchedule = concertScheduleRepository.save(concertSchedule);

        Seat seat = Seat.of(savedConcertHall, 1);
        seat.setUpdatedAt(LocalDateTime.now());
        savedSeat = seatRepository.save(seat);

        SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
        savedSeatGrade = seatGradeRepository.save(vipSeatGrade);
        SeatInfo vipSeatInfo = SeatInfo.of(savedSeat, concertSchedule, savedSeatGrade, SeatStatus.AVAILABLE);

        savedSeatInfo = seatInfoRepository.save(vipSeatInfo);
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
                        reservationFacade.createReservation(memberUuid, savedConcertSchedule.getId(), savedSeatInfo.getSeat().getNumber());
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

*/