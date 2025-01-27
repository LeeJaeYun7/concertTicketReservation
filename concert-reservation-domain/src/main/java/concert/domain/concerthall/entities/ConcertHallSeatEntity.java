package concert.domain.concerthall.entities;

import concert.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "concert_hall_seat")
@NoArgsConstructor
public class ConcertHallSeatEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "concert_hall_id")
    private long concertHallId;

    private long number;
    @Builder
    public ConcertHallSeatEntity(long concertHallId, long number){
        this.concertHallId = concertHallId;
        this.number = number;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static ConcertHallSeatEntity of(long concertHallId, long number){
        return ConcertHallSeatEntity.builder()
                              .concertHallId(concertHallId)
                              .number(number)
                              .build();
    }
}
