package concert.concert;

import concert.concert.fixtures.ConcertEntityFixtureFactory;
import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.services.ConcertService;
import concert.domain.concert.entities.dao.ConcertEntityDAO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Disabled
public class ConcertServiceTest {

  @Mock
  private ConcertEntityDAO concertEntityDAO;

  @InjectMocks
  private ConcertService sut;

  @Nested
  class 콘서트ID로_콘서트를_가져올때 {
    @Test
    @DisplayName("콘서트를 가져온다")
    void 콘서트를_가져온다() {
      ConcertEntity concert = ConcertEntityFixtureFactory.createConcertWithIdAndName(1L, "박효신 콘서트");
      long concertId = 1L;
      given(concertEntityDAO.findById(concertId)).willReturn(Optional.of(concert));

      ConcertEntity foundConcert = sut.getConcertById(concertId);

      assertEquals(1L, foundConcert.getId());
      assertEquals("박효신 콘서트", foundConcert.getName());
    }
  }
}
