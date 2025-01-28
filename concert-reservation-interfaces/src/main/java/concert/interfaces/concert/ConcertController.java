package concert.interfaces.concert;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.concert.business.ConcertFacade;
import concert.domain.concert.entities.vo.ConcertVO;
import concert.interfaces.concert.response.ConcertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ConcertController {

  private final ConcertFacade concertFacade;

  @GetMapping("/api/v1/concert/top30/3days/db")
  public ResponseEntity<List<ConcertResponse>> retrieveAllConcertsFromDB() throws JsonProcessingException {
    List<ConcertVO> concertVOs = concertFacade.getTop30ConcertsFromDB();

    List<ConcertResponse> concertResponses = concertVOs.stream()
                                                       .map(ConcertResponse::of)
                                                       .collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.OK).body(concertResponses);
  }

  @GetMapping("/api/v1/concert/save/top30/3days")
  public ResponseEntity<Void> saveTop30ConcertsIntoRedis() throws JsonProcessingException {
    concertFacade.saveTop30ConcertsIntoRedis();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/api/v1/concert/top30/3days")
  public ResponseEntity<List<ConcertResponse>> retrieveTop30Concerts() throws JsonProcessingException {
    List<ConcertVO> concertVOs = concertFacade.getTop30Concerts();

    List<ConcertResponse> concertResponses = concertVOs.stream()
                                                       .map(ConcertResponse::of)
                                                       .collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.OK).body(concertResponses);
  }
}
