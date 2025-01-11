package concerthall.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertHallRepository extends JpaRepository<ConcertHall, Long> {
}
