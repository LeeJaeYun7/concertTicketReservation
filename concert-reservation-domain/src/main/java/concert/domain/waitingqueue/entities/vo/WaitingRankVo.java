package concert.domain.waitingqueue.entities.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitingRankVo {

    private final long waitingRank;
    private final String status;

    @Builder
    public WaitingRankVo(long waitingRank, String status){
        this.waitingRank = waitingRank;
        this.status = status;
    }

    public static WaitingRankVo of(long waitingRank, String status){
        return WaitingRankVo.builder()
                .waitingRank(waitingRank)
                .status(status)
                .build();
    }
}
