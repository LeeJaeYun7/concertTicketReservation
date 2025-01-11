package concert.application.waitingQueue.presentation;

import concert.application.waitingQueue.application.dto.TokenResponse;
import concert.application.waitingQueue.application.facade.WaitingQueueFacade;
import concert.domain.waitingQueue.application.WaitingQueueService;
import concert.domain.waitingQueue.application.dto.WaitingRankResponse;
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
  private final WaitingQueueService waitingQueueService;

  @GetMapping("/api/v1/waitingQueue/token")
  public ResponseEntity<TokenResponse> retrieveToken(@RequestParam(value = "concertId") long concertId, @RequestParam(value = "uuid") String uuid) {
    TokenResponse tokenResponse = waitingQueueFacade.retrieveToken(concertId, uuid);

    return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
  }

  @GetMapping("/api/v1/waitingQueue/rank")
  public ResponseEntity<WaitingRankResponse> retrieveWaitingRank(@RequestParam(value = "concertId") long concertId, @RequestParam(value = "token") String token) {
    String uuid = token.split(":")[1];

    WaitingRankResponse waitingRankResponse = waitingQueueService.retrieveWaitingRank(concertId, uuid);

    return ResponseEntity.status(HttpStatus.CREATED).body(waitingRankResponse);
  }
}
