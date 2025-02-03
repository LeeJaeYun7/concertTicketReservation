package concert.domain.concerthall.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public class ConcertHallException extends RuntimeException{
    private final ConcertHallExceptionType concertHallExceptionType;
}
