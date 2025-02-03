package concert.interfaces.concert.response;

import concert.domain.concert.entities.enums.ConcertAgeRestriction;
import concert.domain.concert.entities.vo.ConcertVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ConcertResponse {

  private long concertId;
  private String name;
  private String genre;
  private String location;
  private long performanceTime;
  private ConcertAgeRestriction ageRestriction;
  private LocalDate startAt;
  private LocalDate endAt;

  @Builder
  public ConcertResponse(long concertId, String name, String genre, String location, long performanceTime,
                         ConcertAgeRestriction ageRestriction, LocalDate startAt, LocalDate endAt) {
    this.concertId = concertId;
    this.name = name;
    this.genre = genre;
    this.location = location;
    this.performanceTime = performanceTime;
    this.ageRestriction = ageRestriction;
    this.startAt = startAt;
    this.endAt = endAt;
  }

  public static ConcertResponse of(ConcertVO concertVO) {
    return ConcertResponse.builder()
                          .concertId(concertVO.getConcertId())
                          .name(concertVO.getName())
                          .genre(concertVO.getGenre())
                          .location(concertVO.getLocation())
                          .performanceTime(concertVO.getPerformanceTime())
                          .ageRestriction(concertVO.getAgeRestriction())
                          .startAt(concertVO.getStartAt())
                          .endAt(concertVO.getEndAt())
                          .build();
  }
}
