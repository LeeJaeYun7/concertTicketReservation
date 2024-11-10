package com.example.concert.concert.domain;

import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "concert")
@NoArgsConstructor
public class Concert extends BaseTimeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "genre")
    private String genre;

    @Column(name = "location")
    private String location;

    @Column(name = "performance_time")
    private long performanceTime;

    @Enumerated(EnumType.STRING)
    private ConcertAgeRestriction ageRestriction;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Builder
    public Concert(String name, String genre, String location, long performanceTime, ConcertAgeRestriction ageRestriction, LocalDate startAt, LocalDate endAt){
        this.name = name;
        this.genre = genre;
        this.location = location;
        this.performanceTime = performanceTime;
        this.ageRestriction = ageRestriction;
        this.startAt = startAt;
        this.endAt = endAt;
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static Concert of(String name, String genre, String location, long performanceTime, ConcertAgeRestriction ageRestriction, LocalDate startAt, LocalDate endAt){
        return Concert.builder()
                      .genre(genre)
                      .name(name)
                      .location(location)
                      .performanceTime(performanceTime)
                      .ageRestriction(ageRestriction)
                      .startAt(startAt)
                      .endAt(endAt)
                      .build();
    }
}
