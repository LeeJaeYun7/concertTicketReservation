package concert.domain.concert.entities.vo;

import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.entities.enums.ConcertAgeRestriction;
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

    public static ConcertVO of(ConcertEntity concert, String concertHallName){
        return ConcertVO.builder()
                .id(concert.getId())
                .name(concert.getName())
                .location(concertHallName)
                .genre(concert.getGenre())
                .performanceTime(concert.getPerformanceTime())
                .ageRestriction(concert.getAgeRestriction())
                .startAt(concert.getStartAt())
                .endAt(concert.getEndAt())
                .build();
    }
}
