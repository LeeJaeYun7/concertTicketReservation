package concertschedule.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatNumbersRequest {

    private String token;
    private long concertScheduleId;

    public SeatNumbersRequest(String token, long concertScheduleId){
        this.token = token;
        this.concertScheduleId = concertScheduleId;
    }

}
