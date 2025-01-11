package concert.domain.concert.domain;

import concert.domain.concert.domain.enums.ConcertAgeRestriction;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "concert")
@NoArgsConstructor
public class Concert extends BaseTimeEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name")
  private String name;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "concert_hall_id")
  private ConcertHall concertHall;

  @Column(name = "genre")
  private String genre;

  @Column(name = "performance_time")
  private long performanceTime;

  @Enumerated(EnumType.STRING)
  private ConcertAgeRestriction ageRestriction;

  @Column(name = "start_at")
  private LocalDate startAt;

  @Column(name = "end_at")
  private LocalDate endAt;

  @Builder
  public Concert(String name, ConcertHall concertHall, String genre, long performanceTime, ConcertAgeRestriction ageRestriction, LocalDate startAt, LocalDate endAt) {
    this.name = name;
    this.concertHall = concertHall;
    this.genre = genre;
    this.performanceTime = performanceTime;
    this.ageRestriction = ageRestriction;
    this.startAt = startAt;
    this.endAt = endAt;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static Concert of(String name, ConcertHall concertHall, String genre, long performanceTime, ConcertAgeRestriction ageRestriction, LocalDate startAt, LocalDate endAt) {
    return Concert.builder()
            .name(name)
            .concertHall(concertHall)
            .genre(genre)
            .performanceTime(performanceTime)
            .ageRestriction(ageRestriction)
            .startAt(startAt)
            .endAt(endAt)
            .build();
  }
}
