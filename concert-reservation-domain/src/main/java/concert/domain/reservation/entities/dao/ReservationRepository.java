package concert.domain.reservation.entities.dao;

import concert.domain.concert.entities.ConcertEntity;
import concert.domain.reservation.entities.ReservationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT r FROM ReservationEntity r WHERE r.concertScheduleId = :concertScheduleId AND r.concertScheduleSeatId = :concertScheduleSeatId")
  Optional<ReservationEntity> findReservation(@Param(value = "concertScheduleId") long concertScheduleId, @Param(value = "concertScheduleSeatId") long concertScheduleSeatId);

  // 최근 3일 간 콘서트 티켓 예약량 기준으로 Top30을 반환
  @Query("SELECT c, r.salesCount " +
          "FROM ConcertEntity c " +
          "JOIN (SELECT r.concertId AS concertId, COUNT(*) AS salesCount " +
          "FROM ReservationEntity r " +
          "WHERE r.createdAt >= :threeDaysAgo " +
          "GROUP BY r.concertId) r " +
          "ON c.id = r.concertId " +
          "ORDER BY r.salesCount DESC")
  List<ConcertEntity> findTop30Concerts(@Param("threeDaysAgo") LocalDateTime threeDaysAgo);
}
