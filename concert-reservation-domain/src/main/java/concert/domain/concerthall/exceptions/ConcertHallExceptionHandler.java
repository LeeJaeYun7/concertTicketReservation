package concert.domain.concerthall.exceptions;

import concert.commons.common.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConcertHallExceptionHandler {

    @ExceptionHandler(ConcertHallException.class)
    public ResponseEntity<ErrorResponse> handleConcertHallException(ConcertHallException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getConcertHallExceptionType().getHttpStatus(),
                ex.getConcertHallExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getConcertHallExceptionType().getHttpStatus()).body(errorResponse);
    }
}
