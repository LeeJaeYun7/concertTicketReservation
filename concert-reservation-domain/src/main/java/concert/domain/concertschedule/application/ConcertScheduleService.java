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

  // getAllAvailableDateTimes는 특정 콘서트에 대해 모든 예약 가능한 일정을 반환하는 메소드입니다.
  // 콘서트 일정의 좌석을 조사해서, 예약 가능한 좌석이 1개 이상 존재한다면, 해당 일정을 리턴합니다.
  public List<LocalDateTime> getAllAvailableDateTimes(long concertId) {
    LocalDateTime now = timeProvider.now();

    // 특정 콘서트에 대하여 현재 시각 이후의 모든 콘서트 일정을 찾습니다.
    List<ConcertSchedule> allConcertSchedules = concertScheduleRepository.findAllAfterNowByConcertId(concertId, now);

    // 해당 콘서트 일정이 예약 가능한 좌석이 1개 이상 있는지 확인합니다.
    return allConcertSchedules.stream()
            .filter(concertSchedule -> {
              long concertScheduleId = concertSchedule.getId();

              // 콘서트 일정의 좌석을 조사해서, 해당 콘서트 일정의 좌석이 1개 이상 있다면 true를 리턴합니다.
              return !concertScheduleSeatService.getAllAvailableConcertScheduleSeats(concertScheduleId).isEmpty();
            })
            // 해당 콘서트 일정의 날짜와 시간을 반환합니다.
            .map(ConcertSchedule::getDateTime)
            .collect(Collectors.toList());
  }

  public ConcertSchedule getConcertScheduleById(long concertScheduleId) {
    return concertScheduleRepository.findById(concertScheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, Loggable.ALWAYS));
  }
}
