package com.example.concert.concertgrade.domain;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concertgrade.enums.Grade;
import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "concert_grade")
@NoArgsConstructor
public class ConcertGrade extends BaseTimeEntity {

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
    public ConcertGrade(Concert concert, Grade grade, long price){
        this.concert = concert;
        this.grade = grade;
        this.price = price;
    }
}
