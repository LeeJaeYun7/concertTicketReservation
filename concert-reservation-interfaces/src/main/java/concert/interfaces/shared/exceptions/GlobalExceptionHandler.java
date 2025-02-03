package concert.interfaces.shared.exceptions;

import concert.domain.concert.exceptions.ConcertException;
import concert.domain.concerthall.exceptions.ConcertHallException;
import concert.domain.member.exceptions.MemberException;
import concert.domain.reservation.exceptions.ReservationException;
import concert.domain.shared.exceptions.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getCustomExceptionType().getHttpStatus(),
                ex.getCustomExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getCustomExceptionType().getHttpStatus()).body(errorResponse);
    }
    @ExceptionHandler(ConcertException.class)
    public ResponseEntity<ErrorResponse> handleConcertException(ConcertException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getConcertExceptionType().getHttpStatus(),
                ex.getConcertExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getConcertExceptionType().getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(ConcertHallException.class)
    public ResponseEntity<ErrorResponse> handleConcertHallException(ConcertHallException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getConcertHallExceptionType().getHttpStatus(),
                ex.getConcertHallExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getConcertHallExceptionType().getHttpStatus()).body(errorResponse);
    }


    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMemberExceptionType().getHttpStatus(),
                ex.getMemberExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getMemberExceptionType().getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleReservationException(ReservationException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getReservationExceptionType().getHttpStatus(),
                ex.getReservationExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getReservationExceptionType().getHttpStatus()).body(errorResponse);
    }
}
