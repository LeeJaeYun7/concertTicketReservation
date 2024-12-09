package com.example.concert.seatinfo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SeatInfoRequest {

    private String uuid;
    private long concertScheduleId;
    private long seatNumber;

    public SeatInfoRequest(String uuid, long concertScheduleId, long seatNumber){
        this.uuid = uuid;
        this.concertScheduleId = concertScheduleId;
        this.seatNumber = seatNumber;
    }
}
