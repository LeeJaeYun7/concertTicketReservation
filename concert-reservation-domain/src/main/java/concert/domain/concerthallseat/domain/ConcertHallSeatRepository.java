package concert.domain.concerthallseat.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertHallSeatRepository extends JpaRepository<ConcertHallSeat, Long> {

    @Query("SELECT s.number FROM ConcertHallSeat s WHERE s.id = :concertHallSeatId")
    long findConcertHallSeatNumber(@Param("concertHallSeatId") long concertHallSeatId);
    @Query("SELECT s FROM ConcertHallSeat s WHERE s.concertHallId = :concertHallId")
    List<ConcertHallSeat> findAllByConcertHallId(@Param("concertHallId") long concertHallId);
}
