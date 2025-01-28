package concert.domain.concerthall.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ConcertHallExceptionType {
    ;

    private final HttpStatus httpStatus;
    private final String message;

    ConcertHallExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
