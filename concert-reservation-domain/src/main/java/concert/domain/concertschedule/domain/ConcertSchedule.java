package concert.domain.concertschedule.domain;

import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "concert_schedule")
@NoArgsConstructor
public class ConcertSchedule extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "concert_id")
  private long concertId;

  private LocalDateTime dateTime;

  @Builder
  public ConcertSchedule(long concertId, LocalDateTime dateTime) {
    this.concertId = concertId;
    this.dateTime = dateTime;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static ConcertSchedule of(long concertId, LocalDateTime dateTime) {
    return ConcertSchedule.builder()
                          .concertId(concertId)
                          .dateTime(dateTime)
                          .build();
  }
}
