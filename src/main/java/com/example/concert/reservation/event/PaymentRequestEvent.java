package com.example.concert.reservation.event;

import lombok.*;

@Getter
@NoArgsConstructor
public class PaymentRequestEvent {

    private long concertId;
    private long concertScheduleId;
    private String uuid;
    private long seatNumber;
    private long price;

    @Builder
    public PaymentRequestEvent(long concertId, long concertScheduleId, String uuid, long seatNumber, long price){
        this.concertId = concertId;
        this.concertScheduleId = concertScheduleId;
        this.uuid = uuid;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public static PaymentRequestEvent of(long concertId, long concertScheduleId, String uuid, long seatNumber, long price){
        return PaymentRequestEvent.builder()
                                  .concertId(concertId)
                                  .concertScheduleId(concertScheduleId)
                                  .uuid(uuid)
                                  .seatNumber(seatNumber)
                                  .price(price)
                                  .build();
    }
}
