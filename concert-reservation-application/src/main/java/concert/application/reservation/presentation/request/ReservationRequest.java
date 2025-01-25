package concert.application.reservation.presentation.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

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
