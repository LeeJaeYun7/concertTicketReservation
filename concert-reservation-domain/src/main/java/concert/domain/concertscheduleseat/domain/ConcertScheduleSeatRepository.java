package concert.domain.concertscheduleseat.domain;

import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConcertScheduleSeatRepository extends JpaRepository<ConcertScheduleSeat, Long> {

  @Query("SELECT s FROM ConcertScheduleSeat s WHERE s.concertScheduleId = :concertScheduleId AND s.status = :status AND s.updatedAt <= :threshold")
  List<ConcertScheduleSeat> findAllAvailableConcertScheduleSeats(@Param("concertScheduleId") long concertScheduleId, @Param("status") SeatStatus status, @Param("threshold") LocalDateTime threshold);

  @Query("SELECT s FROM ConcertScheduleSeat s WHERE s.concertScheduleId = :concertScheduleId AND s.concertHallSeatId = :concertHallSeatId")
  Optional<ConcertScheduleSeat> findConcertScheduleSeat(@Param("concertScheduleId") long concertScheduleId, @Param("concertHallSeatId") long concertHallSeatId);

  @Query("SELECT s FROM ConcertScheduleSeat s WHERE s.concertScheduleId = :concertScheduleId AND s.concertHallSeatId = :concertHallSeatId")
  Optional<ConcertScheduleSeat> findConcertScheduleSeatWithDistributedLock(@Param("concertScheduleId") long concertScheduleId, @Param("concertHallSeatId") long concertHallSeatId);
}
