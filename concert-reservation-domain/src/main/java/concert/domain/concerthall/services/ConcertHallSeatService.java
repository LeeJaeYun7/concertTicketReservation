package concert.domain.concerthall.services;

import concert.domain.concerthall.entities.ConcertHallSeatEntity;
import concert.domain.concerthall.entities.dao.ConcertHallSeatEntityDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertHallSeatService {

    private final ConcertHallSeatEntityDAO concertHallSeatEntityDAO;

    public List<ConcertHallSeatEntity> getConcertHallSeatsByConcertHallId(long concertHallId){
        return concertHallSeatEntityDAO.findAllByConcertHallId(concertHallId);
    }
}
