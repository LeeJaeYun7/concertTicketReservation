package concertschedule.application;

import common.CustomException;
import common.ErrorCode;
import common.Loggable;
import concert.domain.Concert;
import concertschedule.domain.ConcertSchedule;
import concertschedule.domain.ConcertScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seatinfo.application.SeatInfoService;
import utils.TimeProvider;

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

    public void createConcertSchedule(Concert concert, LocalDateTime dateTime)  {
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
