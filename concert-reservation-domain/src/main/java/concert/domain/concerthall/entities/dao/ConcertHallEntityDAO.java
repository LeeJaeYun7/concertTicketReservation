package concert.domain.concerthall.entities.dao;

import concert.domain.concerthall.entities.ConcertHallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertHallEntityDAO extends JpaRepository<ConcertHallEntity, Long> {
}
