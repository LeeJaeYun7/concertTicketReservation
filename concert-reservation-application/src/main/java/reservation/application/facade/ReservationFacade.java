package reservation.application.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.CustomException;
import common.ErrorCode;
import common.Loggable;
import concert.application.ConcertService;
import concert.domain.Concert;
import concertschedule.application.ConcertScheduleService;
import concertschedule.domain.ConcertSchedule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import member.application.MemberService;
import member.domain.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.application.ReservationService;
import reservation.application.event.PaymentConfirmedEvent;
import reservation.application.event.PaymentRequestEvent;
import reservation.domain.Outbox;
import reservation.domain.OutboxRepository;
import reservation.domain.vo.PaymentConfirmedVO;
import reservation.domain.vo.ReservationVO;
import reservation.infrastructure.kafka.KafkaMessageProducer;
import seatinfo.application.SeatInfoService;
import seatinfo.domain.SeatInfo;
import utils.TimeProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationFacade {

    private final TimeProvider timeProvider;
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final SeatInfoService seatInfoService;
    private final ConcertService concertService;
    private final ConcertScheduleService concertScheduleService;
    private final OutboxRepository outboxRepository;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Getter
    private final CompletableFuture<ReservationVO> reservationFuture = new CompletableFuture<>();

    @Transactional
    public CompletableFuture<ReservationVO> createReservation(String uuid, long concertScheduleId, long seatNumber) throws JsonProcessingException {
        SeatInfo seatInfo = seatInfoService.getSeatInfoWithPessimisticLock(concertScheduleId, seatNumber);
        long price = seatInfo.getSeatGrade().getPrice();

        validateSeatReservation(concertScheduleId, seatNumber);
        checkBalanceOverPrice(uuid, price);

        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);

        PaymentRequestEvent event = PaymentRequestEvent.builder()
                                                        .concertId(concertSchedule.getConcert().getId())
                                                        .concertScheduleId(concertSchedule.getId())
                                                        .uuid(uuid)
                                                        .seatNumber(seatNumber)
                                                        .price(price)
                                                        .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String eventJson = objectMapper.writeValueAsString(event);

        Outbox outbox = Outbox.of("reservation", "payment-request-topic", "PaymentRequest", eventJson, false);
        outboxRepository.save(outbox);

        return reservationFuture;
    }

    private void validateSeatReservation(long concertScheduleId, long seatNumber) {
        SeatInfo seatInfo = seatInfoService.getSeatInfo(concertScheduleId, seatNumber);

        if(isFiveMinutesPassed(seatInfo.getUpdatedAt())){
            throw new CustomException(ErrorCode.SEAT_RESERVATION_EXPIRED, Loggable.ALWAYS);
        }
    }

    private void checkBalanceOverPrice(String uuid, long price) {
        long balance = getMember(uuid).getBalance();

        if(balance - price < 0){
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE, Loggable.NEVER);
        }
    }

    public void handlePaymentConfirmed(PaymentConfirmedEvent event){

        long concertId = event.getConcertId();
        long concertScheduleId = event.getConcertScheduleId();
        String uuid = event.getUuid();
        long seatNumber = event.getSeatNumber();
        long price = event.getPrice();

        PaymentConfirmedVO vo = PaymentConfirmedVO.of(concertId, concertScheduleId, uuid, seatNumber, price);

        try {
            reservationService.handlePaymentConfirmed(vo);
        }catch(Exception e){
            kafkaMessageProducer.sendPaymentConfirmedEvent("payment-compensation-topic", event);
            throw new CustomException(ErrorCode.RESERVATION_FAILED, Loggable.ALWAYS);
        }
    }


    private Concert getConcert(long concertScheduleId) {
        ConcertSchedule concertSchedule = getConcertSchedule(concertScheduleId);
        return concertService.getConcertById(concertSchedule.getConcert().getId());
    }

    private ConcertSchedule getConcertSchedule(long concertScheduleId) {
        return concertScheduleService.getConcertScheduleById(concertScheduleId);
    }

    private Member getMember(String uuid) {
        return memberService.getMemberByUuid(uuid);
    }


    private boolean isFiveMinutesPassed(LocalDateTime updatedAt) {
        LocalDateTime now = timeProvider.now();
        Duration duration = Duration.between(updatedAt, now);
        return duration.toMinutes() >= 5;
    }

}
