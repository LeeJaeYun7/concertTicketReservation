package concert.application.concertschedule.presentation.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ConcertScheduleCreateRequest {

    private String concertName;
    private LocalDateTime dateTime;

    @Builder
    public ConcertScheduleCreateRequest(String concertName, LocalDateTime dateTime){
        this.concertName = concertName;
        this.dateTime = dateTime;
    }
}
