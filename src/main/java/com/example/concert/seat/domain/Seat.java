package com.example.concert.seat.domain;

import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.global.entity.BaseTimeEntity;
import com.example.concert.seat.enums.SeatGrade;
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

    private long price;

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Enumerated(EnumType.STRING)
    private SeatGrade grade;

    @Builder
    public Seat(ConcertHall concertHall, long number, long price, SeatGrade grade, SeatStatus status){
        this.concertHall = concertHall;
        this.number = number;
        this.price = price;
        this.grade = grade;
        this.status = status;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Seat of(ConcertHall concertHall, long number, long price, SeatGrade grade){
        return Seat.builder()
                   .concertHall(concertHall)
                   .number(number)
                   .price(price)
                   .grade(grade)
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
