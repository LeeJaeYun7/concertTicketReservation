package reservation.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reservation.application.facade.ReservationFacade;
import reservation.application.dto.request.ReservationRequest;
import reservation.application.dto.response.ReservationResponse;
import reservation.domain.vo.ReservationVO;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @PostMapping("/api/v1/reservation")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest) throws ExecutionException, InterruptedException, JsonProcessingException {
        String uuid = reservationRequest.getUuid();
        long concertScheduleId = reservationRequest.getConcertScheduleId();
        long seatNumber = reservationRequest.getSeatNumber();

        ReservationVO reservationVO = reservationFacade.createReservation(uuid, concertScheduleId, seatNumber).get();
        ReservationResponse reservationResponse = ReservationResponse.of(reservationVO.getName(), reservationVO.getConcertName(), reservationVO.getDateTime(), reservationVO.getPrice());

        return ResponseEntity.status(HttpStatus.OK).body(reservationResponse);
    }
}
