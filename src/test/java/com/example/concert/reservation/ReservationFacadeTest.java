package com.example.concert.reservation;

import com.example.concert.concert.domain.Concert;
import com.example.concert.concert.fixtures.ConcertFixtureFactory;
import com.example.concert.concert.service.ConcertService;
import com.example.concert.concerthall.domain.ConcertHall;
import com.example.concert.concertschedule.domain.ConcertSchedule;
import com.example.concert.concertschedule.service.ConcertScheduleService;
import com.example.concert.member.domain.Member;
import com.example.concert.member.service.MemberService;
import com.example.concert.reservation.service.ReservationFacade;
import com.example.concert.reservation.service.ReservationService;
import com.example.concert.reservation.vo.ReservationVO;
import com.example.concert.seat.domain.Seat;
import com.example.concert.seat.enums.SeatGrade;
import com.example.concert.seat.enums.SeatStatus;
import com.example.concert.seat.service.SeatService;
import com.example.concert.utils.RandomStringGenerator;
import com.example.concert.utils.TimeProvider;
import com.example.concert.waitingQueue.domain.WaitingQueue;
import com.example.concert.waitingQueue.domain.WaitingQueueStatus;
import com.example.concert.waitingQueue.service.WaitingQueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReservationFacadeTest {

    @Mock
    private TimeProvider timeProvider;
    @Mock
    private MemberService memberService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private SeatService seatService;

    @Mock
    private ConcertService concertService;

    @Mock
    private ConcertScheduleService concertScheduleService;

    @Mock
    private WaitingQueueService waitingQueueService;


    @InjectMocks
    private ReservationFacade sut;

    @Nested
    @DisplayName("예약을 생성할 때")
    class 예약을_생성할때 {
        @Test
        @DisplayName("유효성 검사를 통과하고, 좌석과 대기열의 status를 업데이트한다.")
        void 유효성_검사를_통과하고_좌석과_대기열의_status를_업데이트한다() throws ExecutionException, InterruptedException {

            String token = RandomStringGenerator.generateRandomString(16);

            Member member = Member.of("Tom Cruise");
            String uuid = member.getUuid().toString();
            member.updateBalance(100000);

            long seatNumber = 10;
            ConcertHall concertHall = ConcertHall.of("KSPO DOME", "서울특별시 송파구 올림픽로 424 (방이동 88-2) 올림픽공원", "02-410-1114");

            Concert concert = ConcertFixtureFactory.createConcertWithIdAndName(1L, "박효신 콘서트");
            LocalDateTime dateTime = LocalDateTime.of(2024, 10, 16, 22, 30);
            ConcertSchedule concertSchedule = ConcertSchedule.of(concert, dateTime, 50000);
            long concertScheduleId = 1L;
            WaitingQueue element = WaitingQueue.of(concert, uuid, token, 0);

            Seat seat = Seat.of(concertHall, seatNumber);
            seat.changeUpdatedAt(LocalDateTime.of(2024, 10, 18, 0, 0));

            given(seatService.getSeatByConcertHallIdAndNumber(concertScheduleId, seatNumber)).willReturn(seat);
            given(timeProvider.now()).willReturn(LocalDateTime.of(2024, 10, 18, 0, 3));
            given(memberService.getMemberByUuid(uuid)).willReturn(member);
            given(concertScheduleService.getConcertScheduleById(1L)).willReturn(concertSchedule);
            seat.updateStatus(SeatStatus.RESERVED);
            element.updateWaitingQueueStatus(WaitingQueueStatus.DONE);
            element.updateWaitingNumber();

            given(concertService.getConcertById(1L)).willReturn(concert);
            CompletableFuture<ReservationVO> reservationVO = sut.createReservation(uuid, concertScheduleId, seatNumber);

            assertEquals("Tom Cruise", reservationVO.get().getName());
            assertEquals("박효신 콘서트", reservationVO.get().getConcertName());
            assertEquals(concertSchedule.getDateTime(), reservationVO.get().getDateTime());
            assertEquals(concertSchedule.getPrice(), reservationVO.get().getPrice());
        }
    }
}
