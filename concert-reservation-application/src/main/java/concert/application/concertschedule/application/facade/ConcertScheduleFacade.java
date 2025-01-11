package concert.application.concertschedule.application.facade;

import concert.domain.concert.application.ConcertService;
import concert.domain.concert.domain.Concert;
import concert.domain.concertschedule.application.ConcertScheduleService;
import concert.domain.seatinfo.application.SeatInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConcertScheduleFacade {

  private final ConcertService concertService;
  private final ConcertScheduleService concertScheduleService;
  private final SeatInfoService seatInfoService;

  public void createConcertSchedule(String concertName, LocalDateTime dateTime) {
    Concert concert = concertService.getConcertByName(concertName);
    concertScheduleService.createConcertSchedule(concert, dateTime);
  }

  public List<LocalDateTime> getAvailableDateTimes(long concertId) {
    concertService.getConcertById(concertId);

    return concertScheduleService.getAllAvailableDateTimes(concertId);
  }

  public List<Long> getAvailableSeatNumbers(long concertScheduleId) {
    concertScheduleService.getConcertScheduleById(concertScheduleId);

    return seatInfoService.getAllAvailableSeatNumbers(concertScheduleId);
  }
}
