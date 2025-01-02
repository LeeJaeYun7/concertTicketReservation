package reservation.application;

import common.CustomException;
import common.ErrorCode;
import common.Loggable;
import concert.application.ConcertService;
import concert.domain.Concert;
import concertschedule.application.ConcertScheduleService;
import concertschedule.domain.ConcertSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import member.application.MemberService;
import member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reservation.domain.Reservation;
import reservation.domain.ReservationRepository;
import reservation.domain.vo.PaymentConfirmedVO;
import reservation.domain.vo.ReservationVO;
import seatinfo.application.SeatInfoService;
import seatinfo.domain.SeatInfo;
import seatinfo.domain.enums.SeatStatus;

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
