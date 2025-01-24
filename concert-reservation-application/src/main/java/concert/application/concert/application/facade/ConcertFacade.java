package concert.application.concert.application.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.concert.application.dto.ConcertResponse;
import concert.domain.concert.application.ConcertService;
import concert.domain.concert.domain.Concert;
import concert.domain.concert.domain.vo.ConcertVO;
import concert.domain.concerthall.application.ConcertHallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConcertFacade {

  private final ConcertService concertService;
  private final ConcertHallService concertHallService;

  public List<ConcertResponse> getTop30ConcertsFromDB() {
    List<Concert> concerts = concertService.getTop30ConcertsFromDB();
    return getTop30ConcertResponses(concerts);
  }

  public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
    concertService.saveTop30ConcertsIntoRedis();
  }

  public List<ConcertResponse> getTop30Concerts() throws JsonProcessingException {
    List<Concert> concerts = concertService.getTop30Concerts();
    return getTop30ConcertResponses(concerts);
  }

  public List<ConcertResponse> getTop30ConcertResponses(List<Concert> concerts){
    List<Long> concertHallIds = concerts.stream()
                                        .map(Concert::getConcertHallId)
                                        .distinct()
                                        .collect(Collectors.toList());

    Map<Long, String> concertHallNames = concertHallService.getConcertHallNamesByIds(concertHallIds);

    return concerts.stream()
                   .map(concert -> {
                      String concertHallName = concertHallNames.get(concert.getConcertHallId());
                      return ConcertVO.of(concert, concertHallName); // VO 생성
                    })
                   .map(ConcertResponse::of)
                   .collect(Collectors.toList());
  }
}
