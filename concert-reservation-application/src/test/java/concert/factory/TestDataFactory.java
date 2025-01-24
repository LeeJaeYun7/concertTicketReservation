package concert.factory;

import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concerthallseat.domain.ConcertHallSeatRepository;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import concert.domain.member.domain.Member;
import concert.domain.member.domain.MemberRepository;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.SeatGradeRepository;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeatRepository;
import concert.domain.seatgrade.domain.enums.Grade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Component
public class TestDataFactory {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ConcertRepository concertRepository;

  @Autowired
  private ConcertHallRepository concertHallRepository;
  @Autowired
  private ConcertScheduleRepository concertScheduleRepository;
  @Autowired
  private ConcertHallSeatRepository concertHallSeatRepository;

  @Autowired
  private SeatGradeRepository seatGradeRepository;

  @Autowired
  private ConcertScheduleSeatRepository concertScheduleSeatRepository;

  @Autowired
  private ReservationRepository reservationRepository;


  public Member createMember() {
    Member member = Member.of("Tom Cruise");
    member.updateBalance(100000);
    return memberRepository.save(member);
  }

  public ConcertHall createConcertHall() {
    ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
    return concertHallRepository.save(concertHall);
  }

  public Concert createConcert(ConcertHall concertHall) {
    LocalDate startAt = LocalDate.of(2024, 11, 25);
    LocalDate endAt = LocalDate.of(2024, 11, 28);
    Concert concert = Concert.of("브루노마스 콘서트", concertHall.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);
    return concertRepository.save(concert);
  }

  public ConcertSchedule createConcertSchedule(Concert concert) {
    LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
    return concertScheduleRepository.save(ConcertSchedule.of(concert.getId(), dateTime));
  }

  public ConcertHallSeat createSeat(ConcertHall concertHall) {
    ConcertHallSeat seat = ConcertHallSeat.of(concertHall.getId(), 1);
    return concertHallSeatRepository.save(seat);
  }

  public SeatGrade createSeatGrade(Concert concert) {
    SeatGrade allSeatGrade = SeatGrade.of(concert.getId(), Grade.ALL, 100000);
    return seatGradeRepository.save(allSeatGrade);
  }

  public ConcertScheduleSeat createConcertScheduleSeat(ConcertHallSeat seat, ConcertSchedule concertSchedule, SeatGrade seatGrade) {
    ConcertScheduleSeat allConcertScheduleSeat = ConcertScheduleSeat.of(seat.getId(), concertSchedule.getId(), seatGrade.getId(), SeatStatus.AVAILABLE);
    return concertScheduleSeatRepository.save(allConcertScheduleSeat);
  }
}
