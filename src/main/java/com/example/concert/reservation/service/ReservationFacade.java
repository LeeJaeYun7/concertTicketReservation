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
import com.example.concert.reservation.event.PaymentConfirmedEvent;
import com.example.concert.reservation.event.PaymentFailedEvent;
import com.example.concert.reservation.event.PaymentRequestEvent;
import com.example.concert.reservation.vo.ReservationVO;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.service.SeatInfoService;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationFacade {

    private final TimeProvider timeProvider;
    private final MemberService memberService;
    private final ReservationService reservationService;
    private final SeatInfoService seatInfoService;
    private final ConcertService concertService;
    private final ConcertScheduleService concertScheduleService;
    private final KafkaTemplate kafkaTemplate;

    private final CompletableFuture<ReservationVO> reservationFuture = new CompletableFuture<>();

    @Transactional
    public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long seatNumber) {
        SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);
        long price = seatInfo.getSeatGrade().getPrice();

        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, price);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);

        kafkaTemplate.send("payment-request-topic", new PaymentRequestEvent(concertSchedule.getConcert().getId(), concertScheduleId, uuid, seatNumber, price));

        return reservationFuture;
    }

    @Transactional
    // @KafkaListener(topics = "payment-confirmed-topic")
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        long concertScheduleId = event.getConcertScheduleId();
        String uuid = event.getUuid();
        long seatNumber = event.getSeatNumber();
        long price = event.getPrice();

        try {
            ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
            SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);

            memberService.decreaseBalance(uuid, price);
            updateStatus(concertScheduleId, seatNumber);

            reservationService.createReservation(concertSchedule.getConcert(), concertSchedule, uuid, seatInfo, price);

            String name = getMember(uuid).getName();
            String concertName = getConcert(concertScheduleId).getName();
            LocalDateTime dateTime = getConcertSchedule(concertScheduleId).getDateTime();

            ReservationVO reservationVO = ReservationVO.of(name, concertName, dateTime, price);
            reservationFuture.complete(reservationVO);

        } catch (Exception ex) {
            handleCompensation(event);
            throw new CustomException(ErrorCode.RESERVATION_FAILED, Loggable.ALWAYS);
        }
    }

    private void handleCompensation(PaymentConfirmedEvent event) {
        try {
            PaymentRequestEvent compensationEvent = new PaymentRequestEvent(
                    event.getConcertId(), event.getConcertScheduleId(),
                    event.getUuid(), event.getSeatNumber(), event.getPrice());

            kafkaTemplate.send("payment-compensation-topic", compensationEvent);
        } catch (Exception compensationEx) {
            log.error("Failed to send compensation request to Payment server", compensationEx);
            throw new CustomException(ErrorCode.PAYMENT_COMPENSATION_FAILED, Loggable.ALWAYS);
        }
    }

    // @KafkaListener(topics = "payment-failed-topic")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        throw new CustomException(ErrorCode.PAYMENT_FAILED, Loggable.NEVER);
    }

    private void updateStatus(long concertScheduleId, long seatNumber) {
        seatInfoService.updateSeatStatus(concertScheduleId, seatNumber, SeatStatus.RESERVED);
    }

    private void validateSeatReservation(long concertScheduleId, long seatNumber) {
         SeatInfo seatInfo = seatInfoService.getSeatInfo(concertScheduleId, seatNumber);

         if(isFiveMinutesPassed(seatInfo.getUpdatedAt())){
             throw new CustomException(ErrorCode.SEAT_RESERVATION_EXPIRED, Loggable.ALWAYS);
         }
    }

    private void checkBalanceOverPrice(String uuid, long price) {
        long balance = getMember(uuid).getBalance();

        if(balance - price < 0){
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE, Loggable.NEVER);
        }
    }
    private Member getMember(String uuid) {
        return memberService.getMemberByUuid(uuid);
    }

    private Concert getConcert(long concertScheduleId) {
        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
        return concertService.getConcertById(concertSchedule.getConcert().getId());
    }

    private ConcertSchedule getConcertSchedule(long concertScheduleId) {
        return concertScheduleService.getConcertScheduleById(concertScheduleId);
    }

    private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 5;
    }
}
