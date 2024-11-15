package com.example.concert.reservation.event;

public class PaymentRequestEvent {
    private long concertId;
    private long concertScheduleId;
    private String uuid;
    private long seatNumber;
    private long price;

    public PaymentRequestEvent(long concertId, long concertScheduleId, String uuid, long seatNumber, long price){
        this.concertId = concertId;
        this.concertScheduleId = concertScheduleId;
        this.uuid = uuid;
        this.seatNumber = seatNumber;
        this.price = price;
    }
}
