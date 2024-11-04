package com.example.concert.concertschedule.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AvailableDateTimesResponse {

    private final List<LocalDateTime> availableDateTimes;

    @Builder
    public AvailableDateTimesResponse(List<LocalDateTime> availableDateTimes){
        this.availableDateTimes = availableDateTimes;
    }

    public static AvailableDateTimesResponse of(List<LocalDateTime> availableDateTimes){
        return AvailableDateTimesResponse.builder()
                                      .availableDateTimes(availableDateTimes)
                                      .build();
    }
}
