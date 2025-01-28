package concert.concert.fixtures;

import concert.domain.concert.entities.ConcertEntity;

import java.time.LocalDateTime;

import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ConcertEntityFixtureFactory {

  public static ConcertEntity createConcert() {
    return new ConcertEntity();
  }

  public static ConcertEntity createConcertWithIdAndName(long concertId, String name) {
    ConcertEntity concert = createConcert();
    setField(concert, "id", concertId);
    setField(concert, "name", name);
    return concert;
  }

  public static ConcertEntity createConcertWithParameters(long concertId, String name, long performanceTime) {
    ConcertEntity concert = createConcert();
    setField(concert, "id", concertId);
    setField(concert, "name", name);
    setField(concert, "performanceTime", performanceTime);
    setField(concert, "createdAt", LocalDateTime.now());
    setField(concert, "updatedAt", LocalDateTime.now());

    return concert;
  }
}
