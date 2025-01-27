package concert.application.concertschedule.business;

import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.services.ConcertScheduleService;
import concert.domain.concert.services.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConcertScheduleFacade {

  private final ConcertService concertService;
  private final ConcertScheduleService concertScheduleService;

  public void createConcertSchedule(String concertName, LocalDateTime dateTime) {
    ConcertEntity concert = concertService.getConcertByName(concertName);
    concertScheduleService.createConcertSchedule(concert, dateTime);
  }

  public List<LocalDateTime> getAvailableDateTimes(long concertId) {
    concertService.getConcertById(concertId);
    return concertScheduleService.getAllAvailableDateTimes(concertId);
  }
}
