package com.example.concert.waitingQueue.dto.response;

import lombok.Builder;
import lombok.Getter;
@Getter
public class TokenResponse {

    private final long waitingRank;
    private final String token;

    @Builder
    public TokenResponse(long waitingRank, String token){
        this.waitingRank = waitingRank;
        this.token = token;
    }

    public static TokenResponse of(long waitingRank, String token){
        return TokenResponse.builder()
                            .waitingRank(waitingRank)
                            .token(token)
                            .build();
    }
}
