package concert.domain.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    @Query("SELECT o FROM Outbox o WHERE o.sent = false ORDER BY o.createdAt ASC LIMIT 10")
    List<Outbox> findTop10UnsentEvents();

    Optional<Outbox> findByMessage(String message);
}
