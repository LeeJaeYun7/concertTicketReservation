package com.example.concert.reservation.dto;

import lombok.Builder;

public class ReservationResponse {

    private final boolean isSuccess;

    @Builder
    public ReservationResponse(boolean isSuccess){
        this.isSuccess = isSuccess;
    }

    public static ReservationResponse of(boolean isSuccess){
        return ReservationResponse.builder()
                                  .isSuccess(isSuccess)
                                  .build();
    }
}
