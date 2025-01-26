package concert.domain.concertscheduleseat.application;

import concert.commons.common.CustomException;
import concert.commons.common.ErrorCode;
import concert.commons.common.Loggable;
import concert.commons.lock.DistributedLock;
import concert.commons.utils.TimeProvider;
import concert.domain.concerthallseat.domain.ConcertHallSeat;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeatRepository;
import concert.domain.concertscheduleseat.domain.ConcertScheduleSeat;
import concert.domain.concertscheduleseat.domain.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ConcertScheduleSeatService {

  private final TimeProvider timeProvider;
  private final ConcertScheduleSeatRepository concertScheduleSeatRepository;

  // 특정 콘서트 일정에 대하여 모든 예약 가능한 콘서트 일정 좌석을 반환합니다.
  // 어떤 사용자가 특정 콘서트 일정 좌석에 대해 선점 예약을 하면, 선점 시작 시각이 updatedAt에 업데이트됩니다.
  // 그리고 그 시각부터 5분동안 해당 콘서트 일정 좌석을 선점합니다.
  // 따라서 updatedAt이 5분이 지나지 않은 경우, 다른 사용자가 선점 예약 중인 좌석으로 판단하여서
  // 예약 가능한 콘서트 일정 좌석에서 제외하였습니다.
  public List<ConcertScheduleSeat> getAllAvailableConcertScheduleSeats(long concertScheduleId) {
    LocalDateTime now = timeProvider.now();
    LocalDateTime threshold = now.minusMinutes(5);

    return concertScheduleSeatRepository.findAllAvailableConcertScheduleSeats(concertScheduleId, SeatStatus.AVAILABLE, threshold);
  }

  // 특정 콘서트 일정에 대해서 모든 예약 가능한 좌석 번호를 반환합니다.
  // 특정 콘서트 일정에 대한 콘서트 홀 좌석에 대하여
  // 해당 좌석이 예약 가능하다면, 해당 콘서트 홀 좌석 번호를 반환합니다.
  public List<Long> getAllAvailableConcertScheduleSeatNumbers(long concertScheduleId, List<ConcertHallSeat> concertHallSeats) {
    List<ConcertScheduleSeat> availableConcertScheduleSeats = getAllAvailableConcertScheduleSeats(concertScheduleId);
    Set<Long> availableConcertHallSeatIds = new HashSet<>();

    for (ConcertScheduleSeat seat : availableConcertScheduleSeats) {
      long concertHallSeatId = seat.getConcertHallSeatId();
      availableConcertHallSeatIds.add(concertHallSeatId);
    }

    List<Long> availableConcertScheduleSeatNumbers = new ArrayList<>();

    for(ConcertHallSeat concertHallSeat: concertHallSeats){
      long concertHallSeatId = concertHallSeat.getId();
      if(availableConcertHallSeatIds.contains(concertHallSeatId)){
        availableConcertScheduleSeatNumbers.add(concertHallSeat.getNumber());
      }
    }

    return availableConcertScheduleSeatNumbers;
  }

  public void changeUpdatedAtWithDistributedLock(long concertHallId, long number) {
    String lockName = "SEAT_RESERVATION:" + concertHallId + ":" + number;

    ConcertScheduleSeat concertScheduleSeat = getConcertScheduleSeatWithDistributedLock(concertHallId, number);
    LocalDateTime now = timeProvider.now();
    concertScheduleSeat.changeUpdatedAt(now);
  }


  public void updateSeatStatus(long concertHallId, long number, SeatStatus status) {
    ConcertScheduleSeat concertScheduleSeat = getConcertScheduleSeatWithDistributedLock(concertHallId, number);
    concertScheduleSeat.updateStatus(status);
  }

  public ConcertScheduleSeat getConcertScheduleSeat(long concertScheduleId, long concertHallSeatId) {
    return concertScheduleSeatRepository.findConcertScheduleSeat(concertScheduleId, concertHallSeatId)
            .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
  }


  @DistributedLock(key = "#concertHallId + '_' + #number", waitTime = 60, leaseTime = 300000, timeUnit = TimeUnit.MILLISECONDS)
  public ConcertScheduleSeat getConcertScheduleSeatWithDistributedLock(long concertScheduleId, long concertHallSeatNumber) {
    return concertScheduleSeatRepository.findConcertScheduleSeatWithDistributedLock(concertScheduleId, concertHallSeatNumber)
            .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, Loggable.ALWAYS));
  }
}
