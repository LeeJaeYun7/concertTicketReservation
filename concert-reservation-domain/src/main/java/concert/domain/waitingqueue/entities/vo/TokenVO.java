package concert.domain.waitingqueue.entities.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenVO {

    private final String token;

    @Builder
    public TokenVO(String token){
        this.token = token;
    }

    public static TokenVO of(String token){
        return TokenVO.builder()
                      .token(token)
                      .build();
    }
}
