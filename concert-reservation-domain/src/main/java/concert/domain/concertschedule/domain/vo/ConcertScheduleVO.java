package concert.domain.concertschedule.domain.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ConcertScheduleVO {
    private String concertName;
    private LocalDateTime dateTime;
    private long price;

    @Builder
    public ConcertScheduleVO(String concertName, LocalDateTime dateTime, long price) {
        this.concertName = concertName;
        this.dateTime = dateTime;
        this.price = price;
    }

    public static ConcertScheduleVO of(String concertName, LocalDateTime dateTime, long price){
        return ConcertScheduleVO.builder()
                .concertName(concertName)
                .dateTime(dateTime)
                .price(price)
                .build();
    }
}
