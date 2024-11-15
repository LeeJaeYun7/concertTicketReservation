package com.example.concert.seat.domain;

import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.global.entity.BaseTimeEntity;
import com.example.concert.seat.enums.SeatStatus;
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

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Builder
    public Seat(ConcertHall concertHall, long number, SeatStatus status){
        this.concertHall = concertHall;
        this.number = number;
        this.status = status;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Seat of(ConcertHall concertHall, long number){
        return Seat.builder()
                   .concertHall(concertHall)
                   .number(number)
                   .status(SeatStatus.AVAILABLE)
                   .build();
    }

    public void changeUpdatedAt(LocalDateTime dateTime){
        this.setUpdatedAt(dateTime);
    }

    public void updateStatus(SeatStatus status){
        this.status = status;
    }
}
