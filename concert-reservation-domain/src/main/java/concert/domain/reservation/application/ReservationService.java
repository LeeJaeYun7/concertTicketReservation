package concert.domain.reservation.application;

import concert.domain.concert.application.ConcertService;
import concert.domain.concert.domain.Concert;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.member.application.MemberService;
import concert.domain.member.domain.Member;
import concert.domain.reservation.domain.Reservation;
import concert.domain.reservation.domain.ReservationRepository;
import concert.domain.reservation.domain.vo.PaymentConfirmedVO;
import concert.domain.reservation.domain.vo.ReservationVO;
import concert.domain.seatinfo.application.SeatInfoService;
import concert.domain.seatinfo.domain.SeatInfo;
import concert.domain.seatinfo.domain.enums.SeatStatus;
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
  private final SeatInfoService seatInfoService;
  private final ConcertScheduleService concertScheduleService;
  private final ReservationRepository reservationRepository;

  public Reservation createReservation(ConcertSchedule concertSchedule, String uuid, SeatInfo seatInfo, long price) {
    reservationRepository.findReservation(concertSchedule.getId(), seatInfo.getId());
    Reservation reservation = Reservation.of(concertSchedule, uuid, seatInfo, price);
    return reservationRepository.save(reservation);
  }

  @Transactional
  public ReservationVO handlePaymentConfirmed(PaymentConfirmedVO vo) {

    long concertScheduleId = vo.getConcertScheduleId();
    String uuid = vo.getUuid();
    long seatNumber = vo.getSeatNumber();
    long price = vo.getPrice();

    ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
    SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);

    memberService.decreaseBalance(uuid, price);
    updateStatus(concertScheduleId, seatNumber);

    createReservation(concertSchedule, uuid, seatInfo, price);

    String name = getMember(uuid).getName();
    String concertName = getConcert(concertScheduleId).getName();
    LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

    return ReservationVO.of(name, concertName, dateTime, price);
  }

  private Concert getConcert(long concertScheduleId) {
    ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
    return concertService.getConcertById(concertSchedule.getConcert().getId());
  }


  private Member getMember(String uuid) {
    return memberService.getMemberByUuid(uuid);
  }


  private ConcertSchedule getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private void updateStatus(long concertScheduleId, long seatNumber) {
    seatInfoService.updateSeatStatus(concertScheduleId, seatNumber, SeatStatus.RESERVED);
  }
}
