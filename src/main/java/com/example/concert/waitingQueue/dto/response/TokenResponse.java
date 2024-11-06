package com.example.concert.waitingQueue.dto.response;

import lombok.Builder;
import lombok.Getter;
@Getter
public class TokenResponse {

    private final long waitingNumber;
    @Builder
    public TokenResponse(long waitingNumber){
        this.waitingNumber = waitingNumber;
    }

    public static TokenResponse of(long waitingNumber){
        return TokenResponse.builder()
                            .waitingNumber(waitingNumber)
                            .build();
    }
}
