package concert.application.concert.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.concert.presentation.response.ConcertResponse;
import concert.application.concert.business.ConcertFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertController {

  private final ConcertFacade concertFacade;

  @GetMapping("/api/v1/concert/top30/3days/db")
  public ResponseEntity<List<ConcertResponse>> retrieveAllConcertsFromDB() throws JsonProcessingException {
    List<ConcertResponse> concertResponses = concertFacade.getTop30ConcertsFromDB();
    return ResponseEntity.status(HttpStatus.OK).body(concertResponses);
  }

  @GetMapping("/api/v1/concert/save/top30/3days")
  public ResponseEntity<Void> saveTop30ConcertsIntoRedis() throws JsonProcessingException {
    concertFacade.saveTop30ConcertsIntoRedis();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/api/v1/concert/top30/3days")
  public ResponseEntity<List<ConcertResponse>> retrieveTop30Concerts() throws JsonProcessingException {
    List<ConcertResponse> concertResponseList = concertFacade.getTop30Concerts();
    return ResponseEntity.status(HttpStatus.OK).body(concertResponseList);
  }
}
