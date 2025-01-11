package concert.concert.fixtures;

import concert.domain.concert.domain.Concert;

import java.time.LocalDateTime;

import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ConcertFixtureFactory {

  public static Concert createConcert() {
    return new Concert();
  }

  public static Concert createConcertWithIdAndName(long concertId, String name) {
    Concert concert = createConcert();
    setField(concert, "id", concertId);
    setField(concert, "name", name);
    return concert;
  }

  public static Concert createConcertWithParameters(long concertId, String name, long performanceTime) {
    Concert concert = createConcert();
    setField(concert, "id", concertId);
    setField(concert, "name", name);
    setField(concert, "performanceTime", performanceTime);
    setField(concert, "createdAt", LocalDateTime.now());
    setField(concert, "updatedAt", LocalDateTime.now());

    return concert;
  }
}
