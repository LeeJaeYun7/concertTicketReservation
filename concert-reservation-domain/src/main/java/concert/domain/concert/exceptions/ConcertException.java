package concert.domain.concert.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConcertException extends RuntimeException{
    private final ConcertExceptionType concertExceptionType;
}
