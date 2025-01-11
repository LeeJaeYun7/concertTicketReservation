package concert.factory;

import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.member.domain.Member;
import concert.domain.member.domain.MemberRepository;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.seat.domain.Seat;
import concert.domain.seat.domain.SeatRepository;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatgrade.domain.SeatGradeRepository;
import concert.domain.seatinfo.domain.SeatInfo;
import concert.domain.seatinfo.domain.SeatInfoRepository;
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
  private SeatRepository seatRepository;

  @Autowired
  private SeatGradeRepository seatGradeRepository;

  @Autowired
  private SeatInfoRepository seatInfoRepository;

  @Autowired
  private ReservationRepository reservationRepository;


  public Member createMember() {
    Member member = Member.of("Tom Cruise");
    member.updateBalance(100000);
    return memberRepository.save(member);
  }

  public ConcertHall createConcertHall() {
    ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");
    return concertHallRepository.save(concertHall);
  }

  public Concert createConcert(ConcertHall concertHall) {
    LocalDate startAt = LocalDate.of(2024, 11, 25);
    LocalDate endAt = LocalDate.of(2024, 11, 28);
    Concert concert = Concert.of("브루노마스 콘서트", concertHall, "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);
    return concertRepository.save(concert);
  }

  public ConcertSchedule createConcertSchedule(Concert concert) {
    LocalDateTime dateTime = LocalDateTime.of(2024, 11, 25, 22, 30);
    return concertScheduleRepository.save(ConcertSchedule.of(concert, dateTime));
  }

  public Seat createSeat(ConcertHall concertHall) {
    Seat seat = Seat.of(concertHall, 1);
    return seatRepository.save(seat);
  }

  public SeatGrade createSeatGrade(Concert concert) {
    SeatGrade vipSeatGrade = SeatGrade.of(concert, Grade.VIP, 100000);
    return seatGradeRepository.save(vipSeatGrade);
  }

  public SeatInfo createSeatInfo(Seat seat, ConcertSchedule concertSchedule, SeatGrade seatGrade) {
    SeatInfo vipSeatInfo = SeatInfo.of(seat, concertSchedule, seatGrade, SeatStatus.AVAILABLE);
    return seatInfoRepository.save(vipSeatInfo);
  }
}
