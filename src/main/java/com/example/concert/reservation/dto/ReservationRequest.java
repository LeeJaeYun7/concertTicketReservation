package com.example.concert.reservation.dto;

import lombok.Getter;

@Getter
public class ReservationRequest {

    private final long uuid;
    private final long seatId;
    private final long price;

    public ReservationRequest(long uuid, long seatId, long price){
        this.uuid = uuid;
        this.seatId = seatId;
        this.price = price;
    }
}
