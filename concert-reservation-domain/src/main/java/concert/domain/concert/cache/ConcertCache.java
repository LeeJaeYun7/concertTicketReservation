package concert.domain.concert.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import concert.domain.concert.entities.ConcertEntity;

import java.util.List;

public interface ConcertCache {
    void saveTop30Concerts(List<ConcertEntity> concerts) throws JsonProcessingException;
    List<ConcertEntity> findTop30Concerts() throws JsonProcessingException;
}
