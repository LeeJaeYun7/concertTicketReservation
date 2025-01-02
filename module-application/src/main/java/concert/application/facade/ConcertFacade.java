package concert.application.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.application.ConcertService;
import concert.application.dto.ConcertResponse;
import concert.domain.Concert;
import concert.domain.vo.ConcertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConcertFacade {

    private final ConcertService concertService;
    public List<ConcertResponse> getTop30ConcertsFromDB() {

        List<Concert> concerts = concertService.getTop30ConcertsFromDB();

        return concerts.stream()
                .map(ConcertVO::of)
                .map(ConcertResponse::of)
                .collect(Collectors.toList());
    }

    public void saveTop30ConcertsIntoRedis() throws JsonProcessingException {
        concertService.saveTop30ConcertsIntoRedis();
    }

    public List<ConcertResponse> getTop30Concerts() throws JsonProcessingException {
        List<Concert> concerts = concertService.getTop30Concerts();

        return concerts.stream().map(ConcertVO::of)
                .map(ConcertResponse::of)
                .toList();
    }
}
