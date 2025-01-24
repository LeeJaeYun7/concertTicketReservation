package concert.domain.concertscheduleseat.domain;

import concert.domain.global.entity.BaseTimeEntity;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "concert_schedule_seat")
@NoArgsConstructor
public class ConcertScheduleSeat extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "concert_hall_seat_id")
  private long concertHallSeatId;

  @Column(name = "concert_schedule_id")
  private long concertScheduleId;

  @Column(name = "seat_grade_id")
  private long seatGradeId;

  @Enumerated(EnumType.STRING)
  private SeatStatus status;

  @Builder
  public ConcertScheduleSeat(long concertHallSeatId, long concertScheduleId, long seatGradeId, SeatStatus status) {
    this.concertHallSeatId = concertHallSeatId;
    this.concertScheduleId = concertScheduleId;
    this.seatGradeId = seatGradeId;
    this.status = status;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static ConcertScheduleSeat of(long concertHallSeatId, long concertScheduleId, long seatGradeId, SeatStatus status) {
    return ConcertScheduleSeat.builder()
                              .concertHallSeatId(concertHallSeatId)
                              .concertScheduleId(concertScheduleId)
                              .seatGradeId(seatGradeId)
                              .status(status)
                              .build();
  }

  public void updateStatus(SeatStatus status) {
    this.status = status;
  }

  public void changeUpdatedAt(LocalDateTime dateTime) {
    this.setUpdatedAt(dateTime);
  }
}