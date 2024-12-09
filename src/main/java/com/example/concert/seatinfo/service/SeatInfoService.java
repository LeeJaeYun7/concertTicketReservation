package com.example.concert.seatinfo.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.lock.DistributedLock;
import com.example.concert.seatinfo.domain.SeatInfo;
import com.example.concert.seatinfo.enums.SeatStatus;
import com.example.concert.seatinfo.repository.SeatInfoRepository;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatInfoService {

    private final TimeProvider timeProvider;
    private final SeatInfoRepository seatInfoRepository;

    public List<SeatInfo> getAllAvailableSeats(long concertScheduleId){
        LocalDateTime now = timeProvider.now();
        LocalDateTime threshold = now.minusMinutes(5);

        return seatInfoRepository.findAllAvailableSeats(concertScheduleId, SeatStatus.AVAILABLE, threshold);
    }

    public List<Long> getAllAvailableSeatNumbers(long concertScheduleId){
        List<SeatInfo> availableSeats = getAllAvailableSeats(concertScheduleId);
        List<Long> availableSeatNumbers = new ArrayList<>();

        for(SeatInfo seat: availableSeats){
            availableSeatNumbers.add(seat.getSeat().getNumber());
        }

        return availableSeatNumbers;
    }


    public void changeUpdatedAtWithPessimisticLock(long concertHallId, long number) {
        SeatInfo seatInfo = getSeatInfoWithPessimisticLock(concertHallId, number);
        LocalDateTime now = timeProvider.now();
        seatInfo.changeUpdatedAt(now);
    }

    public void changeUpdatedAtWithOptimisticLock(long concertHallId, long number) {
        SeatInfo seatInfo = getSeatInfoWithOptimisticLock(concertHallId, number);
        LocalDateTime now = timeProvider.now();
        seatInfo.changeUpdatedAt(now);
    }
    public void changeUpdatedAtWithDistributedLock(long concertHallId, long number) {
        String lockName = "SEAT_RESERVATION:" + concertHallId + ":" + number;

        SeatInfo seatInfo = getSeatInfoWithDistributedLock(lockName, concertHallId, number);
        LocalDateTime now = timeProvider.now();
        seatInfo.changeUpdatedAt(now);
    }

    public void updateSeatStatus(long concertHallId, long number, SeatStatus status) {
        SeatInfo seatInfo = getSeatInfoWithPessimisticLock(concertHallId, number);
        seatInfo.updateStatus(status);
    }

    public SeatInfo getSeatInfo(long concertScheduleId, long number) {
        return seatInfoRepository.findSeatInfo(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }

    public SeatInfo getSeatInfoWithPessimisticLock(long concertScheduleId, long number) {
        return seatInfoRepository.findSeatInfoWithPessimisticLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }

    public SeatInfo getSeatInfoWithOptimisticLock(long concertScheduleId, long number) {
        return seatInfoRepository.findSeatInfoWithOptimisticLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }
    @DistributedLock(key = "#lockName", waitTime = 60, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
    public SeatInfo getSeatInfoWithDistributedLock(String lockName, long concertScheduleId, long number) {
        return seatInfoRepository.findSeatInfoWithDistributedLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }
}
