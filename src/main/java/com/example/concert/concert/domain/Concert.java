package com.example.concert.concert.domain;

import com.example.concert.concert.enums.ConcertAgeRestriction;
import com.example.concert.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "concert")
@NoArgsConstructor
public class Concert extends BaseTimeEntity {

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
    private String performanceTime;

    @Enumerated(EnumType.STRING)
    private ConcertAgeRestriction ageRestriction;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Builder
    public Concert(String name, String genre, String location, String performanceTime, ConcertAgeRestriction ageRestriction, LocalDateTime startAt, LocalDateTime endAt){
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

    public static Concert of(String name, String genre, String location, String performanceTime, ConcertAgeRestriction ageRestriction, LocalDateTime startAt, LocalDateTime endAt){
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
