package concert.concert;

import concert.commons.utils.TimeProvider;
import concert.concert.fixtures.ConcertFixtureFactory;
import concert.domain.concert.application.ConcertService;
import concert.domain.concert.cache.ConcertCache;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.ConcertRepository;
import concert.domain.reservation.domain.ReservationRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Disabled
public class ConcertServiceIntegrationTest {

  @Autowired
  private TimeProvider timeProvider;
  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private ConcertCache concertCache;
  @Autowired
  private ConcertService sut;

  @Autowired
  private ConcertRepository concertRepository;

  @Test
  @DisplayName("콘서트를 저장하고 가져온다")
  void 콘서트를_저장하고_가져온다() {
    Concert concert = ConcertFixtureFactory.createConcertWithParameters(1L, "박효신 콘서트", 120);
    concertRepository.save(concert);

    Concert foundConcert = sut.getConcertById(1L);

    assertThat(foundConcert).isNotNull();
    assertThat(foundConcert.getId()).isEqualTo(1L);
    assertThat(foundConcert.getName()).isEqualTo("박효신 콘서트");
  }
}
