package concert.domain.concertschedule.application;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.concertscheduleseat.application.ConcertScheduleSeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertScheduleService {

  private final TimeProvider timeProvider;
  private final ConcertScheduleSeatService concertScheduleSeatService;
  private final ConcertScheduleRepository concertScheduleRepository;

  public void createConcertSchedule(Concert concert, LocalDateTime dateTime) {
    ConcertSchedule concertSchedule = ConcertSchedule.of(concert.getId(), dateTime);
    concertScheduleRepository.save(concertSchedule);
  }

  public List<LocalDateTime> getAllAvailableDateTimes(long concertId) {
    LocalDateTime now = timeProvider.now();
    List<ConcertSchedule> allConcertSchedules = concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);

    for(ConcertSchedule concertSchedule: allConcertSchedules){
      System.out.println(concertSchedule.getDateTime());
    }

    return allConcertSchedules.stream()
            .filter(concertSchedule -> {
              long concertScheduleId = concertSchedule.getId();
              System.out.println("concertScheduleId는?");
              System.out.println(concertScheduleId);
              System.out.println(concertScheduleSeatService.getAllAvailableConcertScheduleSeats(concertScheduleId).isEmpty());
              return !concertScheduleSeatService.getAllAvailableConcertScheduleSeats(concertScheduleId).isEmpty();
            })
            .map(ConcertSchedule::getDateTime)
            .collect(Collectors.toList());
  }

  public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
    return concertScheduleRepository.findById(concertScheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
  }
}
