package concertschedule.application.facade;

import concert.application.ConcertService;
import concert.domain.Concert;
import concertschedule.application.ConcertScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seatinfo.application.SeatInfoService;

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
