package com.example.concert.balance.dto;

import lombok.Builder;

public class ChargeResponse {

    private final boolean isSuccess;

    @Builder
    public ChargeResponse(boolean isSuccess){
        this.isSuccess = isSuccess;
    }

    public static ChargeResponse of(boolean isSuccess){
        return ChargeResponse.builder()
                             .isSuccess(isSuccess)
                             .build();
    }
}
