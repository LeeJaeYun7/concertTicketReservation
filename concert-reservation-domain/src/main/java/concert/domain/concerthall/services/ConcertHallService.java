package concert.domain.concerthall.services;

import concert.domain.concerthall.entities.ConcertHallEntity;
import concert.domain.concerthall.entities.dao.ConcertHallEntityDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertHallService {

  private final ConcertHallEntityDAO concertHallEntityDAO;

  public Map<Long, String> getConcertHallNamesByIds(List<Long> concertHallIds) {
    List<ConcertHallEntity> concertHallEntities = concertHallEntityDAO.findAllById(concertHallIds);

    return concertHallEntities.stream()
                       .collect(Collectors.toMap(ConcertHallEntity::getId, ConcertHallEntity::getName));
  }
}
