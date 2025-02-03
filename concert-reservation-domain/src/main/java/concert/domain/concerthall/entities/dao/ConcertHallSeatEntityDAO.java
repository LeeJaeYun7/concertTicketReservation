package concert.domain.concerthall.entities.dao;

import concert.domain.concerthall.entities.ConcertHallSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertHallSeatEntityDAO extends JpaRepository<ConcertHallSeatEntity, Long> {

    @Query("SELECT s.number FROM ConcertHallSeatEntity s WHERE s.id = :concertHallSeatId")
    long findConcertHallSeatNumber(@Param("concertHallSeatId") long concertHallSeatId);
    @Query("SELECT s FROM ConcertHallSeatEntity s WHERE s.concertHallId = :concertHallId")
    List<ConcertHallSeatEntity> findAllByConcertHallId(@Param("concertHallId") long concertHallId);
}
