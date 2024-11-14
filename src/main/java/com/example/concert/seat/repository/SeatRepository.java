package com.example.concert.seat.repository;

import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.concertHall.id = :concertHallId AND s.status = :status AND s.updatedAt <= :threshold")
    List<Seat> findAllAvailableSeatsByConcertHallIdAndStatus(@Param("concertHallId") long concertHallId, @Param("status") SeatStatus status, @Param("threshold") LocalDateTime threshold);

    Optional<Seat> findByConcertHallIdAndNumber(long concertHallId, long number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.concertHall.id = :concertHallId AND s.number = :number")
    Optional<Seat> findByConcertHallIdAndNumberWithPessimisticLock(@Param("concertHallId") long concertHallId, @Param("number") long number);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Seat s WHERE s.concertHall.id = :concertHallId AND s.number = :number")
    Optional<Seat> findByConcertHallIdAndNumberWithOptimisticLock(@Param("concertHallId") long concertHallId, @Param("number") long number);

    @Query("SELECT s FROM Seat s WHERE s.concertHall.id = :concertHallId AND s.number = :number")
    Optional<Seat> findByConcertHallIdAndNumberWithDistributedLock(@Param("concertHallId") long concertHallId, @Param("number") long number);
}
