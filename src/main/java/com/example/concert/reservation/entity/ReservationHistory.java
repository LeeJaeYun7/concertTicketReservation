package com.example.concert.reservation.entity;

import com.example.concert.concert.entity.Concert;
import com.example.concert.global.entity.BaseTimeEntity;
import com.example.concert.member.entity.Member;
import com.example.concert.seat.entity.Seat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

@Entity
public class ReservationHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Concert concert;

    private Member member;

    private Seat seat;

    private long price;

    @Builder
    public ReservationHistory(Concert concert, Member member, Seat seat, long price){
        this.concert = concert;
        this.member = member;
        this.seat = seat;
        this.price = price;
    }
}
