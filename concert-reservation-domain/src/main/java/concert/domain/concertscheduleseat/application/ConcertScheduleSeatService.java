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

  public List<ConcertScheduleSeat> getAllAvailableConcertScheduleSeats(long concertScheduleId) {
    LocalDateTime now = timeProvider.now();
    LocalDateTime threshold = now.minusMinutes(5);

    return concertScheduleSeatRepository.findAllAvailableConcertScheduleSeats(concertScheduleId, SeatStatus.AVAILABLE, threshold);
  }

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
