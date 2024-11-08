package com.example.concert.concert.vo;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.enums.ConcertAgeRestriction;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ConcertVO {

    private long id;
    private String name;
    private String genre;
    private String location;
    private long performanceTime;
    private ConcertAgeRestriction ageRestriction;
    private LocalDate startAt;
    private LocalDate endAt;
    @Builder
    public ConcertVO(long id, String name, String genre, String location, long performanceTime,
                           ConcertAgeRestriction ageRestriction, LocalDate startAt, LocalDate endAt) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.location = location;
        this.performanceTime = performanceTime;
        this.ageRestriction = ageRestriction;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static ConcertVO of(Concert concert){
            return ConcertVO.builder()
                    .id(concert.getId())
                    .name(concert.getName())
                    .genre(concert.getGenre())
                    .location(concert.getLocation())
                    .performanceTime(concert.getPerformanceTime())
                    .ageRestriction(concert.getAgeRestriction())
                    .startAt(concert.getStartAt())
                    .endAt(concert.getEndAt())
                    .build();
        }
}
