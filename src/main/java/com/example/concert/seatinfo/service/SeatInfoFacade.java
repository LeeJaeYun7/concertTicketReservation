package com.example.concert.seatinfo.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.member.service.MemberService;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class SeatInfoFacade {

    private final TimeProvider timeProvider;
    private final MemberService memberService;
    private final ConcertScheduleService concertScheduleService;
    private final SeatInfoService seatInfoService;

    @Transactional
    public void createSeatInfoReservationWithPessimisticLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertSchedule(concertScheduleId);

        boolean isReservable = validateSeatWithPessimisticLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatInfoService.changeUpdatedAtWithPessimisticLock(concertScheduleId, number);
    }


    @Transactional
    public void createSeatInfoReservationWithOptimisticLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertSchedule(concertScheduleId);

        boolean isReservable = validateSeatWithOptimisticLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatInfoService.changeUpdatedAtWithOptimisticLock(concertScheduleId, number);
    }


    @Transactional
    public void createSeatInfoReservationWithDistributedLock(String uuid, long concertScheduleId, long number) {
        validateMember(uuid);
        validateConcertSchedule(concertScheduleId);

        boolean isReservable = validateSeatWithDistributedLock(concertScheduleId, number);

        if(!isReservable){
            throw new CustomException(ErrorCode.NOT_VALID_SEAT, Loggable.ALWAYS);
        }

        seatInfoService.changeUpdatedAtWithDistributedLock(concertScheduleId, number);
    }


    private void validateMember(String uuid) {
        memberService.getMemberByUuid(uuid);
    }

    private void validateConcertSchedule(long concertScheduleId) {
        concertScheduleService.getConcertScheduleById(concertScheduleId);
    }

    private boolean validateSeatWithPessimisticLock(long concertScheduleId, long number) {
        SeatInfo seatInfo = seatInfoService.getSeatInfo(concertScheduleId, number);
        return seatInfo.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seatInfo.getUpdatedAt());
    }


    private boolean validateSeatWithOptimisticLock(long concertScheduleId, long number) {
        SeatInfo seatInfo = seatInfoService.getSeatInfoWithOptimisticLock(concertScheduleId, number);
        return seatInfo.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seatInfo.getUpdatedAt());
    }

    private boolean validateSeatWithDistributedLock(long concertScheduleId, long number) {
        String lockName = "SEAT_RESERVATION:" + concertScheduleId + ":" + number;

        SeatInfo seatInfo = seatInfoService.getSeatInfoWithDistributedLock(lockName, concertScheduleId, number);
        return seatInfo.getStatus().equals(SeatStatus.AVAILABLE) && isFiveMinutesPassed(seatInfo.getUpdatedAt());
    }

    private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 5;
    }
}
