package concert.application.reservation.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.reservation.presentation.request.ReservationRequest;
import concert.application.reservation.presentation.response.ReservationResponse;
import concert.application.reservation.business.ReservationFacade;
import concert.domain.reservation.domain.vo.ReservationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationFacade reservationFacade;

  @PostMapping("/api/v1/reservation")
  public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest) throws ExecutionException, InterruptedException, JsonProcessingException {
    String uuid = reservationRequest.getUuid();
    long concertScheduleId = reservationRequest.getConcertScheduleId();
    long concertHallSeatId = reservationRequest.getConcertHallSeatId();

    ReservationVO reservationVO = reservationFacade.createReservation(uuid, concertScheduleId, concertHallSeatId).get();
    ReservationResponse reservationResponse = ReservationResponse.of(reservationVO.getName(), reservationVO.getConcertName(), reservationVO.getDateTime(), reservationVO.getPrice());

    return ResponseEntity.status(HttpStatus.OK).body(reservationResponse);
  }
}
