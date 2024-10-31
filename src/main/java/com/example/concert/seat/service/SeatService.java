package com.example.concert.seat.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.lock.DistributedLock;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.domain.SeatStatus;
import com.example.concert.seat.repository.SeatRepository;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final TimeProvider timeProvider;
    private final SeatRepository seatRepository;
    private final RedissonClient redissonClient;


    public List<Seat> getAllAvailableSeats(long concertScheduleId){
        LocalDateTime now = timeProvider.now();
        LocalDateTime threshold = now.minusMinutes(5);

        return seatRepository.findAllAvailableSeatsByConcertScheduleIdAndStatus(concertScheduleId, SeatStatus.AVAILABLE, threshold);
    }

    public void changeUpdatedAt(long concertScheduleId, long number) {
        Seat seat = getSeatByConcertScheduleIdAndNumberWithPessimisticLock(concertScheduleId, number);
        LocalDateTime now = timeProvider.now();
        seat.changeUpdatedAt(now);
    }

    public void updateSeatStatus(long concertScheduleId, long number, SeatStatus status) {
        Seat seat = getSeatByConcertScheduleIdAndNumberWithPessimisticLock(concertScheduleId, number);
        seat.updateStatus(status);
    }

    public Seat getSeatByConcertScheduleIdAndNumber(long concertScheduleId, long number) {
        return seatRepository.findByConcertScheduleIdAndNumber(concertScheduleId, number)
                             .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Seat getSeatByConcertScheduleIdAndNumberWithPessimisticLock(long concertScheduleId, long number) {
        return seatRepository.findByConcertScheduleIdAndNumberWithPessimisticLock(concertScheduleId, number)
                             .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Seat getSeatByConcertScheduleIdAndNumberWithOptimisticLock(long concertScheduleId, long number) {
        return seatRepository.findByConcertScheduleIdAndNumberWithOptimisticLock(concertScheduleId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }
    @DistributedLock(key = "#concertScheduleId + '_' + #number", waitTime = 60, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
    public Seat getSeatByConcertScheduleIdAndNumberWithDistributedLock(String lockName, long concertScheduleId, long number) {

        System.out.println("getSeatByConcertScheduleIdAndNumberWithDistributedLock 진입");

        return seatRepository.findByConcertScheduleIdAndNumberWithDistributedLock(concertScheduleId, number)
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }
}
