package com.example.concert.concertschedule.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ConcertScheduleCreateRequest {

    private String concertName;
    private LocalDateTime dateTime;
    private long price;

    @Builder
    public ConcertScheduleCreateRequest(String concertName, LocalDateTime dateTime, long price){
        this.concertName = concertName;
        this.dateTime = dateTime;
        this.price = price;
    }

}
