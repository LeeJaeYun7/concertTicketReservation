package waitingQueue.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import waitingQueue.application.facade.WaitingQueueFacade;
import waitingQueue.application.WaitingQueueService;
import waitingQueue.application.dto.TokenResponse;
import waitingQueue.application.dto.WaitingRankResponse;

@RestController
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueFacade waitingQueueFacade;
    private final WaitingQueueService waitingQueueService;

    @GetMapping("/api/v1/waitingQueue/token")
    public ResponseEntity<TokenResponse> retrieveToken(@RequestParam(value="concertId") long concertId, @RequestParam(value="uuid") String uuid) {
        TokenResponse tokenResponse = waitingQueueFacade.retrieveToken(concertId, uuid);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @GetMapping("/api/v1/waitingQueue/rank")
    public ResponseEntity<WaitingRankResponse> retrieveWaitingRank(@RequestParam(value="concertId") long concertId, @RequestParam(value="token") String token) {
        String uuid = token.split(":")[1];

        WaitingRankResponse waitingRankResponse = waitingQueueService.retrieveWaitingRank(concertId, uuid);

        return ResponseEntity.status(HttpStatus.CREATED).body(waitingRankResponse);
    }
}
