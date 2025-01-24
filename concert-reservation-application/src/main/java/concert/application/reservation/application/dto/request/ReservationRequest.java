package concert.application.reservation.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    private String uuid;
    private long concertScheduleId;
    private long concertHallSeatId;

    public ReservationRequest(String uuid, long concertScheduleId, long concertHallSeatId){
        this.uuid = uuid;
        this.concertScheduleId = concertScheduleId;
        this.concertHallSeatId = concertHallSeatId;
    }
}
