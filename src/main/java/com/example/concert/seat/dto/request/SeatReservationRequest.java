package com.example.concert.seat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatReservationRequest {

    private String uuid;
    private long concertHallId;
    private long number;

    @Builder
    public SeatReservationRequest(String uuid, long concertHallId, long number){
        this.uuid = uuid;
        this.concertHallId = concertHallId;
        this.number = number;
    }
}
