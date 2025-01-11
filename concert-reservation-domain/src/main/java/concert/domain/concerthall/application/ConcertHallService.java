package concert.domain.concerthall.application;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.domain.concerthall.domain.ConcertHall;
import concert.domain.concerthall.domain.ConcertHallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertHallService {

  private final ConcertHallRepository concertHallRepository;

  public ConcertHall getConcertHallById(long concertHallId) {
    return concertHallRepository.findById(concertHallId).orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND, Loggable.ALWAYS));
  }
}
