package concert.domain.concert.entities.dao;

import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.entities.enums.ConcertScheduleSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConcertScheduleSeatEntityDAO extends JpaRepository<ConcertScheduleSeatEntity, Long> {

  @Query("SELECT s FROM ConcertScheduleSeatEntity s WHERE s.concertScheduleId = :concertScheduleId AND s.status = :status AND s.updatedAt <= :threshold")
  List<ConcertScheduleSeatEntity> findAllAvailableConcertScheduleSeatEntities(@Param("concertScheduleId") long concertScheduleId, @Param("status") ConcertScheduleSeatStatus status, @Param("threshold") LocalDateTime threshold);

  @Query("SELECT s FROM ConcertScheduleSeatEntity s WHERE s.concertScheduleId = :concertScheduleId AND s.concertHallSeatId = :concertHallSeatId")
  Optional<ConcertScheduleSeatEntity> findConcertScheduleSeatEntity(@Param("concertScheduleId") long concertScheduleId, @Param("concertHallSeatId") long concertHallSeatId);

  @Query("SELECT s FROM ConcertScheduleSeatEntity s WHERE s.concertScheduleId = :concertScheduleId AND s.concertHallSeatId = :concertHallSeatId")
  Optional<ConcertScheduleSeatEntity> findConcertScheduleSeatEntityWithDistributedLock(@Param("concertScheduleId") long concertScheduleId, @Param("concertHallSeatId") long concertHallSeatId);
}
