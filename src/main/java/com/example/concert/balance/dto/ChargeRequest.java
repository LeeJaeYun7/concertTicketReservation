package com.example.concert.balance.dto;

import lombok.Getter;

@Getter
public class ChargeRequest {

    private final long uuid;
    private final long amount;

    public ChargeRequest(long uuid, long amount){
        this.uuid = uuid;
        this.amount = amount;
    }
}
