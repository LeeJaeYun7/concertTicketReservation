package concert.domain.order.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PaymentOrderConfirmedCommand {

    private long concertId;
    private long concertScheduleId;
    private String uuid;
    private List<Long> concertScheduleSeatIds;
    private long totalPrice;

    @Builder
    public PaymentOrderConfirmedCommand(long concertId, long concertScheduleId, String uuid, List<Long> concertScheduleSeatIds, long totalPrice){
        this.concertId = concertId;
        this.concertScheduleId = concertScheduleId;
        this.uuid = uuid;
        this.concertScheduleSeatIds = concertScheduleSeatIds;
        this.totalPrice = totalPrice;
    }

    public static PaymentOrderConfirmedCommand of(long concertId, long concertScheduleId, String uuid, List<Long> concertScheduleSeatIds, long totalPrice){
        return PaymentOrderConfirmedCommand.builder()
                                           .concertId(concertId)
                                           .concertScheduleId(concertScheduleId)
                                           .uuid(uuid)
                                           .concertScheduleSeatIds(concertScheduleSeatIds)
                                           .totalPrice(totalPrice)
                                           .build();
    }
}
