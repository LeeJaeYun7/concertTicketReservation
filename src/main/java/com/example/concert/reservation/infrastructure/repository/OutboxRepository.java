package com.example.concert.reservation.infrastructure.repository;

import com.example.concert.reservation.infrastructure.messaging.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    @Query("SELECT o FROM Outbox o WHERE o.sent = false ORDER BY o.createdAt ASC LIMIT 10")
    List<Outbox> findTop10UnsentEvents();
}
