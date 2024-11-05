package com.example.concert.concertschedule.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertScheduleResponse {

    private final String concertName;
    private final LocalDateTime dateTime;
    private final long price;

    @Builder
    public ConcertScheduleResponse(String concertName, LocalDateTime dateTime, long price){
        this.concertName = concertName;
        this.dateTime = dateTime;
        this.price = price;
    }

    public static ConcertScheduleResponse of(String concertName, LocalDateTime dateTime, long price){
        return ConcertScheduleResponse.builder()
                                      .concertName(concertName)
                                      .dateTime(dateTime)
                                      .price(price)
                                      .build();
    }
}
