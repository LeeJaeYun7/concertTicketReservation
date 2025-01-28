package concert.domain.reservation.txservices;

import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.entities.enums.ConcertScheduleSeatStatus;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertService;
import concert.domain.member.services.MemberService;
import concert.domain.member.entities.Member;
import concert.domain.reservation.entities.Reservation;
import concert.domain.reservation.entities.dao.ReservationRepository;
import concert.domain.reservation.command.PaymentConfirmedCommand;
import concert.domain.reservation.vo.ReservationVO;
import concert.domain.concert.services.ConcertScheduleSeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationTxService {

  private final MemberService memberService;
  private final ConcertService concertService;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final ConcertScheduleService concertScheduleService;
  private final ReservationRepository reservationRepository;

  @Transactional
  public Reservation createReservation(long concertId, long concertScheduleId, String uuid, long concertScheduleSeatId, long price) {
    reservationRepository.findReservation(concertScheduleId, concertScheduleSeatId);
    Reservation reservation = Reservation.of(concertId, concertScheduleId, uuid, concertScheduleSeatId, price);
    return reservationRepository.save(reservation);
  }

  @Transactional
  public ReservationVO handlePaymentConfirmed(PaymentConfirmedCommand vo) {

    long concertScheduleId = vo.getConcertScheduleId();
    String uuid = vo.getUuid();
    long seatNumber = vo.getSeatNumber();
    long price = vo.getPrice();

    ConcertScheduleEntity concertSchedule = getConcertSchedule(concertScheduleId);
    long concertId = concertSchedule.getConcertId();

    ConcertScheduleSeatEntity concertScheduleSeat = concertScheduleSeatService.getConcertScheduleSeatWithDistributedLock(concertScheduleId, seatNumber);

    updateStatus(concertScheduleId, seatNumber);

    createReservation(concertId, concertScheduleId, uuid, concertScheduleSeat.getId(), price);

    String name = getMember(uuid).getName();
    String concertName = getConcert(concertScheduleId).getName();
    LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

    return ReservationVO.of(name, concertName, dateTime, price);
  }

  private ConcertEntity getConcert(long concertScheduleId) {
    ConcertScheduleEntity concertSchedule = getConcertSchedule(concertScheduleId);
    return concertService.getConcertById(concertSchedule.getConcertId());
  }

  private Member getMember(String uuid) {
    return memberService.getMemberByUuid(uuid);
  }


  private ConcertScheduleEntity getConcertSchedule(long concertScheduleId) {
    return concertScheduleService.getConcertScheduleById(concertScheduleId);
  }

  private void updateStatus(long concertScheduleId, long seatNumber) {
    concertScheduleSeatService.updateSeatStatus(concertScheduleId, seatNumber, ConcertScheduleSeatStatus.RESERVED);
  }
}
