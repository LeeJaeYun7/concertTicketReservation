package concert.application.concert.business;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class ConcertApplicationService {

  private final ConcertService concertService;
  private final ConcertHallService concertHallService;

  public List<ConcertVO> getTop30ConcertsFromDB() {
    List<ConcertEntity> concerts = concertService.getTop30ConcertsFromDB();
    return getTop30ConcertVOs(concerts);
  }

  public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
    concertService.saveTop30ConcertsIntoRedis();
  }

  public List<ConcertVO> getTop30Concerts() throws JsonProcessingException {
    List<ConcertEntity> concerts = concertService.getTop30Concerts();
    return getTop30ConcertVOs(concerts);
  }

  public List<ConcertVO> getTop30ConcertVOs(List<ConcertEntity> concerts){
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
                   .collect(Collectors.toList());
  }
}
