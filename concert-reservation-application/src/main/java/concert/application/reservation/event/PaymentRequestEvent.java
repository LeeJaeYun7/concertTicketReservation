package concert.application.reservation.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestEvent {

    private long concertId;
    private long concertScheduleId;
    private String uuid;
    private long price;

    @Builder
    public PaymentRequestEvent(long concertId, long concertScheduleId, String uuid, long price){
        this.concertId = concertId;
        this.concertScheduleId = concertScheduleId;
        this.uuid = uuid;
        this.price = price;
    }

    public static PaymentRequestEvent of(long concertId, long concertScheduleId, String uuid, long price){
        return PaymentRequestEvent.builder()
                                  .concertId(concertId)
                                  .concertScheduleId(concertScheduleId)
                                  .uuid(uuid)
                                  .price(price)
                                  .build();
    }
}
