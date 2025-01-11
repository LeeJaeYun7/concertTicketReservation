package concert.concertschedule;


import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.seat.domain.Seat;
import concert.domain.seat.domain.SeatRepository;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.SeatGradeRepository;
import concert.domain.seatgrade.domain.enums.Grade;
import concert.domain.seatinfo.domain.SeatInfo;
import concert.domain.seatinfo.domain.SeatInfoRepository;
import concert.domain.seatinfo.domain.enums.SeatStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Disabled
public class ConcertScheduleIntegrationTest {

  @Autowired
  TimeProvider timeProvider;
  LocalDate startAt;
  LocalDate endAt;
  ConcertHall concertHall;
  ConcertHall savedConcertHall;
  Concert firstConcert;
  Concert secondConcert;
  Concert savedFirstConcert;
  Concert savedSecondConcert;
  ConcertSchedule firstConcertSchedule;
  ConcertSchedule secondConcertSchedule;
  ConcertSchedule savedFirstConcertSchedule;
  ConcertSchedule savedSecondConcertSchedule;
  ConcertSchedule thirdConcertSchedule;
  ConcertSchedule fourthConcertSchedule;
  ConcertSchedule savedThirdConcertSchedule;
  ConcertSchedule savedFourthConcertSchedule;
  LocalDateTime firstDateTime;
  LocalDateTime secondDateTime;
  @Autowired
  private ConcertScheduleService sut;
  @Autowired
  private SeatRepository seatRepository;
  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private ConcertRepository concertRepository;
  @Autowired
  private ConcertHallRepository concertHallRepository;
  @Autowired
  private ConcertScheduleRepository concertScheduleRepository;
  @Autowired
  private SeatInfoRepository seatInfoRepository;
  @Autowired
  private SeatGradeRepository seatGradeRepository;

  @BeforeEach
  void setUp() {

    // 첫번째 테스트

    startAt = LocalDate.of(2024, 12, 1);
    endAt = LocalDate.of(2024, 12, 31);
    concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
    savedConcertHall = concertHallRepository.save(concertHall);

    firstConcert = Concert.of("김연우 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);
    secondConcert = Concert.of("박효신 콘서트", savedConcertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

    firstDateTime = LocalDateTime.of(2024, 12, 11, 22, 30);
    secondDateTime = LocalDateTime.of(2024, 12, 12, 22, 30);

    savedFirstConcert = concertRepository.save(firstConcert);
    firstConcertSchedule = ConcertSchedule.of(savedFirstConcert, firstDateTime);
    secondConcertSchedule = ConcertSchedule.of(savedFirstConcert, secondDateTime);

    savedFirstConcertSchedule = concertScheduleRepository.save(firstConcertSchedule);
    savedSecondConcertSchedule = concertScheduleRepository.save(secondConcertSchedule);

    Seat seat11 = Seat.of(savedConcertHall, 11); // 예시로 Seat 객체를 생성
    Seat seat12 = Seat.of(savedConcertHall, 12);
    Seat savedSeat11 = seatRepository.save(seat11);
    Seat savedSeat12 = seatRepository.save(seat12);

    SeatGrade vipSeatGrade = SeatGrade.of(savedFirstConcert, Grade.VIP, 100000); // 예시로 SeatGrade 생성
    SeatGrade savedVIPSeatGrade = seatGradeRepository.save(vipSeatGrade);

    LocalDateTime now = timeProvider.now();
    LocalDateTime threshold = now.minusMinutes(10);

    SeatInfo firstScheduleSeatInfo1 = SeatInfo.of(savedSeat11, savedFirstConcertSchedule, savedVIPSeatGrade, SeatStatus.AVAILABLE);
    SeatInfo firstScheduleSeatInfo2 = SeatInfo.of(savedSeat12, savedFirstConcertSchedule, savedVIPSeatGrade, SeatStatus.AVAILABLE);
    firstScheduleSeatInfo1.changeUpdatedAt(threshold);
    firstScheduleSeatInfo2.changeUpdatedAt(threshold);

    SeatInfo secondScheduleSeatInfo1 = SeatInfo.of(savedSeat11, savedSecondConcertSchedule, savedVIPSeatGrade, SeatStatus.AVAILABLE);
    SeatInfo secondScheduleSeatInfo2 = SeatInfo.of(savedSeat12, savedSecondConcertSchedule, savedVIPSeatGrade, SeatStatus.AVAILABLE);
    secondScheduleSeatInfo1.changeUpdatedAt(threshold);
    secondScheduleSeatInfo2.changeUpdatedAt(threshold);

    seatInfoRepository.save(firstScheduleSeatInfo1);
    seatInfoRepository.save(firstScheduleSeatInfo2);
    seatInfoRepository.save(secondScheduleSeatInfo1);
    seatInfoRepository.save(secondScheduleSeatInfo2);

    // 두번째 테스트

    savedSecondConcert = concertRepository.save(secondConcert);
    thirdConcertSchedule = ConcertSchedule.of(savedSecondConcert, firstDateTime);
    fourthConcertSchedule = ConcertSchedule.of(savedSecondConcert, secondDateTime);

    ConcertSchedule savedThirdConcertSchedule = concertScheduleRepository.save(thirdConcertSchedule);
    ConcertSchedule savedFourthConcertSchedule = concertScheduleRepository.save(fourthConcertSchedule);

    Seat seat21 = Seat.of(savedConcertHall, 21); // 예시로 Seat 객체를 생성
    Seat seat22 = Seat.of(savedConcertHall, 22);
    Seat savedSeat21 = seatRepository.save(seat21);
    Seat savedSeat22 = seatRepository.save(seat22);

    SeatGrade RSeatGrade = SeatGrade.of(savedSecondConcert, Grade.R, 100000); // 예시로 SeatGrade 생성
    SeatGrade savedRSeatGrade = seatGradeRepository.save(RSeatGrade);

    SeatInfo thirdScheduleSeatInfo1 = SeatInfo.of(savedSeat21, savedThirdConcertSchedule, savedRSeatGrade, SeatStatus.RESERVED);
    SeatInfo thirdScheduleSeatInfo2 = SeatInfo.of(savedSeat22, savedThirdConcertSchedule, savedRSeatGrade, SeatStatus.RESERVED);
    thirdScheduleSeatInfo1.changeUpdatedAt(threshold);
    thirdScheduleSeatInfo2.changeUpdatedAt(threshold);

    SeatInfo fourthScheduleSeatInfo1 = SeatInfo.of(savedSeat21, savedFourthConcertSchedule, savedRSeatGrade, SeatStatus.AVAILABLE);
    SeatInfo fourthScheduleSeatInfo2 = SeatInfo.of(savedSeat22, savedFourthConcertSchedule, savedRSeatGrade, SeatStatus.AVAILABLE);
    fourthScheduleSeatInfo1.changeUpdatedAt(threshold);
    fourthScheduleSeatInfo2.changeUpdatedAt(threshold);

    seatInfoRepository.save(thirdScheduleSeatInfo1);
    seatInfoRepository.save(thirdScheduleSeatInfo2);
    seatInfoRepository.save(fourthScheduleSeatInfo1);
    seatInfoRepository.save(fourthScheduleSeatInfo2);
  }


  @Nested
  @DisplayName("현재 이후의 모든 콘서트 스케줄을 가져올 때")
  class 현재_이후의_모든_콘서트_스케줄을_가져올때 {
    @Test
    @DisplayName("총 스케줄이_2개인 경우 2개를 가져온다.")
    void 총_스케줄이_2개인_경우_2개를_가져온다() {
      List<LocalDateTime> result = sut.getAllAvailableDateTimes(savedFirstConcert.getId());
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("총 스케줄이_2개인 경우 해당하는 1개를 가져온다.")
    void 총_스케줄이_2개인_경우_해당하는_1개를_가져온다() {
      List<LocalDateTime> result = sut.getAllAvailableDateTimes(savedSecondConcert.getId());
      assertEquals(1, result.size());
    }
  }
}
