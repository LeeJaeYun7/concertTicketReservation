package com.example.concert.concertschedule.vo;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertScheduleVO {
    private final String concertName;
    private final LocalDateTime dateTime;
    private final long price;

    public ConcertScheduleVO(String concertName, LocalDateTime dateTime, long price) {
        this.concertName = concertName;
        this.dateTime = dateTime;
        this.price = price;
    }
}
