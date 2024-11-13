package com.example.concert.seat.service;

import com.example.concert.common.CustomException;
import com.example.concert.common.ErrorCode;
import com.example.concert.common.Loggable;
import com.example.concert.lock.DistributedLock;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatStatus;
import com.example.concert.seat.repository.SeatRepository;
import com.example.concert.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final TimeProvider timeProvider;
    private final SeatRepository seatRepository;


    public List<Seat> getAllAvailableSeats(long concertHallId){
        LocalDateTime now = timeProvider.now();
        LocalDateTime threshold = now.minusMinutes(5);

        return seatRepository.findAllAvailableSeatsByConcertHallIdAndStatus(concertHallId, SeatStatus.AVAILABLE, threshold);
    }

    public void changeUpdatedAtWithPessimisticLock(long concertHallId, long number) {
        Seat seat = getSeatByConcertHallIdAndNumberWithPessimisticLock(concertHallId, number);
        LocalDateTime now = timeProvider.now();
        seat.changeUpdatedAt(now);
    }


    public void changeUpdatedAtWithOptimisticLock(long concertHallId, long number) {
        Seat seat = getSeatByConcertHallIdAndNumberWithOptimisticLock(concertHallId, number);
        LocalDateTime now = timeProvider.now();
        seat.changeUpdatedAt(now);
    }
    public void changeUpdatedAtWithDistributedLock(long concertHallId, long number) {
        String lockName = "SEAT_RESERVATION:" + concertHallId + ":" + number;

        Seat seat = getSeatByConcertHallIdAndNumberWithDistributedLock(lockName, concertHallId, number);
        LocalDateTime now = timeProvider.now();
        seat.changeUpdatedAt(now);
    }


    public void updateSeatStatus(long concertHallId, long number, SeatStatus status) {
        Seat seat = getSeatByConcertHallIdAndNumberWithPessimisticLock(concertHallId, number);
        seat.updateStatus(status);
    }

    public Seat getSeatByConcertHallIdAndNumber(long concertHallId, long number) {
        return seatRepository.findByConcertHallIdAndNumber(concertHallId, number)
                             .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Seat getSeatByConcertHallIdAndNumberWithPessimisticLock(long concertHallId, long number) {
        return seatRepository.findByConcertHallIdAndNumberWithPessimisticLock(concertHallId, number)
                             .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }

    public Seat getSeatByConcertHallIdAndNumberWithOptimisticLock(long concertHallId, long number) {
        return seatRepository.findByConcertHallIdAndNumberWithOptimisticLock(concertHallId, number)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }
    @DistributedLock(key = "#concertHallId + '_' + #number", waitTime = 60, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
    public Seat getSeatByConcertHallIdAndNumberWithDistributedLock(String lockName, long concertHallId, long number) {
        return seatRepository.findByConcertHallIdAndNumberWithDistributedLock(concertHallId, number)
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
    }
}
