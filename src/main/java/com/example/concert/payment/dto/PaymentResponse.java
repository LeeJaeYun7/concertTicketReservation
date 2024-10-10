package com.example.concert.payment.dto;

import lombok.Builder;

public class PaymentResponse {

    private final boolean isSuccess;

    @Builder
    public PaymentResponse(boolean isSuccess){
        this.isSuccess = isSuccess;
    }

    public static PaymentResponse of(boolean isSuccess){
        return PaymentResponse.builder()
                              .isSuccess(isSuccess)
                              .build();
    }
}
