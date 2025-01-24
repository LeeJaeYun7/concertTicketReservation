package concert.application.concertschedule.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.concertschedule.application.dto.request.ConcertScheduleCreateRequest;
import concert.application.concertschedule.application.dto.response.AvailableDateTimesResponse;
import concert.application.concertschedule.application.facade.ConcertScheduleFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertScheduleController {

  private final ConcertScheduleFacade concertScheduleFacade;

  @PostMapping("/api/v1/concertSchedule")
  public ResponseEntity<Void> createConcertSchedule(@RequestBody ConcertScheduleCreateRequest concertScheduleCreateRequest) throws JsonProcessingException {
    String concertName = concertScheduleCreateRequest.getConcertName();
    LocalDateTime dateTime = concertScheduleCreateRequest.getDateTime();

    concertScheduleFacade.createConcertSchedule(concertName, dateTime);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/api/v1/concertSchedule")
  public ResponseEntity<AvailableDateTimesResponse> retrieveAvailableDateTimes(@RequestParam(value = "concertId") long concertId) {
    List<LocalDateTime> availableDateTimes = concertScheduleFacade.getAvailableDateTimes(concertId);
    AvailableDateTimesResponse availableDateTimesResponse = AvailableDateTimesResponse.of(availableDateTimes);

    return ResponseEntity.status(HttpStatus.OK).body(availableDateTimesResponse);
  }
}
