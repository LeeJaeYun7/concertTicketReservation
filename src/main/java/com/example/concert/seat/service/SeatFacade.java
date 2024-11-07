package com.example.concert.seat.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.member.service.MemberService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatStatus;
import com.example.concert.utils.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SeatFacade {

    private final TimeProvider timeProvider;
    private final MemberService memberService;
    private final ConcertScheduleService concertScheduleService;

    private final SeatService seatService;

    public SeatFacade(TimeProvider timeProvider, MemberService memberService, ConcertScheduleService concertScheduleService, SeatService seatService){
        this.timeProvider = timeProvider;
        this.memberService = memberService;
        this.concertScheduleService = concertScheduleService;
        this.seatService = seatService;
    }

    @Transactional
    public void createSeatReservationWithPessimisticLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertSchedule(concertScheduleId);

        boolean isReservable = validateSeatWithPessimisticLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatService.changeUpdatedAtWithPessimisticLock(concertScheduleId, number);
    }


    @Transactional
    public void createSeatReservationWithOptimisticLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertSchedule(concertScheduleId);

        boolean isReservable = validateSeatWithOptimisticLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatService.changeUpdatedAtWithOptimisticLock(concertScheduleId, number);
    }


    @Transactional
    public void createSeatReservationWithDistributedLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertSchedule(concertScheduleId);

        boolean isReservable = validateSeatWithDistributedLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatService.changeUpdatedAtWithDistributedLock(concertScheduleId, number);
    }

    private void validateMember(String uuid) {
        memberService.getMemberByUuid(uuid);
    }

    private void validateConcertSchedule(long concertScheduleId) {
        concertScheduleService.getConcertScheduleById(concertScheduleId);
    }

    private boolean validateSeatWithPessimisticLock(long concertScheduleId, long number) {
        Seat seat = seatService.getSeatByConcertScheduleIdAndNumberWithPessimisticLock(concertScheduleId, number);
        return seat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seat.getUpdatedAt());
    }


    private boolean validateSeatWithOptimisticLock(long concertScheduleId, long number) {
        Seat seat = seatService.getSeatByConcertScheduleIdAndNumberWithOptimisticLock(concertScheduleId, number);
        return seat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seat.getUpdatedAt());
    }

    private boolean validateSeatWithDistributedLock(long concertScheduleId, long number) {
        String lockName = "SEAT_RESERVATION:" + concertScheduleId + ":" + number;

        Seat seat = seatService.getSeatByConcertScheduleIdAndNumberWithDistributedLock(lockName, concertScheduleId, number);
        return seat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seat.getUpdatedAt());
    }

    private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 5;
    }
}
