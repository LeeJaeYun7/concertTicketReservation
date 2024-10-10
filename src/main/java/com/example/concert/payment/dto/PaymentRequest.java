package com.example.concert.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentRequest {

    private final long uuid;
    private final long amount;

    @Builder
    public PaymentRequest(long uuid, long amount){
        this.uuid = uuid;
        this.amount = amount;
    }
}
