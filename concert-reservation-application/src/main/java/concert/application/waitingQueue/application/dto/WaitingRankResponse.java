package concert.application.waitingQueue.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitingRankResponse {

    private final long waitingRank;
    private final String status;

    @Builder
    public WaitingRankResponse(long waitingRank, String status){
        this.waitingRank = waitingRank;
        this.status = status;
    }

    public static WaitingRankResponse of(long waitingRank, String status){
        return WaitingRankResponse.builder()
                .waitingRank(waitingRank)
                .status(status)
                .build();
    }
}
