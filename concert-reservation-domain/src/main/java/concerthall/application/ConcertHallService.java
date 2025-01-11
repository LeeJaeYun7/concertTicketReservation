package concerthall.application;

import common.CustomException;
import common.ErrorCode;
import common.Loggable;
import concerthall.domain.ConcertHall;
import concerthall.domain.ConcertHallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertHallService {

    private final ConcertHallRepository concertHallRepository;
    public ConcertHall getConcertHallById(long concertHallId){
        return concertHallRepository.findById(concertHallId).orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND, Loggable.ALWAYS));
    }
}
