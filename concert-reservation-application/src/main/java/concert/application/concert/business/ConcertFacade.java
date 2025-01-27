package concert.application.concert.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.concert.presentation.response.ConcertResponse;
import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.services.ConcertService;
import concert.domain.concert.entities.vo.ConcertVO;
import concert.domain.concerthall.services.ConcertHallService;
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
    List<ConcertEntity> concerts = concertService.getTop30ConcertsFromDB();
    return getTop30ConcertResponses(concerts);
  }

  public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
    concertService.saveTop30ConcertsIntoRedis();
  }

  public List<ConcertResponse> getTop30Concerts() throws JsonProcessingException {
    List<ConcertEntity> concerts = concertService.getTop30Concerts();
    return getTop30ConcertResponses(concerts);
  }

  public List<ConcertResponse> getTop30ConcertResponses(List<ConcertEntity> concerts){
    List<Long> concertHallIds = concerts.stream()
                                        .map(ConcertEntity::getConcertHallId)
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
