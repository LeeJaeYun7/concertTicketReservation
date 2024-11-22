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
import com.example.concert.reservation.event.PaymentRequestEvent;
import com.example.concert.reservation.infrastructure.kafka.producer.KafkaMessageProducer;
import com.example.concert.reservation.vo.ReservationVO;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.service.SeatInfoService;
import com.example.concert.utils.TimeProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final SeatInfoService seatInfoService;
    private final ConcertService concertService;
    private final ConcertScheduleService concertScheduleService;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Getter
    private final CompletableFuture<ReservationVO> reservationFuture = new CompletableFuture<>();

    @Transactional
    public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long seatNumber) {
        SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);
        long price = seatInfo.getSeatGrade().getPrice();

        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, price);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);

        System.out.println("sendPaymentEvent 시작 전");
        kafkaMessageProducer.sendPaymentEvent("payment-request-topic", new PaymentRequestEvent(concertSchedule.getConcert().getId(), concertScheduleId, uuid, seatNumber, price));

        return reservationFuture;
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
    private Concert getConcert(long concertScheduleId) {
        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
        return concertService.getConcertById(concertSchedule.getConcert().getId());
    }

    private ConcertSchedule getConcertSchedule(long concertScheduleId) {
        return concertScheduleService.getConcertScheduleById(concertScheduleId);
    }

    private Member getMember(String uuid) {
        return memberService.getMemberByUuid(uuid);
    }


    private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 5;
    }

}
