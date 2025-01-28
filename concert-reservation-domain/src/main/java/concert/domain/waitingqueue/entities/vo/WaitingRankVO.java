package concert.domain.waitingqueue.entities.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitingRankVO {

    private final long waitingRank;
    private final String status;

    @Builder
    public WaitingRankVO(long waitingRank, String status){
        this.waitingRank = waitingRank;
        this.status = status;
    }

    public static WaitingRankVO of(long waitingRank, String status){
        return WaitingRankVO.builder()
                .waitingRank(waitingRank)
                .status(status)
                .build();
    }
}
