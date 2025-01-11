package concert.domain.reservation.domain.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmedVO {

    private long concertId;
    private long concertScheduleId;
    private String uuid;
    private long seatNumber;
    private long price;

    @Builder
    public PaymentConfirmedVO(long concertId, long concertScheduleId, String uuid, long seatNumber, long price){
        this.concertId = concertId;
        this.concertScheduleId = concertScheduleId;
        this.uuid = uuid;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public static PaymentConfirmedVO of(long concertId, long concertScheduleId, String uuid, long seatNumber, long price){
        return PaymentConfirmedVO.builder()
                .concertId(concertId)
                .concertScheduleId(concertScheduleId)
                .uuid(uuid)
                .seatNumber(seatNumber)
                .price(price)
                .build();
    }
}
