package concert.domain.seat.domain;

import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "seat")
@NoArgsConstructor
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_hall_id")
    private ConcertHall concertHall;

    private long number;
    @Builder
    public Seat(ConcertHall concertHall, long number){
        this.concertHall = concertHall;
        this.number = number;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Seat of(ConcertHall concertHall, long number){
        return Seat.builder()
                .concertHall(concertHall)
                .number(number)
                .build();
    }
}
