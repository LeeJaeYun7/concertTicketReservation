package concert.interfaces.waitingqueue;

import concert.application.waitingqueue.business.WaitingQueueFacade;
import concert.domain.waitingqueue.entities.vo.TokenVO;
import concert.domain.waitingqueue.entities.vo.WaitingRankVO;
import concert.interfaces.waitingqueue.response.TokenResponse;
import concert.interfaces.waitingqueue.response.WaitingRankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WaitingQueueController {

  private final WaitingQueueFacade waitingQueueFacade;

  @GetMapping("/api/v1/waitingQueue/token")
  public ResponseEntity<TokenResponse> retrieveToken(@RequestParam(value = "concertId") long concertId, @RequestParam(value = "uuid") String uuid) {
    TokenVO tokenVO = waitingQueueFacade.retrieveToken(concertId, uuid);
    TokenResponse tokenResponse = TokenResponse.of(tokenVO.getToken());

    return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
  }

  @GetMapping("/api/v1/waitingQueue/rank")
  public ResponseEntity<WaitingRankResponse> retrieveWaitingRank(@RequestParam(value = "concertId") long concertId, @RequestParam(value = "token") String token) {
    String uuid = token.split(":")[1];
    WaitingRankVO waitingRankVo = waitingQueueFacade.retrieveWaitingRank(concertId, uuid);
    WaitingRankResponse waitingRankResponse = WaitingRankResponse.of(waitingRankVo.getWaitingRank(), waitingRankVo.getStatus());

    return ResponseEntity.status(HttpStatus.CREATED).body(waitingRankResponse);
  }
}
