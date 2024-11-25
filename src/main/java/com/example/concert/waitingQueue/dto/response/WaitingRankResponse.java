package com.example.concert.waitingQueue.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitingRankResponse {

    private final long waitingRank;

    @Builder
    public WaitingRankResponse(long waitingRank){
        this.waitingRank = waitingRank;
    }

    public static WaitingRankResponse of(long waitingRank){
        return WaitingRankResponse.builder()
                            .waitingRank(waitingRank)
                            .build();
    }
}
