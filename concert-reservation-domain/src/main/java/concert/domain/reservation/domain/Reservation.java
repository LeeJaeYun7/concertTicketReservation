package concert.domain.reservation.domain;

import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "reservation", indexes = {@Index(name = "idx_created_at_concert_id", columnList = "created_at, concert_id")})
public class Reservation extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "concert_id")
  private long concertId;

  @Column(name = "concert_schedule_id")
  private long concertScheduleId;

  private String uuid;

  @Column(name = "concert_schedule_seat_id")
  private long concertScheduleSeatId;

  private long price;

  @Builder
  public Reservation(long concertId, long concertScheduleId, String uuid, long concertScheduleSeatId, long price) {
    this.concertId = concertId;
    this.concertScheduleId = concertScheduleId;
    this.uuid = uuid;
    this.concertScheduleSeatId = concertScheduleSeatId;
    this.price = price;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static Reservation of(long concertId, long concertScheduleId, String uuid, long concertScheduleSeatId, long price) {
    return Reservation.builder()
            .concertId(concertId)
            .concertScheduleId(concertScheduleId)
            .uuid(uuid)
            .concertScheduleSeatId(concertScheduleSeatId)
            .price(price)
            .build();
  }
}
