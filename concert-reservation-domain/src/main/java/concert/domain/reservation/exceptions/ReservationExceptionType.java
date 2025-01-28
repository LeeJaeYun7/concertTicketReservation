package concert.domain.reservation.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReservationExceptionType {

    RESERVATION_FAILED(HttpStatus.BAD_REQUEST,"결제가 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ReservationExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }


}
