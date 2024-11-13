package com.example.concert.seat.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concerthall.service.ConcertHallService;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.member.service.MemberService;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatStatus;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SeatFacade {

    private final TimeProvider timeProvider;
    private final MemberService memberService;
    private final ConcertHallService concertHallService;
    private final SeatService seatService;

    @Transactional
    public void createSeatReservationWithPessimisticLock(String uuid, long concertHallId, long number) {
        validateMember(uuid);
        validateConcertHall(concertHallId);

        boolean isReservable = validateSeatWithPessimisticLock(concertHallId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatService.changeUpdatedAtWithPessimisticLock(concertHallId, number);
    }


    @Transactional
    public void createSeatReservationWithOptimisticLock(String uuid, long concertHallId, long number) {
        validateMember(uuid);
        validateConcertHall(concertHallId);

        boolean isReservable = validateSeatWithOptimisticLock(concertHallId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatService.changeUpdatedAtWithOptimisticLock(concertHallId, number);
    }


    @Transactional
    public void createSeatReservationWithDistributedLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertHall(concertScheduleId);

        boolean isReservable = validateSeatWithDistributedLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatService.changeUpdatedAtWithDistributedLock(concertScheduleId, number);
    }

    private void validateMember(String uuid) {
        memberService.getMemberByUuid(uuid);
    }

    private void validateConcertHall(long concertHallId) {
        concertHallService.getConcertHallById(concertHallId);
    }

    private boolean validateSeatWithPessimisticLock(long concertScheduleId, long number) {
        Seat seat = seatService.getSeatByConcertHallIdAndNumberWithPessimisticLock(concertScheduleId, number);
        return seat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seat.getUpdatedAt());
    }


    private boolean validateSeatWithOptimisticLock(long concertScheduleId, long number) {
        Seat seat = seatService.getSeatByConcertHallIdAndNumberWithOptimisticLock(concertScheduleId, number);
        return seat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seat.getUpdatedAt());
    }

    private boolean validateSeatWithDistributedLock(long concertScheduleId, long number) {
        String lockName = "SEAT_RESERVATION:" + concertScheduleId + ":" + number;

        Seat seat = seatService.getSeatByConcertHallIdAndNumberWithDistributedLock(lockName, concertScheduleId, number);
        return seat.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seat.getUpdatedAt());
    }

    private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 5;
    }
}
