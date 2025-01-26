package concert.concertschedule;


import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concerthallseat.domain.ConcertHallSeatRepository;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.SeatGradeRepository;
import concert.domain.seatgrade.domain.enums.Grade;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeatRepository;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
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
  private ConcertHallSeatRepository concertHallSeatRepository;
  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private ConcertRepository concertRepository;
  @Autowired
  private ConcertHallRepository concertHallRepository;
  @Autowired
  private ConcertScheduleRepository concertScheduleRepository;
  @Autowired
  private ConcertScheduleSeatRepository concertScheduleSeatRepository;
  @Autowired
  private SeatGradeRepository seatGradeRepository;

  @BeforeEach
  void setUp() {

    // 첫번째 테스트

    startAt = LocalDate.of(2024, 12, 1);
    endAt = LocalDate.of(2024, 12, 31);
    concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
    savedConcertHall = concertHallRepository.save(concertHall);

    firstConcert = Concert.of("김연우 콘서트", savedConcertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);
    secondConcert = Concert.of("박효신 콘서트", savedConcertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

    firstDateTime = LocalDateTime.of(2024, 12, 11, 22, 30);
    secondDateTime = LocalDateTime.of(2024, 12, 12, 22, 30);

    savedFirstConcert = concertRepository.save(firstConcert);
    firstConcertSchedule = ConcertSchedule.of(savedFirstConcert.getId(), firstDateTime);
    secondConcertSchedule = ConcertSchedule.of(savedFirstConcert.getId(), secondDateTime);

    savedFirstConcertSchedule = concertScheduleRepository.save(firstConcertSchedule);
    savedSecondConcertSchedule = concertScheduleRepository.save(secondConcertSchedule);

    ConcertHallSeat seat11 = ConcertHallSeat.of(savedConcertHall.getId(), 11); // 예시로 Seat 객체를 생성
    ConcertHallSeat seat12 = ConcertHallSeat.of(savedConcertHall.getId(), 12);
    ConcertHallSeat savedSeat11 = concertHallSeatRepository.save(seat11);
    ConcertHallSeat savedSeat12 = concertHallSeatRepository.save(seat12);

    SeatGrade allSeatGrade = SeatGrade.of(savedFirstConcert.getId(), Grade.ALL, 100000); // 예시로 SeatGrade 생성
    SeatGrade savedALLSeatGrade = seatGradeRepository.save(allSeatGrade);

    LocalDateTime now = timeProvider.now();
    LocalDateTime threshold = now.minusMinutes(10);

    ConcertScheduleSeat firstScheduleConcertScheduleSeat1 = ConcertScheduleSeat.of(savedSeat11.getId(), savedFirstConcertSchedule.getId(), savedALLSeatGrade.getId(), SeatStatus.AVAILABLE);
    ConcertScheduleSeat firstScheduleConcertScheduleSeat2 = ConcertScheduleSeat.of(savedSeat12.getId(), savedFirstConcertSchedule.getId(), savedALLSeatGrade.getId(), SeatStatus.AVAILABLE);
    firstScheduleConcertScheduleSeat1.changeUpdatedAt(threshold);
    firstScheduleConcertScheduleSeat2.changeUpdatedAt(threshold);

    ConcertScheduleSeat secondScheduleSeatInfo1 = ConcertScheduleSeat.of(savedSeat11.getId(), savedSecondConcertSchedule.getId(), savedALLSeatGrade.getId(), SeatStatus.AVAILABLE);
    ConcertScheduleSeat secondScheduleSeatInfo2 = ConcertScheduleSeat.of(savedSeat12.getId(), savedSecondConcertSchedule.getId(), savedALLSeatGrade.getId(), SeatStatus.AVAILABLE);
    secondScheduleSeatInfo1.changeUpdatedAt(threshold);
    secondScheduleSeatInfo2.changeUpdatedAt(threshold);

    concertScheduleSeatRepository.save(firstScheduleConcertScheduleSeat1);
    concertScheduleSeatRepository.save(firstScheduleConcertScheduleSeat2);
    concertScheduleSeatRepository.save(secondScheduleSeatInfo1);
    concertScheduleSeatRepository.save(secondScheduleSeatInfo2);

    // 두번째 테스트

    savedSecondConcert = concertRepository.save(secondConcert);
    thirdConcertSchedule = ConcertSchedule.of(savedSecondConcert.getId(), firstDateTime);
    fourthConcertSchedule = ConcertSchedule.of(savedSecondConcert.getId(), secondDateTime);

    ConcertSchedule savedThirdConcertSchedule = concertScheduleRepository.save(thirdConcertSchedule);
    ConcertSchedule savedFourthConcertSchedule = concertScheduleRepository.save(fourthConcertSchedule);

    ConcertHallSeat seat21 = ConcertHallSeat.of(savedConcertHall.getId(), 21); // 예시로 Seat 객체를 생성
    ConcertHallSeat seat22 = ConcertHallSeat.of(savedConcertHall.getId(), 22);
    ConcertHallSeat savedSeat21 = concertHallSeatRepository.save(seat21);
    ConcertHallSeat savedSeat22 = concertHallSeatRepository.save(seat22);

    SeatGrade RSeatGrade = SeatGrade.of(savedSecondConcert.getId(), Grade.ALL, 100000); // 예시로 SeatGrade 생성
    SeatGrade savedRSeatGrade = seatGradeRepository.save(RSeatGrade);

    ConcertScheduleSeat thirdConcertScheduleSeat1 = ConcertScheduleSeat.of(savedSeat21.getId(), savedThirdConcertSchedule.getId(), savedALLSeatGrade.getId(), SeatStatus.RESERVED);
    ConcertScheduleSeat thirdConcertScheduleSeat2 = ConcertScheduleSeat.of(savedSeat22.getId(), savedThirdConcertSchedule.getId(), savedALLSeatGrade.getId(), SeatStatus.RESERVED);
    thirdConcertScheduleSeat1.changeUpdatedAt(threshold);
    thirdConcertScheduleSeat2.changeUpdatedAt(threshold);

    ConcertScheduleSeat fourthConcertScheduleSeat1 = ConcertScheduleSeat.of(savedSeat21.getId(), savedFourthConcertSchedule.getId(), savedRSeatGrade.getId(), SeatStatus.AVAILABLE);
    ConcertScheduleSeat fourthConcertScheduleSeat2 = ConcertScheduleSeat.of(savedSeat22.getId(), savedFourthConcertSchedule.getId(), savedRSeatGrade.getId(), SeatStatus.AVAILABLE);
    fourthConcertScheduleSeat1.changeUpdatedAt(threshold);
    fourthConcertScheduleSeat2.changeUpdatedAt(threshold);

    concertScheduleSeatRepository.save(thirdConcertScheduleSeat1);
    concertScheduleSeatRepository.save(thirdConcertScheduleSeat2);
    concertScheduleSeatRepository.save(fourthConcertScheduleSeat1);
    concertScheduleSeatRepository.save(fourthConcertScheduleSeat2);
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
