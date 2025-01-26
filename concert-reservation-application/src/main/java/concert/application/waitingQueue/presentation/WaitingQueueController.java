package concert.application.waitingQueue.presentation;

import concert.application.waitingQueue.business.WaitingQueueFacade;
import concert.application.waitingQueue.presentation.response.TokenResponse;
import concert.domain.waitingQueue.vo.WaitingRankVo;
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
    TokenResponse tokenResponse = waitingQueueFacade.retrieveToken(concertId, uuid);

    return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
  }

  @GetMapping("/api/v1/waitingQueue/rank")
  public ResponseEntity<WaitingRankVo> retrieveWaitingRank(@RequestParam(value = "concertId") long concertId, @RequestParam(value = "token") String token) {
    String uuid = token.split(":")[1];

    WaitingRankVo waitingRankVo = waitingQueueFacade.retrieveWaitingRank(concertId, uuid);

    return ResponseEntity.status(HttpStatus.CREATED).body(waitingRankVo);
  }
}
