package com.example.concert.reservation.domain;

import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.global.entity.BaseTimeEntity;
import com.example.concert.seatinfo.domain.SeatInfo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservation", indexes = {@Index(name = "idx_created_at_concert_id", columnList = "created_at, concert_id")})
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_schedule_id")
    private ConcertSchedule concertSchedule;

    private String uuid;

    @OneToOne
    @JoinColumn(name = "seat_info_id")
    private SeatInfo seatInfo;

    private long price;

    @Builder
    public Reservation(ConcertSchedule concertSchedule, String uuid, SeatInfo seatInfo, long price){
        this.concertSchedule = concertSchedule;
        this.uuid = uuid;
        this.seatInfo = seatInfo;
        this.price = price;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Reservation of(ConcertSchedule concertSchedule, String uuid, SeatInfo seatInfo, long price){
        return Reservation.builder()
                          .concertSchedule(concertSchedule)
                          .uuid(uuid)
                          .seatInfo(seatInfo)
                          .price(price)
                          .build();
    }
}
