package concert.interfaces.concert;

import concert.application.concert.business.ConcertScheduleSeatApplicationService;
import concert.interfaces.concert.response.SeatNumbersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertScheduleSeatController {
  private final ConcertScheduleSeatApplicationService concertScheduleSeatApplicationService;

  @GetMapping("/api/v1/concertScheduleSeat/available")
  public ResponseEntity<SeatNumbersResponse> retrieveAvailableConcertScheduleSeats(@RequestParam(value = "concertScheduleId") long concertScheduleId) {
    List<Long> availableConcertScheduleSeatNumbers = concertScheduleSeatApplicationService.getAvailableConcertScheduleSeatNumbers(concertScheduleId);
    SeatNumbersResponse seatsResponse = SeatNumbersResponse.of(availableConcertScheduleSeatNumbers);

    return ResponseEntity.status(HttpStatus.OK).body(seatsResponse);
  }
}
