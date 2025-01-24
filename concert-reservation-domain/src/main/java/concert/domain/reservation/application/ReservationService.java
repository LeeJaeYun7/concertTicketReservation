package concert.domain.reservation.application;

import concert.domain.concert.application.ConcertService;
import concert.domain.concert.domain.Concert;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.member.application.MemberService;
import concert.domain.member.domain.Member;
import concert.domain.reservation.domain.Reservation;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.reservation.domain.vo.PaymentConfirmedVO;
import concert.domain.reservation.domain.vo.ReservationVO;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

  private final MemberService memberService;
  private final ConcertService concertService;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final ConcertScheduleService concertScheduleService;
  private final ReservationRepository reservationRepository;

  public Reservation createReservation(long concertId, long concertScheduleId, String uuid, long concertScheduleSeatId, long price) {
    reservationRepository.findReservation(concertScheduleId, concertScheduleSeatId);
    Reservation reservation = Reservation.of(concertId, concertScheduleId, uuid, concertScheduleSeatId, price);
    return reservationRepository.save(reservation);
  }

  @Transactional
  public ReservationVO handlePaymentConfirmed(PaymentConfirmedVO vo) {

    long concertScheduleId = vo.getConcertScheduleId();
    String uuid = vo.getUuid();
    long seatNumber = vo.getSeatNumber();
    long price = vo.getPrice();

    ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
    long concertId = concertSchedule.getConcertId();

    ConcertScheduleSeat concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, seatNumber);

    memberService.decreaseBalance(uuid, price);
    updateStatus(concertScheduleId, seatNumber);

    createReservation(concertId, concertScheduleId, uuid, concertScheduleSeat.getId(), price);

    String name = getMember(uuid).getName();
    String concertName = getConcert(concertScheduleId).getName();
    LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

    return ReservationVO.of(name, concertName, dateTime, price);
  }

  private Concert getConcert(long concertScheduleId) {
    ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
    return concertService.getConcertById(concertSchedule.getConcertId());
  }

  private Member getMember(String uuid) {
    return memberService.getMemberByUuid(uuid);
  }


  private ConcertSchedule getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private void updateStatus(long concertScheduleId, long seatNumber) {
    concertScheduleSeatService.updateSeatStatus(concertScheduleId, seatNumber, SeatStatus.RESERVED);
  }
}
