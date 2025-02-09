package concert.domain.concert.entities.dao;

import concert.domain.concert.entities.ConcertScheduleEntity;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConcertScheduleEntityDAO extends JpaRepository<ConcertScheduleEntity, Long> {
    @Query("SELECT c FROM ConcertScheduleEntity c WHERE c.concertId = :concertId AND c.dateTime >= :now")
    List<ConcertScheduleEntity> findAllAfterNowByConcertId(@Param("concertId") long concertId, @Param("now") LocalDateTime now);

    @Query("SELECT c.name, cs.dateTime FROM ConcertScheduleEntity cs INNER JOIN ConcertEntity c ON cs.concertId = c.id WHERE cs.dateTime >= :now")
    List<Tuple> findAllAfterNow(@Param("now") LocalDateTime now);
}
