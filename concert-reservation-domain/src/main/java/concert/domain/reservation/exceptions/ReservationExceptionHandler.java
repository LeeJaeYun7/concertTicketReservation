package concert.domain.reservation.exceptions;

import concert.commons.common.ErrorResponse;
import concert.domain.concerthall.exceptions.ConcertHallException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleReservationException(ReservationException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getReservationExceptionType().getHttpStatus(),
                ex.getReservationExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getReservationExceptionType().getHttpStatus()).body(errorResponse);
    }
}