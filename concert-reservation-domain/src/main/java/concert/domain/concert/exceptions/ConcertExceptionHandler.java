package concert.domain.concert.exceptions;

import concert.commons.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConcertExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(concert.commons.common.GlobalExceptionHandler.class);

    @ExceptionHandler(ConcertException.class)
    public ResponseEntity<ErrorResponse> handleConcertException(ConcertException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getConcertExceptionType().getHttpStatus(),
                ex.getConcertExceptionType().getMessage()
        );

        return ResponseEntity.status(ex.getConcertExceptionType().getHttpStatus()).body(errorResponse);
    }
}
