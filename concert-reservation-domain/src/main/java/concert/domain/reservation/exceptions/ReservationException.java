package concert.domain.reservation.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationException extends Throwable {
    private final ReservationExceptionType reservationExceptionType;
}
