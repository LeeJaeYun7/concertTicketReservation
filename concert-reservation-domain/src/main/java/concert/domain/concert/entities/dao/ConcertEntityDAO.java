package concert.domain.concert.entities.dao;

import concert.domain.concert.entities.ConcertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConcertEntityDAO extends JpaRepository<ConcertEntity, Long> {
    Optional<ConcertEntity> findByName(String concertName);
}
