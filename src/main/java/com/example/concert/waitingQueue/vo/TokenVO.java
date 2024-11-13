package com.example.concert.waitingQueue.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenVO {

    private final long waitingNumber;

    @Builder
    public TokenVO(long waitingNumber){
        this.waitingNumber = waitingNumber;
    }

    public static TokenVO of(long waitingNumber){
        return TokenVO.builder()
                .waitingNumber(waitingNumber)
                .build();
    }
}