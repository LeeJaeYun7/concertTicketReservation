package concert.domain.concert.entities;

import concert.domain.concert.entities.enums.Grade;
import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "seat_grade")
@NoArgsConstructor
public class SeatGradeEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "concert_id")
  private long concertId;

  @Enumerated(EnumType.STRING)
  @Column(name = "grade")
  private Grade grade;

  private long price;

  @Builder
  public SeatGradeEntity(long concertId, Grade grade, long price) {
    this.concertId = concertId;
    this.grade = grade;
    this.price = price;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static SeatGradeEntity of(long concertId, Grade grade, long price) {
    return SeatGradeEntity.builder()
                          .concertId(concertId)
                          .grade(grade)
                          .price(price)
                          .build();
  }
}
