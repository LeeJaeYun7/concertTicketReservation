package concert.application.concertschedule.business;

import concert.domain.concert.application.ConcertService;
import concert.domain.concert.domain.Concert;
import concert.domain.concertschedule.application.ConcertScheduleService;
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
    Concert concert = concertService.getConcertByName(concertName);
    concertScheduleService.createConcertSchedule(concert, dateTime);
  }

  public List<LocalDateTime> getAvailableDateTimes(long concertId) {
    concertService.getConcertById(concertId);
    return concertScheduleService.getAllAvailableDateTimes(concertId);
  }
}
