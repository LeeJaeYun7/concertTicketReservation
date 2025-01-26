package concert.domain.concerthallseat.application;

import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concerthallseat.domain.ConcertHallSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertHallSeatService {

    private final ConcertHallSeatRepository concertHallSeatRepository;

    public List<ConcertHallSeat> getConcertHallSeatsByConcertHallId(long concertHallId){
        return concertHallSeatRepository.findAllByConcertHallId(concertHallId);
    }
}
