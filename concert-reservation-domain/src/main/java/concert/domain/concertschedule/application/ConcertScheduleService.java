package concert.domain.concertschedule.application;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.commons.utils.TimeProvider;
import concert.domain.concert.domain.Concert;
import concert.domain.concertschedule.domain.ConcertSchedule;
import concert.domain.concertschedule.domain.ConcertScheduleRepository;
import concert.domain.seatinfo.application.SeatInfoService;
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
  private final SeatInfoService seatInfoService;
  private final ConcertScheduleRepository concertScheduleRepository;

  public void createConcertSchedule(Concert concert, LocalDateTime dateTime) {
    ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime);
    concertScheduleRepository.save(concertSchedule);
  }

  public List<LocalDateTime> getAllAvailableDateTimes(long concertId) {
    LocalDateTime now = timeProvider.now();
    List<ConcertSchedule> allConcertSchedules = concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);

    return allConcertSchedules.stream()
            .filter(concertSchedule -> {
              long concertScheduleId = concertSchedule.getId();
              return !seatInfoService.getAllAvailableSeats(concertScheduleId).isEmpty();
            })
            .map(ConcertSchedule::getDateTime)
            .collect(Collectors.toList());
  }

  public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
    return concertScheduleRepository.findById(concertScheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
  }
}
