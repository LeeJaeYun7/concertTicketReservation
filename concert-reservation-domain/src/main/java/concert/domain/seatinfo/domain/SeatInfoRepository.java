package concert.domain.seatinfo.domain;

import concert.domain.seatinfo.domain.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatInfoRepository extends JpaRepository<SeatInfo, Long> {

  @Query("SELECT s FROM SeatInfo s WHERE s.concertSchedule.id = :concertScheduleId AND s.status = :status AND s.updatedAt <= :threshold")
  List<SeatInfo> findAllAvailableSeats(@Param("concertScheduleId") long concertScheduleId, @Param("status") SeatStatus status, @Param("threshold") LocalDateTime threshold);

  @Query("SELECT s FROM SeatInfo s WHERE s.concertSchedule.id = :concertScheduleId AND s.seat.number = :number")
  Optional<SeatInfo> findSeatInfo(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);


  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM SeatInfo s WHERE s.concertSchedule.id = :concertScheduleId AND s.seat.number = :number")
  Optional<SeatInfo> findSeatInfoWithPessimisticLock(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);

  @Lock(LockModeType.OPTIMISTIC)
  @Query("SELECT s FROM SeatInfo s WHERE s.concertSchedule.id = :concertScheduleId AND s.seat.number = :number")
  Optional<SeatInfo> findSeatInfoWithOptimisticLock(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);

  @Query("SELECT s FROM SeatInfo s WHERE s.concertSchedule.id = :concertScheduleId AND s.seat.number = :number")
  Optional<SeatInfo> findSeatInfoWithDistributedLock(@Param("concertScheduleId") long concertScheduleId, @Param("number") long number);
}
