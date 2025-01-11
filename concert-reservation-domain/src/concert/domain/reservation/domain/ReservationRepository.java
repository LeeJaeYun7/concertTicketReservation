package reservation.domain;

import concert.domain.Concert;
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
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.concertSchedule.id = :concertScheduleId AND r.seatInfo.id = :seatInfoId")
    Optional<Reservation> findReservation(@Param(value="concertScheduleId") long concertScheduleId, @Param(value="seatInfoId") long seatInfoId);

    // 최근 3일 간 콘서트 티켓 예약량 기준으로 Top30을 반환
    @Query("SELECT c, r.salesCount " +
            "FROM Concert c " +
            "JOIN (SELECT r.concert.id AS concertId, COUNT(*) AS salesCount " +
            "FROM Reservation r " +
            "WHERE r.createdAt >= :threeDaysAgo " +
            "GROUP BY r.concert.id) r " +
            "ON c.id = r.concertId " +
            "ORDER BY r.salesCount DESC")
    List<Concert> findTop30Concerts(@Param("threeDaysAgo") LocalDateTime threeDaysAgo);
}
