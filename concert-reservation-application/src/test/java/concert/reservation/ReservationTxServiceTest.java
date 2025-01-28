package concert.reservation;

import concert.domain.concert.entities.ConcertEntity;
import concert.domain.concert.entities.ConcertScheduleEntity;
import concert.domain.concert.entities.ConcertScheduleSeatEntity;
import concert.domain.concert.entities.ConcertSeatGradeEntity;
import concert.domain.concert.entities.enums.ConcertAgeRestriction;
import concert.domain.concert.entities.enums.ConcertScheduleSeatStatus;
import concert.domain.concert.entities.enums.SeatGrade;
import concert.domain.concerthall.entities.ConcertHallEntity;
import concert.domain.concerthall.entities.ConcertHallSeatEntity;
import concert.domain.reservation.txservices.ReservationTxService;
import concert.domain.reservation.entities.Reservation;
import concert.domain.reservation.entities.dao.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationTxServiceTest {

  @Mock
  private ReservationRepository reservationRepository;

  @InjectMocks
  private ReservationTxService sut;

  @Nested
  @DisplayName("예약을 생성할 때")
  class 예약을_생성할때 {
    @Test
    @DisplayName("ConcertSchedule, uuid, Seat, price가 전달될 때, 예약이 생성된다")
    void ConcertSchedule_uuid_Seat_price가_전달될때_예약이_생성된다() {
      LocalDate startAt = LocalDate.of(2024, 10, 16);
      LocalDate endAt = LocalDate.of(2024, 10, 18);


      ConcertHallEntity concertHallEntity = ConcertHallEntity.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114", null);
      ConcertEntity concert = ConcertEntity.of("박효신 콘서트", concertHallEntity.getId(), "ballad", 120, ConcertAgeRestriction.OVER_15, startAt, endAt);

      LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
      ConcertScheduleEntity concertSchedule = ConcertScheduleEntity.of(concert.getId(), dateTime);

      String uuid = UUID.randomUUID().toString();
      ConcertHallSeatEntity seat = ConcertHallSeatEntity.of(concertHallEntity.getId(), 1);
      ConcertSeatGradeEntity seatGrade = ConcertSeatGradeEntity.of(concert.getId(), SeatGrade.ALL, 100000);
      ConcertScheduleSeatEntity concertScheduleSeat = ConcertScheduleSeatEntity.of(seat.getId(), concertSchedule.getId(), seatGrade.getId(), ConcertScheduleSeatStatus.AVAILABLE);

      Reservation reservation = Reservation.of(concertSchedule.getConcertId(), concertSchedule.getId(), uuid, concertScheduleSeat.getId(), 50000);

      given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

      sut.createReservation(concertSchedule.getConcertId(), concertSchedule.getId(), uuid, concertScheduleSeat.getId(), 50000);

      verify(reservationRepository, times(1)).save(any(Reservation.class));
    }
  }
}
