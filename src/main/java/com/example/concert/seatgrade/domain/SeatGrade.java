package com.example.concert.seatgrade.domain;

import com.example.concert.concert.domain.Concert;
import com.example.concert.seatgrade.enums.Grade;
import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seat_grade")
@NoArgsConstructor
public class SeatGrade extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;

    private long price;

    @Builder
    public SeatGrade(Concert concert, Grade grade, long price){
        this.concert = concert;
        this.grade = grade;
        this.price = price;
    }

    public static SeatGrade of(Concert concert, Grade grade, long price){
        return SeatGrade.builder()
                        .concert(concert)
                        .grade(grade)
                        .price(price)
                        .build();
    }
}
