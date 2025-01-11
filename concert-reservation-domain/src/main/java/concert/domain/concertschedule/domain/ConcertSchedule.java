package concert.domain.concertschedule.domain;

import concert.domain.concert.domain.Concert;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "concert_id")
  private Concert concert;

  private LocalDateTime dateTime;

  @Builder
  public ConcertSchedule(Concert concert, LocalDateTime dateTime) {
    this.concert = concert;
    this.dateTime = dateTime;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static ConcertSchedule of(Concert concert, LocalDateTime dateTime) {
    return ConcertSchedule.builder()
            .concert(concert)
            .dateTime(dateTime)
            .build();
  }
}
