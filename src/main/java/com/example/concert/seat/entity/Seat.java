package com.example.concert.seat.entity;

import com.example.concert.concert.entity.Concert;
import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Concert concert;

    private long number;

    private long price;

    private LocalDate date;

    private boolean isReserved;

    private LocalDateTime pendingAt;

    @Builder
    public Seat(Concert concert, long number, long price, LocalDate date, boolean isReserved){
        this.concert = concert;
        this.number = number;
        this.price = price;
        this.date = date;
        this.isReserved = isReserved;
    }
}
