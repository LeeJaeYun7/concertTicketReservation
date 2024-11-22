package com.example.concert.reservation.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.service.ConcertService;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.member.domain.Member;
import com.example.concert.member.service.MemberService;
import com.example.concert.reservation.domain.Reservation;
import com.example.concert.reservation.event.PaymentConfirmedEvent;
import com.example.concert.reservation.infrastructure.kafka.producer.KafkaMessageProducer;
import com.example.concert.reservation.infrastructure.repository.ReservationRepository;
import com.example.concert.reservation.vo.ReservationVO;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.service.SeatInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationFacade reservationFacade;
    private final MemberService memberService;
    private final ConcertService concertService;
    private final SeatInfoService seatInfoService;
    private final ConcertScheduleService concertScheduleService;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final ReservationRepository reservationRepository;

    public Reservation createReservation(Concert concert, ConcertSchedule concertSchedule, String uuid, SeatInfo seatInfo, long price) {
        reservationRepository.findReservation(concertSchedule.getId(), seatInfo.getId());
        Reservation reservation = Reservation.of(concert, concertSchedule, uuid, seatInfo, price);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {

        log.info("handlePaymentConfirmed 시작!");

        long concertScheduleId = event.getConcertScheduleId();
        String uuid = event.getUuid();
        long seatNumber = event.getSeatNumber();
        long price = event.getPrice();

        try {
            ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
            SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);

            memberService.decreaseBalance(uuid, price);
            updateStatus(concertScheduleId, seatNumber);

            log.info("createReservation 시작!");
            createReservation(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, price);

            String name = getMember(uuid).getName();
            String concertName = getConcert(concertScheduleId).getName();
            LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

            ReservationVO reservationVO = ReservationVO.of(name, concertName, dateTime, price);
            reservationFacade.getReservationFuture().complete(reservationVO);

        } catch (Exception ex) {
            kafkaMessageProducer.sendPaymentConfirmedEvent("payment-compensation-topic", event);
            throw new CustomException(ErrorCode.RESERVATION_FAILED, Loggable.ALWAYS);
        }
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
