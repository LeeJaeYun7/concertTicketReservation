package concert.domain.seatinfo.domain;

import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.global.entity.BaseTimeEntity;
import concert.domain.seat.domain.Seat;
import concert.domain.seatgrade.domain.SeatGrade;
import concert.domain.seatinfo.domain.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "seat_info")
@NoArgsConstructor
public class SeatInfo extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seat_id")
  private Seat seat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "concert_schedule_id")
  private ConcertSchedule concertSchedule;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seat_grade_id")
  private SeatGrade seatGrade;

  @Enumerated(EnumType.STRING)
  private SeatStatus status;

  @Builder
  public SeatInfo(Seat seat, ConcertSchedule concertSchedule, SeatGrade seatGrade, SeatStatus status) {
    this.seat = seat;
    this.concertSchedule = concertSchedule;
    this.seatGrade = seatGrade;
    this.status = status;
    this.setCreatedAt(LocalDateTime.now());
    this.setUpdatedAt(LocalDateTime.now());
  }

  public static SeatInfo of(Seat seat, ConcertSchedule concertSchedule, SeatGrade seatGrade, SeatStatus status) {
    return SeatInfo.builder()
            .seat(seat)
            .concertSchedule(concertSchedule)
            .seatGrade(seatGrade)
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